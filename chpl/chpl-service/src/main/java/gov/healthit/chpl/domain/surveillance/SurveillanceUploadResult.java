package gov.healthit.chpl.domain.surveillance;

import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.domain.schedule.ChplOneTimeTrigger;
import lombok.Data;

@Data
public class SurveillanceUploadResult {
    private List<Surveillance> surveillances = new ArrayList<Surveillance>();
    private ChplOneTimeTrigger trigger;
}
