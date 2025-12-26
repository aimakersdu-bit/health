package com.example.health.plugin.impl;

import com.example.health.plugin.core.HealthCheckPlugin;
import com.example.health.plugin.core.ProbeType;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

@Component
public class LivenessHealthPlugin implements HealthCheckPlugin {

    private final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

    @Override
    public String getName() {
        return "deadlock";
    }

    @Override
    public ProbeType getType() {
        return ProbeType.LIVENESS;
    }

    @Override
    public Health check() {
        long[] deadlockedThreads = threadMXBean.findDeadlockedThreads();
        if (deadlockedThreads != null && deadlockedThreads.length > 0) {
            return Health.down()
                    .withDetail("deadlocked_threads_count", deadlockedThreads.length)
                    .build();
        }
        return Health.up().build();
    }
}
