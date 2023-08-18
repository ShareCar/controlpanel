package pt.sharecar.exceptions;

public class LiquibaseExecutionException extends RuntimeException {
    public LiquibaseExecutionException(String message) {
        super(message);
    }

    public LiquibaseExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public LiquibaseExecutionException(Throwable cause) {
        super(cause);
    }
}
