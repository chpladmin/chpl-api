package gov.healthit.chpl.dto.questionableActivity;

import gov.healthit.chpl.dto.CertificationResultDetailsDTO;
import gov.healthit.chpl.entity.questionableActivity.QuestionableActivityCertificationResultEntity;

public class QuestionableActivityCertificationResultDTO extends QuestionableActivityDTO {
    private Long certResultId;
    private CertificationResultDetailsDTO certResult;
    
    public QuestionableActivityCertificationResultDTO() {
        super();
    }
    
    public QuestionableActivityCertificationResultDTO(QuestionableActivityCertificationResultEntity entity) {
        super(entity);
        this.certResultId = entity.getCertResultId();
        if(entity.getCertResult() != null) {
            this.certResult = new CertificationResultDetailsDTO(entity.getCertResult());
        }
    }
    
    public Class<?> getActivityObjectClass() {
        return CertificationResultDetailsDTO.class;
    }

    public Long getCertResultId() {
        return certResultId;
    }

    public void setCertResultId(Long certResultId) {
        this.certResultId = certResultId;
    }

    public CertificationResultDetailsDTO getCertResult() {
        return certResult;
    }

    public void setCertResult(CertificationResultDetailsDTO certResult) {
        this.certResult = certResult;
    }  
}
