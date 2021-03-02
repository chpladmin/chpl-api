package gov.healthit.chpl.surveillance.report.dto;

import gov.healthit.chpl.surveillance.report.entity.SurveillanceOutcomeEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SurveillanceOutcomeDTO {

    private Long id;
    private String name;

    public SurveillanceOutcomeDTO(SurveillanceOutcomeEntity entity) {
        this.id = entity.getId();
        this.name = entity.getName();
    }
}
