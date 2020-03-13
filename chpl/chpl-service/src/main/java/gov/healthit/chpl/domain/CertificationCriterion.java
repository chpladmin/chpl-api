package gov.healthit.chpl.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.dto.CertificationCriterionDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@AllArgsConstructor
public class CertificationCriterion implements Serializable {
    private static final long serialVersionUID = 5732322243572571895L;
    private static final String CURES_TITLE = "Cures Update";
    private static final String CURES_SUFFIX = " (Cures Update)";

    @XmlElement(required = false, nillable = true)
    private Long id;

    @XmlElement(required = true)
    private String number;

    @XmlElement(required = false, nillable = true)
    private String title;

    @XmlElement(required = false, nillable = true)
    private Long certificationEditionId;

    @XmlElement(required = false, nillable = true)
    private String certificationEdition;

    @XmlElement(required = false, nillable = true)
    private String description;

    @XmlElement(required = true, nillable = false)
    private Boolean removed;

    public CertificationCriterion() {
    }

    public CertificationCriterion(final CertificationCriterionDTO dto) {
        this.id = dto.getId();
        this.certificationEditionId = dto.getCertificationEditionId();
        this.certificationEdition = dto.getCertificationEdition();
        this.description = dto.getDescription();
        this.number = dto.getNumber();
        this.title = dto.getTitle();
        this.removed = dto.getRemoved();
    }

    public String getCertificationEdition() {
        return certificationEdition;
    }

    public void setCertificationEdition(final String certificationEdition) {
        this.certificationEdition = certificationEdition;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(final String number) {
        this.number = number;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public Long getCertificationEditionId() {
        return certificationEditionId;
    }

    public void setCertificationEditionId(final Long certificationEditionId) {
        this.certificationEditionId = certificationEditionId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public Boolean getRemoved() {
        return removed;
    }

    public void setRemoved(final Boolean removed) {
        this.removed = removed;
    }

    public String formatCriteriaNumber() {
        String result = getNumber();
        if (getTitle() != null && getTitle().contains(CURES_TITLE)) {
            result += CURES_SUFFIX;
        }
        return result;
    }
}
