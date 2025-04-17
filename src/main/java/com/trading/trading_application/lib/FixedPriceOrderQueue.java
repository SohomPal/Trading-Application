package com.trading.trading_application.lib;

import java.util.LinkedList;
import java.util.Queue;

public class FixedPriceOrderQueue {

    private final double price;
    private long totalVolume;
    private final Queue<Order> orders;

    public FixedPriceOrderQueue(double price) {
        this.price = price;
        this.totalVolume = 0;
        this.orders = new LinkedList<>();
    }

    // Getter for price
    public double getPrice() {
        return price;
    }

    // Getter for totalVolume
    public long getTotalVolume() {
        return totalVolume;
    }

    // Getter for queue (read-only purposes)
    public Queue<Order> getOrders() {
        return new LinkedList<>(orders); // return a copy to prevent external modification
    }

    // Add an order to the queue
    public void addOrder(Order order) {
        if (order.getPrice() != this.price) {
            throw new IllegalArgumentException("Order price does not match the queue price level.");
        }

        orders.add(order);
        totalVolume += order.getVolume();
    }

    // Optionally, a method to remove the head order (e.g., after full execution)
    public Order pollOrder() {
        Order order = orders.poll();
        if (order != null) {
            totalVolume -= order.getVolume();
            if (totalVolume < 0) totalVolume = 0; // safety check
        }
        return order;
    }

    public Order peekOrder() {
        Order order = orders.peek();
        if(order != null){
            return order;
        }
        return null;
    }


    // Adjust total volume manually (e.g., after a partial fill)
    public void adjustTotalVolume(long delta) {
        this.totalVolume += delta;
        if (this.totalVolume < 0) this.totalVolume = 0; // safety
    }
}
