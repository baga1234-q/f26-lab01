package edu.cmu.cs214.booking.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.cmu.cs214.booking.domain.Room;
import edu.cmu.cs214.booking.domain.TimeInterval;
import edu.cmu.cs214.booking.domain.User;
import edu.cmu.cs214.booking.repo.InMemoryBookingStore;
import org.junit.jupiter.api.Test;

/**
 * Locks isAvailable to the same half-open overlap rule used by book() and
 * TimeInterval.overlaps. Query interval Q is fixed at [600,660) (10:00-11:00).
 *
 * <p>This is the review deliverable for Milestone 3: the original
 * agent-attempt.patch hand-rolled a predicate that only checked whether the
 * existing booking's start fell inside Q, so it missed bookings that start
 * before Q and extend into Q, and bookings that completely contain Q.
 */
class IsAvailableTest {

    private final Room roomA = new Room("A", "Alpha", 10);
    private final User alice = new User("u1", "Alice");
    private static final TimeInterval Q = new TimeInterval(600, 660);

    private BookingService svcWithExisting(int eStart, int eEnd) {
        BookingService svc = new BookingService(new InMemoryBookingStore());
        svc.book(roomA, alice, new TimeInterval(eStart, eEnd));
        return svc;
    }

    @Test
    void noBookings_isAvailable() {
        BookingService svc = new BookingService(new InMemoryBookingStore());
        assertTrue(svc.isAvailable(roomA, Q));
    }

    @Test
    void existingStartsBeforeAndExtendsIntoQuery_isNotAvailable() {
        BookingService svc = svcWithExisting(540, 620);
        assertFalse(svc.isAvailable(roomA, Q));
    }

    @Test
    void existingCompletelyContainsQuery_isNotAvailable() {
        BookingService svc = svcWithExisting(540, 720);
        assertFalse(svc.isAvailable(roomA, Q));
    }

    @Test
    void existingStartsInsideQuery_isNotAvailable() {
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
        BookingService svc = svcWithExisting(540, 600);
        assertTrue(svc.isAvailable(roomA, Q));
    }

    @Test
    void touchingAfter_isAvailable() {
        BookingService svc = svcWithExisting(660, 720);
        assertTrue(svc.isAvailable(roomA, Q));
    }
}
