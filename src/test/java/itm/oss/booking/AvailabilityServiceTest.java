package itm.oss.booking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AvailabilityServiceTest {
    private Room room(String id, int capacity) {
        return new Room(id, "Test room " + id, capacity, Set.of());
    }

    private Booking booking(String roomId, String start, String end, BookingStatus status) {
        return new Booking(1, "test", roomId, sToTime(start),
                sToTime(end), 2, "Study", status);
    }

    private LocalDateTime sToTime(String time) {
        return LocalDateTime.parse(time);
    }

    @Test
    @DisplayName("Check room availability by capacity and time")
    void returnAvailableRoom() {
        List<Room> rooms = List.of(room("r1", 4), room("r2", 2),
                room("r3", 6));

        List<Booking> bookings = List.of(booking("r3", "2026-10-18T09:00", "2026-10-18T10:30", BookingStatus.ACTIVE));

        List<Room> result = AvailabilityService.findAvailableRooms(rooms, bookings, sToTime("2026-10-18T10:00"),
                sToTime("2026-10-18T11:00"), 4);

        assertEquals(1, result.size());
        assertEquals("r1", result.get(0).getId());
    }

    @Test
    @DisplayName("Check overlapping and touching book handling")
    void checkOverlaplogic() {
        List<Room> rooms = List.of(room("r1", 4), room("r2", 6));

        List<Booking> bookings = List.of(booking("r1", "2026-10-18T09:00", "2026-10-18T10:00", BookingStatus.ACTIVE),
                booking("r2", "2026-10-18T11:00", "2026-10-18T12:00", BookingStatus.ACTIVE));
        List<Room> result = AvailabilityService.findAvailableRooms(rooms, bookings, sToTime("2026-10-18T10:00"),
                sToTime("2026-10-18T11:00"), 4);
        // Request touches r1's end and overlappes with r2

        assertEquals(2, result.size());
        assertEquals("r1", result.get(0).getId());
        assertEquals("r2", result.get(1).getId());
    }

    @Test
    @DisplayName("Check when result is empty")

    void checkIfNoResult() {
        List<Room> rooms = List.of(room("r1", 2), room("r2", 6));

        List<Booking> bookings = List.of(booking("r1", "2026-10-18T09:00", "2026-10-18T10:00", BookingStatus.ACTIVE),
                booking("r2", "2026-10-18T11:00", "2026-10-18T12:00", BookingStatus.ACTIVE));
        List<Room> result = AvailabilityService.findAvailableRooms(rooms, bookings, sToTime("2026-10-18T10:00"),
                sToTime("2026-10-18T11:30"), 4);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Check cancelled book availability")
    void checkCancelled() {
        List<Room> rooms = List.of(room("r1", 6));

        List<Booking> bookings = List.of(booking("r1", "2026-10-18T09:00",
                "2026-10-18T10:00", BookingStatus.CANCELLED));

        List<Room> result = AvailabilityService.findAvailableRooms(rooms, bookings, sToTime("2026-10-18T09:00"),
                sToTime("2026-10-18T11:00"), 4);

        assertEquals(1, result.size());
        assertEquals("r1", result.get(0).getId());
    }

    @Test
    @DisplayName("Check if output preserve input order")
    void checkOrder() {
        List<Room> rooms = List.of(room("r2", 6), room("r3", 4), room("r1", 4));

        List<Room> result = AvailabilityService.findAvailableRooms(rooms, List.of(), sToTime("2026-10-18T10:00"),
                sToTime("2026-10-18T11:00"), 4);

        assertEquals(3, result.size());
        assertEquals("r2", result.get(0).getId());
        assertEquals("r3", result.get(1).getId());
        assertEquals("r1", result.get(2).getId());
    }
     @Test 
        void returnsRoomWhenNoBookings(){
            Room room = new Room("R101","Room A", 40, Set.of("JJB"));
            List<Room> rooms = List.of(room);
            List<Booking> bookings = List.of();
            LocalDateTime start = LocalDateTime.parse("2026-10-20T10:00:00");
            LocalDateTime end = LocalDateTime.parse("2026-10-20T12:00:00");
            int minCapacity = 30;
            List<Room> result = AvailabilityService.findAvailableRooms(rooms, bookings, start, end, minCapacity);
            assertEquals(1,result.size(),"Expected exactly one available room");
            assertEquals(room,result.get(0),"Expected the returned room to match the requested available room");
        }
        @Test 
        void excludesRoomWithInsufficientCapacity(){
            Room room = new Room("R102","Room B",20,Set.of());
            List<Room> rooms = List.of(room);
            List<Booking> bookings = List.of();
            LocalDateTime start = LocalDateTime.parse("2026-10-20T10:00:00");
            LocalDateTime end = LocalDateTime.parse("2026-10-20T12:00:00");
            int minCapacity = 30;
            List<Room> result = AvailabilityService.findAvailableRooms(rooms, bookings, start, end, minCapacity);
            assertEquals(0, result.size(),"Rooms with insufficient capacity must be excluded");
        }
        @Test 
        void throwsExceptionForInvalidCapacity(){
            Room room = new Room("R103","Room C",40,Set.of());
            List<Room> rooms = List.of(room);
            List<Booking> bookings = List.of();
            LocalDateTime start = LocalDateTime.parse("2026-10-20T10:00:00");
            LocalDateTime end = LocalDateTime.parse("2026-10-20T12:00:00");
            int minCapacity = 0;
            assertThrows(IllegalArgumentException.class,()-> AvailabilityService.findAvailableRooms(rooms, bookings, start, end, minCapacity),"Invalid minimum capacity must throw IllegalArgumentException");
        }
        @Test 
        void filterRoomsByCapacityAndTime(){
            Room roomA = new Room("R101","Room A",40,Set.of());
            Room roomB = new Room("R102","Room B", 20, Set.of());
            Room roomC = new Room("R103","Room C", 50, Set.of());
            List<Room> rooms = List.of(roomA, roomB, roomC);
            Booking bookingA = new Booking(1, "s1", "R101", LocalDateTime.parse("2026-10-20T10:00"),LocalDateTime.parse("2026-10-20T11:00"),2,"Study",BookingStatus.ACTIVE);
            Booking bookingC = new Booking(3, "s3", "R103", LocalDateTime.parse("2026-10-20T13:00"),LocalDateTime.parse("2026-10-20T15:00"), 2, "Study", BookingStatus.ACTIVE);
            List<Booking> bookings = List.of(bookingA,bookingC);
            LocalDateTime start = LocalDateTime.parse("2026-10-20T10:00");
            LocalDateTime end = LocalDateTime.parse("2026-10-20T12:00");
            int minCapacity = 30;
            List<Room> result = AvailabilityService.findAvailableRooms(rooms, bookings, start, end, minCapacity);
            assertEquals(1,result.size(),"Only one room should satisfy both capacity and time conditions");
            assertEquals(roomC, result.get(0), "Expected the available room to be R103");
        }
}
