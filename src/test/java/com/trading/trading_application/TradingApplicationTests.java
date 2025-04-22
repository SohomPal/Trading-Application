package com.trading.trading_application;


import com.trading.trading_application.lib.TradeRequest;
import com.trading.trading_application.API.OrderBookAPI;
import com.trading.trading_application.service.TradeRequestProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@SpringBootTest
class TradingApplicationTests {

	@Mock
	private OrderBookAPI orderBookAPI;  // Mock the OrderBookAPI

	@InjectMocks
	private TradeRequestProcessor tradeRequestProcessor;  // The service we're testing

	@BeforeEach
	public void setUp() {
		// Initialize mocks before each test
		MockitoAnnotations MockitoAnnotations = null;
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testProcessMultipleOrders() throws InterruptedException {
		// Generate 20 test trade requests
		for (int i = 0; i < 20; i++) {
			TradeRequest request = new TradeRequest(TradeRequest.RequestType.PLACE_ORDER);

			tradeRequestProcessor.submitRequest(request);
		}

		// Allow some time for the dispatcher thread to process requests
		Thread.sleep(2000);

		// Verify that the placeOrder method was called 20 times (once for each order)
		verify(orderBookAPI, times(20)).placeOrder(Mockito.anyString(), Mockito.anyString(), Mockito.anyDouble(), Mockito.anyInt(), Mockito.any(), Mockito.any());
	}

}
