package gov.healthit.chpl.dao.changerequest;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.changerequest.ChangeRequestConverter;
import gov.healthit.chpl.domain.changerequest.ChangeRequestStatusType;
import gov.healthit.chpl.entity.changerequest.ChangeRequestStatusTypeEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;

@Repository("changeRequestStatusTypeDAO")
public class ChangeRequestStatusTypeDAOImpl extends BaseDAOImpl implements ChangeRequestStatusTypeDAO {

    @Override
    public ChangeRequestStatusType getChangeRequestStatusTypeById(Long changeRequestStatusTypeId)
            throws EntityRetrievalException {
        return ChangeRequestConverter.convert(getChangeRequestStatusTypeEntity(changeRequestStatusTypeId));

    }

    @Override
    public List<ChangeRequestStatusType> getChangeRequestStatusTypes() {
        return getChangeRequestStatusTypeEntities().stream()
                .map(ChangeRequestConverter::convert)
                .collect(Collectors.<ChangeRequestStatusType> toList());

    }

    private List<ChangeRequestStatusTypeEntity> getChangeRequestStatusTypeEntities() {
        String hql = "FROM ChangeRequestStatusTypeEntity "
                + "WHERE (NOT deleted = true) ";
        List<ChangeRequestStatusTypeEntity> result = entityManager
                .createQuery(hql, ChangeRequestStatusTypeEntity.class)
                .getResultList();
        return result;
    }

    private ChangeRequestStatusTypeEntity getChangeRequestStatusTypeEntity(final Long changeRequestStatusTypeId)
            throws EntityRetrievalException {
        String hql = "FROM ChangeRequestStatusTypeEntity "
                + "WHERE (NOT deleted = true) "
                + "AND (id = :changeRequestStatusTypeId) ";

        List<ChangeRequestStatusTypeEntity> result = entityManager
                .createQuery(hql, ChangeRequestStatusTypeEntity.class)
                .setParameter("changeRequestStatusTypeId", changeRequestStatusTypeId)
                .getResultList();

        if (result == null || result.size() == 0) {
            throw new EntityRetrievalException("Data error. Change request status type not found in database.");
        } else if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate change request status type in database.");
        }

        if (result.size() == 0) {
            return null;
        }
        return result.get(0);
    }
}
