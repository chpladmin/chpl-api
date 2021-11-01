package gov.healthit.chpl.domain;

import java.io.Serializable;

import gov.healthit.chpl.dto.TestStandardDTO;
import gov.healthit.chpl.entity.TestStandardEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TestStandard implements Serializable {
    private static final long serialVersionUID = 620315627813875501L;
    private Long id;
    private String name;
    private String description;
    private String year;

    public TestStandard(TestStandardDTO dto) {
        this.id = dto.getId();
        this.name = dto.getName();
        this.description = dto.getDescription();
        this.year = dto.getYear();
    }

    public TestStandard(TestStandardEntity entity) {
        this.id = entity.getId();
        this.name = entity.getName();
        this.description = entity.getDescription();
        this.year = entity.getCertificationEdition().getYear();
    }
}
