package gov.healthit.chpl.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.attestation.domain.AttestationPeriod;
import gov.healthit.chpl.domain.concept.PublicAttestationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
public class PublicAttestation {

    /**
     * The internal ID of the attestation
     */
    @XmlElement(required = true)
    private Long id;

    /**
     * The period of time to which the attestation applies.
     */
    @XmlElement(required = true)
    private AttestationPeriod attestationPeriod;

    /**
     * The attestation status.
     */
    @XmlElement(required = true)
    private PublicAttestationStatus status;

    @XmlTransient
    public String getStatusText() {
        return status.getName();
    }
}
