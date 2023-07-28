package gov.healthit.chpl.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;

@Deprecated
@AllArgsConstructor
@Builder
@ToString
@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
public class CertificationEdition implements Serializable {
    private static final long serialVersionUID = 5732322243572571895L;
    public static final String CURES_SUFFIX = " Cures Update";

    @Deprecated
    @XmlElement(required = false, nillable = true)
    private Long certificationEditionId;

    @Deprecated
    @XmlElement(required = false, nillable = true)
    private String year;

    @Deprecated
    @XmlElement(required = true)
    private boolean retired;

    public CertificationEdition() {
    }

    @Deprecated
    public Long getCertificationEditionId() {
        return certificationEditionId;
    }

    @Deprecated
    public void setCertificationEditionId(final Long certificationEditionId) {
        this.certificationEditionId = certificationEditionId;
    }

    @Deprecated
    public String getYear() {
        return year;
    }

    @Deprecated
    public void setYear(final String year) {
        this.year = year;
    }

    @Deprecated
    public boolean isRetired() {
        return retired;
    }

    @Deprecated
    public void setRetired(final boolean retired) {
        this.retired = retired;
    }
}
