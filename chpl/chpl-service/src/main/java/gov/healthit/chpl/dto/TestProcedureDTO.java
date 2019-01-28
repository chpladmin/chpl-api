package gov.healthit.chpl.dto;

import java.io.Serializable;

import gov.healthit.chpl.entity.TestProcedureEntity;

public class TestProcedureDTO implements Serializable {
    private static final long serialVersionUID = 1794582282532931394L;
    public static final String DEFAULT_TEST_PROCEDURE = "ONC Test Method";

    private Long id;
    private String name;

    public TestProcedureDTO() {
    }

    public TestProcedureDTO(TestProcedureEntity entity) {
        this.id = entity.getId();
        this.name = entity.getName();
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
