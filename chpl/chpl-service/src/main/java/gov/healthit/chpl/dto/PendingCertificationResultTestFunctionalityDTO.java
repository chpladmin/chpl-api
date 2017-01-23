package gov.healthit.chpl.dto;

import java.io.Serializable;

import gov.healthit.chpl.entity.PendingCertificationResultTestFunctionalityEntity;

public class PendingCertificationResultTestFunctionalityDTO implements Serializable {
	private static final long serialVersionUID = 4002949679310389672L;
	private Long id;
	private Long pendingCertificationResultId;
	private Long testFunctionalityId;
	private String number;
	
	public PendingCertificationResultTestFunctionalityDTO() {}
	
	public PendingCertificationResultTestFunctionalityDTO(PendingCertificationResultTestFunctionalityEntity entity) {
		this.setId(entity.getId());
		this.setPendingCertificationResultId(entity.getPendingCertificationResultId());
		this.setTestFunctionalityId(entity.getTestFunctionalityId());
		this.setNumber(entity.getTestFunctionalityNumber());
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

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}
}
