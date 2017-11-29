package gov.healthit.chpl.dto;

import java.io.Serializable;

import gov.healthit.chpl.entity.TestDataEntity;

public class TestDataDTO implements Serializable {
    private static final long serialVersionUID = 1794585582532931394L;
    public static final String DEFALUT_TEST_DATA = "ONC Test Method";
    private Long id;
    private String name;

    public TestDataDTO() {
    }

    public TestDataDTO(TestDataEntity entity) {
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
