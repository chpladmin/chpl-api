package gov.healthit.chpl.domain.concept;

import java.io.Serializable;

/**
 * Domain objects for types of notifications.
 * @author alarned
 *
 */
public enum NotificationTypeConcept implements Serializable {
    /** ACB specific daily surveillance broken rules. */
    ONC_ACB_DAILY_SURVEILLANCE_BROKEN_RULES("ONC-ACB Daily Surveillance Broken Rules"),
    /** ACB specific weekly surveillance broken rules. */
    ONC_ACB_WEEKLY_SURVEILLANCE_BROKEN_RULES("ONC-ACB Weekly Surveillance Broken Rules"),
    /** ACB specific ICS family errors. */
    ONC_ACB_WEEKLY_ICS_FAMILY_ERRORS("ONC-ACB Weekly ICS Family Errors"),
    /** ONC-wide daily surveillance broken rules. */
    ONC_DAILY_SURVEILLANCE_BROKEN_RULES("ONC Daily Surveillance Broken Rules"),
    /** ONC-wide weekly surveillance broken rules. */
    ONC_WEEKLY_SURVEILLANCE_BROKEN_RULES("ONC Weekly Surveillance Broken Rules"),
    /** ONC0-wide ICS family errors. */
    ONC_WEEKLY_ICS_FAMILY_ERRORS("ONC Weekly ICS Family Errors"),
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
