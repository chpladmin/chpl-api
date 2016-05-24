package gov.healthit.chpl.dto;

import gov.healthit.chpl.entity.PendingCertificationResultTestTaskParticipantEntity;

public class PendingCertificationResultTestTaskParticipantDTO {
	private Long id;
	private Long pendingCertificationResultTestTaskId;
	private PendingTestParticipantDTO testParticipant;
	
	public PendingCertificationResultTestTaskParticipantDTO() {
	}
	
	public PendingCertificationResultTestTaskParticipantDTO(PendingCertificationResultTestTaskParticipantEntity entity) {
		this();
		this.setId(entity.getId());
		this.pendingCertificationResultTestTaskId = entity.getPendingCertificationResultTestTaskId();
		if(entity.getTestParticipant() != null) {
			this.testParticipant = new PendingTestParticipantDTO(entity.getTestParticipant());
		}
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public Long getPendingCertificationResultTestTaskId() {
		return pendingCertificationResultTestTaskId;
	}

	public void setPendingCertificationResultTestTaskId(Long pendingCertificationResultTestTaskId) {
		this.pendingCertificationResultTestTaskId = pendingCertificationResultTestTaskId;
	}

	public PendingTestParticipantDTO getTestParticipant() {
		return testParticipant;
	}

	public void setTestParticipant(PendingTestParticipantDTO testParticipant) {
		this.testParticipant = testParticipant;
	}
}
