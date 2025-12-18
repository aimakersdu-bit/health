package com.example.health.indicator;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Checks the status of the microservice registration.
 * In a real scenario, this might check the DiscoveryClient status
 * or a heartbeat timestamp.
 */
@Component
public class RegistrationHealthIndicator implements HealthIndicator {

    // Simulating registration status
    private final AtomicBoolean isRegistered = new AtomicBoolean(true);

    @Override
    public Health health() {
        if (isRegistered.get()) {
            return Health.up()
                    .withDetail("serviceRegistry", "Nacos/Eureka") // Example
                    .withDetail("status", "REGISTERED")
                    .build();
        } else {
            return Health.down()
                    .withDetail("serviceRegistry", "Nacos/Eureka")
                    .withDetail("status", "NOT_REGISTERED")
                    .withDetail("error", "Failed to renew lease")
                    .build();
        }
    }

    public void setRegistered(boolean registered) {
        this.isRegistered.set(registered);
    }
}
