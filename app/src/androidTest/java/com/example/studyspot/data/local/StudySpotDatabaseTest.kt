package com.example.studyspot.data.local

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.room.withTransaction
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.studyspot.data.local.entity.BookingEntity
import com.example.studyspot.data.local.entity.SeatEntity
import com.example.studyspot.data.local.entity.TimeSlotEntity
import com.example.studyspot.domain.models.SeatPosition
import com.example.studyspot.domain.models.SeatType
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StudySpotDatabaseTest {
    private lateinit var context: Context
    private lateinit var database: StudySpotDatabase
    private var databaseFileName: String? = null
    private val clock = Clock.fixed(Instant.parse("2026-11-24T06:00:00Z"), ZoneOffset.UTC)
    private val date = LocalDate.now(clock).plusDays(10)

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, StudySpotDatabase::class.java).build()
    }

    @After
    fun tearDown() {
        database.close()
        databaseFileName?.let { context.deleteDatabase(it) }
    }

    @Test
    fun seederPopulatesEmptyReferenceTables() = runTest {
        assertTrue(database.seatDao().getAll().isEmpty())
        assertTrue(database.timeSlotDao().getAll().isEmpty())

        DatabaseSeeder(database, clock).seed()

        val seats = database.seatDao().getAll()
        assertEquals(56, seats.size)
        assertEquals(56, seats.map { it.id }.toSet().size)
        assertEquals(
            (0 until 8).flatMap { row -> (0 until 7).map { column -> SeatPosition(row, column) } },
            seats.map { it.position },
        )
        assertEquals((1..56).map { "seat-$it" }, seats.map { it.id })
        assertEquals((1..56).map { it.toString() }, seats.map { it.label })
        assertEquals(setOf(SeatType.REGULAR, SeatType.COMPUTER), seats.map { it.type }.toSet())
        assertEquals(
            listOf(
                TimeSlotEntity("morning", LocalTime.of(9, 0), LocalTime.of(12, 0)),
                TimeSlotEntity("afternoon", LocalTime.of(12, 0), LocalTime.of(15, 0)),
                TimeSlotEntity("evening", LocalTime.of(15, 0), LocalTime.of(18, 0)),
            ),
            database.timeSlotDao().getAll(),
        )
        assertTrue(database.bookingDao().getInDateRange(date, date.plusDays(1)).isEmpty())
    }

    @Test
    fun repeatedSeedingPreservesReferenceDataAndBookings() = runTest {
        DatabaseSeeder(database, clock).seed()
        val seats = database.seatDao().getAll()
        val slots = database.timeSlotDao().getAll()
        val booking = insertBooking()

        DatabaseSeeder(database, clock).seed()

        assertEquals(seats, database.seatDao().getAll())
        assertEquals(slots, database.timeSlotDao().getAll())
        assertEquals(booking, database.bookingDao().getById(booking.id))
        assertEquals(listOf(booking), database.bookingDao().getForUser(booking.userId))
    }

    @Test
    fun seederPreservesNonemptyTableAndFillsEmptyTable() = runTest {
        val seat = SeatEntity("custom", "Custom", SeatType.COMPUTER, SeatPosition(0, 0))
        database.seatDao().insertAll(listOf(seat))

        DatabaseSeeder(database, clock).seed()

        assertEquals(listOf(seat), database.seatDao().getAll())
        assertEquals(3, database.timeSlotDao().getAll().size)
    }

    @Test
    fun concurrentSeedingDoesNotCreateDuplicates() = runTest {
        coroutineScope {
            repeat(3) { launch { DatabaseSeeder(database, clock).seed() } }
        }
        assertEquals(56, database.seatDao().getAll().size)
        assertEquals(3, database.timeSlotDao().getAll().size)
    }

    @Test
    fun referenceDataCanBeFoundByIdAndMissingIdsReturnNull() = runTest {
        DatabaseSeeder(database, clock).seed()
        val seat = database.seatDao().getAll().first()
        val slot = database.timeSlotDao().getAll().first()

        assertEquals(seat, database.seatDao().getById(seat.id))
        assertEquals(slot, database.timeSlotDao().getById(slot.id))
        assertNull(database.seatDao().getById("missing"))
        assertNull(database.timeSlotDao().getById("missing"))
        assertNull(database.bookingDao().getById(Long.MAX_VALUE))
    }

    @Test
    fun insertGeneratesDistinctIdsAndRoundTripsBookingData() = runTest {
        DatabaseSeeder(database, clock).seed()
        val first = insertBooking()
        val second = insertBooking(booking(seatId = "seat-2"))

        assertTrue(first.id > 0)
        assertTrue(second.id > 0)
        assertFalse(first.id == second.id)
        assertEquals(first, database.bookingDao().getById(first.id))
        assertEquals(second, database.bookingDao().getById(second.id))
    }

    @Test
    fun duplicateSeatDateAndTimeSlotIsRejectedEvenForAnotherUser() = runTest {
        DatabaseSeeder(database, clock).seed()
        val original = insertBooking()

        expectConstraint { database.bookingDao().insert(booking(userId = "another-user")) }

        assertEquals(listOf(original), database.bookingDao().getForTimeSlot(date, "morning"))
        assertTrue(database.bookingDao().getForUser("another-user").isEmpty())
    }

    @Test
    fun changingAnyPartOfSlotAllowsAnotherBooking() = runTest {
        DatabaseSeeder(database, clock).seed()
        insertBooking()
        insertBooking(booking(seatId = "seat-2"))
        insertBooking(booking(date = date.plusDays(1)))
        insertBooking(booking(timeSlotId = "afternoon"))

        assertEquals(4, database.bookingDao().getForUser("user-1").size)
    }

    @Test
    fun missingSeatOrTimeSlotIsRejected() = runTest {
        DatabaseSeeder(database, clock).seed()

        expectConstraint { database.bookingDao().insert(booking(seatId = "missing")) }
        expectConstraint { database.bookingDao().insert(booking(timeSlotId = "missing")) }

        assertTrue(database.bookingDao().getInDateRange(date, date.plusDays(1)).isEmpty())
    }

    @Test
    fun referencedSeatAndTimeSlotCannotBeDeleted() = runTest {
        DatabaseSeeder(database, clock).seed()
        val saved = insertBooking()

        expectConstraint {
            database.withTransaction {
                database.openHelper.writableDatabase.execSQL("DELETE FROM seats WHERE id = 'seat-1'")
            }
        }
        expectConstraint {
            database.withTransaction {
                database.openHelper.writableDatabase.execSQL("DELETE FROM time_slots WHERE id = 'morning'")
            }
        }

        assertEquals(saved, database.bookingDao().getById(saved.id))
        assertEquals("seat-1", database.seatDao().getById("seat-1")?.id)
        assertEquals("morning", database.timeSlotDao().getById("morning")?.id)
    }

    @Test
    fun queriesFilterByDateTimeSlotUserAndExclusiveRangeEnd() = runTest {
        DatabaseSeeder(database, clock).seed()
        val before = insertBooking(booking(date = date.minusDays(1)))
        val morning = insertBooking()
        val afternoon = insertBooking(booking(timeSlotId = "afternoon", userId = "user-2"))
        val nextDay = insertBooking(booking(date = date.plusDays(1)))
        insertBooking(booking(date = date.plusDays(2)))

        assertEquals(listOf(morning), database.bookingDao().getForTimeSlot(date, "morning"))
        assertEquals(listOf(afternoon), database.bookingDao().getForUser("user-2"))
        assertEquals(
            listOf(afternoon, morning, nextDay),
            database.bookingDao().getInDateRange(date, date.plusDays(2)),
        )
        assertTrue(database.bookingDao().getInDateRange(date, date).isEmpty())
        assertTrue(database.bookingDao().getForTimeSlot(before.date, "evening").isEmpty())
        assertTrue(database.bookingDao().getForUser("missing").isEmpty())
    }

    @Test
    fun deletingBookingReturnsCountAndFreesSlotForRebooking() = runTest {
        DatabaseSeeder(database, clock).seed()
        val saved = insertBooking()

        assertEquals(1, database.bookingDao().deleteById(saved.id))
        assertNull(database.bookingDao().getById(saved.id))
        assertEquals(0, database.bookingDao().deleteById(saved.id))
        assertTrue(database.bookingDao().getForTimeSlot(date, "morning").isEmpty())

        val replacement = insertBooking(booking(userId = "user-2"))
        assertEquals(listOf(replacement), database.bookingDao().getForTimeSlot(date, "morning"))
    }

    @Test
    fun timeConverterPreservesSecondsAndNanoseconds() = runTest {
        val slot = TimeSlotEntity(
            "precise", LocalTime.of(9, 15, 30, 123456789), LocalTime.of(10, 45, 40, 987654321),
        )
        database.timeSlotDao().insertAll(listOf(slot))

        assertEquals(slot, database.timeSlotDao().getById(slot.id))
    }

    @Test
    fun dataSurvivesClosingReopeningAndReseedingFileDatabase() = runTest {
        database.close()
        val fileName = "study-spot-test-${UUID.randomUUID()}.db"
        databaseFileName = fileName
        database = Room.databaseBuilder(context, StudySpotDatabase::class.java, fileName).build()
        DatabaseSeeder(database, clock).seed()
        val seats = database.seatDao().getAll()
        val slots = database.timeSlotDao().getAll()
        val saved = insertBooking()
        database.close()

        database = Room.databaseBuilder(context, StudySpotDatabase::class.java, fileName).build()
        DatabaseSeeder(database, clock).seed()

        assertEquals(seats, database.seatDao().getAll())
        assertEquals(slots, database.timeSlotDao().getAll())
        assertEquals(saved, database.bookingDao().getById(saved.id))
        assertEquals(listOf(saved), database.bookingDao().getForTimeSlot(date, "morning"))
    }

    private fun booking(
        seatId: String = "seat-1",
        date: LocalDate = this.date,
        timeSlotId: String = "morning",
        userId: String = "user-1",
    ) = BookingEntity(seatId = seatId, date = date, timeSlotId = timeSlotId, userId = userId)

    private suspend fun insertBooking(booking: BookingEntity = booking()): BookingEntity =
        booking.copy(id = database.bookingDao().insert(booking))

    private suspend fun expectConstraint(action: suspend () -> Unit) {
        try {
            action()
            fail("Expected a SQLite constraint violation")
        } catch (_: SQLiteConstraintException) {
            // The real SQLite engine must reject this write.
        }
    }
}
