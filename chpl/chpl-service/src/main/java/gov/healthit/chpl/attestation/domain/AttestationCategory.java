package gov.healthit.chpl.attestation.domain;

import gov.healthit.chpl.attestation.entity.AttestationCategoryEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttestationCategory {
    private Long id;
    private String name;
    private Long sortOrder;

    public AttestationCategory(AttestationCategoryEntity entity) {
        this.id = entity.getId();
        this.name = entity.getName();
        this.sortOrder = entity.getSortOrder();
    }
}
