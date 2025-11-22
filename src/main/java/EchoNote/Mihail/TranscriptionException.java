package EchoNote.Mihail;

/**
 * Exception thrown when audio transcription fails.
 * This is a runtime exception to avoid cluttering method signatures.
 */
public class TranscriptionException extends RuntimeException {
    
    public TranscriptionException(String message) {
        super(message);
    }
    
    public TranscriptionException(String message, Throwable cause) {
        super(message, cause);
    }
}
