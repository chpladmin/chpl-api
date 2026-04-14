package gov.healthit.chpl.aia;

import java.io.IOException;

import org.springframework.http.HttpStatusCode;

import lombok.Data;

@Data
public class AIARequestFailedException extends IOException {
    private static final long serialVersionUID = 3861221517156321545L;
    private HttpStatusCode statusCode;

    public AIARequestFailedException() {
        super();
    }

    public AIARequestFailedException(String message) {
        super(message);
    }

    public AIARequestFailedException(String message, HttpStatusCode statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public AIARequestFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public AIARequestFailedException(String message, Throwable cause, HttpStatusCode statusCode) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public AIARequestFailedException(Throwable cause) {
        super(cause);
    }

    public AIARequestFailedException(Throwable cause, HttpStatusCode statusCode) {
        super(cause);
        this.statusCode = statusCode;
    }
}
