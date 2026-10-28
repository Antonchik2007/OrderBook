public class PriceLevel {
    final int price;
    Order head;
    Order tail;

    PriceLevel(int price) {
        this.price = price;
    }

    void append(Order order) {
        if (tail == null) {
            head = order;
            tail = order;
        } else {
            order.prev = tail;
            tail.next = order;
            tail = order;
        }
    }
    void remove(Order order){
        if(order.prev != null){ //if it's not the head
            order.prev.next = order.next;
        } else {
            head = order.next; //if remove the head, set the head to next in line
        }
        if (order.next != null){
            order.next.prev = order.prev;
        } else {
            tail = order.prev;
        }

        order.next = null;
        order.prev = null;
    }

    boolean isEmpty() {
        return head == null;
    }
}
