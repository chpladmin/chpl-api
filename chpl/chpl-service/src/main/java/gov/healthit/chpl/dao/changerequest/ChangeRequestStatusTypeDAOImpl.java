package gov.healthit.chpl.dao.changerequest;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.changerequest.ChangeRequestStatusType;
import gov.healthit.chpl.entity.changerequest.ChangeRequestStatusTypeEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;

@Repository
public class ChangeRequestStatusTypeDAOImpl extends BaseDAOImpl {

    public ChangeRequestStatusType getChangeRequestStatusTypeById(Long changeRequestStatusTypeId)
            throws EntityRetrievalException {
        return mapToChangeRequestStatusType.apply(getChangeRequestStatusTypeEntity(changeRequestStatusTypeId));

    }

    public List<ChangeRequestStatusType> getChangeRequestStatusTypes() {
        List<ChangeRequestStatusTypeEntity> entities = getChangeRequestStatusTypeEntities();
        List<ChangeRequestStatusType> domains = entities.stream()
                .map(mapToChangeRequestStatusType)
                .collect(Collectors.<ChangeRequestStatusType> toList());
        return domains;
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

    Function<ChangeRequestStatusTypeEntity, ChangeRequestStatusType> mapToChangeRequestStatusType = new Function<ChangeRequestStatusTypeEntity, ChangeRequestStatusType>() {
        @Override
        public ChangeRequestStatusType apply(ChangeRequestStatusTypeEntity t) {
            ChangeRequestStatusType status = new ChangeRequestStatusType();
            status.setId(t.getId());
            status.setName(t.getName());
            return status;
        }
    };

}
