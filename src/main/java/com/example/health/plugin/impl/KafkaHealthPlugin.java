package com.example.health.plugin.impl;

import com.example.health.plugin.core.HealthCheckPlugin;
import com.example.health.plugin.core.ProbeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.health.Health;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaHealthPlugin implements HealthCheckPlugin {

    // Use ObjectProvider to handle optional dependency if Kafka is not configured
    private final ObjectProvider<KafkaListenerEndpointRegistry> registryProvider;

    @Override
    public String getName() {
        return "kafka";
    }

    @Override
    public ProbeType getType() {
        return ProbeType.READINESS;
    }

    @Override
    public Health check() {
        KafkaListenerEndpointRegistry registry = registryProvider.getIfAvailable();
        if (registry == null) {
            return Health.up().withDetail("message", "Kafka not configured").build();
        }

        try {
            // Check connectivity - In a real app, use AdminClient to describeCluster or check listener status
            // For this plugin demo, we assume if all containers are running or intended to run, it's fine.
            // A more robust check would involve connecting to the broker.

            boolean allRunning = true;
            for (MessageListenerContainer container : registry.getListenerContainers()) {
                if (!container.isRunning()) {
                     // In a real scenario, check if it SHOULD be running (autoStartup)
                     // Here we treat any stopped container as a potential issue unless paused by us
                     // But strictly, we pause on failure.
                }
            }

            // Simulating a connectivity check
            checkKafkaConnectivity();

            return Health.up().build();
        } catch (Exception e) {
            log.error("Kafka health check failed, pausing listeners to prevent rebalance storm", e);
            // Active Isolation: Pause consumers
            for (MessageListenerContainer container : registry.getListenerContainers()) {
                if (container.isRunning()) {
                    container.pause();
                }
            }
            return Health.down(e).withDetail("action", "Listeners Paused").build();
        }
    }

    private void checkKafkaConnectivity() {
        // Placeholder for actual AdminClient connectivity check.
        // If we wanted to really check, we would inject KafkaAdmin or similar.
        // For the purpose of the SDK demo, we assume this method throws if broker is down.
    }
}
