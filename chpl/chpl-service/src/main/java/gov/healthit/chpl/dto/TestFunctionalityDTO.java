package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Objects;

import gov.healthit.chpl.entity.TestFunctionalityEntity;

public class TestFunctionalityDTO implements Serializable {
    private static final long serialVersionUID = -4607291382443032361L;
    private Long id;
    private String name;
    private String number;
    private String year;
    private PracticeTypeDTO practiceType;

    public TestFunctionalityDTO() {
    }

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

    public String getNumber() {
        return number;
    }

    public void setNumber(final String number) {
        this.number = number;
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
    public PracticeTypeDTO getPracticeType() {
        return practiceType;
    }

    /**
     * @param practiceType the practiceType to set
     */
    public void setPracticeType(final PracticeTypeDTO practiceType) {
        this.practiceType = practiceType;
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
