package com.example.health.plugin;

import com.example.health.plugin.core.HealthCheckPlugin;
import com.example.health.plugin.core.ProbeType;
import com.example.health.plugin.registry.PluginRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class PluginRegistryTest {

    @Test
    public void testRegistryExecuteSuccess() {
        HealthCheckPlugin mockPlugin = mock(HealthCheckPlugin.class);
        when(mockPlugin.getName()).thenReturn("test-plugin");
        when(mockPlugin.getType()).thenReturn(ProbeType.LIVENESS);
        when(mockPlugin.check()).thenReturn(Health.up().build());

        PluginRegistry registry = new PluginRegistry(Collections.singletonList(mockPlugin));

        Health result = registry.executeWithTimeout(mockPlugin, 1000);
        assertEquals(Status.UP, result.getStatus());
    }

    @Test
    public void testRegistryExecuteTimeout() {
        HealthCheckPlugin slowPlugin = mock(HealthCheckPlugin.class);
        when(slowPlugin.getName()).thenReturn("slow-plugin");
        when(slowPlugin.getType()).thenReturn(ProbeType.LIVENESS);
        when(slowPlugin.check()).thenAnswer(invocation -> {
            Thread.sleep(2000); // Sleep longer than timeout
            return Health.up().build();
        });

        PluginRegistry registry = new PluginRegistry(Collections.singletonList(slowPlugin));

        // Timeout set to 100ms
        Health result = registry.executeWithTimeout(slowPlugin, 100);
        assertEquals(Status.DOWN, result.getStatus());
        assertTrue(result.getDetails().get("error").toString().contains("timed out"));
    }
}
