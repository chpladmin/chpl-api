package gov.healthit.chpl.dto;

import java.io.Serializable;

import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.entity.listing.CertificationResultTestTaskEntity;
import lombok.Data;

@Data
public class CertificationResultTestTaskDTO implements Serializable {
    private static final long serialVersionUID = -2963883181763817735L;
    private Long id;
    private Long certificationResultId;
    private Long testTaskId;
    private TestTask testTask;

    public CertificationResultTestTaskDTO() {
        this.testTask = new TestTask();
    }

    public CertificationResultTestTaskDTO(CertificationResultTestTaskEntity entity) {
        this();
        this.id = entity.getId();
        this.certificationResultId = entity.getCertificationResultId();
        this.testTaskId = entity.getTestTaskId();
        if (entity.getTestTask() != null) {
            this.testTask = entity.getTestTask().toDomain();
        }
    }
}
