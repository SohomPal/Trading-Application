// src/main/java/com/trading/trading_application/config/OrderBookConfig.java
package com.trading.trading_application.config;

import com.trading.trading_application.API.OrderBookAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderBookConfig {

    @Bean
    public OrderBookAPI orderBookAPI() {
        return new OrderBookAPI();
    }
}
