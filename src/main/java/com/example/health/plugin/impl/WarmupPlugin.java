package com.example.health.plugin.impl;

import com.example.health.plugin.core.HealthCheckPlugin;
import com.example.health.plugin.core.ProbeType;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class WarmupPlugin implements HealthCheckPlugin {

    private final AtomicBoolean warmedUp = new AtomicBoolean(false);
    private final long startTime = System.currentTimeMillis();
    // Simulate 30 seconds warm up time
    private static final long WARMUP_DURATION = 30000;

    @Override
    public String getName() {
        return "warmup";
    }

    @Override
    public ProbeType getType() {
        return ProbeType.READINESS;
    }

    @Override
    public Health check() {
        if (warmedUp.get()) {
            return Health.up().build();
        }

        // Simulate logic checking if cache is hot
        if (System.currentTimeMillis() - startTime > WARMUP_DURATION) {
            warmedUp.set(true);
            return Health.up().withDetail("message", "Warmup complete").build();
        }

        return Health.down()
                .withDetail("message", "Warming up caches...")
                .withDetail("elapsed", System.currentTimeMillis() - startTime)
                .build();
    }
}
