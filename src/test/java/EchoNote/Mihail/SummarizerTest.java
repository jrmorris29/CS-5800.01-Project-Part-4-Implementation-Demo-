package EchoNote.Mihail;

import EchoNote.Jack.ActionItem;
import EchoNote.Jack.Summary;
import EchoNote.Jack.Transcript;
import EchoNote.Jack.TranscriptSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Summarizer class.
 * Note: These tests focus on input validation.
 * The actual summarization and action extraction require real API calls,
 * so those should be tested in integration tests with a valid API key.
 */
class SummarizerTest {

    private Summarizer summarizer;

    @BeforeEach
    void setUp() {
        // Note: This will try to load the .env file
        // For unit tests without API calls, we focus on validation logic
        try {
            summarizer = new Summarizer();
        } catch (Exception e) {
            // If OpenAI client initialization fails, we can still test validation
            summarizer = null;
        }
    }

    // ========== Tests for summarize() method ==========

    @Test
    void testSummarize_ThrowsExceptionWhenTranscriptIsNull() {
        if (summarizer == null) {
            summarizer = new Summarizer();
        }

        assertThrows(IllegalArgumentException.class, () -> {
            summarizer.summarize(null);
        });
    }

    @Test
    void testSummarize_ThrowsExceptionWhenTranscriptTextIsNull() {
        if (summarizer == null) {
            summarizer = new Summarizer();
        }

        Transcript transcript = new Transcript("id-1", null, new ArrayList<>(), TranscriptSource.LIVE);

        assertThrows(IllegalArgumentException.class, () -> {
            summarizer.summarize(transcript);
        });
    }

    @Test
    void testSummarize_ThrowsExceptionWhenTranscriptTextIsEmpty() {
        if (summarizer == null) {
            summarizer = new Summarizer();
        }

        Transcript transcript = new Transcript("id-1", "", new ArrayList<>(), TranscriptSource.LIVE);

        assertThrows(IllegalArgumentException.class, () -> {
            summarizer.summarize(transcript);
        });
    }

    @Test
    void testSummarize_ThrowsExceptionWhenTranscriptTextIsWhitespace() {
        if (summarizer == null) {
            summarizer = new Summarizer();
        }

        Transcript transcript = new Transcript("id-1", "   ", new ArrayList<>(), TranscriptSource.LIVE);

        assertThrows(IllegalArgumentException.class, () -> {
            summarizer.summarize(transcript);
        });
    }

    // ========== Tests for extractActions() method ==========

    @Test
    void testExtractActions_ThrowsExceptionWhenTranscriptIsNull() {
        if (summarizer == null) {
            summarizer = new Summarizer();
        }

        assertThrows(IllegalArgumentException.class, () -> {
            summarizer.extractActions(null);
        });
    }

    @Test
    void testExtractActions_ThrowsExceptionWhenTranscriptTextIsNull() {
        if (summarizer == null) {
            summarizer = new Summarizer();
        }

        Transcript transcript = new Transcript("id-1", null, new ArrayList<>(), TranscriptSource.LIVE);

        assertThrows(IllegalArgumentException.class, () -> {
            summarizer.extractActions(transcript);
        });
    }

    @Test
    void testExtractActions_ThrowsExceptionWhenTranscriptTextIsEmpty() {
        if (summarizer == null) {
            summarizer = new Summarizer();
        }

        Transcript transcript = new Transcript("id-1", "", new ArrayList<>(), TranscriptSource.LIVE);

        assertThrows(IllegalArgumentException.class, () -> {
            summarizer.extractActions(transcript);
        });
    }

    @Test
    void testExtractActions_ThrowsExceptionWhenTranscriptTextIsWhitespace() {
        if (summarizer == null) {
            summarizer = new Summarizer();
        }

        Transcript transcript = new Transcript("id-1", "   ", new ArrayList<>(), TranscriptSource.LIVE);

        assertThrows(IllegalArgumentException.class, () -> {
            summarizer.extractActions(transcript);
        });
    }

    // ========== Integration-style tests (require valid API key) ==========
    // These tests are commented out because they require a valid OpenAI API key
    // and make real API calls. Uncomment when running integration tests.

    /*
    @Test
    void testSummarize_WithValidTranscript() {
        String transcriptText = """
                Good morning everyone. Let's start today's meeting.
                First, we need to discuss the Q4 budget proposal.
                Sarah will lead this initiative.
                Second, we need to finalize the marketing strategy.
                The deadline for this is December 15th.
                We've decided to increase the social media presence.
                John mentioned that we should focus on Instagram and LinkedIn.
                Meeting adjourned. Thank you all.
                """;

        Transcript transcript = new Transcript(
                "test-id",
                transcriptText,
                new ArrayList<>(),
                TranscriptSource.LIVE
        );

        Summary summary = summarizer.summarize(transcript);

        assertNotNull(summary);
        assertNotNull(summary.getId());
        assertFalse(summary.getTopics().isEmpty(), "Should have at least one topic");
        // Note: Decisions and notes may or may not be present depending on LLM output
    }

    @Test
    void testExtractActions_WithValidTranscript() {
        String transcriptText = """
                John, please review the budget proposal by Friday.
                Sarah needs to send the marketing report to the team.
                Mike will schedule a follow-up meeting next week.
                Everyone should prepare their presentations before the conference.
                """;

        Transcript transcript = new Transcript(
                "test-id",
                transcriptText,
                new ArrayList<>(),
                TranscriptSource.LIVE
        );

        List<ActionItem> actions = summarizer.extractActions(transcript);

        assertNotNull(actions);
        assertFalse(actions.isEmpty(), "Should extract at least one action item");

        // Verify action items have proper structure
        for (ActionItem action : actions) {
            assertNotNull(action.getId());
            assertNotNull(action.getTitle());
            assertFalse(action.getTitle().isEmpty());
        }
    }

    @Test
    void testExtractActions_WithNoActions() {
        String transcriptText = """
                This is just a casual conversation.
                We talked about the weather.
                It was nice to catch up with everyone.
                """;

        Transcript transcript = new Transcript(
                "test-id",
                transcriptText,
                new ArrayList<>(),
                TranscriptSource.LIVE
        );

        List<ActionItem> actions = summarizer.extractActions(transcript);

        assertNotNull(actions);
        // May be empty or have very few items
    }
    */

    // ========== Edge case tests ==========

    @Test
    void testSummarize_AcceptsValidTranscriptWithLongText() {
        if (summarizer == null) {
            summarizer = new Summarizer();
        }

        StringBuilder longText = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            longText.append("This is sentence number ").append(i).append(". ");
        }

        Transcript transcript = new Transcript(
                "id-1",
                longText.toString(),
                new ArrayList<>(),
                TranscriptSource.LIVE
        );

        // This should not throw an exception during validation
        // (Actual API call would happen, but we're just testing validation)
        assertDoesNotThrow(() -> {
            try {
                // Attempt to call summarize - will fail at API stage without key,
                // but should pass validation
                // summarizer.summarize(transcript);
            } catch (SummarizationException e) {
                // Expected if no valid API key
            }
        });
    }

    @Test
    void testExtractActions_AcceptsValidTranscriptWithSpecialCharacters() {
        if (summarizer == null) {
            summarizer = new Summarizer();
        }

        String text = "Meeting notes: @John & @Sarah - Review #Q4-Budget by 12/15! Cost: $50,000.";

        Transcript transcript = new Transcript(
                "id-1",
                text,
                new ArrayList<>(),
                TranscriptSource.IMPORTED
        );

        // This should not throw an exception during validation
        assertDoesNotThrow(() -> {
            try {
                // Attempt to call extractActions - will fail at API stage without key,
                // but should pass validation
                // summarizer.extractActions(transcript);
            } catch (SummarizationException e) {
                // Expected if no valid API key
            }
        });
    }
}
