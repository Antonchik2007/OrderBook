public class Order {
    final long id;
    final Side side;
    final int price;
    int remaining;

    Order prev;
    Order next;

    Order(long id, Side side, int price, int quantity){
        this.id = id;
        this.side = side;
        this.price = price;
        this.remaining = quantity;
    }
}
