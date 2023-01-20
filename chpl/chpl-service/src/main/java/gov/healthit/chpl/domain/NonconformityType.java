package gov.healthit.chpl.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import gov.healthit.chpl.domain.surveillance.NonconformityClassification;
import gov.healthit.chpl.util.NullSafeEvaluator;
import gov.healthit.chpl.util.Util;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
public class NonconformityType implements Serializable {

    private static final long serialVersionUID = -7437221753188417890L;

    private Long id;

    @XmlTransient
    private CertificationEdition certificationEdition;

    @JsonIgnore
    private String edition;

    private String number;
    private String title;
    private Boolean removed;

    @JsonIgnore
    @XmlTransient
    private NonconformityClassification classification;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setCertificationEdition(CertificationEdition certificationEdition) {
        this.certificationEdition = certificationEdition;
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

    public NonconformityClassification getClassification() {
        return classification;
    }

    public void setClassification(NonconformityClassification classification) {
        this.classification = classification;
    }

    public void setEdition(String edition) {
        this.edition = edition;
    }

    @JsonIgnore
    public String getEdition() {
        return NullSafeEvaluator.eval(() -> certificationEdition.getYear(), null);
    }

    @JsonIgnore
    public String getFormattedTitle() {
        if (StringUtils.isNotEmpty(number)) {
            return Util.formatCriteriaNumber(this);
        } else {
            return title;
        }
    }
}
