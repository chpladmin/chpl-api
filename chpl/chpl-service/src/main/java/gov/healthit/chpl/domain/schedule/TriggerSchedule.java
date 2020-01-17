package gov.healthit.chpl.domain.schedule;

public enum TriggerSchedule {
    REPEATABLE("Repeatable"), ONE_TIME("One Time");

    private final String prettyName;
    TriggerSchedule(String prettyName) {
        this.prettyName = prettyName;
    }

    public String getPrettyName() {
        return prettyName;
    }
}
