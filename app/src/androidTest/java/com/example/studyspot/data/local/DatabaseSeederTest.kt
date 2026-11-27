package com.example.studyspot.data.local

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.UUID
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseSeederTest {
    private lateinit var context: Context
    private lateinit var database: StudySpotDatabase
    private var fileName: String? = null
    private val clock = Clock.fixed(Instant.parse("2026-11-24T06:00:00Z"), ZoneOffset.UTC)
    private val today = LocalDate.now(clock)

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, StudySpotDatabase::class.java).build()
    }

    @After
    fun tearDown() {
        database.close()
        fileName?.let { context.deleteDatabase(it) }
    }

    @Test
    fun concurrentSeedingPreservesDemoBookingsAndDoesNotRestoreDeletedBooking() = runTest {
        val seeder = DatabaseSeeder(database, clock)
        coroutineScope {
            repeat(3) { launch { seeder.seed() } }
        }

        assertEquals(56, database.seatDao().getAll().size)
        assertEquals(3, database.timeSlotDao().getAll().size)
        assertEquals(12, database.bookingDao().getForTimeSlot(today, "morning").size)
        val demo = database.bookingDao().getForTimeSlot(today, "morning").first()
        database.bookingDao().deleteById(demo.id)
        seeder.seed()
        assertEquals(11, database.bookingDao().getForTimeSlot(today, "morning").size)
    }

    @Test
    fun failedSeedingRollsBackAndCanBeRetried() = runTest {
        database.openHelper.writableDatabase.execSQL(
            "CREATE TRIGGER fail_seeding BEFORE INSERT ON bookings " +
                "BEGIN SELECT RAISE(ABORT, 'Test seeding failure'); END",
        )
        val seeder = DatabaseSeeder(database, clock)
        try {
            seeder.seed()
            fail("Expected seeding to fail")
        } catch (_: SQLiteConstraintException) {
            // Initial reference inserts must roll back together with demo bookings.
        }
        assertTrue(database.seatDao().getAll().isEmpty())
        assertTrue(database.timeSlotDao().getAll().isEmpty())
        database.openHelper.writableDatabase.execSQL("DROP TRIGGER fail_seeding")

        seeder.seed()

        assertEquals(56, database.seatDao().getAll().size)
        assertEquals(12, database.bookingDao().getForTimeSlot(today, "morning").size)
    }

    @Test
    fun reopeningDatabaseDoesNotRestoreDeletedDemoBookingsOrSeedNewDates() = runTest {
        database.close()
        val name = "startup-test-${UUID.randomUUID()}.db"
        fileName = name
        database = Room.databaseBuilder(context, StudySpotDatabase::class.java, name).build()
        DatabaseSeeder(database, clock).seed()
        val demo = database.bookingDao().getForTimeSlot(today, "morning").first()
        database.bookingDao().deleteById(demo.id)
        val remaining = database.bookingDao().getInDateRange(today, today.plusDays(4))
        database.close()

        database = Room.databaseBuilder(context, StudySpotDatabase::class.java, name).build()
        val laterClock = Clock.offset(clock, java.time.Duration.ofDays(10))
        DatabaseSeeder(database, laterClock).seed()

        assertEquals(remaining, database.bookingDao().getInDateRange(today, today.plusDays(4)))
        assertEquals(11, database.bookingDao().getForTimeSlot(today, "morning").size)
        assertTrue(database.bookingDao().getInDateRange(today.plusDays(10), today.plusDays(14)).isEmpty())
    }
}
