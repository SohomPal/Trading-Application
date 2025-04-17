package com.trading.trading_application.API.Testing;

import com.trading.trading_application.API.OrderBookAPI;
import com.trading.trading_application.lib.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OrderBookAPITest {

    private OrderBookAPI api;

    @BeforeEach
    public void setup() {
        api = new OrderBookAPI();
    }

    @Test
    public void testBasicOrderPlacementAndQuerying() {
        // Place BUY orders for AAPL
        String buy1 = api.placeOrder("u1", "AAPL", 150.0, 100, Order.Side.BUY, OrderBookAPI.Type.GoodTilCancel);
        String buy2 = api.placeOrder("u2", "AAPL", 152.0, 200, Order.Side.BUY, OrderBookAPI.Type.GoodTilCancel);

        // Place SELL orders for AAPL
        String sell1 = api.placeOrder("u3", "AAPL", 155.0, 50, Order.Side.SELL, OrderBookAPI.Type.GoodTilCancel);
        String sell2 = api.placeOrder("u4", "AAPL", 154.0, 75, Order.Side.SELL, OrderBookAPI.Type.GoodTilCancel);

        // Place orders for GOOG
        api.placeOrder("u5", "GOOG", 2800.0, 300, Order.Side.BUY, OrderBookAPI.Type.GoodTilCancel);
        api.placeOrder("u6", "GOOG", 2810.0, 150, Order.Side.SELL, OrderBookAPI.Type.GoodTilCancel);

        // === AAPL Checks ===
        assertEquals(152.0, api.quotePrice("AAPL", Order.Side.BUY), 0.001, "Best AAPL bid should be 152.0");
        assertEquals(154.0, api.quotePrice("AAPL", Order.Side.SELL), 0.001, "Best AAPL ask should be 154.0");

        assertEquals(200L, api.getVolumeAtPrice("AAPL", 152.0, Order.Side.BUY), "Volume at AAPL 152.0 should be 200");
        assertEquals(75L, api.getVolumeAtPrice("AAPL", 154.0, Order.Side.SELL), "Volume at AAPL 154.0 should be 75");

        // === GOOG Checks ===
        assertEquals(2800.0, api.quotePrice("GOOG", Order.Side.BUY), 0.001, "Best GOOG bid should be 2800.0");
        assertEquals(2810.0, api.quotePrice("GOOG", Order.Side.SELL), 0.001, "Best GOOG ask should be 2810.0");

        assertEquals(300L, api.getVolumeAtPrice("GOOG", 2800.0, Order.Side.BUY), "Volume at GOOG 2800.0 should be 300");
        assertEquals(150L, api.getVolumeAtPrice("GOOG", 2810.0, Order.Side.SELL), "Volume at GOOG 2810.0 should be 150");
    }

    @Test
    public void testCancelOrderAndVolumeUpdate() {
        String id = api.placeOrder("u1", "MSFT", 310.0, 120, Order.Side.SELL, OrderBookAPI.Type.GoodTilCancel);

        assertEquals(120L, api.getVolumeAtPrice("MSFT", 310.0, Order.Side.SELL), "Initial volume should be 120");
        boolean removed = api.cancelOrder(id);
        assertTrue(removed, "Order should be cancelled");

        assertEquals(0L, api.getVolumeAtPrice("MSFT", 310.0, Order.Side.SELL), "Volume should be 0 after cancel");
        assertEquals(-1.0, api.quotePrice("MSFT", Order.Side.SELL), "No best price after cancellation");
    }

    @Test
    public void testMultipleOrdersSamePrice() {
        api.placeOrder("u1", "NFLX", 500.0, 100, Order.Side.BUY, OrderBookAPI.Type.GoodTilCancel);
        api.placeOrder("u2", "NFLX", 500.0, 200, Order.Side.BUY, OrderBookAPI.Type.GoodTilCancel);

        assertEquals(500.0, api.quotePrice("NFLX", Order.Side.BUY), 0.001);
        assertEquals(300L, api.getVolumeAtPrice("NFLX", 500.0, Order.Side.BUY), "Total volume at 500.0 should be 300");
    }

    @Test
    public void testOrderTypes_FOK_and_GTC() {
        // Setup: Add a SELL order for TSLA at 700.0, 100 shares (GTC)
        api.placeOrder("seller1", "TSLA", 700.0, 100, Order.Side.SELL, OrderBookAPI.Type.GoodTilCancel);


        // === FOK Test: Should succeed ===
        String fokSuccess = api.placeOrder("buyer1", "TSLA", 700.0, 100, Order.Side.BUY, OrderBookAPI.Type.FillOrKill);
        assertNotNull(fokSuccess, "FOK order should succeed when fully matchable");
        assertEquals(0L, api.getVolumeAtPrice("TSLA", 700.0, Order.Side.SELL), "Volume should be 0 after FOK match");

        // Setup again for failure test
        api.placeOrder("seller2", "TSLA", 705.0, 50, Order.Side.SELL, OrderBookAPI.Type.GoodTilCancel);

        // === FOK Test: Should fail ===
        String fokFail = api.placeOrder("buyer2", "TSLA", 705.0, 100, Order.Side.BUY, OrderBookAPI.Type.FillOrKill);
        assertNull(fokFail, "FOK order should fail if full volume is not available");
        assertEquals(50L, api.getVolumeAtPrice("TSLA", 705.0, Order.Side.SELL), "Volume should remain unchanged after failed FOK");

        // === GTC Test: Always accepted and added ===
        String gtcOrder = api.placeOrder("buyer3", "TSLA", 695.0, 70, Order.Side.BUY, OrderBookAPI.Type.GoodTilCancel);
        assertNotNull(gtcOrder, "GTC order should always be placed");
        assertEquals(70L, api.getVolumeAtPrice("TSLA", 695.0, Order.Side.BUY), "GTC order volume should be reflected");
    }

    @Test
    public void testCrossingOrdersAndDifferentTypes() {
        // === TSLA ===
        // GoodTilCancel Buy @ 700 x 100
        api.placeOrder("u1", "TSLA", 700.0, 100, Order.Side.BUY, OrderBookAPI.Type.GoodTilCancel);
        // FOK Sell @ 700 x 100 (should match fully)
        String sellFOK1 = api.placeOrder("u2", "TSLA", 700.0, 100, Order.Side.SELL, OrderBookAPI.Type.FillOrKill);
        assertNotNull(sellFOK1, "TSLA sell FOK should match and succeed");

        // FOK Buy @ 695 x 100 (no matching sell, should fail)
        String buyFOKFail = api.placeOrder("u3", "TSLA", 695.0, 100, Order.Side.BUY, OrderBookAPI.Type.FillOrKill);
        assertNull(buyFOKFail, "TSLA buy FOK at 695 should fail (no sellers)");

        // === AMZN ===
        // GoodTilCancel Sell @ 3300 x 50
        api.placeOrder("u4", "AMZN", 3300.0, 50, Order.Side.SELL, OrderBookAPI.Type.GoodTilCancel);
        // GoodTilCancel Buy @ 3305 x 30 (partial match)
        String buyGTC = api.placeOrder("u5", "AMZN", 3305.0, 30, Order.Side.BUY, OrderBookAPI.Type.GoodTilCancel);
        assertNotNull(sellFOK1, "AMZN buy GTC should match and succeed");
        // Remaining: Sell 3300 x 20

        // === NFLX ===
        // Add Buy and Sell orders at different prices
        api.placeOrder("u6", "NFLX", 510.0, 60, Order.Side.BUY, OrderBookAPI.Type.GoodTilCancel);
        api.placeOrder("u7", "NFLX", 515.0, 90, Order.Side.BUY, OrderBookAPI.Type.GoodTilCancel);
        api.placeOrder("u8", "NFLX", 520.0, 30, Order.Side.SELL, OrderBookAPI.Type.GoodTilCancel);
        api.placeOrder("u9", "NFLX", 515.0, 40, Order.Side.SELL, OrderBookAPI.Type.GoodTilCancel); // matches 90 at 515. Remainder should be 50

        // === TSLA Checks ===
        assertEquals(-1.0, api.quotePrice("TSLA", Order.Side.BUY), "No TSLA buy orders left");
        assertEquals(-1.0, api.quotePrice("TSLA", Order.Side.SELL), "No TSLA sell orders left");

        // === AMZN Checks ===
        assertEquals(3300.0, api.quotePrice("AMZN", Order.Side.SELL), 0.001, "Remaining AMZN ask should be 3300");
        assertEquals(20L, api.getVolumeAtPrice("AMZN", 3300.0, Order.Side.SELL), "Remaining AMZN ask volume should be 20");
        assertEquals(-1.0, api.quotePrice("AMZN", Order.Side.BUY), "All AMZN buys should be filled");

        // === NFLX Checks ===
        assertEquals(515.0, api.quotePrice("NFLX", Order.Side.BUY), 0.001, "Best NFLX buy should be 510");
        assertEquals(60L, api.getVolumeAtPrice("NFLX", 510.0, Order.Side.BUY), "Volume at 510 should be 60");
        assertEquals(520.0, api.quotePrice("NFLX", Order.Side.SELL), 0.001, "Best NFLX sell should be 520");
        assertEquals(30L, api.getVolumeAtPrice("NFLX", 520.0, Order.Side.SELL), "Remaining NFLX ask should be 30");
    }

    @Test
    public void testMarketOrders() {
        api.placeOrder("u1", "MSFT", 310.0, 20, Order.Side.BUY, OrderBookAPI.Type.GoodTilCancel);
        api.placeOrder("u2", "MSFT", 312.0, 20, Order.Side.BUY, OrderBookAPI.Type.GoodTilCancel);
        api.placeOrder("u3", "MSFT", 314.0, 20, Order.Side.BUY, OrderBookAPI.Type.GoodTilCancel);
        api.placeOrder("u4", "MSFT", 311.0, 50, Order.Side.SELL, OrderBookAPI.Type.MarketOrder);

        assertEquals(10L, api.getVolumeAtPrice("MSFT", 310, Order.Side.BUY), "There should be 10 left on buy side");
        assertEquals(0L, api.getVolumeAtPrice("MSFT", 310, Order.Side.SELL), "There should be 0 on sell side");
        assertEquals(310.0, api.getStockPrice("MSFT"));
    }

}
