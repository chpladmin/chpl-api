package gov.healthit.chpl.attestation.domain;

import java.util.List;
import java.util.stream.Collectors;

import gov.healthit.chpl.attestation.entity.DeveloperAttestationEntity;
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
public class DeveloperAttestation {
    private Long id;
    private Developer developer;
    private AttestationPeriod period;
    private List<AttestationResponse> responses;

    public DeveloperAttestation(DeveloperAttestationEntity entity) {
        this.id = entity.getId();
        this.developer = new Developer(new DeveloperDTO(entity.getDeveloper()));
        this.period = new AttestationPeriod(entity.getPeriod());
        this.responses = entity.getResponses().stream()
                .map(ent -> AttestationResponse.builder()
                        .id(ent.getId())
                        .answer(new AttestationAnswer(ent.getAnswer()))
                        .question(new AttestationQuestion(ent.getQuestion()))
                        .build())
                .collect(Collectors.toList());
    }
}
