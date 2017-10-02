package gov.healthit.chpl.dto;

import java.io.Serializable;

import gov.healthit.chpl.entity.listing.CertificationResultTestTaskEntity;

public class CertificationResultTestTaskDTO implements Serializable {
	private static final long serialVersionUID = -2963883181763817735L;
	private Long id;
	private Long certificationResultId;
	private Long testTaskId;
	private TestTaskDTO testTask;

	public CertificationResultTestTaskDTO(){
		this.testTask = new TestTaskDTO();
	}

	public CertificationResultTestTaskDTO(CertificationResultTestTaskEntity entity){
		this();
		this.id = entity.getId();
		this.certificationResultId = entity.getCertificationResultId();
		this.testTaskId = entity.getTestTaskId();
		if(entity.getTestTask() != null) {
			this.testTask = new TestTaskDTO(entity.getTestTask());
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

	public Long getTestTaskId() {
		return testTaskId;
	}

	public void setTestTaskId(Long testTaskId) {
		this.testTaskId = testTaskId;
	}

	public TestTaskDTO getTestTask() {
		return testTask;
	}

	public void setTestTask(TestTaskDTO testTask) {
		this.testTask = testTask;
	}
}
