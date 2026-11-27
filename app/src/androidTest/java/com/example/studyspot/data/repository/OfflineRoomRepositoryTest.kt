package com.example.studyspot.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.studyspot.data.local.DatabaseSeeder
import com.example.studyspot.data.local.StudySpotDatabase
import com.example.studyspot.data.local.entity.BookingEntity
import com.example.studyspot.domain.models.AvailabilityStatus
import com.example.studyspot.domain.models.SeatType
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OfflineRoomRepositoryTest {
    private lateinit var database: StudySpotDatabase
    private lateinit var repository: OfflineRoomRepository
    private val today = LocalDate.of(2026, 11, 24)
    private val clock = Clock.fixed(Instant.parse("2026-11-24T06:00:00Z"), ZoneOffset.UTC)

    @Before
    fun setUp() = runTest {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, StudySpotDatabase::class.java).build()
        DatabaseSeeder(database, clock).seed()
        repository = OfflineRoomRepository(database)
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun calendarPreservesDemoScenariosAndComputerFilter() = runTest {
        val slots = repository.getTimeSlots()
        val all = repository.getAvailability(today, today.plusDays(5), false)
        val computers = repository.getAvailability(today, today.plusDays(5), true)

        assertEquals(listOf("morning", "afternoon", "evening"), slots.map { it.id })
        assertEquals((0L..4L).map { today.plusDays(it) }, all.map { it.date })
        assertEquals(slots, all[0].availableTimeSlots)
        assertEquals(slots, all[1].availableTimeSlots)
        assertTrue(computers[1].availableTimeSlots.isEmpty())
        assertEquals(listOf("morning", "evening"), all[2].availableTimeSlots.map { it.id })
        assertTrue(all[3].availableTimeSlots.isEmpty())
        assertEquals(slots, all[4].availableTimeSlots)
        assertEquals(slots, computers[4].availableTimeSlots)
    }

    @Test
    fun mapUsesDateAndIntervalAndPreservesSeatModels() = runTest {
        val slots = repository.getTimeSlots()
        val morning = repository.getSeatSlots(today, slots.first())
        val evening = repository.getSeatSlots(today, slots.last())
        val tomorrow = repository.getSeatSlots(today.plusDays(1), slots.first())

        assertEquals(56, morning.size)
        assertEquals(12, morning.count { it.status == AvailabilityStatus.OCCUPIED })
        assertEquals(AvailabilityStatus.AVAILABLE, morning.first().status)
        assertEquals(AvailabilityStatus.OCCUPIED, morning.first { it.seat.id == "seat-2" }.status)
        assertTrue(evening.all { it.status == AvailabilityStatus.AVAILABLE })
        assertEquals(morning.map { it.seat }, evening.map { it.seat })
        assertTrue(tomorrow.all {
            (it.status == AvailabilityStatus.OCCUPIED) == (it.seat.type == SeatType.COMPUTER)
        })
    }

    @Test
    fun databaseWritesChangeMapAndCalendarWithoutAnInMemoryOccupancySource() = runTest {
        val slot = repository.getTimeSlots().first()
        val date = today.plusDays(4)
        val seats = repository.getSeatSlots(date, slot)
        val bookingIds = seats.map {
            database.bookingDao().insert(
                BookingEntity(seatId = it.seat.id, date = date, timeSlotId = slot.id, userId = "test"),
            )
        }

        assertTrue(repository.getSeatSlots(date, slot).all { it.status == AvailabilityStatus.OCCUPIED })
        assertEquals(
            listOf("afternoon", "evening"),
            repository.getAvailability(date, date.plusDays(1), false).single()
                .availableTimeSlots.map { it.id },
        )

        database.bookingDao().deleteById(bookingIds.first())
        assertEquals(AvailabilityStatus.AVAILABLE, repository.getSeatSlots(date, slot).first().status)
        assertEquals(3, repository.getAvailability(date, date.plusDays(1), false).single().availableTimeSlots.size)
    }

    @Test
    fun deletingDemoBookingDoesNotRecreateItOnAnotherRead() = runTest {
        val slot = repository.getTimeSlots().first()
        val demo = database.bookingDao().getForTimeSlot(today, slot.id).first()
        database.bookingDao().deleteById(demo.id)

        val map = repository.getSeatSlots(today, slot)
        assertEquals(AvailabilityStatus.AVAILABLE, map.first { it.seat.id == demo.seatId }.status)
        assertEquals(11, database.bookingDao().getForTimeSlot(today, slot.id).size)
    }

    @Test
    fun emptyAndReversedDateRangesAreRejected() = runTest {
        for (endDate in listOf(today, today.minusDays(1))) {
            try {
                repository.getAvailability(today, endDate, false)
                org.junit.Assert.fail("Expected an invalid date range to be rejected")
            } catch (exception: IllegalArgumentException) {
                assertEquals(
                    "Конец диапазона должен быть позже начала: $today — $endDate",
                    exception.message,
                )
            }
        }
    }
}
