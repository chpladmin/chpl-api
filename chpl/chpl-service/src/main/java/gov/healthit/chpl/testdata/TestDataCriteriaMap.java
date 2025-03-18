package gov.healthit.chpl.testdata;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import lombok.Data;

@Data
@Deprecated
public class TestDataCriteriaMap implements Serializable {
    private static final long serialVersionUID = -1863384619196377463L;
    private Long id;
    private Long criteriaId;
    private CertificationCriterion criteria;
    private Long testDataId;
    private TestData testData;
    private Date creationDate;
    private Boolean deleted;
    private Date lastModifiedDate;
    private Long lastModifiedUser;

    public TestDataCriteriaMap() {
    }

    public TestDataCriteriaMap(TestDataCriteriaMapEntity entity) {
        this();

        this.id = entity.getId();
        this.criteriaId = entity.getCertificationCriterionId();
        if (entity.getCertificationCriterion() != null) {
            this.criteria = entity.getCertificationCriterion().toDomain();
        }
        this.testDataId = entity.getTestDataId();
        if (entity.getTestData() != null) {
            this.testData = entity.getTestData().toDomain();
        }

        this.creationDate = entity.getCreationDate();
        this.deleted = entity.getDeleted();
        this.lastModifiedDate = entity.getLastModifiedDate();
        this.lastModifiedUser = entity.getLastModifiedUser();
    }
}
