package gov.healthit.chpl.surveillance.report.dto;

import gov.healthit.chpl.entity.surveillance.report.SurveillanceOutcomeEntity;

public class SurveillanceOutcomeDTO {

    private Long id;
    private String name;

    public SurveillanceOutcomeDTO() {}

    public SurveillanceOutcomeDTO(final SurveillanceOutcomeEntity entity) {
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

    public void setName(final String name) {
        this.name = name;
    }
}
