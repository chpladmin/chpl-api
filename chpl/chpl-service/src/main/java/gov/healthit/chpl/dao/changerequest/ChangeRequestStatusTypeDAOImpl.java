package gov.healthit.chpl.dao.changerequest;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.changerequest.ChangeRequestConverter;
import gov.healthit.chpl.domain.changerequest.ChangeRequestStatusType;
import gov.healthit.chpl.entity.changerequest.ChangeRequestStatusTypeEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;

@Repository("changeRequestStatusTypeDAO")
public class ChangeRequestStatusTypeDAOImpl extends BaseDAOImpl implements ChangeRequestStatusTypeDAO {

    public ChangeRequestStatusType getChangeRequestStatusTypeById(Long changeRequestStatusTypeId)
            throws EntityRetrievalException {
        return ChangeRequestConverter.convert(getChangeRequestStatusTypeEntity(changeRequestStatusTypeId));

    }

    public List<ChangeRequestStatusType> getChangeRequestStatusTypes() {
        return getChangeRequestStatusTypeEntities().stream()
                .map(ChangeRequestConverter::convert)
                .collect(Collectors.<ChangeRequestStatusType> toList());

    }

    private List<ChangeRequestStatusTypeEntity> getChangeRequestStatusTypeEntities() {
        Query query = entityManager.createQuery("from ChangeRequestStatusTypeEntity where (NOT deleted = true) ",
                ChangeRequestStatusTypeEntity.class);
        List<ChangeRequestStatusTypeEntity> result = query.getResultList();
        return result;
    }

    private ChangeRequestStatusTypeEntity getChangeRequestStatusTypeEntity(final Long changeRequestStatusTypeId)
            throws EntityRetrievalException {
        Query query = entityManager.createQuery(
                "from ChangeRequestStatusTypeEntity where (NOT deleted = true) "
                        + "AND (id = :changeRequestStatusTypeId) ",
                ChangeRequestStatusTypeEntity.class);
        query.setParameter("changeRequestStatusTypeId", changeRequestStatusTypeId);
        List<ChangeRequestStatusTypeEntity> result = query.getResultList();

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
