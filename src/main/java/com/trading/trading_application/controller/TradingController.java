// src/main/java/com/trading/trading_application/controller/TradingController.java
package com.trading.trading_application.controller;

import com.trading.trading_application.API.OrderBookAPI;
import com.trading.trading_application.lib.Order;
import com.trading.trading_application.lib.TradeRequest;
import com.trading.trading_application.service.TradeRequestProcessor;
import com.trading.trading_application.API.OrderBookAPI.Type;
import com.trading.trading_application.utils.OrderLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class TradingController {

    @Autowired private TradeRequestProcessor processor;
    @Autowired private OrderBookAPI api;

    @PostMapping("/placeOrder")
    public String placeOrder(
            @RequestParam String userId,
            @RequestParam String stockSymbol,
            @RequestParam double price,
            @RequestParam long volume,
            @RequestParam Order.Side side,
            @RequestParam Type orderType
    ) {
        TradeRequest request = new TradeRequest(TradeRequest.RequestType.PLACE_ORDER);
        request.userId = userId;
        request.stockSymbol = stockSymbol;
        request.price = price;
        request.volume = volume;
        request.side = side;
        request.orderType = orderType;

        processor.submitRequest(request);
        OrderLogger.requestQueue.offer(request);

        return "Order Queued";
    }

    @PostMapping("/placeMarketOrder")
    public String placeMarketOrder(
            @RequestParam String userId,
            @RequestParam String stockSymbol,
            @RequestParam long volume,
            @RequestParam Order.Side side
    ) {
        TradeRequest request = new TradeRequest(TradeRequest.RequestType.PLACE_MARKET_ORDER);
        //System.out.println("Received order request from " + userId + " for " + stockSymbol);
        request.userId = userId;
        request.stockSymbol = stockSymbol;
        request.volume = volume;
        request.price = api.getStockPrice(stockSymbol);
        request.side = side;
        request.orderType = Type.MarketOrder;

        processor.submitRequest(request);
        OrderLogger.requestQueue.offer(request);

        return "Market Order Queued";
    }

    @DeleteMapping("/cancelOrder")
    public String cancelOrder(@RequestParam String orderId) {
        TradeRequest request = new TradeRequest(TradeRequest.RequestType.CANCEL_ORDER);
        request.orderId = orderId;

        processor.submitRequest(request);

        return "Cancel Request Queued";
    }

    @GetMapping("/quotePrice")
    public double getQuotePrice(@RequestParam String stockSymbol, @RequestParam Order.Side side) {
        return api.quotePrice(stockSymbol, side);
    }

    @GetMapping("/volumeAtPrice")
    public long getVolumeAtPrice(
            @RequestParam String stockSymbol,
            @RequestParam double price,
            @RequestParam Order.Side side
    ) {
        return api.getVolumeAtPrice(stockSymbol, price, side);
    }

    @PostMapping("/print")
    public void printOrderbook() {
        api.printOrderBook();
    }
}
