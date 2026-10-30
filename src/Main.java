public class Main {
    public static void main(String[] args) {
        OrderBook book = new OrderBook();

        long a = book.add(Side.BUY, 100, 10);
        book.add(Side.BUY, 99, 5);
        book.add(Side.SELL, 102, 7);
        System.out.println("best bid " + book.bestBid() + ", best ask " + book.bestAsk());

        book.cancel(a);
        System.out.println("after cancel: best bid " + book.bestBid());

        System.out.println("cancel unknown id: " + book.cancel(999));

        book.add(Side.SELL, 99, 3);
        System.out.println(book.lastTrades());
        System.out.println("best bid " + book.bestBid() + ", best ask " + book.bestAsk());
    }
}
