package gov.healthit.chpl.domain.surveillance;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import gov.healthit.chpl.domain.CertificationEdition;
import gov.healthit.chpl.util.NullSafeEvaluator;
import gov.healthit.chpl.util.Util;
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
    private Boolean removed;

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Boolean getRemoved() {
        return removed;
    }

    public void setRemoved(Boolean removed) {
        this.removed = removed;
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
