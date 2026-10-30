# orderbook

A simple limit order book in Java. One instrument, limit orders only, price-time priority matching. I built it to learn Java and to understand how a matching engine works.

## Run it

```
javac -d out src/*.java
java -cp out BookTest     # tests
java -cp out Benchmark    # speed test
```

Java 17 or newer. I used Java 25 on a MacBook Air M4.

## How it works

- `add(side, price, qty)` adds an order and returns its id. If it crosses the other side, it trades first, best price first and oldest order first at each price. Whatever is left waits in the book.
- `lastTrades()` returns the trades from the last `add`.
- `cancel(id)` removes a waiting order. Returns false if the id isn't in the book.
- `bestBid()` / `bestAsk()` return the best price on each side, or 0 if that side is empty.

Trades happen at the price of the order that was already waiting. Prices are whole numbers (ticks).

## Design

- Each side is a `TreeMap` from price to the line of orders at that price. Bids are sorted high to low and asks low to high, so the best price is always the first entry.
- Each price's line is a doubly linked list, with the `prev`/`next` pointers stored on the `Order` itself. This lets cancel remove an order from the middle of a line in O(1), the same trick as an LRU cache.
- A `HashMap` from id to order makes cancel find the order in O(1).
- When a price's line becomes empty, it's removed from the `TreeMap`, so the best price is never a price nobody is at.

Finding a price in the `TreeMap` is O(log P), where P is the number of prices. Adding or removing an order within a line is O(1).

## Tests

`BookTest` checks six cases: no trade, full fill, partial fill (leftover rests), one order trading across two prices, cancel then match, and cancelling an id that doesn't exist.

## Benchmark

500,000 random orders around one price, median of 10 runs after warming up. MacBook Air M4:

| Version | Orders/sec |
|---|---|
| First version | 11.6 million |
| Reusing one trades list instead of a new one per order | 14.2 million |

The garbage collector log showed pauses of around 10 ms, which is huge compared to under 100 ns per order. Reusing the list meant less garbage, so fewer pauses and a faster run.

## What I'd do next

- Replace the `TreeMap` with a plain array indexed by price (a "price ladder"), which is faster when prices stay in a known range.
- Create fewer objects per order, so the garbage collector has less to clean up.
- Measure the time of each order, not just the total, and add cancels to the benchmark.
