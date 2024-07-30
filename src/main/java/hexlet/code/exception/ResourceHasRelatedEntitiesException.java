package hexlet.code.exception;

public class ResourceHasRelatedEntitiesException extends RuntimeException {
    public ResourceHasRelatedEntitiesException(String message) {
        super(message);
    }
}
