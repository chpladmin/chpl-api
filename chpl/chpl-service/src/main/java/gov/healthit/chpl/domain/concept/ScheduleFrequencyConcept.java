package gov.healthit.chpl.domain.concept;

import java.io.Serializable;

/**
 * Enumeration that is used to indicate the minimum frequency a job should be scheduled.
 * @author TYoung
 *
 */
public enum ScheduleFrequencyConcept implements Serializable {
    /**
     * Indicates that a job can be scheduled per minute.
     */
    MINUTES("MINUTES"),
    /**
     * Indicates that a job should be scheduled hourly.
     */
    HOURLY("HOURLY"),
    /**
     * Indicates that a job should be scheduled daily.
     */
    DAILY("DAILY"),
    /**
     * Indicates that a job should be scheduled weekly.
     */
    WEEKLY("WEEKLY"),
    /**
     * Indicates that a job should be scheduled monthly.
     */
    MONTHLY("MONTHLY");

    private String name;

    ScheduleFrequencyConcept(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
