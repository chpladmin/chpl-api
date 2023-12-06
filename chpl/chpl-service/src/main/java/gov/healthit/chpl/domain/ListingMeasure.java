package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.LinkedHashSet;

import org.apache.commons.lang3.ObjectUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@AllArgsConstructor
public class ListingMeasure implements Serializable {
    private static final long serialVersionUID = 3070403246291821852L;

    @Schema(description = "An internal ID for each mapping between measure and listing.")
    private Long id;

    @Schema(description = "The measure being applied to a listing.")
    private Measure measure;

    @Schema(description = "Indicates whether this measure was applied to the listing using "
            + "Automated Numerator Recording (G1) or Automated Measure Calculation (G2).")
    private MeasureType measureType;

    @Builder.Default
    private LinkedHashSet<CertificationCriterion> associatedCriteria = new LinkedHashSet<CertificationCriterion>();

    public ListingMeasure() {
        super();
    }

    public boolean matches(ListingMeasure anotherMeasure) {
        if (!propertiesMatch(anotherMeasure)) {
            return false;
        }
        return matchesCriteria(anotherMeasure);
    }

    public boolean matchesCriteria(ListingMeasure anotherMeasure) {
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

    private boolean propertiesMatch(ListingMeasure anotherMeasure) {
        if (this.id == null && anotherMeasure.id != null || this.id != null && anotherMeasure.id == null) {
            return false;
        } else if (ObjectUtils.allNotNull(this.id, anotherMeasure.id)
                && this.id.longValue() != anotherMeasure.id.longValue()) {
            return false;
        }

        if (this.measure == null && anotherMeasure.measure != null || this.measure != null
                && anotherMeasure.measure == null) {
            return false;
        } else if (ObjectUtils.allNotNull(this.measure, anotherMeasure.measure)
                && !this.measure.matches(anotherMeasure.measure)) {
            return false;
        }

        if (this.measureType == null && anotherMeasure.measureType != null
                || this.measureType != null && anotherMeasure.measureType == null) {
            return false;
        } else if (ObjectUtils.allNotNull(this.measureType, anotherMeasure.measureType)
                && !this.measureType.matches(anotherMeasure.measureType)) {
            return false;
        }
        return true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Measure getMeasure() {
        return measure;
    }

    public void setMeasure(Measure measure) {
        this.measure = measure;
    }

    public MeasureType getMeasureType() {
        return measureType;
    }

    public void setMeasureType(MeasureType measureType) {
        this.measureType = measureType;
    }

    public LinkedHashSet<CertificationCriterion> getAssociatedCriteria() {
        return associatedCriteria;
    }

    public void setAssociatedCriteria(LinkedHashSet<CertificationCriterion> associatedCriteria) {
        this.associatedCriteria = associatedCriteria;
    }
}
