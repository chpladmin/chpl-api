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

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.util.Util;
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
    private static long serialVersionUID = -4406043308588618231L;

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
    @XmlElement(required = true)
    private SurveillanceRequirementType type;

    /**
     * Name of the surveilled requirement (ex: 170.314 (a)(1))
     */
    @XmlElement(required = true)
    private String requirement;

    /**
     * If the surveilled requirement is a certified capability
     * then this field will have the criterion details (number, title, etc).
     */
    @XmlElement(required = false)
    private CertificationCriterion criterion;

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
        if (StringUtils.isEmpty(this.requirement) && !StringUtils.isEmpty(anotherRequirement.requirement)
                || !StringUtils.isEmpty(this.requirement) && StringUtils.isEmpty(anotherRequirement.requirement)) {
            return false;
        } else if (!StringUtils.isEmpty(this.requirement) && !StringUtils.isEmpty(anotherRequirement.requirement)
                && !this.requirement.equalsIgnoreCase(anotherRequirement.requirement)) {
            return false;
        }

        if ((this.getCriterion() == null && anotherRequirement.getCriterion() != null)
                || (this.getCriterion() != null && anotherRequirement.getCriterion() == null)) {
            return false;
        } else if (this.getCriterion() != null && anotherRequirement.getCriterion() != null
                && !this.getCriterion().getId().equals(anotherRequirement.getCriterion().getId())) {
            return false;
        }

        if (this.type == null && anotherRequirement.type != null
                || this.type != null && anotherRequirement.type == null) {
            return false;
        } else if (this.type != null && anotherRequirement.type != null
                && !this.type.matches(anotherRequirement.type)) {
            return false;
        }
        if (this.result == null && anotherRequirement.result != null
                || this.result != null && anotherRequirement.result == null) {
            return false;
        } else if (this.result != null && anotherRequirement.result != null
                && !this.result.matches(anotherRequirement.result)) {
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

    public SurveillanceRequirementType getType() {
        return type;
    }

    public void setType(SurveillanceRequirementType type) {
        this.type = type;
    }

    public String getRequirement() {
        return requirement;
    }

    public void setRequirement(String requirement) {
        this.requirement = requirement;
    }

    public CertificationCriterion getCriterion() {
        return criterion;
    }

    public void setCriterion(CertificationCriterion criterion) {
        this.criterion = criterion;
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
    public String getRequirementName() {
        if (getCriterion() == null) {
            return getRequirement();
        }
        return Util.formatCriteriaNumber(getCriterion());
    }

    @Override
    public boolean equals(Object anotherObject) {
        // If the object is compared with itself then return true
        if (anotherObject == this) {
            return true;
        }

        // check if anotherObject is the same type of class as this
        if (!(anotherObject instanceof SurveillanceRequirement)) {
            return false;
        }

        // typecast anotherObject to this type so that we can compare data
        // members
        SurveillanceRequirement anotherReq = (SurveillanceRequirement) anotherObject;

        // Compare the data members and return accordingly
        if ((this.getRequirement() == null && anotherReq.getRequirement() != null)
                || (this.getRequirement() != null && anotherReq.getRequirement() == null)) {
            return false;
        }
        if ((this.getCriterion() == null && anotherReq.getCriterion() != null)
                || (this.getCriterion() != null && anotherReq.getCriterion() == null)) {
            return false;
        }

        boolean requirementsMatch = this.getRequirement().equals(anotherReq.getRequirement());
        if (this.getCriterion() != null && anotherReq.getCriterion() != null) {
            requirementsMatch = requirementsMatch
                    && this.getCriterion().getId().equals(anotherReq.getCriterion().getId());
        }
        return requirementsMatch;
    }

    @Override
    public int hashCode() {
        if (this.getRequirement() == null) {
            return -1;
        }
        if (this.getCriterion() == null) {
            return this.getRequirement().hashCode();
        }
        return this.getCriterion().getId().hashCode();
    }
}
