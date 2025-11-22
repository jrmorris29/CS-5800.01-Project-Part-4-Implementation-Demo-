package EchoNote.Mihail;

/**
 * Exception thrown when text summarization or action item extraction fails.
 * This is a runtime exception to avoid cluttering method signatures.
 */
public class SummarizationException extends RuntimeException {
    
    public SummarizationException(String message) {
        super(message);
    }
    
    public SummarizationException(String message, Throwable cause) {
        super(message, cause);
    }
}
