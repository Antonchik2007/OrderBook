import java.util.List;

public class BookTest {
    static int passed = 0;

    static void check(boolean condition, String name) {
        if (!condition) {
            throw new AssertionError("FAILED: " + name);
        }
        passed++;
    }

    public static void main(String[] args) {
        noCross();
        fullFill();
        partialFillMakerRests();
        partialFillTakerRests();
        sweep();
        cancelThenMatch();
        cancelUnknown();
        System.out.println("All " + passed + " checks passed");
    }

    // Scenario 1: incoming order doesn't cross, both rest
    static void noCross() {
        OrderBook book = new OrderBook();
        book.add(Side.BUY, 100, 10);
        book.add(Side.SELL, 101, 5);
        check(book.lastTrades().isEmpty(), "noCross: no trades");
        check(book.bestBid() == 100, "noCross: best bid");
        check(book.bestAsk() == 101, "noCross: best ask");
    }

    // Scenario 2: taker and maker fill each other completely
    static void fullFill() {
        OrderBook book = new OrderBook();
        book.add(Side.BUY, 100, 10);
        book.add(Side.SELL, 80, 10);
        check(book.lastTrades().equals(List.of(new Trade(1, 2, 100, 10))), "fullFill: trades at resting price");
        check(book.bestBid() == OrderBook.NO_PRICE, "fullFill: bids empty");
        check(book.bestAsk() == OrderBook.NO_PRICE, "fullFill: asks empty");
        check(!book.cancel(1), "fullFill: filled order can't be cancelled");
    }

    // Scenario 3a: maker partially filled, its remainder keeps resting
    static void partialFillMakerRests() {
        OrderBook book = new OrderBook();
        book.add(Side.BUY, 100, 10);
        book.add(Side.SELL, 90, 5);
        check(book.lastTrades().equals(List.of(new Trade(1, 2, 100, 5))), "partialFillMakerRests: trades");
        check(book.bestBid() == 100, "partialFillMakerRests: maker still resting at 100");
        check(book.bestAsk() == OrderBook.NO_PRICE, "partialFillMakerRests: taker fully filled, no asks");

        book.add(Side.SELL, 100, 5);
        check(book.lastTrades().equals(List.of(new Trade(1, 3, 100, 5))), "partialFillMakerRests: maker had exactly 5 left");
        check(book.bestBid() == OrderBook.NO_PRICE, "partialFillMakerRests: bids empty after second fill");
    }

    // Scenario 3b: taker partially filled, its remainder rests at its own price
    static void partialFillTakerRests() {
        OrderBook book = new OrderBook();
        book.add(Side.BUY, 100, 5);
        book.add(Side.SELL, 90, 10);
        check(book.lastTrades().equals(List.of(new Trade(1, 2, 100, 5))), "partialFillTakerRests: trades");
        check(book.bestBid() == OrderBook.NO_PRICE, "partialFillTakerRests: bids empty");
        check(book.bestAsk() == 90, "partialFillTakerRests: remainder rests at its own price");
    }

    // Scenario 4: taker sweeps two levels, time priority within a level
    static void sweep() {
        OrderBook book = new OrderBook();
        book.add(Side.BUY, 100, 10);
        book.add(Side.BUY, 100, 5);
        book.add(Side.BUY, 95, 10);
        book.add(Side.SELL, 90, 18);
        List<Trade> expected = List.of(
            new Trade(1, 4, 100, 10),
            new Trade(2, 4, 100, 5),
            new Trade(3, 4, 95, 3)
        );
        check(book.lastTrades().equals(expected), "sweep: trades in price-time order");
        check(book.bestBid() == 95, "sweep: best bid");
        check(book.bestAsk() == OrderBook.NO_PRICE, "sweep: asks empty");

        book.add(Side.SELL, 95, 7);
        check(book.lastTrades().equals(List.of(new Trade(3, 5, 95, 7))), "sweep: order 3 had exactly 7 left");
        check(book.bestBid() == OrderBook.NO_PRICE, "sweep: bids empty after");
    }

    // Scenario 5: cancelled order is skipped, next in line moves up
    static void cancelThenMatch() {
        OrderBook book = new OrderBook();
        book.add(Side.SELL, 100, 10);
        book.add(Side.SELL, 100, 10);
        check(book.cancel(1), "cancelThenMatch: cancel resting order");
        book.add(Side.BUY, 120, 15);
        check(book.lastTrades().equals(List.of(new Trade(2, 3, 100, 10))), "cancelThenMatch: skips cancelled, matches next in line");
        check(book.bestBid() == 120, "cancelThenMatch: remainder rests at 120");
        check(book.bestAsk() == OrderBook.NO_PRICE, "cancelThenMatch: asks empty");
    }

    // Scenario 6: cancel of an id that is never issued, already cancelled, or already filled
    static void cancelUnknown() {
        OrderBook a = new OrderBook();
        check(!a.cancel(999), "cancelUnknown: never issued");

        OrderBook b = new OrderBook();
        b.add(Side.BUY, 100, 10);
        check(b.cancel(1), "cancelUnknown: first cancel succeeds");
        check(!b.cancel(1), "cancelUnknown: second cancel fails");
        check(b.bestBid() == OrderBook.NO_PRICE, "cancelUnknown: bids empty after cancel");

        OrderBook c = new OrderBook();
        c.add(Side.BUY, 100, 10);
        c.add(Side.SELL, 80, 10);
        check(!c.cancel(1), "cancelUnknown: filled order can't be cancelled");
    }
}