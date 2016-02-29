package gov.healthit.chpl.dto;

import gov.healthit.chpl.entity.PendingCertificationResultTestStandardEntity;

public class PendingCertificationResultTestStandardDTO {
	private Long id;
	private Long pendingCertificationResultId;
	private Long testStandardId;
	private String number;
	
	public PendingCertificationResultTestStandardDTO() {}
	
	public PendingCertificationResultTestStandardDTO(PendingCertificationResultTestStandardEntity entity) {
		this.setId(entity.getId());
		this.setPendingCertificationResultId(entity.getPendingCertificationResultId());
		this.setTestStandardId(entity.getTestStandardId());
		this.setNumber(entity.getTestStandardNumber());
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public Long getPendingCertificationResultId() {
		return pendingCertificationResultId;
	}

	public void setPendingCertificationResultId(Long pendingCertificationResultId) {
		this.pendingCertificationResultId = pendingCertificationResultId;
	}

	public Long getTestStandardId() {
		return testStandardId;
	}

	public void setTestStandardId(Long testStandardId) {
		this.testStandardId = testStandardId;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String name) {
		this.number = name;
	}
}
