package pt.sharecar.exceptions;

public class SchemaCreationException extends RuntimeException {
    public SchemaCreationException(String message) {
        super(message);
    }

    public SchemaCreationException(String message, Throwable cause) {
        super(message, cause);
    }

    public SchemaCreationException(Throwable cause) {
        super(cause);
    }
}
