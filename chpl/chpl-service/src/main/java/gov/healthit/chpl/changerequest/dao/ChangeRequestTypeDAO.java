package gov.healthit.chpl.changerequest.dao;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.changerequest.domain.ChangeRequestConverter;
import gov.healthit.chpl.changerequest.domain.ChangeRequestType;
import gov.healthit.chpl.changerequest.entity.ChangeRequestTypeEntity;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.exception.EntityRetrievalException;

@Repository("changeRequestTypeDAO")
public class ChangeRequestTypeDAO extends BaseDAOImpl {

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
        String hql = "FROM ChangeRequestTypeEntity "
                + "WHERE (NOT deleted = true) ";
        return entityManager.createQuery(hql, ChangeRequestTypeEntity.class)
                .getResultList();
    }

    private ChangeRequestTypeEntity getChangeRequestTypeEntity(Long changeRequestTypeId)
            throws EntityRetrievalException {
        String hql = "FROM ChangeRequestTypeEntity "
                + "WHERE (NOT deleted = true) "
                + "AND (id = :changeRequestTypeId) ";
        List<ChangeRequestTypeEntity> result = entityManager
                .createQuery(hql, ChangeRequestTypeEntity.class)
                .setParameter("changeRequestTypeId", changeRequestTypeId)
                .getResultList();

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
