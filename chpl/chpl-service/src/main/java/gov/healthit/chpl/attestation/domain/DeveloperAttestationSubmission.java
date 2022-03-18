package gov.healthit.chpl.attestation.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.healthit.chpl.attestation.entity.DeveloperAttestationSubmissionEntity;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.util.DateUtil;
import gov.healthit.chpl.util.LocalDateDeserializer;
import gov.healthit.chpl.util.LocalDateSerializer;
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

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate datePublished;

    public DeveloperAttestationSubmission(DeveloperAttestationSubmissionEntity entity) {
        this.id = entity.getId();
        this.developer = entity.getDeveloper().toDomain();
        this.period = new AttestationPeriod(entity.getPeriod());
        this.signature = entity.getSignature();
        this.signatureEmail = entity.getSignatureEmail();
        this.datePublished = DateUtil.toLocalDate(entity.getLastModifiedDate().getTime());
        this.responses = entity.getResponses().stream()
                .map(ent -> AttestationSubmittedResponse.builder()
                        .id(ent.getId())
                        .attestation(new Attestation(ent.getAttestation()))
                        .response(new AttestationValidResponse(ent.getValidResponse()))
                        .build())
                .collect(Collectors.toList());
    }
}
