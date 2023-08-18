package pt.sharecar.exceptions;

public class TenantCreationException extends RuntimeException {

    public TenantCreationException(String message) {
        super(message);
    }

    public TenantCreationException(String message, Throwable cause) {
        super(message, cause);
    }

    public TenantCreationException(Throwable cause) {
        super(cause);
    }
}
