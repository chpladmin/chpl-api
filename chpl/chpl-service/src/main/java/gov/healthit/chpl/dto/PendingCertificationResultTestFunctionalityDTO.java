package gov.healthit.chpl.dto;

import gov.healthit.chpl.entity.PendingCertificationResultTestFunctionalityEntity;

public class PendingCertificationResultTestFunctionalityDTO {
	private Long id;
	private Long pendingCertificationResultId;
	private Long testFunctionalityId;
	private String name;
	private String category;
	
	public PendingCertificationResultTestFunctionalityDTO() {}
	
	public PendingCertificationResultTestFunctionalityDTO(PendingCertificationResultTestFunctionalityEntity entity) {
		this.setId(entity.getId());
		this.setPendingCertificationResultId(entity.getPendingCertificationResultId());
		this.setTestFunctionalityId(entity.getTestFunctionalityId());
		this.setName(entity.getTestFunctionalityName());
		this.setCategory(entity.getTestFunctionalityCategory());
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

	public Long getTestFunctionalityId() {
		return testFunctionalityId;
	}

	public void setTestFunctionalityId(Long testFunctionalityId) {
		this.testFunctionalityId = testFunctionalityId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}
}
