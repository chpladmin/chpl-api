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
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@AllArgsConstructor
public class PublicAttestation implements Serializable {
    private static final long serialVersionUID = -5574794434517251480L;

    /**
     * Identifier used to reference the Attestations status for a given
     * Developer during the associated Attestations period.
     */
    @Schema(description = "Identifier used to reference the Attestations status for a given "
            + "Developer during the associated Attestations period.")
    @XmlElement
    private Long id;

    /**
     * The period for which the Attestations status is valid.
     */
    @Schema(description = "The period for which the Attestations status is valid.")
    @XmlElement(required = true)
    private AttestationPeriod attestationPeriod;

    /**
     * A status of 'Attestations submitted' indicates that the Attestations for the selected
     * Developer have been submitted to the Office of the National Coordinator.
     */
    @Schema(description = "A status of 'Attestations submitted' indicates that the Attestations for the selected "
            + "Developer have been submitted to the Office of the National Coordinator.")
    @XmlElement(required = true)
    private PublicAttestationStatus status;

    @XmlTransient
    public String getStatusText() {
        return status.getName();
    }

    public PublicAttestation() {
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AttestationPeriod getAttestationPeriod() {
        return attestationPeriod;
    }

    public void setAttestationPeriod(AttestationPeriod attestationPeriod) {
        this.attestationPeriod = attestationPeriod;
    }

    public PublicAttestationStatus getStatus() {
        return status;
    }

    public void setStatus(PublicAttestationStatus status) {
        this.status = status;
    }
}
