package hexlet.code.app.exception;

public class ResourceHasRelatedEntitiesException extends RuntimeException {
    public ResourceHasRelatedEntitiesException(String message) {
        super(message);
    }
}
