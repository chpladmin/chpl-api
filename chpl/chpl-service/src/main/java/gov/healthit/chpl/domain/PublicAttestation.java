package gov.healthit.chpl.domain;

import java.io.Serializable;

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
import lombok.NoArgsConstructor;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicAttestation implements Serializable {
    private static final long serialVersionUID = -5574794434517251480L;

    /**
     * Identifier used to reference the Attestations status for a given
     * Developer during the associated Attestations period.
     */
    @XmlElement(required = true)
    private Long id;

    /**
     * The period for which the Attestations status is valid.
     */
    @XmlElement(required = true)
    private AttestationPeriod attestationPeriod;

    /**
     * A status of 'Attestations submitted' indicates that the Attestations for the selected
     * Developer have been submitted to the Office of the National Coordinator.
     */
    @XmlElement(required = true)
    private PublicAttestationStatus status;

    @XmlTransient
    public String getStatusText() {
        return status.getName();
    }
}
