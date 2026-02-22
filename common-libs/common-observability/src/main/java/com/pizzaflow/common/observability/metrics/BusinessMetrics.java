package com.pizzaflow.common.observability.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.Callable;

/**
 * Utility class for recording custom business metrics.
 */
@Component
public class BusinessMetrics {

    private final MeterRegistry meterRegistry;

    public BusinessMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * Records an order placed event.
     */
    public void recordOrderPlaced(String restaurantId, String orderType, double amount) {
        Counter.builder("pizzaflow.orders.placed")
                .tag("restaurant_id", restaurantId)
                .tag("order_type", orderType)
                .description("Number of orders placed")
                .register(meterRegistry)
                .increment();

        Counter.builder("pizzaflow.orders.revenue")
                .tag("restaurant_id", restaurantId)
                .tag("order_type", orderType)
                .description("Total revenue from orders")
                .register(meterRegistry)
                .increment(amount);
    }

    /**
     * Records an order completion event.
     */
    public void recordOrderCompleted(String restaurantId, Duration preparationTime) {
        Counter.builder("pizzaflow.orders.completed")
                .tag("restaurant_id", restaurantId)
                .description("Number of orders completed")
                .register(meterRegistry)
                .increment();

        Timer.builder("pizzaflow.orders.preparation_time")
                .tag("restaurant_id", restaurantId)
                .description("Order preparation time")
                .register(meterRegistry)
                .record(preparationTime);
    }

    /**
     * Records an order cancellation event.
     */
    public void recordOrderCancelled(String restaurantId, String reason) {
        Counter.builder("pizzaflow.orders.cancelled")
                .tag("restaurant_id", restaurantId)
                .tag("reason", reason)
                .description("Number of orders cancelled")
                .register(meterRegistry)
                .increment();
    }

    /**
     * Records a payment event.
     */
    public void recordPayment(String paymentMethod, String status, double amount) {
        Counter.builder("pizzaflow.payments")
                .tag("method", paymentMethod)
                .tag("status", status)
                .description("Number of payment transactions")
                .register(meterRegistry)
                .increment();

        if ("success".equals(status)) {
            Counter.builder("pizzaflow.payments.amount")
                    .tag("method", paymentMethod)
                    .description("Total payment amount")
                    .register(meterRegistry)
                    .increment(amount);
        }
    }

    /**
     * Records a delivery event.
     */
    public void recordDelivery(String status, Duration deliveryTime) {
        Counter.builder("pizzaflow.deliveries")
                .tag("status", status)
                .description("Number of deliveries")
                .register(meterRegistry)
                .increment();

        if ("completed".equals(status) && deliveryTime != null) {
            Timer.builder("pizzaflow.deliveries.time")
                    .description("Delivery time")
                    .register(meterRegistry)
                    .record(deliveryTime);
        }
    }

    /**
     * Records inventory stock levels.
     */
    public void recordStockLevel(String restaurantId, String ingredientId, int quantity, int threshold) {
        meterRegistry.gauge("pizzaflow.inventory.stock_level",
                io.micrometer.core.instrument.Tags.of(
                        "restaurant_id", restaurantId,
                        "ingredient_id", ingredientId
                ),
                quantity);

        if (quantity <= threshold) {
            Counter.builder("pizzaflow.inventory.low_stock_alerts")
                    .tag("restaurant_id", restaurantId)
                    .tag("ingredient_id", ingredientId)
                    .description("Low stock alerts")
                    .register(meterRegistry)
                    .increment();
        }
    }

    /**
     * Records kitchen queue metrics.
     */
    public void recordKitchenQueue(String restaurantId, int pendingOrders, int inProgressOrders) {
        meterRegistry.gauge("pizzaflow.kitchen.pending_orders",
                io.micrometer.core.instrument.Tags.of("restaurant_id", restaurantId),
                pendingOrders);

        meterRegistry.gauge("pizzaflow.kitchen.in_progress_orders",
                io.micrometer.core.instrument.Tags.of("restaurant_id", restaurantId),
                inProgressOrders);
    }

    /**
     * Times a business operation.
     */
    public <T> T timeOperation(String name, String operation, Callable<T> callable) throws Exception {
        Timer timer = Timer.builder("pizzaflow.operations")
                .tag("name", name)
                .tag("operation", operation)
                .description("Business operation timing")
                .register(meterRegistry);

        return timer.recordCallable(callable);
    }

    /**
     * Records a customer event.
     */
    public void recordCustomerEvent(String eventType, String customerId) {
        Counter.builder("pizzaflow.customers.events")
                .tag("event_type", eventType)
                .description("Customer events")
                .register(meterRegistry)
                .increment();
    }
}
