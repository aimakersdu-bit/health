package com.example.health.plugin.impl;

import com.example.health.plugin.core.HealthCheckPlugin;
import com.example.health.plugin.core.ProbeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.LivenessState;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;

@Slf4j
@Component
@RequiredArgsConstructor
public class LivenessHealthPlugin implements HealthCheckPlugin {

    private final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
    private final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
    private final ApplicationEventPublisher eventPublisher;

    private static final double MEMORY_THRESHOLD = 0.90; // 90%

    @Override
    public String getName() {
        return "liveness-core";
    }

    @Override
    public ProbeType getType() {
        return ProbeType.LIVENESS;
    }

    @Override
    public Health check() {
        // 1. Check Deadlocks
        long[] deadlockedThreads = threadMXBean.findDeadlockedThreads();
        if (deadlockedThreads != null && deadlockedThreads.length > 0) {
            publishBrokenEvent("Deadlocked threads detected");
            return Health.down()
                    .withDetail("reason", "Deadlock detected")
                    .withDetail("deadlocked_threads_count", deadlockedThreads.length)
                    .build();
        }

        // 2. Check Memory Usage
        MemoryUsage heapUsage = memoryMXBean.getHeapMemoryUsage();
        long used = heapUsage.getUsed();
        long max = heapUsage.getMax();

        if (max > 0) { // Max might be -1 if undefined
            double usageRatio = (double) used / max;
            if (usageRatio > MEMORY_THRESHOLD) {
                publishBrokenEvent("Memory usage exceeded threshold: " + (usageRatio * 100) + "%");
                return Health.down()
                        .withDetail("reason", "Memory exhaustion")
                        .withDetail("usage_ratio", usageRatio)
                        .withDetail("used", used)
                        .withDetail("max", max)
                        .build();
            }
        }

        return Health.up().build();
    }

    private void publishBrokenEvent(String reason) {
        log.error("Liveness Check Failed: {}. Publishing BROKEN event to trigger diagnostics.", reason);
        // We catch exception to ensure we don't break the health check flow if publishing fails,
        // although publishing is usually safe.
        try {
            AvailabilityChangeEvent.publish(eventPublisher, this, LivenessState.BROKEN);
        } catch (Exception e) {
            log.error("Failed to publish LivenessState.BROKEN", e);
        }
    }
}
