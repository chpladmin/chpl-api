package SurveillanceTypeDTO;

import java.util.HashSet;
import java.util.Set;

import gov.healthit.chpl.entity.CertificationResultTestTaskEntity;
import gov.healthit.chpl.entity.CertificationResultTestTaskParticipantEntity;

public class CertificationResultTestTaskDTO {
	private Long id;
	private Long certificationResultId;
	private Long testTaskId;
	private TestTaskDTO testTask;
	private Set<CertificationResultTestTaskParticipantDTO> taskParticipants;
	
	public CertificationResultTestTaskDTO(){
		this.taskParticipants = new HashSet<CertificationResultTestTaskParticipantDTO>();
	}
	
	public CertificationResultTestTaskDTO(CertificationResultTestTaskEntity entity){
		this();
		this.id = entity.getId();
		this.certificationResultId = entity.getCertificationResultId();
		this.testTaskId = entity.getTestTaskId();
		if(entity.getTestTask() != null) {
			this.testTask = new TestTaskDTO(entity.getTestTask());
		}
		if(entity.getTestParticipants() != null && entity.getTestParticipants().size() > 0) {
			for(CertificationResultTestTaskParticipantEntity tpEntity : entity.getTestParticipants()) {
				CertificationResultTestTaskParticipantDTO tpDto = new CertificationResultTestTaskParticipantDTO(tpEntity);
				this.taskParticipants.add(tpDto);
			}
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

	public Set<CertificationResultTestTaskParticipantDTO> getTaskParticipants() {
		return taskParticipants;
	}

	public void setTaskParticipants(Set<CertificationResultTestTaskParticipantDTO> taskParticipants) {
		this.taskParticipants = taskParticipants;
	}
}
