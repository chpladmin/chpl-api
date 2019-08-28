package gov.healthit.chpl.dao.changerequest;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.changerequest.ChangeRequestConverter;
import gov.healthit.chpl.domain.changerequest.ChangeRequestWebsite;
import gov.healthit.chpl.entity.changerequest.ChangeRequestEntity;
import gov.healthit.chpl.entity.changerequest.ChangeRequestWebsiteEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;

@Repository("changeRequestWebsiteDAO")
public class ChangeRequestWebsiteDAOImpl extends BaseDAOImpl implements ChangeRequestWebsiteDAO {

    @Override
    public ChangeRequestWebsite create(ChangeRequestWebsite crWebsite) throws EntityRetrievalException {
        ChangeRequestWebsiteEntity entity = getNewEntity(crWebsite);
        create(entity);
        return ChangeRequestConverter.convert(getEntity(entity.getId()));
    }

    @Override
    public ChangeRequestWebsite getByChangeRequestId(final Long changeRequestId) throws EntityRetrievalException {
        return ChangeRequestConverter.convert(getEntityByChangeRequestId(changeRequestId));
    }

    private ChangeRequestWebsiteEntity getNewEntity(final ChangeRequestWebsite crWebsite) {
        ChangeRequestWebsiteEntity entity = new ChangeRequestWebsiteEntity();
        entity.setChangeRequest(getSession().load(ChangeRequestEntity.class, crWebsite.getChangeRequest().getId()));
        entity.setWebsite(crWebsite.getWebsite());
        entity.setDeleted(false);
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        entity.setCreationDate(new Date());
        entity.setLastModifiedDate(new Date());
        return entity;
    }

    private ChangeRequestWebsiteEntity getEntity(final Long changeRequestWebsiteId)
            throws EntityRetrievalException {
        Query query = entityManager.createQuery(
                "FROM ChangeRequestWebsiteEntity crWebsite "
                        + "JOIN FETCH crWebsite.changeRequest "
                        + "WHERE (NOT crWebsite.deleted = true) "
                        + "AND (crWebsite.id = :changeRequestWebsiteId) ",
                ChangeRequestWebsiteEntity.class);
        query.setParameter("changeRequestWebsiteId", changeRequestWebsiteId);
        List<ChangeRequestWebsiteEntity> result = query.getResultList();

        if (result == null || result.size() == 0) {
            throw new EntityRetrievalException(
                    "Data error. Change request website not found in database.");
        } else if (result.size() > 1) {
            throw new EntityRetrievalException(
                    "Data error. Duplicate change request website in database.");
        }

        if (result.size() == 0) {
            return null;
        }
        return result.get(0);
    }

    private ChangeRequestWebsiteEntity getEntityByChangeRequestId(final Long changeRequestId)
            throws EntityRetrievalException {
        Query query = entityManager.createQuery(
                "FROM ChangeRequestWebsiteEntity crWebsite "
                        + "JOIN FETCH crWebsite.changeRequest "
                        + "WHERE (NOT crWebsite.deleted = true) "
                        + "AND (crWebsite.changeRequest.id = :changeRequestId) ",
                ChangeRequestWebsiteEntity.class);
        query.setParameter("changeRequestId", changeRequestId);
        List<ChangeRequestWebsiteEntity> result = query.getResultList();

        if (result == null || result.size() == 0) {
            throw new EntityRetrievalException(
                    "Data error. Change request website not found in database.");
        } else if (result.size() > 1) {
            throw new EntityRetrievalException(
                    "Data error. Duplicate change request website in database.");
        }

        if (result.size() == 0) {
            return null;
        }
        return result.get(0);
    }

}
