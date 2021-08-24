package gov.healthit.chpl.domain.surveillance;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.util.LocalDateAdapter;
import gov.healthit.chpl.util.LocalDateDeserializer;
import gov.healthit.chpl.util.LocalDateSerializer;
import gov.healthit.chpl.util.Util;

/**
 * Domain object for Non-conformities related to surveillance.
 */
@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SurveillanceNonconformity implements Serializable {
    private static final long serialVersionUID = -1116153210791576784L;

    /**
     * Non-conformity internal ID
     */
    @XmlElement(required = true)
    private Long id;

    /**
     * Type of non-conformity; this is either a certification criteria number or
     * a textual description
     */
    @XmlElement(required = true)
    private String nonconformityType;

    /**
     * If the non-conformity type is a certified capability
     * then this field will have the criterion details (number, title, etc).
     */
    @XmlElement(required = false)
    private CertificationCriterion criterion;

    /**
     * The status of a non-conformity found as a result of a surveillance
     * activity. Allowable values are "Open" or "Closed".
     */
    @XmlTransient
    @Deprecated
    private SurveillanceNonconformityStatus status;

    /**
     * The status of a non-conformity found as a result of a surveillance
     * activity. Allowable values are "Open" or "Closed".
     */
    @XmlElement(required = true)
    private String nonconformityStatus;

    /**
     * DEPRECATED. Date of determination of non-conformity
     */
    @Deprecated
    @XmlTransient
    private Date dateOfDetermination;

    /**
     * Date of determination of non-conformity
     */
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @XmlJavaTypeAdapter(value = LocalDateAdapter.class)
    @XmlElement(required = true)
    private LocalDate dateOfDeterminationDay;

    /**
     * DEPRECATED. Corrective action plan approval date
     */
    @Deprecated
    @XmlTransient
    private Date capApprovalDate;

    /**
     * Corrective action plan approval day
     */
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @XmlJavaTypeAdapter(value = LocalDateAdapter.class)
    @XmlElement(required = false, nillable = true)
    private LocalDate capApprovalDay;

    /**
     * DEPRECATED. Corrective action plan start date
     */
    @Deprecated
    @XmlTransient
    private Date capStartDate;

    /**
     * Corrective action plan start day
     */
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @XmlJavaTypeAdapter(value = LocalDateAdapter.class)
    @XmlElement(required = false, nillable = true)
    private LocalDate capStartDay;

    /**
     * DEPRECATED. Corrective action plan end date
     */
    @Deprecated
    @XmlTransient
    private Date capEndDate;

    /**
     * Corrective action plan end day
     */
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @XmlJavaTypeAdapter(value = LocalDateAdapter.class)
    @XmlElement(required = false, nillable = true)
    private LocalDate capEndDay;

    /**
     * DEPRECATED. Corrective action plan must complete date
     */
    @Deprecated
    @XmlTransient
    private Date capMustCompleteDate;

    /**
     * Corrective action plan must complete date
     */
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @XmlJavaTypeAdapter(value = LocalDateAdapter.class)
    @XmlElement(required = false, nillable = true)
    private LocalDate capMustCompleteDay;

    /**
     * Date non-conformity was closed
     */
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @XmlJavaTypeAdapter(value = LocalDateAdapter.class)
    @XmlElement(required = false, nillable = true)
    private LocalDate nonconformityCloseDate;

    /**
     * Non-conformity summary
     */
    @XmlElement(required = false, nillable = true)
    private String summary;

    /**
     * Non-conformity findings.
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
     * Developer explanation for the non-conformity
     */
    @XmlElement(required = false, nillable = true)
    private String developerExplanation;

    /**
     * Resolution description of the non-conformity
     */
    @XmlElement(required = false, nillable = true)
    private String resolution;

    /**
     * Any documents associated with the non-conformity
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
     * Determines if this non-conformity matches another non-conformity.
     * @param anotherNonconformity
     * @return whether the two non-conformity objects are the same
     */
    public boolean matches(SurveillanceNonconformity anotherNonconformity) {
        if (!propertiesMatch(anotherNonconformity)) {
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
        //so the two non-conformities must be identical
        return true;
    }

    public boolean propertiesMatch(SurveillanceNonconformity anotherNonconformity) {
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
        if ((this.criterion == null && anotherNonconformity.criterion != null)
                || (this.criterion != null && anotherNonconformity.criterion == null)) {
            return false;
        } else if (this.criterion != null && anotherNonconformity.criterion != null
                && !this.criterion.getId().equals(anotherNonconformity.criterion.getId())) {
            return false;
        }
        if (this.dateOfDeterminationDay == null && anotherNonconformity.dateOfDeterminationDay != null
                || this.dateOfDeterminationDay != null && anotherNonconformity.dateOfDeterminationDay == null) {
            return false;
        } else if (this.dateOfDeterminationDay != null && anotherNonconformity.dateOfDeterminationDay != null
                && !this.dateOfDeterminationDay.equals(anotherNonconformity.dateOfDeterminationDay)) {
            return false;
        }
        if (this.capApprovalDay == null && anotherNonconformity.capApprovalDay != null
                || this.capApprovalDay != null && anotherNonconformity.capApprovalDay == null) {
            return false;
        } else if (this.capApprovalDay != null && anotherNonconformity.capApprovalDay != null
                && !this.capApprovalDay.equals(anotherNonconformity.capApprovalDay)) {
            return false;
        }
        if (this.capStartDay == null && anotherNonconformity.capStartDay != null
                || this.capStartDay != null && anotherNonconformity.capStartDay == null) {
            return false;
        } else if (this.capStartDay != null && anotherNonconformity.capStartDay != null
                && !this.capStartDay.equals(anotherNonconformity.capStartDay)) {
            return false;
        }
        if (this.capEndDay == null && anotherNonconformity.capEndDay != null
                || this.capEndDay != null && anotherNonconformity.capEndDay == null) {
            return false;
        } else if (this.capEndDay != null && anotherNonconformity.capEndDay != null
                && !this.capEndDay.equals(anotherNonconformity.capEndDay)) {
            return false;
        }
        if (this.capMustCompleteDay == null && anotherNonconformity.capMustCompleteDay != null
                || this.capMustCompleteDay != null && anotherNonconformity.capMustCompleteDay == null) {
            return false;
        } else if (this.capMustCompleteDay != null && anotherNonconformity.capMustCompleteDay != null
                && !this.capMustCompleteDay.equals(anotherNonconformity.capMustCompleteDay)) {
            return false;
        }
        if (this.nonconformityCloseDate == null && anotherNonconformity.nonconformityCloseDate != null
                || this.nonconformityCloseDate != null && anotherNonconformity.nonconformityCloseDate == null) {
            return false;
        } else if (this.nonconformityCloseDate != null && anotherNonconformity.nonconformityCloseDate != null
                && this.nonconformityCloseDate != anotherNonconformity.nonconformityCloseDate) {
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
        return true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNonconformityType() {
        return nonconformityType;
    }

    public void setNonconformityType(String nonconformityType) {
        this.nonconformityType = nonconformityType;
    }

    public CertificationCriterion getCriterion() {
        return criterion;
    }

    public void setCriterion(CertificationCriterion criterion) {
        this.criterion = criterion;
    }

    @Deprecated
    public SurveillanceNonconformityStatus getStatus() {
        return status;
    }

    @Deprecated
    public void setStatus(SurveillanceNonconformityStatus status) {
        this.status = status;
    }

    public String getNonconformityStatus() {
        return nonconformityStatus;
    }

    public void setNonconformityStatus(String nonconformityStatus) {
        this.nonconformityStatus = nonconformityStatus;
    }

    @Deprecated
    public Date getDateOfDetermination() {
        return Util.getNewDate(dateOfDetermination);
    }

    @Deprecated
    public void setDateOfDetermination(Date dateOfDetermination) {
        this.dateOfDetermination = Util.getNewDate(dateOfDetermination);
    }

    public LocalDate getDateOfDeterminationDay() {
        return this.dateOfDeterminationDay;
    }

    public void setDateOfDeterminationDay(LocalDate dateOfDeterminationDay) {
        this.dateOfDeterminationDay = dateOfDeterminationDay;
    }

    @Deprecated
    public Date getCapApprovalDate() {
        return Util.getNewDate(capApprovalDate);
    }

    @Deprecated
    public void setCapApprovalDate(Date capApprovalDate) {
        this.capApprovalDate = Util.getNewDate(capApprovalDate);
    }

    public LocalDate getCapApprovalDay() {
        return this.capApprovalDay;
    }

    public void setCapApprovalDay(LocalDate capApprovalDay) {
        this.capApprovalDay = capApprovalDay;
    }

    @Deprecated
    public Date getCapStartDate() {
        return Util.getNewDate(capStartDate);
    }

    @Deprecated
    public void setCapStartDate(Date capStartDate) {
        this.capStartDate = Util.getNewDate(capStartDate);
    }

    public LocalDate getCapStartDay() {
        return this.capStartDay;
    }

    public void setCapStartDay(LocalDate capStartDay) {
        this.capStartDay = capStartDay;
    }

    @Deprecated
    public Date getCapEndDate() {
        return Util.getNewDate(capEndDate);
    }

    @Deprecated
    public void setCapEndDate(Date capEndDate) {
        this.capEndDate = Util.getNewDate(capEndDate);
    }

    public LocalDate getCapEndDay() {
        return this.capEndDay;
    }

    public void setCapEndDay(LocalDate capEndDay) {
        this.capEndDay = capEndDay;
    }

    @Deprecated
    public Date getCapMustCompleteDate() {
        return Util.getNewDate(capMustCompleteDate);
    }

    @Deprecated
    public void setCapMustCompleteDate(Date capMustCompleteDate) {
        this.capMustCompleteDate = Util.getNewDate(capMustCompleteDate);
    }

    public LocalDate getCapMustCompleteDay() {
        return this.capMustCompleteDay;
    }

    public void setCapMustCompleteDay(LocalDate capMustCompleteDay) {
        this.capMustCompleteDay = capMustCompleteDay;
    }

    public LocalDate getNonconformityCloseDate() {
        return nonconformityCloseDate;
    }

    public void setNonconformityCloseDate(LocalDate nonconformityCloseDate) {
        this.nonconformityCloseDate = nonconformityCloseDate;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getFindings() {
        return findings;
    }

    public void setFindings(String findings) {
        this.findings = findings;
    }

    public Integer getSitesPassed() {
        return sitesPassed;
    }

    public void setSitesPassed(Integer sitesPassed) {
        this.sitesPassed = sitesPassed;
    }

    public Integer getTotalSites() {
        return totalSites;
    }

    public void setTotalSites(Integer totalSites) {
        this.totalSites = totalSites;
    }

    public String getDeveloperExplanation() {
        return developerExplanation;
    }

    public void setDeveloperExplanation(String developerExplanation) {
        this.developerExplanation = developerExplanation;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public List<SurveillanceNonconformityDocument> getDocuments() {
        return documents;
    }

    public void setDocuments(List<SurveillanceNonconformityDocument> documents) {
        this.documents = documents;
    }

    public Date getLastModifiedDate() {
        return Util.getNewDate(lastModifiedDate);
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = Util.getNewDate(lastModifiedDate);
    }

    @XmlTransient
    public String getNonconformityTypeName() {
        if (getCriterion() == null) {
            return getNonconformityType();
        }
        return Util.formatCriteriaNumber(getCriterion());
    }
}
