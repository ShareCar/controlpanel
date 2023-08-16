package pt.sharecar.tenant;

public class LiquibaseRunException extends RuntimeException {
    public LiquibaseRunException(String message) {
        super(message);
    }

    public LiquibaseRunException(String message, Throwable cause) {
        super(message, cause);
    }

    public LiquibaseRunException(Throwable cause) {
        super(cause);
    }
}
