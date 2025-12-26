package com.example.health.plugin.config;

import com.example.health.plugin.core.ProbeType;
import com.example.health.plugin.registry.PluginRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class PluginHealthConfig {

    private final PluginRegistry registry;

    @Bean("livenessPluginIndicator")
    public HealthIndicator livenessPluginIndicator() {
        return () -> {
            Map<String, Health> results = registry.runAll(ProbeType.LIVENESS);
            return aggregate(results);
        };
    }

    @Bean("readinessPluginIndicator")
    public HealthIndicator readinessPluginIndicator() {
        return () -> {
            Map<String, Health> results = registry.runAll(ProbeType.READINESS);
            return aggregate(results);
        };
    }

    private Health aggregate(Map<String, Health> results) {
        Health.Builder builder = Health.up();
        boolean allUp = true;
        for (Map.Entry<String, Health> entry : results.entrySet()) {
            builder.withDetail(entry.getKey(), entry.getValue());
            if (entry.getValue().getStatus().equals(org.springframework.boot.actuate.health.Status.DOWN)) {
                allUp = false;
            }
        }
        return allUp ? builder.build() : builder.down().build();
    }
}
