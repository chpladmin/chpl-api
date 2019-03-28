package gov.healthit.chpl.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.dto.TestFunctionalityDTO;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TestFunctionality implements Serializable {
    private static final long serialVersionUID = 620315627813874301L;
    private Long id;
    private String name;
    private String description;
    private String year;
    private PracticeType practiceType;

    public TestFunctionality() {
    }

    public TestFunctionality(TestFunctionalityDTO dto) {
        this.id = dto.getId();
        this.name = dto.getNumber();
        this.description = dto.getName();
        this.year = dto.getYear();
        if (dto.getPracticeType() != null) {
            this.practiceType = new PracticeType(dto.getPracticeType());
        }
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

    /**
     * @return the practiceType
     */
    public PracticeType getPracticeType() {
        return practiceType;
    }

    /**
     * @param practiceType the practiceType to set
     */
    public void setPracticeType(PracticeType practiceType) {
        this.practiceType = practiceType;
    }
}
