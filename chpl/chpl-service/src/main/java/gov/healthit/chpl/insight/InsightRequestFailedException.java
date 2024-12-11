package gov.healthit.chpl.insight;

import java.io.IOException;

import org.springframework.http.HttpStatusCode;

import lombok.Data;

@Data
public class InsightRequestFailedException extends IOException {
    private static final long serialVersionUID = 3861201909156321545L;
    private HttpStatusCode statusCode;

    public InsightRequestFailedException() {
        super();
    }

    public InsightRequestFailedException(String message) {
        super(message);
    }

    public InsightRequestFailedException(String message, HttpStatusCode statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public InsightRequestFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public InsightRequestFailedException(String message, Throwable cause, HttpStatusCode statusCode) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public InsightRequestFailedException(Throwable cause) {
        super(cause);
    }

    public InsightRequestFailedException(Throwable cause, HttpStatusCode statusCode) {
        super(cause);
        this.statusCode = statusCode;
    }
}
