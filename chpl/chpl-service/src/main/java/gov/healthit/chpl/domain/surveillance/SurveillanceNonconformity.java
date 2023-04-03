package gov.healthit.chpl.domain.surveillance;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.healthit.chpl.api.deprecatedUsage.DeprecatedResponseField;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.NonconformityType;
import gov.healthit.chpl.util.LocalDateAdapter;
import gov.healthit.chpl.util.LocalDateDeserializer;
import gov.healthit.chpl.util.LocalDateSerializer;
import gov.healthit.chpl.util.NullSafeEvaluator;
import gov.healthit.chpl.util.Util;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Domain object for Non-conformities related to surveillance.
 */
@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
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
    @Deprecated
    @DeprecatedResponseField(removalDate = "2023-05-01",
        message = "This field is deprecated and will be removed from the response data in a future release. "
                + "Please replace usage of the 'nonconformityType' field with 'type'.")
    private String nonconformityType;

    /**
     * If the non-conformity type is a certified capability
     * then this field will have the criterion details (number, title, etc).
     */
    @Deprecated
    @DeprecatedResponseField(removalDate = "2023-05-01",
    message = "This field is deprecated and will be removed from the response data in a future release. "
            + "Please replace usage of the 'criterion' field with 'type'.")
    private CertificationCriterion criterion;

    /**
     * Type of non-conformity; this is either a certification criteria number or
     * a textual description
     */
    @XmlElement(required = false)
    private NonconformityType type;

    /**
     * The status of a non-conformity found as a result of a surveillance
     * activity. Allowable values are "Open" or "Closed".
     */
    @XmlElement(required = true)
    private String nonconformityStatus;

    /**
     * Date of determination of non-conformity
     */
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @XmlJavaTypeAdapter(value = LocalDateAdapter.class)
    @XmlElement(required = true)
    private LocalDate dateOfDeterminationDay;

    /**
     * Corrective action plan approval day
     */
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @XmlJavaTypeAdapter(value = LocalDateAdapter.class)
    @XmlElement(required = false, nillable = true)
    private LocalDate capApprovalDay;

    /**
     * Corrective action plan start day
     */
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @XmlJavaTypeAdapter(value = LocalDateAdapter.class)
    @XmlElement(required = false, nillable = true)
    private LocalDate capStartDay;

    /**
     * Corrective action plan end day
     */
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @XmlJavaTypeAdapter(value = LocalDateAdapter.class)
    @XmlElement(required = false, nillable = true)
    private LocalDate capEndDay;

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
    private LocalDate nonconformityCloseDay;

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
     * Date of the last modification of the surveillance.
     */
    @XmlElement(required = true)
    private Date lastModifiedDate;

    public boolean matches(SurveillanceNonconformity anotherNonconformity) {
        if (!propertiesMatch(anotherNonconformity)) {
            return false;
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
        if (!Objects.equals(
                NullSafeEvaluator.eval(() -> this.type.getId(), null),
                NullSafeEvaluator.eval(() -> anotherNonconformity.getType().getId(), null))) {
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
        if (this.nonconformityCloseDay == null && anotherNonconformity.nonconformityCloseDay != null
                || this.nonconformityCloseDay != null && anotherNonconformity.nonconformityCloseDay == null) {
            return false;
        } else if (this.nonconformityCloseDay != null && anotherNonconformity.nonconformityCloseDay != null
                && !this.nonconformityCloseDay.equals(anotherNonconformity.nonconformityCloseDay)) {
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

    @Deprecated
    public String getNonconformityType() {
        return nonconformityType;
    }

    @Deprecated
    public void setNonconformityType(String nonconformityType) {
        this.nonconformityType = nonconformityType;
    }

    @Deprecated
    public CertificationCriterion getCriterion() {
        return criterion;
    }

    @Deprecated
    public void setCriterion(CertificationCriterion criterion) {
        this.criterion = criterion;
    }

    public NonconformityType getType() {
        return type;
    }

    public void setType(NonconformityType type) {
        this.type = type;
    }

    public String getNonconformityStatus() {
        return nonconformityStatus;
    }

    public void setNonconformityStatus(String nonconformityStatus) {
        this.nonconformityStatus = nonconformityStatus;
    }

    public LocalDate getDateOfDeterminationDay() {
        return this.dateOfDeterminationDay;
    }

    public void setDateOfDeterminationDay(LocalDate dateOfDeterminationDay) {
        this.dateOfDeterminationDay = dateOfDeterminationDay;
    }

    public LocalDate getCapApprovalDay() {
        return this.capApprovalDay;
    }

    public void setCapApprovalDay(LocalDate capApprovalDay) {
        this.capApprovalDay = capApprovalDay;
    }

    public LocalDate getCapStartDay() {
        return this.capStartDay;
    }

    public void setCapStartDay(LocalDate capStartDay) {
        this.capStartDay = capStartDay;
    }

    public LocalDate getCapEndDay() {
        return this.capEndDay;
    }

    public void setCapEndDay(LocalDate capEndDay) {
        this.capEndDay = capEndDay;
    }

    public LocalDate getCapMustCompleteDay() {
        return this.capMustCompleteDay;
    }

    public void setCapMustCompleteDay(LocalDate capMustCompleteDay) {
        this.capMustCompleteDay = capMustCompleteDay;
    }

    public LocalDate getNonconformityCloseDay() {
        return nonconformityCloseDay;
    }

    public void setNonconformityCloseDay(LocalDate nonconformityCloseDay) {
        this.nonconformityCloseDay = nonconformityCloseDay;
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

    public Date getLastModifiedDate() {
        return Util.getNewDate(lastModifiedDate);
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = Util.getNewDate(lastModifiedDate);
    }
}
