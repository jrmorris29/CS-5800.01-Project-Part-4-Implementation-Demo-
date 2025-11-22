package EchoNote.Mihail;

import EchoNote.Jack.ActionItem;
import EchoNote.Jack.ActionStatus;
import EchoNote.Jack.Summary;
import EchoNote.Jack.Transcript;
import EchoNote.Jack.Participant;
import EchoNote.Config.OpenAiClientFactory;
import com.openai.client.OpenAIClient;
import com.openai.models.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Summarizes meeting transcripts and extracts action items using OpenAI's GPT models.
 */
public class Summarizer {

    private final OpenAIClient client;
    private static final String MODEL = "gpt-4o-mini"; // Cost-effective model for summarization

    public Summarizer() {
        this.client = OpenAiClientFactory.getClient();
    }

    /**
     * Summarizes a transcript into topics, decisions, and general notes.
     *
     * @param transcript the transcript to summarize
     * @return a Summary object containing topics, decisions, and notes
     * @throws IllegalArgumentException if transcript is null or empty
     * @throws SummarizationException if the API call fails
     */
    public Summary summarize(Transcript transcript) {
        if (transcript == null) {
            throw new IllegalArgumentException("Transcript cannot be null");
        }

        String rawText = transcript.getRawText();
        if (rawText == null || rawText.trim().isEmpty()) {
            throw new IllegalArgumentException("Transcript text cannot be empty");
        }

        try {
            String prompt = buildSummarizationPrompt(rawText);

            ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                    .model(MODEL)
                    .addMessage(ChatCompletionMessageParam.ofChatCompletionUserMessageParam(
                            ChatCompletionUserMessageParam.builder()
                                    .content(ChatCompletionUserMessageParam.Content.ofTextContent(prompt))
                                    .build()
                    ))
                    .temperature(0.3) // Lower temperature for more consistent results
                    .maxTokens(1000)
                    .build();

            ChatCompletion response = client.chat().completions().create(params);

            String content = response.choices().get(0).message().content().get();
            return parseSummaryResponse(content);

        } catch (Exception e) {
            throw new SummarizationException("Failed to summarize transcript", e);
        }
    }

    /**
     * Extracts action items from a transcript.
     *
     * @param transcript the transcript to extract action items from
     * @return a list of ActionItem objects
     * @throws IllegalArgumentException if transcript is null or empty
     * @throws SummarizationException if the API call fails
     */
    public List<ActionItem> extractActions(Transcript transcript) {
        if (transcript == null) {
            throw new IllegalArgumentException("Transcript cannot be null");
        }

        String rawText = transcript.getRawText();
        if (rawText == null || rawText.trim().isEmpty()) {
            throw new IllegalArgumentException("Transcript text cannot be empty");
        }

        try {
            String prompt = buildActionExtractionPrompt(rawText);

            ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                    .model(MODEL)
                    .addMessage(ChatCompletionMessageParam.ofChatCompletionUserMessageParam(
                            ChatCompletionUserMessageParam.builder()
                                    .content(ChatCompletionUserMessageParam.Content.ofTextContent(prompt))
                                    .build()
                    ))
                    .temperature(0.3)
                    .maxTokens(1000)
                    .build();

            ChatCompletion response = client.chat().completions().create(params);

            String content = response.choices().get(0).message().content().get();
            return parseActionItemsResponse(content);

        } catch (Exception e) {
            throw new SummarizationException("Failed to extract action items from transcript", e);
        }
    }

    /**
     * Builds the prompt for summarization.
     */
    private String buildSummarizationPrompt(String transcriptText) {
        return """
                You are an AI assistant that summarizes meeting transcripts.
                
                Analyze the following meeting transcript and provide a structured summary.
                
                Format your response EXACTLY as follows:
                
                TOPICS:
                - [Topic 1]
                - [Topic 2]
                - [Topic 3]
                
                DECISIONS:
                - [Decision 1]
                - [Decision 2]
                
                NOTES:
                [General notes and key points from the meeting]
                
                Transcript:
                """ + transcriptText + """
                
                
                Important:
                - List the main topics discussed (bullet points starting with -)
                - List key decisions made (bullet points starting with -)
                - Provide concise general notes summarizing the meeting
                - Use the exact format shown above with TOPICS:, DECISIONS:, and NOTES: headers
                """;
    }

    /**
     * Builds the prompt for action item extraction.
     */
    private String buildActionExtractionPrompt(String transcriptText) {
        return """
                You are an AI assistant that extracts action items from meeting transcripts.
                
                Analyze the following meeting transcript and extract all action items.
                
                Format your response EXACTLY as follows (one action per line):
                
                ACTION: [Action description] | OWNER: [Person's name or "Unassigned"] | DUE: [YYYY-MM-DD or "No date"]
                ACTION: [Action description] | OWNER: [Person's name or "Unassigned"] | DUE: [YYYY-MM-DD or "No date"]
                
                Example:
                ACTION: Review the Q4 budget proposal | OWNER: John Smith | DUE: 2024-12-15
                ACTION: Send follow-up email to clients | OWNER: Sarah Johnson | DUE: No date
                ACTION: Update the project timeline | OWNER: Unassigned | DUE: 2024-12-10
                
                Transcript:
                """ + transcriptText + """
                
                
                Important:
                - Each action item must be on its own line
                - Use the exact format: ACTION: ... | OWNER: ... | DUE: ...
                - If no owner is mentioned, use "Unassigned"
                - If no due date is mentioned, use "No date"
                - For due dates, use YYYY-MM-DD format
                - If the transcript contains no action items, respond with "No action items found"
                """;
    }

    /**
     * Parses the LLM response into a Summary object.
     */
    private Summary parseSummaryResponse(String response) {
        Summary summary = new Summary(UUID.randomUUID().toString());

        String[] sections = response.split("(?i)(TOPICS:|DECISIONS:|NOTES:)");

        String topicsSection = "";
        String decisionsSection = "";
        String notesSection = "";

        // Find sections by looking for the keywords
        Pattern topicsPattern = Pattern.compile("(?i)TOPICS:\\s*([\\s\\S]*?)(?=DECISIONS:|NOTES:|$)");
        Pattern decisionsPattern = Pattern.compile("(?i)DECISIONS:\\s*([\\s\\S]*?)(?=NOTES:|$)");
        Pattern notesPattern = Pattern.compile("(?i)NOTES:\\s*([\\s\\S]*)");

        Matcher topicsMatcher = topicsPattern.matcher(response);
        if (topicsMatcher.find()) {
            topicsSection = topicsMatcher.group(1).trim();
        }

        Matcher decisionsMatcher = decisionsPattern.matcher(response);
        if (decisionsMatcher.find()) {
            decisionsSection = decisionsMatcher.group(1).trim();
        }

        Matcher notesMatcher = notesPattern.matcher(response);
        if (notesMatcher.find()) {
            notesSection = notesMatcher.group(1).trim();
        }

        // Parse topics (lines starting with -)
        for (String line : topicsSection.split("\\n")) {
            line = line.trim();
            if (line.startsWith("-")) {
                summary.addTopic(line.substring(1).trim());
            }
        }

        // Parse decisions (lines starting with -)
        for (String line : decisionsSection.split("\\n")) {
            line = line.trim();
            if (line.startsWith("-")) {
                summary.addDecision(line.substring(1).trim());
            }
        }

        // Set notes
        if (!notesSection.isEmpty()) {
            summary.setNotes(notesSection);
        }

        return summary;
    }

    /**
     * Parses the LLM response into a list of ActionItem objects.
     */
    private List<ActionItem> parseActionItemsResponse(String response) {
        List<ActionItem> actionItems = new ArrayList<>();

        if (response.toLowerCase().contains("no action items found")) {
            return actionItems; // Empty list
        }

        // Pattern to match: ACTION: ... | OWNER: ... | DUE: ...
        Pattern actionPattern = Pattern.compile(
                "ACTION:\\s*([^|]+)\\s*\\|\\s*OWNER:\\s*([^|]+)\\s*\\|\\s*DUE:\\s*(.+)",
                Pattern.CASE_INSENSITIVE
        );

        String[] lines = response.split("\\n");
        for (String line : lines) {
            line = line.trim();
            Matcher matcher = actionPattern.matcher(line);

            if (matcher.find()) {
                String title = matcher.group(1).trim();
                String ownerName = matcher.group(2).trim();
                String dueDateStr = matcher.group(3).trim();

                // Create owner (or null if unassigned)
                Participant owner = null;
                if (!ownerName.equalsIgnoreCase("Unassigned")) {
                    owner = new Participant(ownerName, "", ""); // Email and role are unknown
                }

                // Parse due date
                LocalDate dueDate = null;
                if (!dueDateStr.equalsIgnoreCase("No date")) {
                    try {
                        dueDate = LocalDate.parse(dueDateStr, DateTimeFormatter.ISO_LOCAL_DATE);
                    } catch (DateTimeParseException e) {
                        // If parsing fails, leave dueDate as null
                    }
                }

                ActionItem actionItem = new ActionItem(
                        UUID.randomUUID().toString(),
                        title,
                        owner,
                        dueDate
                );

                actionItems.add(actionItem);
            }
        }

        return actionItems;
    }
}