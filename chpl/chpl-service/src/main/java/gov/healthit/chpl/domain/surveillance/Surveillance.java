package gov.healthit.chpl.domain.surveillance;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.util.LocalDateAdapter;
import gov.healthit.chpl.util.LocalDateDeserializer;
import gov.healthit.chpl.util.LocalDateSerializer;
import gov.healthit.chpl.util.Util;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

/**
 * Domain object for Surveillance.
 */
@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Surveillance implements Serializable {
    private static final long serialVersionUID = 7018071250912371691L;

    /**
     * Surveillance internal ID
     */
    @XmlElement(required = true)
    private Long id;

    @XmlTransient
    private String surveillanceIdToReplace;

    /**
     * The user-friendly ID of this surveillance relative to a listing. Ex:
     * SURV01
     */
    @XmlElement(required = true)
    private String friendlyId;

    /**
     * The listing under surveillance
     */
    @XmlElement(required = true)
    private CertifiedProduct certifiedProduct;

    /**
     * Day surveillance began
     */
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @XmlJavaTypeAdapter(value = LocalDateAdapter.class)
    @XmlElement(required = true)
    private LocalDate startDay;

    /**
     * Day surveillance ended
     */
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @XmlJavaTypeAdapter(value = LocalDateAdapter.class)
    @XmlElement(required = false, nillable = true)
    private LocalDate endDay;

    /**
     * The type of surveillance conducted. Allowable values are "Reactive" or
     * "Randomized".
     */
    @XmlElement(required = true)
    private SurveillanceType type;

    /**
     * Number of randomized sites used. Only applicable for randomized
     * surveillance.
     */
    @XmlElement(required = false, nillable = true)
    private Integer randomizedSitesUsed;

    /**
     * For a given surveillance activity, the certification criteria or program
     * requirement being surveilled. Where applicable, the surveillance
     * requirement will be presented as the regulation text number (e.g.
     * 170.315(a)(2) or 170.315(k)(1)). However, other values are allowed to
     * provide a brief description of the surveilled requirement.
     */
    @XmlElementWrapper(name = "surveilledRequirements", nillable = true, required = false)
    @XmlElement(name = "requirement")
    @Builder.Default
    private Set<SurveillanceRequirement> requirements = new LinkedHashSet<SurveillanceRequirement>();

    @XmlTransient
    @Builder.Default
    private Set<String> errorMessages = new HashSet<String>();

    @XmlTransient
    @Builder.Default
    private Set<String> warningMessages = new HashSet<String>();

    /**
     * Date of the last modification of the surveillance.
     */
    @XmlElement(required = true)
    private Date lastModifiedDate;

    /**
     * Determines if this surveillance matches another surveillance.
     * Not overriding equals and hashCode out of fear of messing something up.
     * @param anotherSurveillance
     * @return whether the two surveillance objects are the same
     */
    public boolean matches(Surveillance anotherSurveillance) {
        if (!propertiesMatch(anotherSurveillance)) {
            return false;
        }

        if (this.requirements == null && anotherSurveillance.requirements != null
                || this.requirements != null && anotherSurveillance.requirements == null) {
            return false;
        } else if (this.requirements != null && anotherSurveillance.requirements != null
                && this.requirements.size() != anotherSurveillance.requirements.size()) {
            // easy check if the sizes are different
            return false;
        } else {
            // surveillance requirements - were any removed?
            for (SurveillanceRequirement thisReq : this.requirements) {
                boolean foundInOtherSurveillance = false;
                for (SurveillanceRequirement otherReq : anotherSurveillance.requirements) {
                    if (thisReq.getId() != null && otherReq.getId() != null
                            && thisReq.getId().longValue() == otherReq.getId().longValue()) {
                        foundInOtherSurveillance = true;
                    }
                }
                if (!foundInOtherSurveillance) {
                    return false;
                }
            }
            // surveillance requirements - were any added?
            for (SurveillanceRequirement otherReq : anotherSurveillance.requirements) {
                boolean foundInThisSurveillance = false;
                for (SurveillanceRequirement thisReq : this.requirements) {
                    if (thisReq.getId().longValue() == otherReq.getId().longValue()) {
                        foundInThisSurveillance = true;
                    }
                }
                if (!foundInThisSurveillance) {
                    return false;
                }
            }
            // surveillance requirements - were any changed?
            for (SurveillanceRequirement otherReq : anotherSurveillance.requirements) {
                for (SurveillanceRequirement thisReq : this.requirements) {
                    if (thisReq.getId().longValue() == otherReq.getId().longValue()) {
                        if (!thisReq.matches(otherReq)) {
                            return false;
                        }
                    }
                }
            }
        }
        // all checks passed and turned out to be matching
        // so the two surveillances must be identical
        return true;
    }

    public boolean propertiesMatch(Surveillance anotherSurveillance) {
        if (this.id == null && anotherSurveillance.id != null || this.id != null && anotherSurveillance.id == null) {
            return false;
        } else if (this.id != null && anotherSurveillance.id != null
                && this.id.longValue() != anotherSurveillance.id.longValue()) {
            return false;
        }
        if (StringUtils.isEmpty(this.friendlyId) && !StringUtils.isEmpty(anotherSurveillance.friendlyId)
                || !StringUtils.isEmpty(this.friendlyId) && StringUtils.isEmpty(anotherSurveillance.friendlyId)) {
            return false;
        } else if (!StringUtils.isEmpty(this.friendlyId) && !StringUtils.isEmpty(anotherSurveillance.friendlyId)
                && !this.friendlyId.equalsIgnoreCase(anotherSurveillance.friendlyId)) {
            return false;
        }
        if (this.startDay == null && anotherSurveillance.startDay != null
                || this.startDay != null && anotherSurveillance.startDay == null) {
            return false;
        } else if (this.startDay != null && anotherSurveillance.startDay != null
                && !this.startDay.equals(anotherSurveillance.startDay)) {
            return false;
        }
        if (this.endDay == null && anotherSurveillance.endDay != null
                || this.endDay != null && anotherSurveillance.endDay == null) {
            return false;
        } else if (this.endDay != null && anotherSurveillance.endDay != null
                && !this.endDay.equals(anotherSurveillance.endDay)) {
            return false;
        }
        if (this.randomizedSitesUsed == null && anotherSurveillance.randomizedSitesUsed != null
                || this.randomizedSitesUsed != null && anotherSurveillance.randomizedSitesUsed == null) {
            return false;
        } else if (this.randomizedSitesUsed != null && anotherSurveillance.randomizedSitesUsed != null
                && this.randomizedSitesUsed.intValue() != anotherSurveillance.randomizedSitesUsed.intValue()) {
            return false;
        }
        if (this.certifiedProduct == null && anotherSurveillance.certifiedProduct != null
                || this.certifiedProduct != null && anotherSurveillance.certifiedProduct == null) {
            return false;
        } else if (this.certifiedProduct != null && anotherSurveillance.certifiedProduct != null
                && !this.certifiedProduct.matches(anotherSurveillance.certifiedProduct)) {
            return false;
        }
        if (this.type == null && anotherSurveillance.type != null
                || this.type != null && anotherSurveillance.type == null) {
            return false;
        } else if (this.type != null && anotherSurveillance.type != null
                && !this.type.matches(anotherSurveillance.type)) {
            return false;
        }
        return true;
    }

    public Set<String> getErrorMessages() {
        return errorMessages;
    }

    public void setErrorMessages(Set<String> errors) {
        this.errorMessages = errors;
    }

    public Set<String> getWarningMessages() {
        return warningMessages;
    }

    public void setWarningMessages(Set<String> warnings) {
        this.warningMessages = warnings;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CertifiedProduct getCertifiedProduct() {
        return certifiedProduct;
    }

    public void setCertifiedProduct(CertifiedProduct certifiedProduct) {
        this.certifiedProduct = certifiedProduct;
    }

    public LocalDate getStartDay() {
        return this.startDay;
    }

    public void setStartDay(LocalDate startDay) {
        this.startDay = startDay;
    }

    public LocalDate getEndDay() {
        return this.endDay;
    }

    public void setEndDay(LocalDate endDay) {
        this.endDay = endDay;
    }

    public SurveillanceType getType() {
        return type;
    }

    public void setType(SurveillanceType type) {
        this.type = type;
    }

    public Integer getRandomizedSitesUsed() {
        return randomizedSitesUsed;
    }

    public void setRandomizedSitesUsed(Integer randomizedSitesUsed) {
        this.randomizedSitesUsed = randomizedSitesUsed;
    }

    public Set<SurveillanceRequirement> getRequirements() {
        return requirements;
    }

    public void setRequirements(Set<SurveillanceRequirement> requirements) {
        this.requirements = requirements;
    }

    public String getSurveillanceIdToReplace() {
        return surveillanceIdToReplace;
    }

    public void setSurveillanceIdToReplace(String surveillanceIdToReplace) {
        this.surveillanceIdToReplace = surveillanceIdToReplace;
    }

    public String getFriendlyId() {
        return friendlyId;
    }

    public void setFriendlyId(String friendlyId) {
        this.friendlyId = friendlyId;
    }

    public Date getLastModifiedDate() {
        return Util.getNewDate(lastModifiedDate);
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = Util.getNewDate(lastModifiedDate);
    }
}
