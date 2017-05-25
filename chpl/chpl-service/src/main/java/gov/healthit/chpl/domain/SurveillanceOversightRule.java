package gov.healthit.chpl.domain;

public enum SurveillanceOversightRule {
	LONG_SUSPENSION("Lengthy Suspension Rule"),
	CAP_NOT_APPROVED("CAP Not Approved Rule"),
	CAP_NOT_STARTED("CAP Not Started Rule"),
	CAP_NOT_COMPLETED("CAP Not Completed Rule"),
    CAP_NOT_CLOSED("CAP Not Closed Rule"),
    NONCONFORMITY_OPEN_CAP_COMPLETE("Closed CAP with Open Nonconformity Rule");

	private String title;

	private SurveillanceOversightRule(String title) {
		this.title = title;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
}
