package gov.healthit.chpl.dao.changerequest;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.changerequest.ChangeRequest;
import gov.healthit.chpl.domain.changerequest.ChangeRequestConverter;
import gov.healthit.chpl.domain.changerequest.ChangeRequestWebsite;
import gov.healthit.chpl.entity.changerequest.ChangeRequestEntity;
import gov.healthit.chpl.entity.changerequest.ChangeRequestWebsiteEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;

@Repository("changeRequestWebsiteDAO")
public class ChangeRequestWebsiteDAOImpl extends BaseDAOImpl implements ChangeRequestWebsiteDAO {

    @Override
    public ChangeRequestWebsite create(final ChangeRequest cr, final ChangeRequestWebsite crWebsite)
            throws EntityRetrievalException {
        ChangeRequestWebsiteEntity entity = getNewEntity(cr, crWebsite);
        create(entity);
        return ChangeRequestConverter.convert(getEntity(entity.getId()));
    }

    @Override
    public ChangeRequestWebsite getByChangeRequestId(final Long changeRequestId) throws EntityRetrievalException {
        return ChangeRequestConverter.convert(getEntityByChangeRequestId(changeRequestId));
    }

    private ChangeRequestWebsiteEntity getNewEntity(final ChangeRequest cr, final ChangeRequestWebsite crWebsite) {
        ChangeRequestWebsiteEntity entity = new ChangeRequestWebsiteEntity();
        entity.setChangeRequest(getSession().load(ChangeRequestEntity.class, cr.getId()));
        entity.setWebsite(crWebsite.getWebsite());
        entity.setDeleted(false);
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        entity.setCreationDate(new Date());
        entity.setLastModifiedDate(new Date());
        return entity;
    }

    private ChangeRequestWebsiteEntity getEntity(final Long changeRequestWebsiteId)
            throws EntityRetrievalException {
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

    private ChangeRequestWebsiteEntity getEntityByChangeRequestId(final Long changeRequestId)
            throws EntityRetrievalException {
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
