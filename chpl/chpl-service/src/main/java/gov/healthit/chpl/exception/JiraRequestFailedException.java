package gov.healthit.chpl.exception;

import java.io.IOException;

import org.springframework.http.HttpStatus;

import lombok.Data;

@Data
public class JiraRequestFailedException extends IOException {
    private static final long serialVersionUID = 3861201937156321545L;
    private HttpStatus statusCode;

    public JiraRequestFailedException() {
        super();
    }

    public JiraRequestFailedException(String message) {
        super(message);
    }

    public JiraRequestFailedException(String message, HttpStatus statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public JiraRequestFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public JiraRequestFailedException(String message, Throwable cause, HttpStatus statusCode) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public JiraRequestFailedException(Throwable cause) {
        super(cause);
    }

    public JiraRequestFailedException(Throwable cause, HttpStatus statusCode) {
        super(cause);
        this.statusCode = statusCode;
    }
}
