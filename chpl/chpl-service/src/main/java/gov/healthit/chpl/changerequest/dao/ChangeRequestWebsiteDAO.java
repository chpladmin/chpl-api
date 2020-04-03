package gov.healthit.chpl.changerequest.dao;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestConverter;
import gov.healthit.chpl.changerequest.domain.ChangeRequestWebsite;
import gov.healthit.chpl.changerequest.entity.ChangeRequestEntity;
import gov.healthit.chpl.changerequest.entity.ChangeRequestWebsiteEntity;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;

@Repository("changeRequestWebsiteDAO")
public class ChangeRequestWebsiteDAO extends BaseDAOImpl {

    public ChangeRequestWebsite create(ChangeRequest cr, ChangeRequestWebsite crWebsite)
            throws EntityRetrievalException {
        ChangeRequestWebsiteEntity entity = getNewEntity(cr, crWebsite);
        create(entity);
        return ChangeRequestConverter.convert(getEntity(entity.getId()));
    }

    public ChangeRequestWebsite getByChangeRequestId(Long changeRequestId) throws EntityRetrievalException {
        return ChangeRequestConverter.convert(getEntityByChangeRequestId(changeRequestId));
    }

    public ChangeRequestWebsite update(ChangeRequestWebsite crWebsite) throws EntityRetrievalException {
        ChangeRequestWebsiteEntity entity = getEntity(crWebsite.getId());
        entity.setWebsite(crWebsite.getWebsite());
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        update(entity);
        return ChangeRequestConverter.convert(getEntity(entity.getId()));
    }

    private ChangeRequestWebsiteEntity getNewEntity(ChangeRequest cr, ChangeRequestWebsite crWebsite) {
        ChangeRequestWebsiteEntity entity = new ChangeRequestWebsiteEntity();
        entity.setChangeRequest(getSession().load(ChangeRequestEntity.class, cr.getId()));
        entity.setWebsite(crWebsite.getWebsite());
        entity.setDeleted(false);
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        entity.setCreationDate(new Date());
        entity.setLastModifiedDate(new Date());
        return entity;
    }

    private ChangeRequestWebsiteEntity getEntity(Long changeRequestWebsiteId) throws EntityRetrievalException {
        String hql = "FROM ChangeRequestWebsiteEntity crWebsite "
                + "JOIN FETCH crWebsite.changeRequest "
                + "WHERE (NOT crWebsite.deleted = true) "
                + "AND (crWebsite.id = :changeRequestWebsiteId) ";

        List<ChangeRequestWebsiteEntity> result = entityManager
                .createQuery(hql, ChangeRequestWebsiteEntity.class)
                .setParameter("changeRequestWebsiteId", changeRequestWebsiteId)
                .getResultList();

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

    private ChangeRequestWebsiteEntity getEntityByChangeRequestId(Long changeRequestId) throws EntityRetrievalException {
        String hql = "FROM ChangeRequestWebsiteEntity crWebsite "
                + "JOIN FETCH crWebsite.changeRequest "
                + "WHERE (NOT crWebsite.deleted = true) "
                + "AND (crWebsite.changeRequest.id = :changeRequestId) ";

        List<ChangeRequestWebsiteEntity> result = entityManager
                .createQuery(hql, ChangeRequestWebsiteEntity.class)
                .setParameter("changeRequestId", changeRequestId)
                .getResultList();

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
