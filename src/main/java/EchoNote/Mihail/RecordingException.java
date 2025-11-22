package EchoNote.Mihail;

/**
 * Exception thrown when audio recording fails.
 * This is a runtime exception to avoid cluttering method signatures.
 */
public class RecordingException extends RuntimeException {
    
    public RecordingException(String message) {
        super(message);
    }
    
    public RecordingException(String message, Throwable cause) {
        super(message, cause);
    }
}
