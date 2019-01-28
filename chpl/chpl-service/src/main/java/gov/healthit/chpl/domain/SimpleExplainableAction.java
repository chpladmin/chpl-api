package gov.healthit.chpl.domain;

public class SimpleExplainableAction implements ExplainableAction {
    private String reason;

    @Override
    public String getReason() {
        return this.reason;
    }

    @Override
    public void setReason(final String reason) {
       this.reason = reason;
    }

}
