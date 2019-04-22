package gov.healthit.chpl.domain;

import java.io.Serializable;

import gov.healthit.chpl.dto.CertificationResultMacraMeasureDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultMacraMeasureDTO;

public class CertificationResultMacraMeasure implements Serializable {
    private static final long serialVersionUID = -8007889129011680045L;
    private Long id;
    Long certificationResultId;
    private MacraMeasure measure;

    public CertificationResultMacraMeasure() {
        super();
    }

    public CertificationResultMacraMeasure(CertificationResultMacraMeasureDTO dto) {
        this.id = dto.getId();
        this.certificationResultId = dto.getCertificationResultId();
        if (dto.getMeasure() != null) {
            this.measure = new MacraMeasure(dto.getMeasure());
        }
    }

    public CertificationResultMacraMeasure(PendingCertificationResultMacraMeasureDTO dto) {
        this.id = dto.getId();
        this.certificationResultId = dto.getPendingCertificationResultId();
        if (dto.getMacraMeasure() != null) {
            this.measure = new MacraMeasure(dto.getMacraMeasure());
        } else {
            this.measure = new MacraMeasure();
            this.measure.setId(dto.getMacraMeasureId());
        }
    }

    // not overriding equals on purpose
    // this is meant to determine if a user would think two macra measures
    // are the same, not as thorough as equals
    public boolean matches(CertificationResultMacraMeasure anotherMeasure) {
        boolean result = false;
        if (this.getId() != null && anotherMeasure.getId() != null
                && this.getId().longValue() == anotherMeasure.getId().longValue()) {
            result = true;
        } else if (this.getMeasure() != null && anotherMeasure.getMeasure() != null) {
            result = this.getMeasure().matches(anotherMeasure.getMeasure());
        }
        return result;
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

    public MacraMeasure getMeasure() {
        return measure;
    }

    public void setMeasure(final MacraMeasure measure) {
        this.measure = measure;
    }
}
