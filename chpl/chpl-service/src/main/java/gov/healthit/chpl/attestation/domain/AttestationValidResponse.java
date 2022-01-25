package gov.healthit.chpl.attestation.domain;

import gov.healthit.chpl.attestation.entity.AttestationValidResponseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
