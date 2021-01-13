package gov.healthit.chpl.dto.listing.pending;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.Measure;
import gov.healthit.chpl.domain.MeasureType;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.listing.measure.PendingListingMeasureEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class PendingCertifiedProductMeasureDTO implements Serializable {
    private static final long serialVersionUID = 6800290521633648570L;
    private Long id;
    private Long pendingCertifiedProductId;
    private Measure measure;
    private MeasureType measureType;
    private String uploadedValue;
    private Set<CertificationCriterion> associatedCriteria;

    public PendingCertifiedProductMeasureDTO() {
        this.associatedCriteria = new LinkedHashSet<CertificationCriterion>();
    }

    public PendingCertifiedProductMeasureDTO(PendingListingMeasureEntity entity) {
        this();
        this.setId(entity.getId());
        this.setPendingCertifiedProductId(entity.getPendingCertifiedProductId());
        if (entity.getMeasure() != null) {
            this.measure = entity.getMeasure().convert();
        }
        if (entity.getType() != null) {
            this.measureType = entity.getType().convert();
        }
        this.setUploadedValue(entity.getUploadedValue());
        entity.getAssociatedCriteria().stream().forEach(assocCriterionEntity -> {
            CertificationCriterionDTO criterionDto = new CertificationCriterionDTO(assocCriterionEntity.getCriterion());
            this.associatedCriteria.add(new CertificationCriterion(criterionDto));
        });

    }
}
