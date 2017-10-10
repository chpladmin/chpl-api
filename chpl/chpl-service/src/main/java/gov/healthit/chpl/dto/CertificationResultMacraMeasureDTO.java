package gov.healthit.chpl.dto;

import java.io.Serializable;

import gov.healthit.chpl.entity.listing.CertificationResultG1MacraMeasureEntity;
import gov.healthit.chpl.entity.listing.CertificationResultG2MacraMeasureEntity;

public class CertificationResultMacraMeasureDTO implements Serializable {
    private static final long serialVersionUID = 6206786831988717072L;
    private Long id;
    private Long certificationResultId;
    private MacraMeasureDTO measure;

    public CertificationResultMacraMeasureDTO() {
    }

    public CertificationResultMacraMeasureDTO(CertificationResultG1MacraMeasureEntity entity) {
        this.id = entity.getId();
        this.certificationResultId = entity.getCertificationResultId();
        if (entity.getMacraMeasure() != null) {
            this.measure = new MacraMeasureDTO(entity.getMacraMeasure());
        } else {
            this.measure = new MacraMeasureDTO();
            this.measure.setId(entity.getMacraId());
        }
    }

    public CertificationResultMacraMeasureDTO(CertificationResultG2MacraMeasureEntity entity) {
        this.id = entity.getId();
        this.certificationResultId = entity.getCertificationResultId();
        if (entity.getMacraMeasure() != null) {
            this.measure = new MacraMeasureDTO(entity.getMacraMeasure());
        } else {
            this.measure = new MacraMeasureDTO();
            this.measure.setId(entity.getMacraId());
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getCertificationResultId() {
        return certificationResultId;
    }

    public void setCertificationResultId(final Long certificationResultId) {
        this.certificationResultId = certificationResultId;
    }

    public MacraMeasureDTO getMeasure() {
        return measure;
    }

    public void setMeasure(final MacraMeasureDTO measure) {
        this.measure = measure;
    }
}
