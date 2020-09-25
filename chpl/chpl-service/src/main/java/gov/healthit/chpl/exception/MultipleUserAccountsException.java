package gov.healthit.chpl.exception;

public class MultipleUserAccountsException extends Exception {
    private static final long serialVersionUID = 6252445316121125739L;

    public MultipleUserAccountsException() {
        super();
    }

    public MultipleUserAccountsException(String message) {
        super(message);
    }

    public MultipleUserAccountsException(String message, Throwable cause) {
        super(message, cause);
    }

    public MultipleUserAccountsException(Throwable cause) {
        super(cause);
    }

}
