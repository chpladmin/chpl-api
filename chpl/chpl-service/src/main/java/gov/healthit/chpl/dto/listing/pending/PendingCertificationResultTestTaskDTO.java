package gov.healthit.chpl.dto.listing.pending;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultTestTaskEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultTestTaskParticipantEntity;

public class PendingCertificationResultTestTaskDTO implements Serializable {
    private static final long serialVersionUID = 8314437789172874904L;
    private Long id;
    private Long pendingCertificationResultId;
    private Long pendingTestTaskId;
    private PendingTestTaskDTO pendingTestTask;
    private Set<PendingCertificationResultTestTaskParticipantDTO> taskParticipants;

    public PendingCertificationResultTestTaskDTO() {
        taskParticipants = new HashSet<PendingCertificationResultTestTaskParticipantDTO>();
    }

    public PendingCertificationResultTestTaskDTO(final PendingCertificationResultTestTaskEntity entity) {
        this();
        this.setId(entity.getId());
        this.pendingCertificationResultId = entity.getPendingCertificationResultId();
        this.pendingTestTaskId = entity.getPendingTestTaskId();
        if (entity.getTestTask() != null) {
            pendingTestTask = new PendingTestTaskDTO(entity.getTestTask());
        }
        if (entity.getTestParticipants() != null) {
            for (PendingCertificationResultTestTaskParticipantEntity partEntity : entity.getTestParticipants()) {
                PendingCertificationResultTestTaskParticipantDTO partDto = new PendingCertificationResultTestTaskParticipantDTO(
                        partEntity);
                this.taskParticipants.add(partDto);
            }
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getPendingCertificationResultId() {
        return pendingCertificationResultId;
    }

    public void setPendingCertificationResultId(final Long pendingCertificationResultId) {
        this.pendingCertificationResultId = pendingCertificationResultId;
    }

    public Long getPendingTestTaskId() {
        return pendingTestTaskId;
    }

    public void setPendingTestTaskId(final Long pendingTestTaskId) {
        this.pendingTestTaskId = pendingTestTaskId;
    }

    public PendingTestTaskDTO getPendingTestTask() {
        return pendingTestTask;
    }

    public void setPendingTestTask(final PendingTestTaskDTO pendingTestTask) {
        this.pendingTestTask = pendingTestTask;
    }

    public Set<PendingCertificationResultTestTaskParticipantDTO> getTaskParticipants() {
        return taskParticipants;
    }

    public void setTaskParticipants(final Set<PendingCertificationResultTestTaskParticipantDTO> taskParticipants) {
        this.taskParticipants = taskParticipants;
    }

    @Override
    public String toString() {
        return "PendingCertificationResultTestTaskDTO [id=" + id + ", pendingCertificationResultId="
                + pendingCertificationResultId + ", pendingTestTaskId=" + pendingTestTaskId + ", pendingTestTask="
                + pendingTestTask + ", taskParticipants=" + taskParticipants + "]";
    }
}
