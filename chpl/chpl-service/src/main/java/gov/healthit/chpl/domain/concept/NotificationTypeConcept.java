package gov.healthit.chpl.domain.concept;

import java.io.Serializable;

/**
 * Domain objects for types of notifications.
 * @author alarned
 *
 */
public enum NotificationTypeConcept implements Serializable {
    /** Questionable Activity. */
    QUESTIONABLE_ACTIVITY("Questionable Activity"),
    /** Summary statistics. */
    SUMMARY_STATISTICS("Summary Statistics");

    private String name;

    NotificationTypeConcept(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
