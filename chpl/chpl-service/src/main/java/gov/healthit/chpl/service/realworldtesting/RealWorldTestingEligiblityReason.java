package gov.healthit.chpl.service.realworldtesting;

public enum RealWorldTestingEligiblityReason {
    NOT_ELIGIBLE("Not Eligible"),
    SELF("Self"),
    ICS("ICS"),
    SELF_AND_ICS("Self & ICS");

    private String reason;

    RealWorldTestingEligiblityReason(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

}
