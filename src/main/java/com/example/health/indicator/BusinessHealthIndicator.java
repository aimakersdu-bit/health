package com.example.health.indicator;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Checks a specific business logic condition.
 * For example: "Are we accepting new orders?" or "Is the payment gateway active?"
 */
@Component
public class BusinessHealthIndicator implements HealthIndicator {

    // Default to healthy
    private final AtomicBoolean businessUp = new AtomicBoolean(true);

    @Override
    public Health health() {
        if (businessUp.get()) {
            return Health.up()
                    .withDetail("businessFeature", "OrderProcessing")
                    .withDetail("queueDepth", 0) // Example metric
                    .build();
        } else {
            return Health.down()
                    .withDetail("businessFeature", "OrderProcessing")
                    .withDetail("reason", "Manual Maintenance Mode")
                    .build();
        }
    }

    public void setBusinessUp(boolean up) {
        this.businessUp.set(up);
    }
}
