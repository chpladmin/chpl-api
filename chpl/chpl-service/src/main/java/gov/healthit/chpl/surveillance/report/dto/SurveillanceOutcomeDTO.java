package gov.healthit.chpl.surveillance.report.dto;

import gov.healthit.chpl.entity.surveillance.report.SurveillanceOutcomeEntity;
import lombok.Data;

@Data
public class SurveillanceOutcomeDTO {

    private Long id;
    private String name;

    public SurveillanceOutcomeDTO() {}

    public SurveillanceOutcomeDTO(SurveillanceOutcomeEntity entity) {
        this.id = entity.getId();
        this.name = entity.getName();
    }
}
