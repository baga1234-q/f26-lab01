package edu.cmu.cs214.booking.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.cmu.cs214.booking.domain.Room;
import edu.cmu.cs214.booking.domain.TimeInterval;
import edu.cmu.cs214.booking.domain.User;
import edu.cmu.cs214.booking.repo.InMemoryBookingStore;
import org.junit.jupiter.api.Test;

class CancelBookingTest {

    private final Room roomA = new Room("A", "Alpha", 10);
    private final Room roomB = new Room("B", "Beta", 4);
    private final User alice = new User("u1", "Alice");
    private final User bob = new User("u2", "Bob");
    private final User carol = new User("u3", "Carol");

    private BookingService newService() {
        return new BookingService(new InMemoryBookingStore());
    }

    @Test
    void cancelExistingBooking_removesIt() {
        BookingService svc = newService();
        BookingResult.Confirmed r =
                (BookingResult.Confirmed) svc.book(roomA, alice, new TimeInterval(600, 660));
        svc.cancelBooking(r.booking().id());
        assertEquals(0, svc.listBookings(roomA).size());
    }

    @Test
    void cancelUnknownId_isNoOp() {
        BookingService svc = newService();
        svc.book(roomA, alice, new TimeInterval(600, 660));
        svc.cancelBooking("does-not-exist");
        assertEquals(1, svc.listBookings(roomA).size());
    }

    @Test
    void waitingUserIsPromotedAfterCancel() {
        BookingService svc = newService();
        BookingResult.Confirmed aliceR =
                (BookingResult.Confirmed) svc.book(roomA, alice, new TimeInterval(600, 660));
        BookingResult.Waitlisted bobR =
                (BookingResult.Waitlisted) svc.book(roomA, bob, new TimeInterval(600, 660));
        assertEquals(1, bobR.position());

        svc.cancelBooking(aliceR.booking().id());

        assertEquals(1, svc.listBookings(roomA).size());
        assertEquals(bob, svc.listBookings(roomA).get(0).user());
        assertEquals(600, svc.listBookings(roomA).get(0).interval().start());
        assertEquals(660, svc.listBookings(roomA).get(0).interval().end());
    }

    @Test
    void stillConflictingWaiterIsSkippedAndNextFitIsPromoted() {
        // Alice holds [600,660). Bob waits on [600,660) (conflicts with Alice).
        // Carol waits on [660,720) (back-to-back, no conflict with Alice).
        // Cancelling Alice must skip Bob (still conflicts with Carol? no--Carol
        // holds [660,720) which does not overlap [600,660). Actually after Alice
        // is removed, Bob's [600,660) is free; promote Bob, not Carol.
        // To exercise the skip path, use a third booking blocking Bob:
        // Alice holds [600,660), Dave holds [660,720). Bob waits [600,720) which
        // conflicts with both. Carol waits [600,660) which conflicts only with
        // Alice. Cancelling Alice: Bob still conflicts with Dave -> skip;
        // Carol is now free -> promote Carol.
        BookingService svc = newService();
        BookingResult.Confirmed aliceR =
                (BookingResult.Confirmed) svc.book(roomA, alice, new TimeInterval(600, 660));
        svc.book(roomA, new User("u4", "Dave"), new TimeInterval(660, 720));
        svc.book(roomA, bob, new TimeInterval(600, 720));   // conflicts with both
        svc.book(roomA, carol, new TimeInterval(600, 660)); // conflicts only with Alice

        svc.cancelBooking(aliceR.booking().id());

        // After cancel: Dave holds [660,720). Bob's [600,720) still overlaps Dave,
        // so Bob is skipped; Carol's [600,660) is free and gets promoted.
        var confirmed = svc.listBookings(roomA);
        assertEquals(2, confirmed.size());
        assertEquals(carol, confirmed.stream().filter(b -> b.user().equals(carol)).findFirst().orElseThrow().user());
    }

    @Test
    void cancelPromotesAtMostOne() {
        BookingService svc = newService();
        BookingResult.Confirmed aliceR =
                (BookingResult.Confirmed) svc.book(roomA, alice, new TimeInterval(600, 660));
        svc.book(roomA, bob, new TimeInterval(600, 660));
        svc.book(roomA, carol, new TimeInterval(600, 660));

        svc.cancelBooking(aliceR.booking().id());

        assertEquals(1, svc.listBookings(roomA).size());
        assertEquals(bob, svc.listBookings(roomA).get(0).user());
    }
}
