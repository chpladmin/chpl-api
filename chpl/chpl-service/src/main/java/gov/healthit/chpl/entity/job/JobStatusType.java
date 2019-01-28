package gov.healthit.chpl.entity.job;

import org.springframework.util.StringUtils;

public enum JobStatusType {
    In_Progress("In Progress"), Complete, Error;

    private String name;

    JobStatusType() {

    }

    JobStatusType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        if (!StringUtils.isEmpty(this.name)) {
            return this.name;
        }
        return name();
    }

    public static JobStatusType getValue(String value) {
        if (value == null) {
            return null;
        }

        JobStatusType result = null;
        JobStatusType[] values = JobStatusType.values();
        for (int i = 0; i < values.length && result == null; i++) {
            if (value.equalsIgnoreCase(values[i].toString())) {
                result = values[i];
            }
        }
        return result;
    }
}
