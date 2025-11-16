package EchoNote.Mihail;

import EchoNote.Jack.ActionItem;
import EchoNote.Jack.Summary;
import EchoNote.Jack.Transcript;
import EchoNote.Config.OpenAiClientFactory;
import com.openai.client.OpenAIClient;

import java.util.List;

public class Summarizer {

    private final OpenAIClient client;

    public Summarizer() {
        this.client = OpenAiClientFactory.getClient();
    }

    public Summary summarize(Transcript transcript) {
        // TODO: use `client` to call the OpenAI chat/response API
        //       and map the result into a Summary object.
        return null;
    }

    public List<ActionItem> extractActions(Transcript transcript) {
        // TODO: use `client` to call the OpenAI chat/response API
        //       and map the result into a List<ActionItem>.
        return null;
    }
}