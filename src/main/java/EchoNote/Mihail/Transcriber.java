package EchoNote.Mihail;

import EchoNote.Jack.Transcript;
import EchoNote.Jack.TranscriptSource;
import EchoNote.Config.OpenAiClientFactory;
import com.openai.client.OpenAIClient;
import com.openai.models.CreateTranscriptionResponse;
import com.openai.models.AudioTranscriptionCreateParams;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Transcribes audio files to text using OpenAI's Whisper API.
 * Also supports parsing existing transcript files (.vtt, .srt, .txt).
 */
public class Transcriber {

    private final OpenAIClient client;

    public Transcriber() {
        this.client = OpenAiClientFactory.getClient();
    }

    /**
     * Transcribes an audio file using OpenAI's Whisper API.
     *
     * @param audioFile the audio file to transcribe (WAV, MP3, etc.)
     * @return a Transcript containing the transcribed text
     * @throws IllegalArgumentException if audioFile is null or doesn't exist
     * @throws TranscriptionException if the API call fails
     */
    public Transcript transcribe(File audioFile) {
        if (audioFile == null) {
            throw new IllegalArgumentException("Audio file cannot be null");
        }

        if (!audioFile.exists()) {
            throw new IllegalArgumentException("Audio file does not exist: " + audioFile.getPath());
        }

        if (!audioFile.isFile()) {
            throw new IllegalArgumentException("Path is not a file: " + audioFile.getPath());
        }

        try {
            // Create transcription request with verbose_json to get timestamps
            AudioTranscriptionCreateParams params = AudioTranscriptionCreateParams.builder()
                    .file(audioFile.toPath())
                    .model("whisper-1")
                    .responseFormat(AudioTranscriptionCreateParams.ResponseFormat.VERBOSE_JSON)
                    .build();

            CreateTranscriptionResponse response = client.audio().transcriptions().create(params);

            // Extract the transcription text
            String rawText = response.text();

            // Extract timestamps if available (segments would be in the verbose response)
            List<String> timestamps = new ArrayList<>();
            // Note: The OpenAI Java SDK might not expose segments directly
            // For now, we'll leave timestamps empty, but the structure supports them

            return new Transcript(
                    UUID.randomUUID().toString(),
                    rawText,
                    timestamps,
                    TranscriptSource.LIVE
            );

        } catch (Exception e) {
            throw new TranscriptionException("Failed to transcribe audio file: " + audioFile.getName(), e);
        }
    }

    /**
     * Parses a transcript file (.vtt, .srt, .txt) into a Transcript object.
     *
     * @param transcriptFile the transcript file to parse
     * @return a Transcript containing the parsed text and timestamps
     * @throws IllegalArgumentException if transcriptFile is null, doesn't exist, or has an unsupported format
     * @throws TranscriptionException if parsing fails
     */
    public Transcript parse(File transcriptFile) {
        if (transcriptFile == null) {
            throw new IllegalArgumentException("Transcript file cannot be null");
        }

        if (!transcriptFile.exists()) {
            throw new IllegalArgumentException("Transcript file does not exist: " + transcriptFile.getPath());
        }

        if (!transcriptFile.isFile()) {
            throw new IllegalArgumentException("Path is not a file: " + transcriptFile.getPath());
        }

        String fileName = transcriptFile.getName().toLowerCase();

        try {
            if (fileName.endsWith(".vtt")) {
                return parseVTT(transcriptFile);
            } else if (fileName.endsWith(".srt")) {
                return parseSRT(transcriptFile);
            } else if (fileName.endsWith(".txt")) {
                return parseTXT(transcriptFile);
            } else {
                throw new IllegalArgumentException(
                        "Unsupported file format. Supported formats: .vtt, .srt, .txt"
                );
            }
        } catch (IOException e) {
            throw new TranscriptionException("Failed to parse transcript file: " + transcriptFile.getName(), e);
        }
    }

    /**
     * Parses a WebVTT (.vtt) file.
     */
    private Transcript parseVTT(File file) throws IOException {
        List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
        StringBuilder rawText = new StringBuilder();
        List<String> timestamps = new ArrayList<>();

        // VTT format: timestamp --> timestamp, then text lines
        Pattern timestampPattern = Pattern.compile("(\\d{2}:\\d{2}:\\d{2}\\.\\d{3})\\s*-->\\s*(\\d{2}:\\d{2}:\\d{2}\\.\\d{3})");

        boolean inCue = false;
        for (String line : lines) {
            line = line.trim();

            // Skip the WEBVTT header
            if (line.startsWith("WEBVTT")) {
                continue;
            }

            Matcher matcher = timestampPattern.matcher(line);
            if (matcher.find()) {
                timestamps.add(matcher.group(1) + " --> " + matcher.group(2));
                inCue = true;
                continue;
            }

            // If we're in a cue and the line is not empty, it's text
            if (inCue && !line.isEmpty()) {
                rawText.append(line).append(" ");
            }

            // Empty line marks end of cue
            if (line.isEmpty()) {
                inCue = false;
            }
        }

        return new Transcript(
                UUID.randomUUID().toString(),
                rawText.toString().trim(),
                timestamps,
                TranscriptSource.IMPORTED
        );
    }

    /**
     * Parses a SubRip (.srt) file.
     */
    private Transcript parseSRT(File file) throws IOException {
        List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
        StringBuilder rawText = new StringBuilder();
        List<String> timestamps = new ArrayList<>();

        // SRT format: sequence number, timestamp, text lines, blank line
        Pattern timestampPattern = Pattern.compile("(\\d{2}:\\d{2}:\\d{2},\\d{3})\\s*-->\\s*(\\d{2}:\\d{2}:\\d{2},\\d{3})");

        boolean inSubtitle = false;
        for (String line : lines) {
            line = line.trim();

            Matcher matcher = timestampPattern.matcher(line);
            if (matcher.find()) {
                timestamps.add(matcher.group(1) + " --> " + matcher.group(2));
                inSubtitle = true;
                continue;
            }

            // Skip sequence numbers (lines that are just digits)
            if (line.matches("^\\d+$")) {
                continue;
            }

            // If we're in a subtitle and the line is not empty, it's text
            if (inSubtitle && !line.isEmpty()) {
                rawText.append(line).append(" ");
            }

            // Empty line marks end of subtitle
            if (line.isEmpty()) {
                inSubtitle = false;
            }
        }

        return new Transcript(
                UUID.randomUUID().toString(),
                rawText.toString().trim(),
                timestamps,
                TranscriptSource.IMPORTED
        );
    }

    /**
     * Parses a plain text (.txt) file.
     */
    private Transcript parseTXT(File file) throws IOException {
        String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);

        return new Transcript(
                UUID.randomUUID().toString(),
                content.trim(),
                new ArrayList<>(), // No timestamps in plain text
                TranscriptSource.IMPORTED
        );
    }
}
