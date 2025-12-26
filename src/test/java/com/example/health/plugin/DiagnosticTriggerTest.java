package com.example.health.plugin;

import com.example.health.plugin.diagnostics.DiagnosticTrigger;
import org.junit.jupiter.api.Test;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.LivenessState;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DiagnosticTriggerTest {

    @Test
    public void testTriggerWritesFile() throws Exception {
        String tmpDir = System.getProperty("java.io.tmpdir");
        // Create a separate subdir to avoid clutter
        Path diagnosticsDir = Path.of(tmpDir, "health-plugin-diagnostics-test");

        // Mocking the Value injection via setter or reflection is hard without Spring context in unit test
        // So we will just use reflection or a partial mock if needed, but since we are unit testing,
        // we can just use the public method or rely on the field injection if we bring up a context.
        // Easiest is to set the field via reflection or assume the class uses the injected value.
        // Let's modify DiagnosticTrigger to have a setter for easier testing or use ReflectionTestUtils.

        DiagnosticTrigger trigger = new DiagnosticTrigger();
        org.springframework.test.util.ReflectionTestUtils.setField(trigger, "diagnosticsPath", diagnosticsDir.toString());

        // Trigger the event
        trigger.onLivenessStateChange(new AvailabilityChangeEvent<>(new Object(), LivenessState.BROKEN));

        // Check if a file was created
        File dir = diagnosticsDir.toFile();
        assertTrue(dir.exists());
        assertTrue(dir.isDirectory());

        File[] files = dir.listFiles((d, name) -> name.startsWith("thread_dump_liveness_broken"));
        assertTrue(files != null && files.length > 0, "Should have created a thread dump file");

        // Cleanup
        for (File f : files) {
            f.delete();
        }
        dir.delete();
    }
}
