package gov.healthit.chpl.domain.concept;

import java.io.Serializable;

/**
 * Domain objects for types of schedules.
 * @author alarned
 *
 */
public enum ScheduleTypeConcept implements Serializable {
    /** Notification for when the cache is too old. */
    CACHE_STATUS_AGE_NOTIFICATION("Cache Status Age Notification");

    private String name;

    ScheduleTypeConcept(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
