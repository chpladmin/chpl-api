package gov.healthit.chpl.dto;

import java.io.Serializable;

import gov.healthit.chpl.entity.TestStandardEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class TestStandardDTO implements Serializable {
    private static final long serialVersionUID = -7473233688407477963L;
    private Long id;
    private String description;
    private String name;
    private Long certificationEditionId;
    private String year;

    public TestStandardDTO(TestStandardEntity entity) {
        this.id = entity.getId();
        this.description = entity.getDescription();
        this.name = entity.getName();
        this.certificationEditionId = entity.getCertificationEditionId();
        if (entity.getCertificationEdition() != null) {
            this.year = entity.getCertificationEdition().getYear();
        }
    }
}
