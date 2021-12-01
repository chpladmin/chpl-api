package gov.healthit.chpl.listing.measure;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;

@Repository
public class LegacyMacraMeasureCriterionMapDAO extends BaseDAOImpl {
    public LegacyMacraMeasureCriterionMapEntity create(LegacyMacraMeasureCriterionMapEntity entity) {
        super.create(entity);
        return entity;
    }
}
