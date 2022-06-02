package gov.healthit.chpl.attestation.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.attestation.entity.AttestationValidResponseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AttestationValidResponse {
    private Long id;
    private String response;
    private String meaning;
    private Long sortOrder;

    public AttestationValidResponse(AttestationValidResponseEntity entity) {
        this.id = entity.getId();
        this.response = entity.getResponse();
        this.meaning = entity.getMeaning();
        this.sortOrder = entity.getSortOrder();
    }
}
