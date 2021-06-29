package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Objects;

import gov.healthit.chpl.entity.TestFunctionalityEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestFunctionalityDTO implements Serializable {
    private static final long serialVersionUID = -4607291382443032361L;
    private Long id;
    private String name;
    private String number;
    private String year;
    private PracticeTypeDTO practiceType;

    public TestFunctionalityDTO(TestFunctionalityEntity entity) {
        this.id = entity.getId();
        this.name = entity.getName();
        this.number = entity.getNumber();
        if (entity.getCertificationEdition() != null) {
            this.year = entity.getCertificationEdition().getYear();
        }
        if (entity.getPracticeType() != null) {
            this.setPracticeType(new PracticeTypeDTO(entity.getPracticeType()));
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (getClass() != o.getClass()) {
            return false;
        }
        TestFunctionalityDTO dto = (TestFunctionalityDTO) o;
        // field comparison
        return Objects.equals(id, dto.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }
}
