package gov.healthit.chpl.dto;

import java.io.Serializable;

import gov.healthit.chpl.entity.TestDataEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class TestDataDTO implements Serializable {
    private static final long serialVersionUID = 1794585582532931394L;
    public static final String DEFALUT_TEST_DATA = "ONC Test Method";
    private Long id;
    private String name;

    public TestDataDTO(TestDataEntity entity) {
        this.id = entity.getId();
        this.name = entity.getName();
    }
}
