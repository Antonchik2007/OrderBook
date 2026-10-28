import java.util.Collections;
import java.util.HashMap;
import java.util.TreeMap;

public class OrderBook {
    private final TreeMap<Integer, PriceLevel> bids = new TreeMap<>(Collections.reverseOrder());
    private final TreeMap<Integer, PriceLevel> asks = new TreeMap<>();
    private final HashMap<Long, Order> orders = new HashMap<>();
    private long nextId = 1;

    public static final int NO_PRICE = 0;

    public int bestBid() {
        return bids.isEmpty() ? NO_PRICE : bids.firstKey();
    }

    public int bestAsk() {
        return asks.isEmpty() ? NO_PRICE : asks.firstKey();
    }

    public long add(Side side, int price, int quantity) {
        if (price <= 0 || quantity <= 0) {
            throw new IllegalArgumentException("price and quantity must be positive");
        }
        Order order = new Order(nextId++, side, price, quantity);

        rest(order);
        return order.id;
    }

    private void rest(Order order) {
        TreeMap<Integer, PriceLevel> book = (order.side == Side.BUY) ? bids : asks;
        PriceLevel level = book.get(order.price);
        if (level == null) {
            level = new PriceLevel(order.price);
            book.put(order.price, level);
        }
        level.append(order);
        orders.put(order.id, order);
    }

    public boolean cancel(long id){
        Order order = orders.remove(id);
        if (order == null){
            return false;
        }
        TreeMap<Integer, PriceLevel> book = (order.side == Side.BUY) ? bids : asks;
        PriceLevel level = book.get(order.price);
        level.remove(order);
        if(level.isEmpty()){
            book.remove(order.price);
        }
        return true;
    }
}
