package gov.healthit.chpl.domain.surveillance;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.api.deprecatedUsage.DeprecatedResponseField;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.util.NullSafeEvaluator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurveillanceRequirement implements Serializable {
    private static final long serialVersionUID = -5658812197618734286L;

    /**
     * Surveilled requirement internal ID
     */
    @XmlElement(required = true)
    private Long id;

    /**
     * For a given surveillance activity, the type of requirement being
     * surveilled. Allowable values include: "Certified Capability";
     * "Transparency or Disclosure Requirement", or "Other Requirement"
     */
    @Deprecated
    @XmlElement(required = true)
    private SurveillanceRequirementType type;

    /**
     * Name of the surveilled requirement (ex: 170.314 (a)(1))
     */
    @Deprecated
    @XmlElement(required = true)
    private String requirement;

    /**
     * If the surveilled requirement is a certified capability
     * then this field will have the criterion details (number, title, etc).
     */
    @Deprecated
    @XmlElement(required = false)
    private CertificationCriterion criterion;

    /**
     * TODO - Need to add description (OCD-4029)
     */
    @XmlElement
    private RequirementDetailType requirementDetailType;

    /**
     * TODO - Need to add description (OCD-4029)
     */
    @XmlElement
    private String requirementDetailOther;

    /**
     * The result for surveillance conducted on each surveillance requirement.
     * Allowable values are "Non-Conformity" or "No Non-Conformity"
     */
    @XmlElement(required = false, nillable = true)
    private SurveillanceResultType result;

    /**
     * List of nonconformities found for this surveilled requirement
     */
    @XmlElementWrapper(name = "nonconformities", nillable = true, required = false)
    @XmlElement(name = "nonconformity")
    @Builder.Default
    private List<SurveillanceNonconformity> nonconformities = new ArrayList<SurveillanceNonconformity>();

    public boolean matches(SurveillanceRequirement anotherRequirement) {
        if (!propertiesMatch(anotherRequirement)) {
            return false;
        }

        if (this.nonconformities == null && anotherRequirement.nonconformities != null
                || this.nonconformities != null && anotherRequirement.nonconformities == null) {
            return false;
        } else if (this.nonconformities != null && anotherRequirement.nonconformities != null
                && this.nonconformities.size() != anotherRequirement.nonconformities.size()) {
            //easy check if the sizes are different
            return false;
        } else {
            //nonconformities - were any removed?
            for (SurveillanceNonconformity thisNc : this.nonconformities) {
                boolean foundInOtherRequirement = false;
                for (SurveillanceNonconformity otherNc : anotherRequirement.nonconformities) {
                    if (thisNc.getId() != null && otherNc.getId() != null
                            && thisNc.getId().longValue() == otherNc.getId().longValue()) {
                        foundInOtherRequirement = true;
                    }
                }
                if (!foundInOtherRequirement) {
                    return false;
                }
            }
            //nonconformities - were any added?
            for (SurveillanceNonconformity otherNc : anotherRequirement.nonconformities) {
                boolean foundInThisRequirement = false;
                for (SurveillanceNonconformity thisNc : this.nonconformities) {
                    if (thisNc.getId().longValue() == otherNc.getId().longValue()) {
                        foundInThisRequirement = true;
                    }
                }
                if (!foundInThisRequirement) {
                    return false;
                }
            }
            //nonconformities - were any changed?
            for (SurveillanceNonconformity otherNc : anotherRequirement.nonconformities) {
                for (SurveillanceNonconformity thisNc : this.nonconformities) {
                    if (thisNc.getId().longValue() == otherNc.getId().longValue()) {
                        if (!thisNc.matches(otherNc)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public boolean propertiesMatch(SurveillanceRequirement anotherRequirement) {
        if (this.id == null && anotherRequirement.id != null
                || this.id != null && anotherRequirement.id == null) {
            return false;
        } else if (this.id != null && anotherRequirement.id != null
                && this.id.longValue() != anotherRequirement.id.longValue()) {
            return false;
        }

        if (!NullSafeEvaluator.eval(() -> this.getRequirementDetailType().getId(), 0L).equals(
                NullSafeEvaluator.eval(() -> anotherRequirement.getRequirementDetailType().getId(), 0L))) {
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

    @Deprecated
    public SurveillanceRequirementType getType() {
        return type;
    }


    @Deprecated
    public void setType(SurveillanceRequirementType type) {
        this.type = type;
    }

    @Deprecated
    public String getRequirement() {
        return requirement;
    }

    @Deprecated
    public void setRequirement(String requirement) {
        this.requirement = requirement;
    }

    @Deprecated
    public CertificationCriterion getCriterion() {
        return criterion;
    }

    @Deprecated
    public void setCriterion(CertificationCriterion criterion) {
        this.criterion = criterion;
    }

    public RequirementDetailType getRequirementDetailType() {
        return requirementDetailType;
    }

    public void setRequirementDetailType(RequirementDetailType requirementDetailType) {
        this.requirementDetailType = requirementDetailType;
    }

    public String getRequirementDetailOther() {
        return requirementDetailOther;
    }

    public void setRequirementDetailOther(String requirementDetailOther) {
        this.requirementDetailOther = requirementDetailOther;
    }

    public SurveillanceResultType getResult() {
        return result;
    }

    public void setResult(SurveillanceResultType result) {
        this.result = result;
    }

    public List<SurveillanceNonconformity> getNonconformities() {
        return nonconformities;
    }


    public void setNonconformities(List<SurveillanceNonconformity> nonconformities) {
        this.nonconformities = nonconformities;
    }

    @XmlTransient
    @Deprecated //this field should be removed from Json responses but left in the API code
    @DeprecatedResponseField(removalDate = "2023-01-01",
        message = "This field is deprecated and will be removed from the response data in a future release.")
    public String getRequirementName() {
        return NullSafeEvaluator.eval(() -> getRequirementDetailType().getFormattedTitle(), "");
    }

}
