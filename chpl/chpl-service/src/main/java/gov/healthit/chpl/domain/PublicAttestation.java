package gov.healthit.chpl.domain;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.attestation.domain.AttestationPeriod;
import gov.healthit.chpl.domain.concept.PublicAttestationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@Data
@NoArgsConstructor
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
}
