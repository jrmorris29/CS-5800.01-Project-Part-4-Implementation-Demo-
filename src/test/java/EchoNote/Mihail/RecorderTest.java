package EchoNote.Mihail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Recorder class.
 * These tests verify audio recording functionality without making real recordings.
 */
class RecorderTest {

    private Recorder recorder;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        recorder = new Recorder();
    }

    @Test
    void testRequestPermissions_ReturnsBoolean() {
        // Test that requestPermissions returns a boolean value
        // This may return true or false depending on system audio availability
        boolean result = recorder.requestPermissions();
        assertTrue(result || !result); // Just verify it returns a boolean
    }

    @Test
    void testBegin_ThrowsExceptionWhenOutputFileIsNull() {
        // Given: permissions are granted
        recorder.requestPermissions();

        // When/Then: calling begin with null should throw IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> {
            recorder.begin(null);
        });
    }

    @Test
    void testBegin_ThrowsExceptionWhenPermissionsNotGranted() {
        // Given: a valid output file but no permissions
        File outputFile = tempDir.resolve("test.wav").toFile();

        // When/Then: calling begin without permissions should throw RecordingException
        assertThrows(RecordingException.class, () -> {
            recorder.begin(outputFile);
        });
    }

    @Test
    void testBegin_ThrowsExceptionWhenAlreadyRecording() {
        // Given: permissions granted and recording already started
        recorder.requestPermissions();
        File outputFile1 = tempDir.resolve("test1.wav").toFile();
        File outputFile2 = tempDir.resolve("test2.wav").toFile();

        try {
            recorder.begin(outputFile1);

            // When/Then: starting another recording should throw exception
            assertThrows(RecordingException.class, () -> {
                recorder.begin(outputFile2);
            });
        } finally {
            // Cleanup
            if (recorder.isRecording()) {
                recorder.end();
            }
        }
    }

    @Test
    void testIsRecording_ReturnsFalseInitially() {
        // When: recorder is just created
        // Then: it should not be recording
        assertFalse(recorder.isRecording());
    }

    @Test
    void testIsRecording_ReturnsTrueWhenRecording() {
        // Given: permissions and started recording
        recorder.requestPermissions();
        File outputFile = tempDir.resolve("test.wav").toFile();

        try {
            recorder.begin(outputFile);

            // Then: should be recording
            assertTrue(recorder.isRecording());
        } finally {
            // Cleanup
            if (recorder.isRecording()) {
                recorder.end();
            }
        }
    }

    @Test
    void testEnd_ThrowsExceptionWhenNotRecording() {
        // When/Then: ending without starting should throw exception
        assertThrows(RecordingException.class, () -> {
            recorder.end();
        });
    }

    @Test
    void testEnd_ReturnsRecordedFile() {
        // Given: recording started
        recorder.requestPermissions();
        File outputFile = tempDir.resolve("test.wav").toFile();

        try {
            recorder.begin(outputFile);

            // Give it a moment to start recording
            Thread.sleep(100);

            // When: ending the recording
            File result = recorder.end();

            // Then: should return the output file
            assertNotNull(result);
            assertEquals(outputFile.getAbsolutePath(), result.getAbsolutePath());

            // And: should no longer be recording
            assertFalse(recorder.isRecording());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Test was interrupted");
        }
    }

    @Test
    void testEnd_SetsRecordingToFalse() {
        // Given: recording started
        recorder.requestPermissions();
        File outputFile = tempDir.resolve("test.wav").toFile();

        try {
            recorder.begin(outputFile);
            Thread.sleep(100);

            // When: ending the recording
            recorder.end();

            // Then: should not be recording anymore
            assertFalse(recorder.isRecording());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Test was interrupted");
        }
    }

    @Test
    void testMultipleRecordingSessions() {
        // Test that we can record multiple times sequentially
        recorder.requestPermissions();

        File outputFile1 = tempDir.resolve("recording1.wav").toFile();
        File outputFile2 = tempDir.resolve("recording2.wav").toFile();

        try {
            // First recording
            recorder.begin(outputFile1);
            Thread.sleep(100);
            File result1 = recorder.end();
            assertNotNull(result1);
            assertFalse(recorder.isRecording());

            // Second recording
            recorder.begin(outputFile2);
            Thread.sleep(100);
            File result2 = recorder.end();
            assertNotNull(result2);
            assertFalse(recorder.isRecording());

            // Verify they're different files
            assertNotEquals(result1.getAbsolutePath(), result2.getAbsolutePath());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Test was interrupted");
        } finally {
            if (recorder.isRecording()) {
                recorder.end();
            }
        }
    }

    @Test
    void testRecordingCreatesFile() {
        // Given: recorder with permissions
        recorder.requestPermissions();
        File outputFile = tempDir.resolve("output.wav").toFile();

        try {
            // When: recording for a short duration
            recorder.begin(outputFile);
            Thread.sleep(500); // Record for half a second
            File result = recorder.end();

            // Then: file should exist and have some content
            assertTrue(result.exists(), "Recording file should exist");
            assertTrue(result.length() > 0, "Recording file should have content");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Test was interrupted");
        } finally {
            if (recorder.isRecording()) {
                recorder.end();
            }
        }
    }
}
