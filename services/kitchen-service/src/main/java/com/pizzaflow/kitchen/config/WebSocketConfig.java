package com.pizzaflow.kitchen.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration for Kitchen Display System.
 * Enables real-time updates to kitchen displays.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable a simple in-memory message broker
        // Clients subscribe to /topic/kitchen/{restaurantId} for updates
        config.enableSimpleBroker("/topic", "/queue");

        // Messages with /app destination are routed to @MessageMapping methods
        config.setApplicationDestinationPrefixes("/app");

        // User-specific messages prefix
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket endpoint for STOMP connections
        registry.addEndpoint("/ws/kitchen")
                .setAllowedOriginPatterns("*")
                .withSockJS();

        // Plain WebSocket endpoint (without SockJS fallback)
        registry.addEndpoint("/ws/kitchen")
                .setAllowedOriginPatterns("*");
    }
}
