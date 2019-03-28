package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.util.Util;

/**
 * Domain object for Nonconformities related to surveillance.
 */
@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
public class SurveillanceNonconformity implements Serializable {
    private static final long serialVersionUID = -1116153210791576784L;

    /**
     * Nonconformity internal ID
     */
    @XmlElement(required = true)
    private Long id;

    /**
     * Type of nonconformity; this is either a certification criteria number or
     * a textual description
     */
    @XmlElement(required = true)
    private String nonconformityType;

    /**
     * The status of a non-conformity found as a result of a surveillance
     * activity. Allowable values are "Open" or "Closed".
     */
    @XmlElement(required = true)
    private SurveillanceNonconformityStatus status;

    /**
     * Date of determination of nonconformity
     */
    @XmlElement(required = true)
    private Date dateOfDetermination;

    /**
     * Corrective action plan approval date
     */
    @XmlElement(required = false, nillable = true)
    private Date capApprovalDate;

    /**
     * Corrective action plan start date
     */
    @XmlElement(required = false, nillable = true)
    private Date capStartDate;

    /**
     * Corrective action plan end date
     */
    @XmlElement(required = false, nillable = true)
    private Date capEndDate;

    /**
     * Corrective action plan must complete date
     */
    @XmlElement(required = false, nillable = true)
    private Date capMustCompleteDate;

    /**
     * Nonconformity summary
     */
    @XmlElement(required = false, nillable = true)
    private String summary;

    /**
     * Nonconformity findings.
     */
    @XmlElement(required = false, nillable = true)
    private String findings;

    /**
     * Number of sites passed
     */
    @XmlElement(required = false, nillable = true)
    private Integer sitesPassed;

    /**
     * Total number of sites tested
     */
    @XmlElement(required = false, nillable = true)
    private Integer totalSites;

    /**
     * Developer explanation for the nonconformity
     */
    @XmlElement(required = false, nillable = true)
    private String developerExplanation;

    /**
     * Resolution description of the nonconformity
     */
    @XmlElement(required = false, nillable = true)
    private String resolution;

    /**
     * Any documents associated with the nonconformity
     */
    @XmlElementWrapper(name = "documents", nillable = true, required = false)
    @XmlElement(name = "document")
    private List<SurveillanceNonconformityDocument> documents = new ArrayList<SurveillanceNonconformityDocument>();

    /**
     * Date of the last modification of the surveillance.
     */
    @XmlElement(required = true)
    private Date lastModifiedDate;

    /**
     * Determines if this nonconformity matches another nonconformity.
     * @param anotherNonconformity
     * @return whether the two nonconformity objects are the same
     */
    public boolean matches(final SurveillanceNonconformity anotherNonconformity) {
        if (this.id == null && anotherNonconformity.id != null
                || this.id != null && anotherNonconformity.id == null) {
            return false;
        } else if (this.id != null && anotherNonconformity.id != null
                && this.id.longValue() != anotherNonconformity.id.longValue()) {
            return false;
        }
        if (StringUtils.isEmpty(this.nonconformityType) && !StringUtils.isEmpty(anotherNonconformity.nonconformityType)
                || !StringUtils.isEmpty(this.nonconformityType) && StringUtils.isEmpty(anotherNonconformity.nonconformityType)) {
            return false;
        } else if (!StringUtils.isEmpty(this.nonconformityType) && !StringUtils.isEmpty(anotherNonconformity.nonconformityType)
                && !this.nonconformityType.equalsIgnoreCase(anotherNonconformity.nonconformityType)) {
            return false;
        }
        if (this.status == null && anotherNonconformity.status != null
                || this.status != null && anotherNonconformity.status == null) {
            return false;
        } else if (this.status != null && anotherNonconformity.status != null
                && !this.status.matches(anotherNonconformity.status)) {
            return false;
        }
        if (this.dateOfDetermination == null && anotherNonconformity.dateOfDetermination != null
                || this.dateOfDetermination != null && anotherNonconformity.dateOfDetermination == null) {
            return false;
        } else if (this.dateOfDetermination != null && anotherNonconformity.dateOfDetermination != null
                && this.dateOfDetermination.getTime() != anotherNonconformity.dateOfDetermination.getTime()) {
            return false;
        }
        if (this.capApprovalDate == null && anotherNonconformity.capApprovalDate != null
                || this.capApprovalDate != null && anotherNonconformity.capApprovalDate == null) {
            return false;
        } else if (this.capApprovalDate != null && anotherNonconformity.capApprovalDate != null
                && this.capApprovalDate.getTime() != anotherNonconformity.capApprovalDate.getTime()) {
            return false;
        }
        if (this.capStartDate == null && anotherNonconformity.capStartDate != null
                || this.capStartDate != null && anotherNonconformity.capStartDate == null) {
            return false;
        } else if (this.capStartDate != null && anotherNonconformity.capStartDate != null
                && this.capStartDate.getTime() != anotherNonconformity.capStartDate.getTime()) {
            return false;
        }
        if (this.capEndDate == null && anotherNonconformity.capEndDate != null
                || this.capEndDate != null && anotherNonconformity.capEndDate == null) {
            return false;
        } else if (this.capEndDate != null && anotherNonconformity.capEndDate != null
                && this.capEndDate.getTime() != anotherNonconformity.capEndDate.getTime()) {
            return false;
        }
        if (this.capMustCompleteDate == null && anotherNonconformity.capMustCompleteDate != null
                || this.capMustCompleteDate != null && anotherNonconformity.capMustCompleteDate == null) {
            return false;
        } else if (this.capMustCompleteDate != null && anotherNonconformity.capMustCompleteDate != null
                && this.capMustCompleteDate.getTime() != anotherNonconformity.capMustCompleteDate.getTime()) {
            return false;
        }
        if (StringUtils.isEmpty(this.summary) && !StringUtils.isEmpty(anotherNonconformity.summary)
                || !StringUtils.isEmpty(this.summary) && StringUtils.isEmpty(anotherNonconformity.summary)) {
            return false;
        } else if (!StringUtils.isEmpty(this.summary) && !StringUtils.isEmpty(anotherNonconformity.summary)
                && !this.summary.equals(anotherNonconformity.summary)) {
            return false;
        }
        if (StringUtils.isEmpty(this.findings) && !StringUtils.isEmpty(anotherNonconformity.findings)
                || !StringUtils.isEmpty(this.findings) && StringUtils.isEmpty(anotherNonconformity.findings)) {
            return false;
        } else if (!StringUtils.isEmpty(this.findings) && !StringUtils.isEmpty(anotherNonconformity.findings)
                && !this.findings.equals(anotherNonconformity.findings)) {
            return false;
        }
        if (this.sitesPassed == null && anotherNonconformity.sitesPassed != null
                || this.sitesPassed != null && anotherNonconformity.sitesPassed == null) {
            return false;
        } else if (this.sitesPassed != null && anotherNonconformity.sitesPassed != null
                && this.sitesPassed.intValue() != anotherNonconformity.sitesPassed.intValue()) {
            return false;
        }
        if (this.totalSites == null && anotherNonconformity.totalSites != null
                || this.totalSites != null && anotherNonconformity.totalSites == null) {
            return false;
        } else if (this.totalSites != null && anotherNonconformity.totalSites != null
                && this.totalSites.intValue() != anotherNonconformity.totalSites.intValue()) {
            return false;
        }
        if (StringUtils.isEmpty(this.developerExplanation) && !StringUtils.isEmpty(anotherNonconformity.developerExplanation)
                || !StringUtils.isEmpty(this.developerExplanation) && StringUtils.isEmpty(anotherNonconformity.developerExplanation)) {
            return false;
        } else if (!StringUtils.isEmpty(this.developerExplanation) && !StringUtils.isEmpty(anotherNonconformity.developerExplanation)
                && !this.developerExplanation.equalsIgnoreCase(anotherNonconformity.developerExplanation)) {
            return false;
        }
        if (StringUtils.isEmpty(this.resolution) && !StringUtils.isEmpty(anotherNonconformity.resolution)
                || !StringUtils.isEmpty(this.resolution) && StringUtils.isEmpty(anotherNonconformity.resolution)) {
            return false;
        } else if (!StringUtils.isEmpty(this.resolution) && !StringUtils.isEmpty(anotherNonconformity.resolution)
                && !this.resolution.equalsIgnoreCase(anotherNonconformity.resolution)) {
            return false;
        }
        if (this.lastModifiedDate == null && anotherNonconformity.lastModifiedDate != null
                || this.lastModifiedDate != null && anotherNonconformity.lastModifiedDate == null) {
            return false;
        } else if (this.lastModifiedDate != null && anotherNonconformity.lastModifiedDate != null
                && this.lastModifiedDate.getTime() != anotherNonconformity.lastModifiedDate.getTime()) {
            return false;
        }

        //check documents
        if (this.documents == null && anotherNonconformity.documents != null
                || this.documents != null && anotherNonconformity.documents == null) {
            return false;
        } else if (this.documents != null && anotherNonconformity.documents != null
                && this.documents.size() != anotherNonconformity.documents.size()) {
            //easy check if the sizes are different
            return false;
        } else {
            //documents - were any removed?
            for (SurveillanceNonconformityDocument thisDoc : this.documents) {
                boolean foundInOtherNonconformity = false;
                for (SurveillanceNonconformityDocument otherDoc : anotherNonconformity.documents) {
                    if (thisDoc.getId().longValue() == otherDoc.getId().longValue()) {
                        foundInOtherNonconformity = true;
                    }
                }
                if (!foundInOtherNonconformity) {
                    return false;
                }
            }
            //documents - were any added?
            for (SurveillanceNonconformityDocument otherDoc : anotherNonconformity.documents) {
                boolean foundInThisNonconformity = false;
                for (SurveillanceNonconformityDocument thisDoc : this.documents) {
                    if (thisDoc.getId().longValue() == otherDoc.getId().longValue()) {
                        foundInThisNonconformity = true;
                    }
                }
                if (!foundInThisNonconformity) {
                    return false;
                }
            }
            //documents - were any changed?
            for (SurveillanceNonconformityDocument otherDoc : anotherNonconformity.documents) {
                for (SurveillanceNonconformityDocument thisDoc : this.documents) {
                    if (thisDoc.getId().longValue() == otherDoc.getId().longValue()) {
                        if (!thisDoc.matches(otherDoc)) {
                            return false;
                        }
                    }
                }
            }
        }
        //all checks passed and turned out to be matching
        //so the two nonconformities must be identical
        return true;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getNonconformityType() {
        return nonconformityType;
    }

    public void setNonconformityType(final String nonconformityType) {
        this.nonconformityType = nonconformityType;
    }

    public SurveillanceNonconformityStatus getStatus() {
        return status;
    }

    public void setStatus(final SurveillanceNonconformityStatus status) {
        this.status = status;
    }

    public Date getDateOfDetermination() {
        return Util.getNewDate(dateOfDetermination);
    }

    public void setDateOfDetermination(final Date dateOfDetermination) {
        this.dateOfDetermination = Util.getNewDate(dateOfDetermination);
    }

    public Date getCapApprovalDate() {
        return Util.getNewDate(capApprovalDate);
    }

    public void setCapApprovalDate(final Date capApprovalDate) {
        this.capApprovalDate = Util.getNewDate(capApprovalDate);
    }

    public Date getCapStartDate() {
        return Util.getNewDate(capStartDate);
    }

    public void setCapStartDate(final Date capStartDate) {
        this.capStartDate = Util.getNewDate(capStartDate);
    }

    public Date getCapEndDate() {
        return Util.getNewDate(capEndDate);
    }

    public void setCapEndDate(final Date capEndDate) {
        this.capEndDate = Util.getNewDate(capEndDate);
    }

    public Date getCapMustCompleteDate() {
        return Util.getNewDate(capMustCompleteDate);
    }

    public void setCapMustCompleteDate(final Date capMustCompleteDate) {
        this.capMustCompleteDate = Util.getNewDate(capMustCompleteDate);
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(final String summary) {
        this.summary = summary;
    }

    public String getFindings() {
        return findings;
    }

    public void setFindings(final String findings) {
        this.findings = findings;
    }

    public Integer getSitesPassed() {
        return sitesPassed;
    }

    public void setSitesPassed(final Integer sitesPassed) {
        this.sitesPassed = sitesPassed;
    }

    public Integer getTotalSites() {
        return totalSites;
    }

    public void setTotalSites(final Integer totalSites) {
        this.totalSites = totalSites;
    }

    public String getDeveloperExplanation() {
        return developerExplanation;
    }

    public void setDeveloperExplanation(final String developerExplanation) {
        this.developerExplanation = developerExplanation;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(final String resolution) {
        this.resolution = resolution;
    }

    public List<SurveillanceNonconformityDocument> getDocuments() {
        return documents;
    }

    public void setDocuments(final List<SurveillanceNonconformityDocument> documents) {
        this.documents = documents;
    }

    public Date getLastModifiedDate() {
        return Util.getNewDate(lastModifiedDate);
    }

    public void setLastModifiedDate(final Date lastModifiedDate) {
        this.lastModifiedDate = Util.getNewDate(lastModifiedDate);
    }
}
