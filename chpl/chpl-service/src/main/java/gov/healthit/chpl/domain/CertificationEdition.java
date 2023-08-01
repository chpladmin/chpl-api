package gov.healthit.chpl.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * Domain object for Certification Edition.
 * @author alarned
 *
 */
@AllArgsConstructor
@Builder
@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
public class CertificationEdition implements Serializable {
    private static final long serialVersionUID = 5732322243572571895L;
    public static final String CURES_SUFFIX = " Cures Update";

    @XmlElement(required = false, nillable = true)
    private Long certificationEditionId;

    @XmlElement(required = false, nillable = true)
    private String year;

    @XmlElement(required = true)
    private boolean retired;

    public CertificationEdition() {
    }

    public Long getCertificationEditionId() {
        return certificationEditionId;
    }

    public void setCertificationEditionId(final Long certificationEditionId) {
        this.certificationEditionId = certificationEditionId;
    }

    public String getYear() {
        return year;
    }

    public void setYear(final String year) {
        this.year = year;
    }

    public boolean isRetired() {
        return retired;
    }

    public void setRetired(final boolean retired) {
        this.retired = retired;
    }
}
