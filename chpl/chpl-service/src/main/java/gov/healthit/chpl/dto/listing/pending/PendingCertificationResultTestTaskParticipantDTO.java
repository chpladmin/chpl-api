package gov.healthit.chpl.dto.listing.pending;

import java.io.Serializable;

import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultTestTaskParticipantEntity;

public class PendingCertificationResultTestTaskParticipantDTO implements Serializable {
    private static final long serialVersionUID = -6337914333458568698L;
    private Long id;
    private Long pendingCertificationResultTestTaskId;
    private PendingTestParticipantDTO testParticipant;

    public PendingCertificationResultTestTaskParticipantDTO() {
    }

    public PendingCertificationResultTestTaskParticipantDTO(
            PendingCertificationResultTestTaskParticipantEntity entity) {
        this();
        this.setId(entity.getId());
        this.pendingCertificationResultTestTaskId = entity.getPendingCertificationResultTestTaskId();
        if (entity.getTestParticipant() != null) {
            this.testParticipant = new PendingTestParticipantDTO(entity.getTestParticipant());
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getPendingCertificationResultTestTaskId() {
        return pendingCertificationResultTestTaskId;
    }

    public void setPendingCertificationResultTestTaskId(final Long pendingCertificationResultTestTaskId) {
        this.pendingCertificationResultTestTaskId = pendingCertificationResultTestTaskId;
    }

    public PendingTestParticipantDTO getTestParticipant() {
        return testParticipant;
    }

    public void setTestParticipant(final PendingTestParticipantDTO testParticipant) {
        this.testParticipant = testParticipant;
    }
}
