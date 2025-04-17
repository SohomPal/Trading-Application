package com.trading.trading_application.utils;

import com.trading.trading_application.lib.TradeRequest;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class OrderLogger {

    private static final String LOG_FILE = "OrderLogs.txt";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static final AtomicInteger counter = new AtomicInteger(0);

    // 🔓 Public queue for incoming trade requests
    public static final BlockingQueue<TradeRequest> requestQueue = new LinkedBlockingQueue<>();

    private static final Thread loggerThread;

    static {
        loggerThread = new Thread(() -> {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
                while (true) {
                    try {
                        TradeRequest request = requestQueue.take(); // blocks until available
                        String logEntry = logOrder(request);
                        writer.write(logEntry);
                        writer.flush();
                    } catch (InterruptedException e) {
                        System.err.println("Logger thread interrupted.");
                        break;
                    } catch (IOException e) {
                        System.err.println("IO error while writing log: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                System.err.println("Logger failed to open file: " + e.getMessage());
                e.printStackTrace();
            }
        });

        loggerThread.setName("OrderLogger-Thread");
        loggerThread.setDaemon(true);
        loggerThread.start();
    }

    // 🔒 Internal logger function to format string
    private static String logOrder(TradeRequest request) {
        String orderId = String.format("%08x", counter.getAndIncrement()).toUpperCase();
        String timestamp = DATE_FORMAT.format(new Date());

        return String.format(
                "%s %s %s %.2f %d %s %s %s%n",
                orderId,
                timestamp,
                request.stockSymbol,
                request.price,
                request.volume,
                request.userId,
                request.side.name(),
                request.type.name()
        );
    }

    // Optional: Graceful shutdown method
    public static void stopLogger() {
        loggerThread.interrupt();
    }
}
