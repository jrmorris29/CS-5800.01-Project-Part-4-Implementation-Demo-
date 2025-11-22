package EchoNote.Mihail;

import EchoNote.Jack.Transcript;
import EchoNote.Jack.TranscriptSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Transcriber class.
 * Note: These tests focus on file parsing functionality.
 * The transcribe() method requires a valid OpenAI API key and makes real API calls,
 * so it should be tested in integration tests, not unit tests.
 */
class TranscriberTest {

    private Transcriber transcriber;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        // Note: This will try to load the .env file
        // For unit tests without API calls, we focus on the parse() method
        try {
            transcriber = new Transcriber();
        } catch (Exception e) {
            // If OpenAI client initialization fails (e.g., no .env file in test environment)
            // we can still test the parse() methods which don't require the API
            transcriber = null;
        }
    }

    // ========== Tests for parse() method ==========

    @Test
    void testParse_ThrowsExceptionWhenFileIsNull() {
        if (transcriber == null) {
            transcriber = new Transcriber();
        }

        assertThrows(IllegalArgumentException.class, () -> {
            transcriber.parse(null);
        });
    }

    @Test
    void testParse_ThrowsExceptionWhenFileDoesNotExist() {
        if (transcriber == null) {
            transcriber = new Transcriber();
        }

        File nonExistentFile = new File("/nonexistent/file.vtt");

        assertThrows(IllegalArgumentException.class, () -> {
            transcriber.parse(nonExistentFile);
        });
    }

    @Test
    void testParse_ThrowsExceptionForUnsupportedFileFormat() throws IOException {
        if (transcriber == null) {
            transcriber = new Transcriber();
        }

        File unsupportedFile = tempDir.resolve("test.doc").toFile();
        Files.writeString(unsupportedFile.toPath(), "Some content");

        assertThrows(IllegalArgumentException.class, () -> {
            transcriber.parse(unsupportedFile);
        });
    }

    @Test
    void testParse_ParsesSimpleTextFile() throws IOException {
        if (transcriber == null) {
            transcriber = new Transcriber();
        }

        String content = "This is a simple transcript of a meeting.";
        File txtFile = tempDir.resolve("transcript.txt").toFile();
        Files.writeString(txtFile.toPath(), content);

        Transcript result = transcriber.parse(txtFile);

        assertNotNull(result);
        assertEquals(content, result.getRawText());
        assertEquals(TranscriptSource.IMPORTED, result.getSource());
        assertTrue(result.getTimestamps().isEmpty());
        assertNotNull(result.getId());
    }

    @Test
    void testParse_ParsesVTTFile() throws IOException {
        if (transcriber == null) {
            transcriber = new Transcriber();
        }

        String vttContent = """
                WEBVTT
                
                00:00:00.000 --> 00:00:05.000
                Welcome to the meeting.
                
                00:00:05.000 --> 00:00:10.000
                Let's discuss the project status.
                
                00:00:10.000 --> 00:00:15.000
                We have made good progress.
                """;

        File vttFile = tempDir.resolve("transcript.vtt").toFile();
        Files.writeString(vttFile.toPath(), vttContent);

        Transcript result = transcriber.parse(vttFile);

        assertNotNull(result);
        assertFalse(result.getRawText().isEmpty());
        assertTrue(result.getRawText().contains("Welcome to the meeting"));
        assertTrue(result.getRawText().contains("discuss the project status"));
        assertTrue(result.getRawText().contains("made good progress"));
        assertEquals(TranscriptSource.IMPORTED, result.getSource());
        assertEquals(3, result.getTimestamps().size());
        assertNotNull(result.getId());
    }

    @Test
    void testParse_ParsesSRTFile() throws IOException {
        if (transcriber == null) {
            transcriber = new Transcriber();
        }

        String srtContent = """
                1
                00:00:00,000 --> 00:00:05,000
                Welcome to the meeting.
                
                2
                00:00:05,000 --> 00:00:10,000
                Let's discuss the project status.
                
                3
                00:00:10,000 --> 00:00:15,000
                We have made good progress.
                """;

        File srtFile = tempDir.resolve("transcript.srt").toFile();
        Files.writeString(srtFile.toPath(), srtContent);

        Transcript result = transcriber.parse(srtFile);

        assertNotNull(result);
        assertFalse(result.getRawText().isEmpty());
        assertTrue(result.getRawText().contains("Welcome to the meeting"));
        assertTrue(result.getRawText().contains("discuss the project status"));
        assertTrue(result.getRawText().contains("made good progress"));
        assertEquals(TranscriptSource.IMPORTED, result.getSource());
        assertEquals(3, result.getTimestamps().size());
        assertNotNull(result.getId());
    }

    @Test
    void testParse_HandlesEmptyTextFile() throws IOException {
        if (transcriber == null) {
            transcriber = new Transcriber();
        }

        File emptyFile = tempDir.resolve("empty.txt").toFile();
        Files.writeString(emptyFile.toPath(), "");

        Transcript result = transcriber.parse(emptyFile);

        assertNotNull(result);
        assertEquals("", result.getRawText());
        assertEquals(TranscriptSource.IMPORTED, result.getSource());
    }

    @Test
    void testParse_HandlesMultilineTextFile() throws IOException {
        if (transcriber == null) {
            transcriber = new Transcriber();
        }

        String content = """
                This is line one.
                This is line two.
                This is line three.
                """;

        File txtFile = tempDir.resolve("multiline.txt").toFile();
        Files.writeString(txtFile.toPath(), content);

        Transcript result = transcriber.parse(txtFile);

        assertNotNull(result);
        assertTrue(result.getRawText().contains("line one"));
        assertTrue(result.getRawText().contains("line two"));
        assertTrue(result.getRawText().contains("line three"));
    }

    @Test
    void testParse_VTTFileWithMultipleLines() throws IOException {
        if (transcriber == null) {
            transcriber = new Transcriber();
        }

        String vttContent = """
                WEBVTT
                
                00:00:00.000 --> 00:00:03.000
                First line of subtitle
                Second line of same subtitle
                
                00:00:03.000 --> 00:00:06.000
                Another subtitle here
                """;

        File vttFile = tempDir.resolve("multiline.vtt").toFile();
        Files.writeString(vttFile.toPath(), vttContent);

        Transcript result = transcriber.parse(vttFile);

        assertNotNull(result);
        assertTrue(result.getRawText().contains("First line"));
        assertTrue(result.getRawText().contains("Second line"));
        assertTrue(result.getRawText().contains("Another subtitle"));
    }

    @Test
    void testParse_ThrowsExceptionWhenFileIsDirectory() {
        if (transcriber == null) {
            transcriber = new Transcriber();
        }

        File directory = tempDir.toFile();

        assertThrows(IllegalArgumentException.class, () -> {
            transcriber.parse(directory);
        });
    }

    // ========== Tests for transcribe() method ==========

    @Test
    void testTranscribe_ThrowsExceptionWhenFileIsNull() {
        if (transcriber == null) {
            transcriber = new Transcriber();
        }

        assertThrows(IllegalArgumentException.class, () -> {
            transcriber.transcribe(null);
        });
    }

    @Test
    void testTranscribe_ThrowsExceptionWhenFileDoesNotExist() {
        if (transcriber == null) {
            transcriber = new Transcriber();
        }

        File nonExistentFile = new File("/nonexistent/audio.wav");

        assertThrows(IllegalArgumentException.class, () -> {
            transcriber.transcribe(nonExistentFile);
        });
    }

    @Test
    void testTranscribe_ThrowsExceptionWhenFileIsDirectory() {
        if (transcriber == null) {
            transcriber = new Transcriber();
        }

        File directory = tempDir.toFile();

        assertThrows(IllegalArgumentException.class, () -> {
            transcriber.transcribe(directory);
        });
    }

    // Note: Actual transcription with real API calls should be done in integration tests
    // We cannot easily mock the OpenAI client in unit tests without additional mocking frameworks
    // The above tests verify the input validation logic
}
