package gov.healthit.chpl.domain.concept;

import java.io.Serializable;

public enum NotificationTypeConcept implements Serializable {
	ONC_ACB_DAILY_SURVEILLANCE_BROKEN_RULES("ONC-ACB Daily Surveillance Broken Rules"),
	ONC_ACB_WEEKLY_SURVEILLANCE_BROKEN_RULES("ONC-ACB Weekly Surveillance Broken Rules"),
	ONC_ACB_WEEKLY_ICS_FAMILY_ERRORS("ONC-ACB Weekly ICS Family Errors"),
	ONC_DAILY_SURVEILLANCE_BROKEN_RULES("ONC Daily Surveillance Broken Rules"),
	ONC_WEEKLY_SURVEILLANCE_BROKEN_RULES("ONC Weekly Surveillance Broken Rules"),
	ONC_WEEKLY_ICS_FAMILY_ERRORS("ONC Weekly ICS Family Errors"),
	SUMMARY_STATISTICS("Summary Statistics"),
	QUESTIONABLE_ACTIVITY("Questionable Activity");

	private String name;

	private NotificationTypeConcept(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
