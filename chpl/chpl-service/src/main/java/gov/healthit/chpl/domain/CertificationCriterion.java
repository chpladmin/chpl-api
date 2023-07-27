package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.api.deprecatedUsage.DeprecatedResponseField;
import jakarta.xml.bind.annotation.XmlTransient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@AllArgsConstructor
@ToString
public class CertificationCriterion implements Serializable {
    private static final long serialVersionUID = 5732322243572571895L;

    @XmlElement(required = false, nillable = true)
    private Long id;

    @XmlElement(required = true)
    private String number;

    @XmlElement(required = false, nillable = true)
    private String title;

    @XmlTransient
    @Deprecated
    @DeprecatedResponseField(message = "The certification edition will be removed.", removalDate = "2024-02-01")
    private Long certificationEditionId;

    @XmlTransient
    @Deprecated
    @DeprecatedResponseField(message = "The certification edition will be removed.", removalDate = "2024-02-01")
    private String certificationEdition;

    @XmlElement(required = false, nillable = true)
    private String description;

    @XmlElement(required = true, nillable = false)
    private Boolean removed;

    public CertificationCriterion() {
    }

    @Deprecated
    public String getCertificationEdition() {
        return certificationEdition;
    }

    @Deprecated
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

    @Override
    public int hashCode() {
        return Objects.hash(certificationEdition, certificationEditionId, description, id, number, removed, title);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        CertificationCriterion other = (CertificationCriterion) obj;
        return Objects.equals(certificationEdition, other.certificationEdition)
                && Objects.equals(certificationEditionId, other.certificationEditionId)
                && Objects.equals(description, other.description) && Objects.equals(id, other.id)
                && Objects.equals(number, other.number) && Objects.equals(removed, other.removed)
                && Objects.equals(title, other.title);
    }
}
