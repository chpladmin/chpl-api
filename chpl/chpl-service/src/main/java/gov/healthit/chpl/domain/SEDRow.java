package gov.healthit.chpl.domain;

import gov.healthit.chpl.dto.CertificationResultDTO;
import gov.healthit.chpl.dto.CertificationResultTestTaskDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.TestParticipantDTO;
import gov.healthit.chpl.dto.TestTaskDTO;

public class SEDRow {
	
	private CertifiedProductDetailsDTO details;
	private CertificationResultDTO certificationResult;
	private CertificationResultTestTaskDTO testTask;
	private String criteria;
	
	public CertifiedProductDetailsDTO getDetails() {
		return details;
	}
	public void setDetails(CertifiedProductDetailsDTO details) {
		this.details = details;
	}
	public CertificationResultDTO getCertificationResult() {
		return certificationResult;
	}
	public void setCertificationResult(CertificationResultDTO certificationResult) {
		this.certificationResult = certificationResult;
	}
	public CertificationResultTestTaskDTO getTestTask() {
		return testTask;
	}
	public void setTestTask(CertificationResultTestTaskDTO testTask) {
		this.testTask = testTask;
	}
	public String getCriteria() {
		return criteria;
	}
	public void setCriteria(String criteria) {
		this.criteria = criteria;
	}
}
