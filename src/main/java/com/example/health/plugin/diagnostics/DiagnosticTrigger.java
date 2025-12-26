package com.example.health.plugin.diagnostics;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.LivenessState;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class DiagnosticTrigger {

    @Value("${health.plugin.diagnostics.path:/tmp/diagnostics}")
    private String diagnosticsPath;

    @EventListener
    public void onLivenessStateChange(AvailabilityChangeEvent<LivenessState> event) {
        if (event.getState() == LivenessState.BROKEN) {
            log.warn("Liveness BROKEN detected. Triggering diagnostics...");
            triggerDiagnostics("liveness_broken");
        }
    }

    public void triggerDiagnostics(String reason) {
        try {
            Path dir = Paths.get(diagnosticsPath);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = String.format("thread_dump_%s_%s.txt", reason, timestamp);
            File dumpFile = dir.resolve(filename).toFile();

            try (FileWriter writer = new FileWriter(dumpFile)) {
                writer.write("Reason: " + reason + "\n");
                writer.write("Timestamp: " + LocalDateTime.now() + "\n\n");
                writer.write(ManagementFactory.getThreadMXBean().dumpAllThreads(true, true).toString());
            }

            log.info("Diagnostics saved to {}", dumpFile.getAbsolutePath());

        } catch (IOException e) {
            log.error("Failed to write diagnostic dump", e);
        }
    }
}
