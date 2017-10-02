package gov.healthit.chpl.domain;

import java.io.Serializable;

import gov.healthit.chpl.dto.TestFunctionalityDTO;

public class TestFunctionality implements Serializable {
    private static final long serialVersionUID = 620315627813874301L;
    private Long id;
    private String name;
    private String description;
    private String year;

    public TestFunctionality() {
    }

    public TestFunctionality(TestFunctionalityDTO dto) {
        this.id = dto.getId();
        this.name = dto.getNumber();
        this.description = dto.getName();
        this.year = dto.getYear();
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

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getYear() {
        return year;
    }

    public void setYear(final String year) {
        this.year = year;
    }
}
