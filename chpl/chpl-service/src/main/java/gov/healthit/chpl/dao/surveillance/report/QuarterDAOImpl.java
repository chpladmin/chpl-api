package gov.healthit.chpl.dao.surveillance.report;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.dto.surveillance.report.QuarterDTO;
import gov.healthit.chpl.entity.surveillance.report.QuarterEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;

public class QuarterDAOImpl extends BaseDAOImpl implements QuarterDAO {

    @Override
    public QuarterDTO getById(final Long id) throws EntityRetrievalException {
        QuarterEntity entity = entityManager.find(QuarterEntity.class, id);
        if (entity == null) {
            throw new EntityRetrievalException("No quarter exists with database ID " + id);
        }
        return new QuarterDTO(entity);
    }
}
