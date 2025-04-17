package com.trading.trading_application.lib;

import java.util.HashMap;
import java.util.Map;

public class OrderBook {

    private final Map<String, StockOrders> BIDS;
    private final Map<String, StockOrders> ASKS;
    private final Map<String, Order> OrderMap;
    private final Map<String, Double> ExecutionPrice;

    public OrderBook() {
        this.BIDS = new HashMap<>();
        this.ASKS = new HashMap<>();
        this.OrderMap = new HashMap<>();
        this.ExecutionPrice = new HashMap<>();
    }

    // Add a new order to the appropriate side
    public void addOrder(Order order) {
        String symbol = order.getStockSymbol();
        StockOrders.Side side = order.getSide() == Order.Side.BUY ? StockOrders.Side.BUY : StockOrders.Side.SELL;
        Map<String, StockOrders> bookSide = side == StockOrders.Side.BUY ? BIDS : ASKS;

        bookSide.computeIfAbsent(symbol, s -> new StockOrders(side)).addOrder(order);
        OrderMap.put(order.getOrderId(), order);
    }

    public Order getOrder(String orderId){
        return OrderMap.get(orderId);
    }

    // Get best bid price for a stock symbol
    public Double getBestBid(String symbol) {
        StockOrders stockBids = BIDS.get(symbol);
        return stockBids != null ? stockBids.getBestPrice() : null;
    }

    // Get best ask price for a stock symbol
    public Double getBestAsk(String symbol) {
        StockOrders stockAsks = ASKS.get(symbol);
        return stockAsks != null ? stockAsks.getBestPrice() : null;
    }

    public boolean getStockAvailability(String symbol, Order.Side side) {
        if(side == Order.Side.BUY){
            return BIDS.containsKey(symbol);
        }
        else{
            return ASKS.containsKey((symbol));
        }
    }

    public long getTotalStockVolume(String symbol, Order.Side side) {
        if(side == Order.Side.BUY){
            return BIDS.containsKey(symbol) ? BIDS.get(symbol).getTotalVolume() : 0;
        }
        else{
            return ASKS.containsKey(symbol) ? ASKS.get(symbol).getTotalVolume() : 0;
        }
    }

    public long getStockVolumeAtPrice(String symbol, Order.Side side, double price) {
        if(side == Order.Side.BUY){
            Map<Double, FixedPriceOrderQueue> pricetoQueueMap = BIDS.containsKey(symbol) ? BIDS.get(symbol).getPriceToQueueMap() : null;
            if(pricetoQueueMap == null) return 0;
            return pricetoQueueMap.containsKey(price) ?  pricetoQueueMap.get(price).getTotalVolume() :  0;
        }
        else{
            Map<Double, FixedPriceOrderQueue> pricetoQueueMap = ASKS.containsKey(symbol) ? ASKS.get(symbol).getPriceToQueueMap() : null;
            if(pricetoQueueMap == null) return 0;
            return pricetoQueueMap.containsKey(price) ?  pricetoQueueMap.get(price).getTotalVolume() :  0;
        }
    }

    // Get all orders by side for a stock
    public StockOrders getOrdersBySide(String symbol, Order.Side side) {
        return side == Order.Side.BUY ? BIDS.get(symbol) : ASKS.get(symbol);
    }

    // Remove a specific order by object
    public boolean removeOrder(Order order) {
        String symbol = order.getStockSymbol();
        Map<String, StockOrders> bookSide = order.getSide() == Order.Side.BUY ? BIDS : ASKS;

        StockOrders stockOrders = bookSide.get(symbol);
        return stockOrders != null && stockOrders.removeOrder(order);
    }

    private long getTotalSellVolumeBelow(String stock, double priceThreshold) {
        StockOrders sellBook = ASKS.get(stock);
        if (sellBook == null) return 0;

        long total = 0;
        for (Map.Entry<Double, FixedPriceOrderQueue> entry : sellBook.getPriceToQueueMap().entrySet()) {
            if (entry.getKey() <= priceThreshold) {
                total += entry.getValue().getTotalVolume();
            }
        }
        return total;
    }

    private long getTotalBuyVolumeAbove(String stock, double priceThreshold) {
        StockOrders buyBook = BIDS.get(stock);
        if (buyBook == null) return 0;

        long total = 0;
        for (Map.Entry<Double, FixedPriceOrderQueue> entry : buyBook.getPriceToQueueMap().entrySet()) {
            if (entry.getKey() >= priceThreshold) {
                total += entry.getValue().getTotalVolume();
            }
        }
        return total;
    }

    public long getCrossableVolume(String stock, Order.Side side, double price) {
        if (side == Order.Side.BUY) {
            // A BUY order can cross with existing SELL orders priced <= price
            return getTotalBuyVolumeAbove(stock, price);

        } else if (side == Order.Side.SELL) {
            // A SELL order can cross with existing BUY orders priced >= price
            return getTotalSellVolumeBelow(stock, price);

        }
        return 0;
    }

    public double getPrice(String symbol){
        return ExecutionPrice.containsKey(symbol) ? ExecutionPrice.get(symbol) : 0.0;
    }

    public void updatePrice(String symbol, Double price){
        ExecutionPrice.put(symbol, price);
    }



    public void printOrderBook() {
        System.out.println("========== ORDER BOOK ==========");

        System.out.println("\n--- BIDS (Buy Orders) ---");
        for (String symbol : BIDS.keySet()) {
            System.out.println("Stock: " + symbol);
            StockOrders stockOrders = BIDS.get(symbol);
            stockOrders.printSide();
        }

        System.out.println("\n--- ASKS (Sell Orders) ---");
        for (String symbol : ASKS.keySet()) {
            System.out.println("Stock: " + symbol);
            StockOrders stockOrders = ASKS.get(symbol);
            stockOrders.printSide();
        }

        System.out.println("========== END ==========");
    }

    public void printLastExecutionPrices() {
        System.out.println("====== LAST EXECUTION PRICES ======");
        if (ExecutionPrice.isEmpty()) {
            System.out.println("No trades have been executed yet.");
        } else {
            System.out.printf("%-10s | %-10s\n", "Symbol", "Last Price");
            System.out.println("-----------------------------");
            for (Map.Entry<String, Double> entry : ExecutionPrice.entrySet()) {
                System.out.printf("%-10s | %-10.2f\n", entry.getKey(), entry.getValue());
            }
        }
        System.out.println("===================================");
    }


}
