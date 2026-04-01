package gov.healthit.chpl.astpai;

import java.io.IOException;

import org.springframework.http.HttpStatusCode;

import lombok.Data;

@Data
public class AstpAiRequestFailedException extends IOException {
    private static final long serialVersionUID = 3861221517156321545L;
    private HttpStatusCode statusCode;

    public AstpAiRequestFailedException() {
        super();
    }

    public AstpAiRequestFailedException(String message) {
        super(message);
    }

    public AstpAiRequestFailedException(String message, HttpStatusCode statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public AstpAiRequestFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public AstpAiRequestFailedException(String message, Throwable cause, HttpStatusCode statusCode) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public AstpAiRequestFailedException(Throwable cause) {
        super(cause);
    }

    public AstpAiRequestFailedException(Throwable cause, HttpStatusCode statusCode) {
        super(cause);
        this.statusCode = statusCode;
    }
}
