package com.trading.trading_application.service;

import com.trading.trading_application.API.OrderBookAPI;
import com.trading.trading_application.lib.TradeRequest;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;

@Service
public class TradeRequestProcessor {

    private final BlockingQueue<TradeRequest> queue = new LinkedBlockingQueue<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(10); // Fixed 10 threads
    private final OrderBookAPI api;

    public TradeRequestProcessor(OrderBookAPI api) {
        this.api = api;
    }

    // Submit a trade request to the queue
    public void submitRequest(TradeRequest request) {
        queue.offer(request);
    }

    @PostConstruct
    public void startProcessing() {
        // Runnable dispatcher for managing stock symbol letter ranges
        Runnable dispatcher = () -> {
            while (true) {
                try {
                    TradeRequest req = queue.take(); // Blocking call to take request from queue
                    // Dispatch based on the first letter of the stock symbol (A-C, D-F, etc.)
                    executor.submit(() -> processRequest(req));
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
        // Dispatch requests to specific threads based on stock symbol
        char firstLetter = req.stockSymbol.toUpperCase().charAt(0);
        int threadIndex = getThreadIndex(firstLetter); // Determine which thread should handle it

        // Dispatch the request based on thread index (each thread will handle a group of stock symbols)
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

    // Get thread index based on the first letter of the stock symbol
    private int getThreadIndex(char firstLetter) {
        if (firstLetter >= 'A' && firstLetter <= 'C') return 0;
        if (firstLetter >= 'D' && firstLetter <= 'F') return 1;
        if (firstLetter >= 'G' && firstLetter <= 'I') return 2;
        if (firstLetter >= 'J' && firstLetter <= 'L') return 3;
        if (firstLetter >= 'M' && firstLetter <= 'O') return 4;
        if (firstLetter >= 'P' && firstLetter <= 'R') return 5;
        if (firstLetter >= 'S' && firstLetter <= 'U') return 6;
        if (firstLetter >= 'V' && firstLetter <= 'X') return 7;
        if (firstLetter >= 'Y' && firstLetter <= 'Z') return 8;
        return 9; // Default, for any unexpected character
    }
}
