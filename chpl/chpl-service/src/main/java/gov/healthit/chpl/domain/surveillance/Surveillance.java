package gov.healthit.chpl.domain.surveillance;

import java.io.Serializable;
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

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.util.Util;

/**
 * Domain object for Surveillance.
 */
@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
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
     * Date surveillance began
     */
    @XmlElement(required = true)
    private Date startDate;

    /**
     * Date surveillance ended
     */
    @XmlElement(required = false, nillable = true)
    private Date endDate;

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
    private Set<SurveillanceRequirement> requirements;

    @XmlTransient
    private String authority;

    @XmlTransient
    private Set<String> errorMessages;

    @XmlTransient
    private Set<String> warningMessages;

    /**
     * Date of the last modification of the surveillance.
     */
    @XmlElement(required = true)
    private Date lastModifiedDate;

    /** Default constructor. */
    public Surveillance() {
        this.requirements = new LinkedHashSet<SurveillanceRequirement>();
        this.errorMessages = new HashSet<String>();
        this.warningMessages = new HashSet<String>();
    }

    /**
     * Determines if this surveillance matches another surveillance.
     * Not overriding equals and hashCode out of fear of messing something up.
     * @param anotherSurveillance
     * @return whether the two surveillance objects are the same
     */
    public boolean matches(final Surveillance anotherSurveillance) {
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
        if (this.startDate == null && anotherSurveillance.startDate != null
                || this.startDate != null && anotherSurveillance.startDate == null) {
            return false;
        } else if (this.startDate != null && anotherSurveillance.startDate != null
                && this.startDate.getTime() != anotherSurveillance.startDate.getTime()) {
            return false;
        }
        if (this.endDate == null && anotherSurveillance.endDate != null
                || this.endDate != null && anotherSurveillance.endDate == null) {
            return false;
        } else if (this.endDate != null && anotherSurveillance.endDate != null
                && this.endDate.getTime() != anotherSurveillance.endDate.getTime()) {
            return false;
        }
        if (this.randomizedSitesUsed == null && anotherSurveillance.randomizedSitesUsed != null
                || this.randomizedSitesUsed != null && anotherSurveillance.randomizedSitesUsed == null) {
            return false;
        } else if (this.randomizedSitesUsed != null && anotherSurveillance.randomizedSitesUsed != null
                && this.randomizedSitesUsed.intValue() != anotherSurveillance.randomizedSitesUsed.intValue()) {
            return false;
        }
        if (StringUtils.isEmpty(this.authority) && !StringUtils.isEmpty(anotherSurveillance.authority)
                || !StringUtils.isEmpty(this.authority) && StringUtils.isEmpty(anotherSurveillance.authority)) {
            return false;
        } else if (!StringUtils.isEmpty(this.authority) && !StringUtils.isEmpty(anotherSurveillance.authority)
                && !this.authority.equalsIgnoreCase(anotherSurveillance.authority)) {
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
                    if (thisReq.getId().longValue() == otherReq.getId().longValue()) {
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

    public Set<String> getErrorMessages() {
        return errorMessages;
    }

    public void setErrorMessages(final Set<String> errors) {
        this.errorMessages = errors;
    }

    public Set<String> getWarningMessages() {
        return warningMessages;
    }

    public void setWarningMessages(final Set<String> warnings) {
        this.warningMessages = warnings;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public CertifiedProduct getCertifiedProduct() {
        return certifiedProduct;
    }

    public void setCertifiedProduct(final CertifiedProduct certifiedProduct) {
        this.certifiedProduct = certifiedProduct;
    }

    public Date getStartDate() {
        return Util.getNewDate(startDate);
    }

    public void setStartDate(final Date startDate) {
        this.startDate = Util.getNewDate(startDate);
    }

    public Date getEndDate() {
        return Util.getNewDate(endDate);
    }

    public void setEndDate(final Date endDate) {
        this.endDate = Util.getNewDate(endDate);
    }

    public SurveillanceType getType() {
        return type;
    }

    public void setType(final SurveillanceType type) {
        this.type = type;
    }

    public Integer getRandomizedSitesUsed() {
        return randomizedSitesUsed;
    }

    public void setRandomizedSitesUsed(final Integer randomizedSitesUsed) {
        this.randomizedSitesUsed = randomizedSitesUsed;
    }

    public Set<SurveillanceRequirement> getRequirements() {
        return requirements;
    }

    public void setRequirements(final Set<SurveillanceRequirement> requirements) {
        this.requirements = requirements;
    }

    public String getSurveillanceIdToReplace() {
        return surveillanceIdToReplace;
    }

    public void setSurveillanceIdToReplace(final String surveillanceIdToReplace) {
        this.surveillanceIdToReplace = surveillanceIdToReplace;
    }

    public String getFriendlyId() {
        return friendlyId;
    }

    public void setFriendlyId(final String friendlyId) {
        this.friendlyId = friendlyId;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(final String authority) {
        this.authority = authority;
    }

    public Date getLastModifiedDate() {
        return Util.getNewDate(lastModifiedDate);
    }

    public void setLastModifiedDate(final Date lastModifiedDate) {
        this.lastModifiedDate = Util.getNewDate(lastModifiedDate);
    }
}
