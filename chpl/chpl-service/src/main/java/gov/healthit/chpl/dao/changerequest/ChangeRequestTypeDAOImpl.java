package gov.healthit.chpl.dao.changerequest;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.changerequest.ChangeRequestConverter;
import gov.healthit.chpl.domain.changerequest.ChangeRequestType;
import gov.healthit.chpl.entity.changerequest.ChangeRequestTypeEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;

@Repository("changeRequestTypeDAO")
public class ChangeRequestTypeDAOImpl extends BaseDAOImpl {

    public ChangeRequestType getChangeRequestTypeById(Long changeRequestTypeId)
            throws EntityRetrievalException {
        return ChangeRequestConverter.convert(getChangeRequestTypeEntity(changeRequestTypeId));

    }

    public List<ChangeRequestType> getChangeRequestTypes() {
        List<ChangeRequestTypeEntity> entities = getChangeRequestTypeEntities();
        List<ChangeRequestType> domains = entities.stream()
                .map(ChangeRequestConverter::convert)
                .collect(Collectors.<ChangeRequestType> toList());
        return domains;
    }

    private List<ChangeRequestTypeEntity> getChangeRequestTypeEntities() {
        Query query = entityManager.createQuery("from ChangeRequestTypeEntity where (NOT deleted = true) ",
                ChangeRequestTypeEntity.class);
        List<ChangeRequestTypeEntity> result = query.getResultList();
        return result;
    }

    private ChangeRequestTypeEntity getChangeRequestTypeEntity(final Long changeRequestTypeId)
            throws EntityRetrievalException {
        Query query = entityManager.createQuery(
                "from ChangeRequestTypeEntity where (NOT deleted = true) "
                        + "AND (id = :changeRequestStatusTypeId) ",
                ChangeRequestTypeEntity.class);
        query.setParameter("changeRequestTypeId", changeRequestTypeId);
        List<ChangeRequestTypeEntity> result = query.getResultList();

        if (result == null || result.size() == 0) {
            throw new EntityRetrievalException("Data error. Change request type not found in database.");
        } else if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate change request type in database.");
        }

        if (result.size() == 0) {
            return null;
        }
        return result.get(0);
    }
}
