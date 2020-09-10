package gov.healthit.chpl.exception;

import java.io.IOException;

public class JiraRequestFailedException extends IOException {
    private static final long serialVersionUID = 3861201937156321545L;

    public JiraRequestFailedException() {
        super();
    }

    public JiraRequestFailedException(String message) {
        super(message);
    }

    public JiraRequestFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public JiraRequestFailedException(Throwable cause) {
        super(cause);
    }

}
