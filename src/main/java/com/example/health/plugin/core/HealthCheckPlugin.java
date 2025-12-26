package com.example.health.plugin.core;

import org.springframework.boot.actuate.health.Health;

public interface HealthCheckPlugin {
    /**
     * Plugin identifier, e.g., "database", "kafka"
     */
    String getName();

    /**
     * Execute check logic, return Health status object
     */
    Health check();

    /**
     * Probe type applicable for this plugin: LIVENESS or READINESS
     */
    ProbeType getType();
}
