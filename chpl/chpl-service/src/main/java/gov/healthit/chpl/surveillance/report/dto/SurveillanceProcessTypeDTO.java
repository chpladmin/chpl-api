package gov.healthit.chpl.surveillance.report.dto;

import gov.healthit.chpl.entity.surveillance.report.SurveillanceProcessTypeEntity;
import lombok.Data;

@Data
public class SurveillanceProcessTypeDTO {

    private Long id;
    private String name;

    public SurveillanceProcessTypeDTO() {}

    public SurveillanceProcessTypeDTO(SurveillanceProcessTypeEntity entity) {
        this.id = entity.getId();
        this.name = entity.getName();
    }
}
