package gov.healthit.chpl.dto;

import java.io.Serializable;

import gov.healthit.chpl.entity.listing.CertificationResultTestDataEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CertificationResultTestDataDTO implements Serializable {
    private static final long serialVersionUID = -8409772564902652781L;
    private Long id;
    private Long certificationResultId;
    private Long testDataId;
    private TestDataDTO testData;
    private String version;
    private String alteration;
    private Boolean deleted;

    public CertificationResultTestDataDTO(CertificationResultTestDataEntity entity) {
        this.id = entity.getId();
        this.certificationResultId = entity.getCertificationResultId();
        this.testDataId = entity.getTestDataId();
        if (entity.getTestData() != null) {
            this.testData = new TestDataDTO(entity.getTestData());
        }
        this.version = entity.getTestDataVersion();
        this.alteration = entity.getAlterationDescription();
        this.deleted = entity.getDeleted();
    }
}
