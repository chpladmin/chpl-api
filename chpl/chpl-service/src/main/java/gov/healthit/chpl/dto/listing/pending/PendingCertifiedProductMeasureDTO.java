package gov.healthit.chpl.dto.listing.pending;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.ObjectUtils;

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

    public boolean matchesCriteria(PendingCertifiedProductMeasureDTO anotherMeasure) {
        if (this.associatedCriteria == null && anotherMeasure.associatedCriteria != null
                || this.associatedCriteria != null && anotherMeasure.associatedCriteria == null) {
            return false;
        } else if (ObjectUtils.allNotNull(this.associatedCriteria, anotherMeasure.associatedCriteria)
                && this.associatedCriteria.size() != anotherMeasure.associatedCriteria.size()) {
            // easy check if the sizes are different
            return false;
        } else {
            // associated criteria - were any removed?
            for (CertificationCriterion thisCriterion : this.associatedCriteria) {
                boolean foundInOtherMeasure = false;
                for (CertificationCriterion otherCriterion : anotherMeasure.associatedCriteria) {
                    if (thisCriterion.getId().longValue() == otherCriterion.getId().longValue()) {
                        foundInOtherMeasure = true;
                    }
                }
                if (!foundInOtherMeasure) {
                    return false;
                }
            }
            // associated criteria - were any added?
            for (CertificationCriterion otherCriterion : anotherMeasure.associatedCriteria) {
                boolean foundInThisMeasure = false;
                for (CertificationCriterion thisCriterion : this.associatedCriteria) {
                    if (thisCriterion.getId().longValue() == otherCriterion.getId().longValue()) {
                        foundInThisMeasure = true;
                    }
                }
                if (!foundInThisMeasure) {
                    return false;
                }
            }
            // associated criteria - were any changed?
            for (CertificationCriterion otherCriterion : anotherMeasure.associatedCriteria) {
                for (CertificationCriterion thisCriterion : this.associatedCriteria) {
                    if (thisCriterion.getId().longValue() == otherCriterion.getId().longValue()) {
                        if (!thisCriterion.equals(otherCriterion)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
}
