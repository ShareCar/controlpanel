package pt.sharecar.exceptions;

public class KeycloakUserCreationException extends RuntimeException {

    public KeycloakUserCreationException(String message) {
        super(message);
    }

    public KeycloakUserCreationException(String message, Throwable cause) {
        super(message, cause);
    }

    public KeycloakUserCreationException(Throwable cause) {
        super(cause);
    }
}
