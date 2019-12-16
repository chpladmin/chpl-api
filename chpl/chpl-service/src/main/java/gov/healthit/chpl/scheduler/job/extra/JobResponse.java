package gov.healthit.chpl.scheduler.job.extra;

public class JobResponse {
    private String identifier;
    private String message = "";
    private boolean completedSuccessfully = false;

    public JobResponse(final String identifier, final boolean completedSuccessfully, final String message) {
        this.setIdentifier(identifier);
        this.setMessage(message);
        this.setCompletedSuccessfully(completedSuccessfully);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isCompletedSuccessfully() {
        return completedSuccessfully;
    }

    public void setCompletedSuccessfully(boolean completedSuccessfully) {
        this.completedSuccessfully = completedSuccessfully;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(identifier);
        sb.append(",");
        sb.append(completedSuccessfully);
        sb.append(",");
        sb.append(message);
        return sb.toString();
    }

}
