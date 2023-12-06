package gov.healthit.chpl.domain;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.attestation.domain.AttestationPeriod;
import gov.healthit.chpl.domain.concept.PublicAttestationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@AllArgsConstructor
public class PublicAttestation implements Serializable {
    private static final long serialVersionUID = -5574794434517251480L;

    @Schema(description = "Identifier used to reference the Attestations status for a given "
            + "Developer during the associated Attestations period.")
    private Long id;

    @Schema(description = "The period for which the Attestations status is valid.")
    private AttestationPeriod attestationPeriod;

    @Schema(description = "A status of 'Attestations submitted' indicates that the Attestations for the selected "
            + "Developer have been submitted to the Office of the National Coordinator.")
    private PublicAttestationStatus status;

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
