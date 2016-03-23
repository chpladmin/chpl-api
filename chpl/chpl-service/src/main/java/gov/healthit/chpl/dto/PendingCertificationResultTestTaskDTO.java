package gov.healthit.chpl.dto;

import java.util.HashSet;
import java.util.Set;

import gov.healthit.chpl.entity.PendingCertificationResultTestTaskEntity;
import gov.healthit.chpl.entity.PendingCertificationResultTestTaskParticipantEntity;

public class PendingCertificationResultTestTaskDTO {
	private Long id;
	private Long pendingCertificationResultId;
	private Long pendingTestTaskId;
	private PendingTestTaskDTO pendingTestTask;
	private Set<PendingCertificationResultTestTaskParticipantDTO> taskParticipants;
	
	public PendingCertificationResultTestTaskDTO() {
		taskParticipants = new HashSet<PendingCertificationResultTestTaskParticipantDTO>();
	}
	
	public PendingCertificationResultTestTaskDTO(PendingCertificationResultTestTaskEntity entity) {
		this();
		this.setId(entity.getId());
		this.pendingCertificationResultId = entity.getPendingCertificationResultId();
		this.pendingTestTaskId = entity.getPendingTestTaskId();
		if(entity.getTestTask() != null) {
			pendingTestTask = new PendingTestTaskDTO(entity.getTestTask());
		}
		if(entity.getTestParticipants() != null) {
			for(PendingCertificationResultTestTaskParticipantEntity partEntity : entity.getTestParticipants()) {
				PendingCertificationResultTestTaskParticipantDTO partDto = new PendingCertificationResultTestTaskParticipantDTO(partEntity);
				this.taskParticipants.add(partDto);
			}
		}
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

	public Long getPendingTestTaskId() {
		return pendingTestTaskId;
	}

	public void setPendingTestTaskId(Long pendingTestTaskId) {
		this.pendingTestTaskId = pendingTestTaskId;
	}

	public PendingTestTaskDTO getPendingTestTask() {
		return pendingTestTask;
	}

	public void setPendingTestTask(PendingTestTaskDTO pendingTestTask) {
		this.pendingTestTask = pendingTestTask;
	}

	public Set<PendingCertificationResultTestTaskParticipantDTO> getTaskParticipants() {
		return taskParticipants;
	}

	public void setTaskParticipants(Set<PendingCertificationResultTestTaskParticipantDTO> taskParticipants) {
		this.taskParticipants = taskParticipants;
	}
}
