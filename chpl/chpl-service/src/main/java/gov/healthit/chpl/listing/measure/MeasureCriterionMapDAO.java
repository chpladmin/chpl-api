package gov.healthit.chpl.listing.measure;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;

@Repository
public class MeasureCriterionMapDAO extends BaseDAOImpl {
    public MeasureCriterionMapEntity create(Long certificationCriterionId, Long measureId, Long lastUpdateUserId) {
        MeasureCriterionMapEntity entity = new MeasureCriterionMapEntity();
        entity.setCertificationCriterionId(certificationCriterionId);
        entity.setMeasureId(measureId);
        entity.setLastModifiedUser(lastUpdateUserId);
        entity.setDeleted(false);
        super.create(entity);
        return entity;
    }
}
