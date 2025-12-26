package com.example.health.plugin.registry;

import com.example.health.plugin.core.HealthCheckPlugin;
import com.example.health.plugin.core.ProbeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class PluginRegistry {

    private final List<HealthCheckPlugin> plugins;
    // Dedicated executor for health checks to avoid blocking main threads
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public List<HealthCheckPlugin> getPlugins(ProbeType type) {
        return plugins.stream()
                .filter(p -> p.getType() == type)
                .collect(Collectors.toList());
    }

    public Health executeWithTimeout(HealthCheckPlugin plugin, long timeoutMillis) {
        CompletableFuture<Health> future = CompletableFuture.supplyAsync(plugin::check, executor);
        try {
            return future.get(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("Plugin {} execution failed or timed out", plugin.getName(), e);
            future.cancel(true); // Attempt to interrupt
            return Health.down()
                    .withException(e)
                    .withDetail("plugin", plugin.getName())
                    .withDetail("error", "Execution timed out or failed")
                    .build();
        }
    }

    public Map<String, Health> runAll(ProbeType type) {
         List<HealthCheckPlugin> targetPlugins = getPlugins(type);
         Map<String, Health> results = new ConcurrentHashMap<>();

         // In a real scenario, we might want to run these in parallel
         // For simplicity and safety, we iterate, but execute each with a timeout protection
         for (HealthCheckPlugin plugin : targetPlugins) {
             results.put(plugin.getName(), executeWithTimeout(plugin, 1000)); // 1s default timeout per plugin
         }
         return results;
    }
}
