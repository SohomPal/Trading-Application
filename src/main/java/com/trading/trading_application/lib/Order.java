package com.trading.trading_application.lib;

import java.util.Date;
import java.util.UUID;

public class Order {

    public enum Side {
        BUY, SELL
    }

    private final String stockSymbol;
    private final double price;
    private long volume;
    private final Date timestamp;
    private final String userId;
    private final String orderId;
    private final Side side;

    // Private constructor to enforce use of builder
    private Order(Builder builder) {
        this.stockSymbol = builder.stockSymbol;
        this.price = builder.price;
        this.volume = builder.volume;
        this.timestamp = builder.timestamp;
        this.userId = builder.userId;
        this.orderId = builder.orderId;
        this.side = builder.side;
    }

    // Public getters
    public String getStockSymbol() {
        return stockSymbol;
    }

    public double getPrice() {
        return price;
    }

    public long getVolume() {
        return volume;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public String getOrderId() {
        return orderId;
    }

    public Side getSide() {
        return side;
    }

    // Public method to adjust volume
    public void adjustVolume(long delta) {
        this.volume += delta;
        if (this.volume < 0) this.volume = 0;
    }

    // Static Builder class
    public static class Builder {
        private String stockSymbol;
        private double price;
        private long volume;
        private Date timestamp;
        private final String userId;
        private final String orderId;
        private Side side;

        public Builder(String userId) {
            this.userId = userId;
            this.orderId = UUID.randomUUID().toString();
        }

        public Builder stockSymbol(String stockSymbol) {
            this.stockSymbol = stockSymbol;
            return this;
        }

        public Builder price(double price) {
            this.price = price;
            return this;
        }

        public Builder volume(long volume) {
            this.volume = volume;
            return this;
        }

        public Builder timestamp(Date timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder side(Side side) {
            this.side = side;
            return this;
        }

        public Order build() {
            if (timestamp == null) {
                this.timestamp = new Date();
            }
            return new Order(this);
        }
    }

    @Override
    public String toString() {
        return "Order{" +
                "stockSymbol='" + stockSymbol + '\'' +
                ", price=" + price +
                ", volume=" + volume +
                ", timestamp=" + timestamp +
                ", userId='" + userId + '\'' +
                ", orderId='" + orderId + '\'' +
                ", side=" + side +
                '}';
    }
}
