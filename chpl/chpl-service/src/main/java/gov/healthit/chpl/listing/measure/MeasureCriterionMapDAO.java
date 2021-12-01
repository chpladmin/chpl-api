package gov.healthit.chpl.listing.measure;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;

@Repository
public class MeasureCriterionMapDAO extends BaseDAOImpl {
    public MeasureCriterionMapEntity create(Long certificationCriterionId, Long measureId, Long lastUpdateUserId) {
        MeasureCriterionMapEntity entity = MeasureCriterionMapEntity.builder()
                .certificationCriterionId(certificationCriterionId)
                .measureId(measureId)
                .lastModifiedUser(lastUpdateUserId)
                .build();
        super.create(entity);
        return entity;
    }
}
