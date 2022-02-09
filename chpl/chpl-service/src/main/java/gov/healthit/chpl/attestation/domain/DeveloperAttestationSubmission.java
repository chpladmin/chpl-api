package gov.healthit.chpl.attestation.domain;

import java.util.List;
import java.util.stream.Collectors;

import gov.healthit.chpl.attestation.entity.DeveloperAttestationSubmissionEntity;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.dto.DeveloperDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeveloperAttestationSubmission {
    private Long id;
    private Developer developer;
    private AttestationPeriod period;
    private List<AttestationSubmittedResponse> responses;
    private String signature;
    private String signatureEmail;

    public DeveloperAttestationSubmission(DeveloperAttestationSubmissionEntity entity) {
        this.id = entity.getId();
        this.developer = new Developer(new DeveloperDTO(entity.getDeveloper()));
        this.period = new AttestationPeriod(entity.getPeriod());
        this.signature = entity.getSignature();
        this.signatureEmail = entity.getSignatureEmail();
        this.responses = entity.getResponses().stream()
                .map(ent -> AttestationSubmittedResponse.builder()
                        .id(ent.getId())
                        .attestation(new Attestation(ent.getAttestation()))
                        .response(new AttestationValidResponse(ent.getValidResponse()))
                        .build())
                .collect(Collectors.toList());
    }
}
