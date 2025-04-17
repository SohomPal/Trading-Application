package com.trading.trading_application.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import com.trading.trading_application.lib.Order;

public class TradeLogger {

    private static final String LOG_FILE = "TradeLogs.txt";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    public static void logTrade(Order buyer, Order seller, double price, long volume, Date executionTime) {
        String tradeId = UUID.randomUUID().toString();
        String timestamp = DATE_FORMAT.format(executionTime);

        String logEntry = String.format(
                "%s %s %s %.2f %d %s %s %s %s\n",
                tradeId,
                timestamp,
                buyer.getStockSymbol(),
                price,
                volume,
                buyer.getOrderId(), buyer.getUserId(),
                seller.getOrderId(), seller.getUserId()
        );

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
            writer.write(logEntry);
        } catch (IOException e) {
            System.err.println("Failed to log trade: " + e.getMessage());
        }
    }
}
