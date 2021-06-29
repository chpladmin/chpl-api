package gov.healthit.chpl.dto;

import java.io.Serializable;

import gov.healthit.chpl.entity.TestProcedureEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestProcedureDTO implements Serializable {
    private static final long serialVersionUID = 1794582282532931394L;
    public static final String DEFAULT_TEST_PROCEDURE = "ONC Test Method";

    private Long id;
    private String name;

    public TestProcedureDTO(TestProcedureEntity entity) {
        this.id = entity.getId();
        this.name = entity.getName();
    }
}
