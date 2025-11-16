package EchoNote.Mihail;

import EchoNote.Jack.Transcript;
import EchoNote.Config.OpenAiClientFactory;
import com.openai.client.OpenAIClient;

import java.io.File;

public class Transcriber {

    private final OpenAIClient client;

    public Transcriber() {
        this.client = OpenAiClientFactory.getClient();
    }

    public Transcript transcribe(File audioFile) {
        // TODO: use `client` to call the OpenAI audio transcription (Whisper) API
        //       with the given audioFile and return a Transcript.
        return null;
    }

    public Transcript parse(File transcriptFile) {
        // TODO: optionally handle .vtt / .srt / .txt transcript files here,
        //       converting them into a Transcript without using the API.
        return null;
    }
}
