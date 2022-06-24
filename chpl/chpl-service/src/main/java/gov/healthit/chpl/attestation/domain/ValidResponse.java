package gov.healthit.chpl.attestation.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.attestation.entity.ValidResponseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ValidResponse {
    private Long id;
    private String response;
    private String meaning;
    private Long sortOrder;

    public ValidResponse(ValidResponseEntity entity) {
        this.id = entity.getId();
        this.response = entity.getResponse();
        this.meaning = entity.getMeaning();
        this.sortOrder = entity.getSortOrder();
    }
}
