package gov.healthit.chpl.surveillance.report.dto;

import gov.healthit.chpl.surveillance.report.entity.SurveillanceProcessTypeEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SurveillanceProcessTypeDTO {

    private Long id;
    private String name;

    public SurveillanceProcessTypeDTO(SurveillanceProcessTypeEntity entity) {
        this.id = entity.getId();
        this.name = entity.getName();
    }
}
