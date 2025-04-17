// src/main/java/com/trading/trading_application/service/TradeRequestProcessor.java
package com.trading.trading_application.service;

import com.trading.trading_application.API.OrderBookAPI;
import com.trading.trading_application.lib.TradeRequest;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;

@Service
public class TradeRequestProcessor {

    private final BlockingQueue<TradeRequest> queue = new LinkedBlockingQueue<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(10); // 👈 Tune this for performance
    private final OrderBookAPI api;

    public TradeRequestProcessor(OrderBookAPI api) {
        this.api = api;
    }

    public void submitRequest(TradeRequest request) {
        //System.out.println("Submitting request for user: " + request.userId);
        queue.offer(request);
    }

    @PostConstruct
    public void startProcessing() {
        Runnable dispatcher = () -> {
            while (true) {
                try {
                    TradeRequest req = queue.take(); // Blocking
                    executor.submit(() -> processRequest(req)); // Process in thread pool
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        };

        Thread dispatcherThread = new Thread(dispatcher);
        dispatcherThread.setDaemon(true);
        dispatcherThread.start();
    }

    private void processRequest(TradeRequest req) {
        //System.out.println("Processing request: " + req.userId);
        switch (req.type) {
            case PLACE_ORDER -> api.placeOrder(req.userId, req.stockSymbol, req.price, req.volume, req.side, req.orderType);
            case PLACE_MARKET_ORDER -> {
                double marketPrice = api.quotePrice(req.stockSymbol,
                        req.side == com.trading.trading_application.lib.Order.Side.BUY
                                ? com.trading.trading_application.lib.Order.Side.SELL
                                : com.trading.trading_application.lib.Order.Side.BUY);
                if (marketPrice != -1) {
                    api.placeOrder(req.userId, req.stockSymbol, marketPrice, req.volume, req.side, OrderBookAPI.Type.MarketOrder);
                }
            }
            case CANCEL_ORDER -> api.cancelOrder(req.orderId);
        }
    }
}
