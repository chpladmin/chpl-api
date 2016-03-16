package gov.healthit.chpl.dto;

import gov.healthit.chpl.entity.CertificationResultTestParticipantEntity;

public class CertificationResultTestParticipantDTO {
	private Long id;
	private Long certificationResultId;
	private Long testParticipantId;
	private TestParticipantDTO testParticipant;
	
	public CertificationResultTestParticipantDTO(){}
	
	public CertificationResultTestParticipantDTO(CertificationResultTestParticipantEntity entity){		
		this.id = entity.getId();
		this.certificationResultId = entity.getCertificationResultId();
		this.testParticipantId = entity.getTestParticipantId();
		if(entity.getTestParticipantEntity() != null) {
			this.testParticipant = new TestParticipantDTO(entity.getTestParticipantEntity());
		}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getCertificationResultId() {
		return certificationResultId;
	}

	public void setCertificationResultId(Long certificationResultId) {
		this.certificationResultId = certificationResultId;
	}

	public Long getTestParticipantId() {
		return testParticipantId;
	}

	public void setTestParticipantId(Long testParticipantId) {
		this.testParticipantId = testParticipantId;
	}

	public TestParticipantDTO getTestParticipant() {
		return testParticipant;
	}

	public void setTestParticipant(TestParticipantDTO testParticipant) {
		this.testParticipant = testParticipant;
	}
	
	
}
