package com.trading.trading_application.utils;

import com.trading.trading_application.lib.Order;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TradeLogger {

    private static final String LOG_FILE = "TradeLogs.txt";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static final BlockingQueue<TradeLogRequest> logQueue = new LinkedBlockingQueue<>();
    private static final Thread loggerThread;

    // Wrapper for trade logging data
    private static class TradeLogRequest {
        Order buyer;
        Order seller;
        double price;
        long volume;
        Date executionTime;

        TradeLogRequest(Order buyer, Order seller, double price, long volume, Date executionTime) {
            this.buyer = buyer;
            this.seller = seller;
            this.price = price;
            this.volume = volume;
            this.executionTime = executionTime;
        }
    }

    static {
        loggerThread = new Thread(() -> {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
                while (true) {
                    TradeLogRequest request = logQueue.take();
                    String logEntry = formatLogEntry(request);
                    writer.write(logEntry);
                    writer.flush(); // flush per entry
                }
            } catch (Exception e) {
                System.err.println("TradeLogger thread crashed: " + e.getMessage());
                e.printStackTrace();
            }
        });

        loggerThread.setDaemon(true); // Optional: closes when app exits
        loggerThread.start();
    }

    public static Boolean logTrade(Order buyer, Order seller, double price, long volume, Date executionTime) {
        TradeLogRequest request = new TradeLogRequest(buyer, seller, price, volume, executionTime);
        logQueue.offer(request);
        return logQueue.offer(request);
    }

    private static String formatLogEntry(TradeLogRequest req) {
        String tradeId = UUID.randomUUID().toString();
        String timestamp = DATE_FORMAT.format(req.executionTime);

        return String.format(
                "%s %s %s %.2f %d %s %s %s %s\n",
                tradeId,
                timestamp,
                req.buyer.getStockSymbol(),
                req.price,
                req.volume,
                req.buyer.getOrderId(), req.buyer.getUserId(),
                req.seller.getOrderId(), req.seller.getUserId()
        );
    }

    public static void shutdown() {
        loggerThread.interrupt(); // stop the logger thread cleanly
    }
}
