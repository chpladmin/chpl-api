package gov.healthit.chpl.dto;

import java.io.Serializable;

import gov.healthit.chpl.entity.AgeRangeEntity;

public class AgeRangeDTO implements Serializable {
    private static final long serialVersionUID = -8992186632969057189L;
    private Long id;
    private String age;

    public AgeRangeDTO() {
    }

    public AgeRangeDTO(AgeRangeEntity entity) {
        this.id = entity.getId();
        this.age = entity.getAge();
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getAge() {
        return age;
    }

    public void setAge(final String age) {
        this.age = age;
    }
}
