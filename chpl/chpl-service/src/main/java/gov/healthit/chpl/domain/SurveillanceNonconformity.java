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
