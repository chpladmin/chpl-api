package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.ObjectUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@AllArgsConstructor
public class ListingMipsMeasure implements Serializable {
    private static final long serialVersionUID = 3070403246291821852L;

    /**
     * An internal ID for each mapping between measure and listing.
     */
    @XmlElement(required = false, nillable = true)
    private Long id;

    /**
     * The measure being applied to a listing.
     */
    @XmlElement(required = true)
    private MipsMeasure measure;

    /**
     * Indicates whether this measure was applied to the listing using
     * Automated Numerator Recording (G1) or Automated Measure Calculation (G2).
     */
    @XmlElement(required = true)
    private MipsMeasurementType measurementType;

    @XmlElement(required = true)
    private Set<CertificationCriterion> associatedCriteria = new LinkedHashSet<CertificationCriterion>();

    public ListingMipsMeasure() {
    }

    public boolean matches(ListingMipsMeasure anotherMeasure) {
        if (!propertiesMatch(anotherMeasure)) {
            return false;
        }

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

    private boolean propertiesMatch(ListingMipsMeasure anotherMeasure) {
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
                && this.measure.matches(anotherMeasure.measure)) {
            return false;
        }

        if (this.measurementType == null && anotherMeasure.measurementType != null
                || this.measurementType != null && anotherMeasure.measurementType == null) {
            return false;
        } else if (ObjectUtils.allNotNull(this.measurementType, anotherMeasure.measurementType)
                && this.measurementType.matches(anotherMeasure.measurementType)) {
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

    public MipsMeasure getMeasure() {
        return measure;
    }

    public void setMeasure(MipsMeasure measure) {
        this.measure = measure;
    }

    public MipsMeasurementType getMeasurementType() {
        return measurementType;
    }

    public void setMeasurementType(MipsMeasurementType measurementType) {
        this.measurementType = measurementType;
    }

    public Set<CertificationCriterion> getAssociatedCriteria() {
        return associatedCriteria;
    }

    public void setAssociatedCriteria(Set<CertificationCriterion> associatedCriteria) {
        this.associatedCriteria = associatedCriteria;
    }
}
