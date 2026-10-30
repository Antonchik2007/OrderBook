import java.util.Random;

public class Benchmark {
    static final int N = 500_000;
    static final int MID = 10_000;
    static final int RANGE = 50;

    public static void main(String[] args) {

        Random rng = new Random(42);
        Side[] sides = new Side[N];
        int[] prices = new int[N];
        int[] quantities = new int[N];

        for (int i = 0; i < N; i++) {
            sides[i] = rng.nextBoolean() ? Side.BUY : Side.SELL;
            prices[i] = MID - RANGE + rng.nextInt(2 * RANGE + 1);
            quantities[i] = 1 + rng.nextInt(100);
        }

        for (int w = 0; w < 5; w++) {
            run(sides, prices, quantities);
        }

        int RUNS = 10;
        long[] times = new long[RUNS];
        long trades = 0;
        for (int k = 0; k < RUNS; k++) {
            long start = System.nanoTime();
            trades = run(sides, prices, quantities);
            times[k] = System.nanoTime() - start;
        }
        java.util.Arrays.sort(times);
        double seconds = times[RUNS / 2] / 1e9;

        System.out.printf("median of %d runs: %d orders in %.3f s%n", RUNS, N, seconds);
        System.out.printf("%.0f orders/sec, %.0f trades/sec (%d trades)%n",
                N / seconds, trades / seconds, trades);
        System.out.printf("fastest %.3f s, slowest %.3f s%n", times[0] / 1e9, times[RUNS - 1] / 1e9);
    }

    static long run(Side[] sides, int[] prices, int[] quantities) {
        OrderBook book = new OrderBook();
        long trades = 0;
        for (int i = 0; i < N; i++) {
            book.add(sides[i], prices[i], quantities[i]);
            trades += book.lastTrades().size();
        }
        return trades;
    }

}
