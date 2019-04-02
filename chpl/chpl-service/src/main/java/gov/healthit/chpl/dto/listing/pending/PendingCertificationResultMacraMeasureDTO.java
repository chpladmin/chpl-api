package gov.healthit.chpl.dto.listing.pending;

import java.io.Serializable;

import gov.healthit.chpl.dto.MacraMeasureDTO;
import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultG1MacraMeasureEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultG2MacraMeasureEntity;

public class PendingCertificationResultMacraMeasureDTO implements Serializable {
    private static final long serialVersionUID = 6501711625182363630L;
    private Long id;
    private Long pendingCertificationResultId;
    private Long macraMeasureId;
    private MacraMeasureDTO macraMeasure;
    private String enteredValue;

    public PendingCertificationResultMacraMeasureDTO() {
    }

    public PendingCertificationResultMacraMeasureDTO(PendingCertificationResultG1MacraMeasureEntity entity) {
        this.setId(entity.getId());
        this.setEnteredValue(entity.getEnteredValue());
        this.setPendingCertificationResultId(entity.getPendingCertificationResultId());
        this.macraMeasureId = entity.getMacraId();
        if (entity.getMacraMeasure() != null) {
            this.macraMeasure = new MacraMeasureDTO(entity.getMacraMeasure());
        }
        this.enteredValue = entity.getEnteredValue();
    }

    public PendingCertificationResultMacraMeasureDTO(PendingCertificationResultG2MacraMeasureEntity entity) {
        this.setId(entity.getId());
        this.setEnteredValue(entity.getEnteredValue());
        this.setPendingCertificationResultId(entity.getPendingCertificationResultId());
        this.macraMeasureId = entity.getMacraId();
        if (entity.getMacraMeasure() != null) {
            this.macraMeasure = new MacraMeasureDTO(entity.getMacraMeasure());
        }
        this.enteredValue = entity.getEnteredValue();
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

    public Long getMacraMeasureId() {
        return macraMeasureId;
    }

    public void setMacraMeasureId(final Long macraMeasureId) {
        this.macraMeasureId = macraMeasureId;
    }

    public MacraMeasureDTO getMacraMeasure() {
        return macraMeasure;
    }

    public void setMacraMeasure(final MacraMeasureDTO macraMeasure) {
        this.macraMeasure = macraMeasure;
    }

    public String getEnteredValue() {
        return enteredValue;
    }

    public void setEnteredValue(final String enteredValue) {
        this.enteredValue = enteredValue;
    }
}
