package gov.healthit.chpl.domain.error;

public class ErrorResponse {
    private String error;

    public ErrorResponse() {}
    public ErrorResponse(final String error) {
        this.error = error;
    }
    public String getError() {
        return error;
    }
    public void setError(final String error) {
        this.error = error;
    }
}
