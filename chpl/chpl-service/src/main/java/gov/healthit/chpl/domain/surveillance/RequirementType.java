package gov.healthit.chpl.domain.surveillance;

import java.time.LocalDate;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.healthit.chpl.certificationCriteria.CriterionStatus;
import gov.healthit.chpl.domain.CertificationEdition;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.util.CriterionStatusAdapter;
import gov.healthit.chpl.util.LocalDateAdapter;
import gov.healthit.chpl.util.LocalDateDeserializer;
import gov.healthit.chpl.util.LocalDateSerializer;
import gov.healthit.chpl.util.NullSafeEvaluator;
import gov.healthit.chpl.util.Util;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
public class RequirementType {
    private Long id;
    private String number;
    private String title;

    /**
     * A date value representing the date by which the Requirement Type became available.
     */
    @Schema(description = "A date value representing the date by which the Requirement Type became available.")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @XmlElement(required = false, nillable = true)
    @XmlJavaTypeAdapter(value = LocalDateAdapter.class)
    private LocalDate startDay;

    /**
     * A date value representing the date by which the Requirement Type can no longer be used.
     */
    @Schema(description = "A date value representing the date by which the Requirement Type can no longer be used.")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @XmlElement(required = false, nillable = true)
    @XmlJavaTypeAdapter(value = LocalDateAdapter.class)
    private LocalDate endDay;

    @XmlTransient
    private CertificationEdition certificationEdition;

    @JsonIgnore
    private String edition;

    private RequirementGroupType requirementGroupType;

    public String getEdition() {
        return NullSafeEvaluator.eval(() -> certificationEdition.getName(), null);
    }

    @JsonIgnore
    public String getFormattedTitle() {
        if (StringUtils.isNotEmpty(number)) {
            return Util.formatCriteriaNumber(this);
        } else {
            return title;
        }
    }

    @JsonIgnore
    public String getFormattedTitleForReport() {
        String formattedTitleForReport = "";
        if (StringUtils.isNotEmpty(number)) {
            CriterionStatus status = getStatus();
            if (status != null && status.equals(CriterionStatus.REMOVED)) {
                formattedTitleForReport = "Removed | ";
            }
            formattedTitleForReport += Util.formatCriteriaNumber(this);
        } else {
            formattedTitleForReport = title;
        }
        return formattedTitleForReport;
    }

    @JsonProperty(access = Access.READ_ONLY)
    @XmlElement(required = true, nillable = false)
    @XmlJavaTypeAdapter(value = CriterionStatusAdapter.class)
    public CriterionStatus getStatus() {
        if (certificationEdition != null && certificationEdition.getName() != null
                && (certificationEdition.getName().equals(CertificationEditionConcept.CERTIFICATION_EDITION_2011.getYear())
                        || certificationEdition.getName().equals(CertificationEditionConcept.CERTIFICATION_EDITION_2014.getYear()))) {
            return CriterionStatus.RETIRED;
        } else {
            LocalDate end = endDay != null ? endDay : LocalDate.MAX;
            if (end.isBefore(LocalDate.now())) {
                return CriterionStatus.REMOVED;
            }
            return CriterionStatus.ACTIVE;
        }
    }

    @JsonProperty(access = Access.READ_ONLY)
    @XmlTransient
    public Boolean isRemoved() {
        return getStatus().equals(CriterionStatus.REMOVED);
    }

    public LocalDate getEndDay() {
        return endDay;
    }

    public void setEndDay(LocalDate endDay) {
        this.endDay = endDay;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public LocalDate getStartDay() {
        return startDay;
    }

    public void setStartDay(LocalDate startDay) {
        this.startDay = startDay;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public CertificationEdition getCertificationEdition() {
        return certificationEdition;
    }

    public void setCertificationEdition(CertificationEdition certificationEdition) {
        this.certificationEdition = certificationEdition;
    }

    public RequirementGroupType getRequirementGroupType() {
        return requirementGroupType;
    }

    public void setRequirementGroupType(RequirementGroupType requirementGroupType) {
        this.requirementGroupType = requirementGroupType;
    }

    public void setEdition(String edition) {
        this.edition = edition;
    }
}
