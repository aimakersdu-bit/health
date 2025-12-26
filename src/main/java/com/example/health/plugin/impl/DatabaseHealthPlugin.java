package com.example.health.plugin.impl;

import com.example.health.plugin.core.HealthCheckPlugin;
import com.example.health.plugin.core.ProbeType;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
@RequiredArgsConstructor
public class DatabaseHealthPlugin implements HealthCheckPlugin {

    private final DataSource dataSource;

    @Override
    public String getName() {
        return "database";
    }

    @Override
    public ProbeType getType() {
        return ProbeType.READINESS;
    }

    @Override
    public Health check() {
        try {
            if (dataSource instanceof HikariDataSource) {
                HikariDataSource hikariDS = (HikariDataSource) dataSource;
                HikariPoolMXBean poolMetrics = hikariDS.getHikariPoolMXBean();

                if (poolMetrics == null) {
                     // Metrics might not be enabled or available yet
                     return Health.up().withDetail("message", "Hikari pool metrics not available").build();
                }

                int active = poolMetrics.getActiveConnections();
                int max = hikariDS.getMaximumPoolSize();

                // If active connections occupy more than 95%, mark as down
                if (max > 0 && active > max * 0.95) {
                    return Health.down()
                            .withDetail("reason", "Connection pool exhausted")
                            .withDetail("active", active)
                            .withDetail("max", max)
                            .build();
                }
                return Health.up()
                        .withDetail("active", active)
                        .withDetail("idle", poolMetrics.getIdleConnections())
                        .build();
            } else {
                // Fallback for other data sources (basic connectivity check)
                try (var conn = dataSource.getConnection()) {
                    if (conn.isValid(1)) {
                        return Health.up().withDetail("database", "reachable").build();
                    }
                }
            }
        } catch (Exception e) {
            return Health.down(e).build();
        }
        return Health.down().withDetail("reason", "Unknown database error").build();
    }
}
