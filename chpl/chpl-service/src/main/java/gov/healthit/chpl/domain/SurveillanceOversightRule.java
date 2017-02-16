package gov.healthit.chpl.domain;

public enum SurveillanceOversightRule {
	LONG_SUSPENSION("Lengthy Suspension Rule", 13),
	CAP_NOT_APPROVED("CAP Not Approved Rule", 14),
	CAP_NOT_STARTED("CAP Not Started Rule", 15),
	CAP_NOT_COMPLETED("CAP Not Completed Rule", 16);
	
	private String title;
	private int columnOffset;
	
	private SurveillanceOversightRule(String title) {
		this.title = title;
	}
	
	private SurveillanceOversightRule(String title, int offset) {
		this.title = title;
		this.columnOffset = offset;
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}

	public int getColumnOffset() {
		return columnOffset;
	}

	public void setColumnOffset(int columnOffset) {
		this.columnOffset = columnOffset;
	}
}
