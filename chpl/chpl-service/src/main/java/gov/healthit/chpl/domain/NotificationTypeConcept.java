package gov.healthit.chpl.domain;

import java.io.Serializable;

public enum NotificationTypeConcept implements Serializable {
	ONC_ACB_DAILY_SURVEILLANCE_BROKEN_RULES("ONC-ACB Daily Surveillance Broken Rules"),
	ONC_ACB_WEEKLY_SURVEILLANCE_BROKEN_RULES("ONC-ACB Weekly Surveillance Broken Rules"),
	ONC_DAILY_SURVEILLANCE_BROKEN_RULES("ONC Daily Surveillance Broken Rules"),
	ONC_WEEKLY_SURVEILLANCE_BROKEN_RULES("ONC Weekly Surveillance Broken Rules");
	
	private String name;
	
	private NotificationTypeConcept(String name){
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}
