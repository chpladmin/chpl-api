package gov.healthit.chpl.standard;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlAccessorOrder(value = XmlAccessOrder.ALPHABETICAL)
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class CertificationResultStandard implements Serializable {
    private static final long serialVersionUID = 418146022721549464L;
    /**
     * Standard to certification result mapping internal ID
     */
    @Schema(description = "Standard to certification result mapping internal ID")
    @XmlElement(required = true)
    private Long id;

    /**
     * Standard
     */
    @Schema(description = "Standard")
    @XmlElement(required = true)
    private Standard standard;

    @XmlTransient
    @JsonIgnore
    private Long certificationResultId;

    public boolean matches(CertificationResultStandard anotherStandard) {
        boolean result = false;
        if (this.getStandard().getId() != null && anotherStandard.getStandard().getId() != null
                && this.getStandard().getId().longValue() == anotherStandard.getStandard().getId().longValue()) {
            result = true;
        } else if (!StringUtils.isEmpty(this.getStandard().getRegulatoryTextCitation())
                    && !StringUtils.isEmpty(anotherStandard.getStandard().getRegulatoryTextCitation())
                && this.getStandard().getRegulatoryTextCitation().equalsIgnoreCase(anotherStandard.getStandard().getRegulatoryTextCitation())) {
            result = true;
        }
        return result;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getCertificationResultId() {
        return this.certificationResultId;
    }

    public void setCertificationResultId(Long certificationResultId) {
        this.certificationResultId = certificationResultId;
    }

    public Standard getStandard() {
        return standard;
    }

    public void setStandard(Standard standard) {
        this.standard = standard;
    }

}
