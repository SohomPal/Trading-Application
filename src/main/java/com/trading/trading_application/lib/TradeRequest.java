package com.trading.trading_application.lib;

import com.trading.trading_application.API.OrderBookAPI.Type;

public class TradeRequest {
    public enum RequestType {
        PLACE_ORDER,
        PLACE_MARKET_ORDER,
        CANCEL_ORDER
    }

    public RequestType type;
    public String userId;
    public String stockSymbol;
    public double price;
    public long volume;
    public Order.Side side;
    public Type orderType;
    public String orderId;

    public TradeRequest(RequestType type) {
        this.type = type;
    }
}
