package edu.cmu.cs214.booking.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.cmu.cs214.booking.domain.Room;
import edu.cmu.cs214.booking.domain.TimeInterval;
import edu.cmu.cs214.booking.domain.User;
import edu.cmu.cs214.booking.repo.InMemoryBookingStore;
import org.junit.jupiter.api.Test;

/**
 * Locks the isAvailable semantics to the same half-open overlap rule used by
 * TimeInterval.overlaps and by book(). Query interval Q is fixed at
 * [600, 660) (10:00-11:00); each test books one existing booking E in room A
 * and asks whether Q is available.
 */
class IsAvailableTest {

    private final Room roomA = new Room("A", "Alpha", 10);
    private final User alice = new User("u1", "Alice");

    private BookingService svcWithExisting(int eStart, int eEnd) {
        BookingService svc = new BookingService(new InMemoryBookingStore());
        svc.book(roomA, alice, new TimeInterval(eStart, eEnd));
        return svc;
    }

    private static final TimeInterval Q = new TimeInterval(600, 660);

    @Test
    void noBookings_isAvailable() {
        BookingService svc = new BookingService(new InMemoryBookingStore());
        assertTrue(svc.isAvailable(roomA, Q));
    }

    @Test
    void existingStartsBeforeAndExtendsIntoQuery_isNotAvailable() {
        // E = [540, 620): E starts before Q but its tail overlaps Q.
        BookingService svc = svcWithExisting(540, 620);
        assertFalse(svc.isAvailable(roomA, Q));
    }

    @Test
    void existingCompletelyContainsQuery_isNotAvailable() {
        // E = [540, 720): E spans the whole Q.
        BookingService svc = svcWithExisting(540, 720);
        assertFalse(svc.isAvailable(roomA, Q));
    }

    @Test
    void existingStartsInsideQuery_isNotAvailable() {
        // E = [620, 680): E starts inside Q.
        BookingService svc = svcWithExisting(620, 680);
        assertFalse(svc.isAvailable(roomA, Q));
    }

    @Test
    void existingExactlyEqualsQuery_isNotAvailable() {
        BookingService svc = svcWithExisting(600, 660);
        assertFalse(svc.isAvailable(roomA, Q));
    }

    @Test
    void touchingBefore_isAvailable() {
        // E = [540, 600): ends exactly when Q starts; half-open -> no overlap.
        BookingService svc = svcWithExisting(540, 600);
        assertTrue(svc.isAvailable(roomA, Q));
    }

    @Test
    void touchingAfter_isAvailable() {
        // E = [660, 720): starts exactly when Q ends; half-open -> no overlap.
        BookingService svc = svcWithExisting(660, 720);
        assertTrue(svc.isAvailable(roomA, Q));
    }
}
