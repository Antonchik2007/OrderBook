import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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

    public AddResult add(Side side, int price, int quantity) {
        if (price <= 0 || quantity <= 0) {
            throw new IllegalArgumentException("price and quantity must be positive");
        }
        Order order = new Order(nextId++, side, price, quantity);
        List<Trade> trades = new ArrayList<>();
        match(order, trades);
        if (order.remaining > 0){
            rest(order);
        }

        return new AddResult(order.id, trades);
    }

    private void match(Order taker, List<Trade> trades){
        TreeMap<Integer, PriceLevel> opposite = (taker.side == Side.BUY) ? asks : bids; 

        while (taker.remaining > 0 && !opposite.isEmpty()){
            PriceLevel level = opposite.firstEntry().getValue();
            if(!crosses(taker, level.price)){
                break;
            }

            Order maker = level.head;
            int quantity = Math.min(taker.remaining, maker.remaining);
            trades.add(new Trade(maker.id, taker.id, level.price, quantity));
            taker.remaining -= quantity;
            maker.remaining -= quantity;
            
            if (maker.remaining == 0){
                level.remove(maker);
                orders.remove(maker.id);
                if (level.isEmpty()){
                    opposite.remove(level.price);
                }
            }
        }

    }

    private boolean crosses (Order taker, int restingPrice){
        return (taker.side == Side.BUY) 
            ? taker.price >= restingPrice
            : taker.price <= restingPrice;
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
