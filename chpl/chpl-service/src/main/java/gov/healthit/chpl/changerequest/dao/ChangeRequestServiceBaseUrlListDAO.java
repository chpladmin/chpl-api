package gov.healthit.chpl.changerequest.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestConverter;
import gov.healthit.chpl.changerequest.domain.ChangeRequestServiceBaseUrlList;
import gov.healthit.chpl.changerequest.entity.ChangeRequestEntity;
import gov.healthit.chpl.changerequest.entity.ChangeRequestServiceBaseUrlListEntity;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.exception.EntityRetrievalException;

public class ChangeRequestServiceBaseUrlListDAO extends BaseDAOImpl {
    private ChangeRequestConverter changeRequestConverter;

    @Autowired
    public ChangeRequestServiceBaseUrlListDAO(ChangeRequestConverter changeRequestConverter) {
        this.changeRequestConverter = changeRequestConverter;
    }

    public ChangeRequestServiceBaseUrlList create(ChangeRequest cr, ChangeRequestServiceBaseUrlList crSbul)
            throws EntityRetrievalException {
        ChangeRequestServiceBaseUrlListEntity entity = getNewEntity(cr, crSbul);
        create(entity);
        return changeRequestConverter.convert(getEntity(entity.getId()));
    }

    public ChangeRequestServiceBaseUrlList getByChangeRequestId(Long changeRequestId) throws EntityRetrievalException {
        return changeRequestConverter.convert(getEntityByChangeRequestId(changeRequestId));
    }


    public ChangeRequestServiceBaseUrlList update(ChangeRequestServiceBaseUrlList crSbul) throws EntityRetrievalException {
        ChangeRequestServiceBaseUrlListEntity entity = getEntity(crSbul.getId());
        entity.setServiceBaseUrlList(crSbul.getServiceBaseUrlList());
        entity.setListingId(crSbul.getListingId());
        update(entity);
        return changeRequestConverter.convert(getEntity(entity.getId()));
    }

    private ChangeRequestServiceBaseUrlListEntity getNewEntity(ChangeRequest cr, ChangeRequestServiceBaseUrlList crSbul) {
        ChangeRequestServiceBaseUrlListEntity entity = new ChangeRequestServiceBaseUrlListEntity();
        entity.setChangeRequest(getSession().get(ChangeRequestEntity.class, cr.getId()));
        entity.setServiceBaseUrlList(crSbul.getServiceBaseUrlList());
        entity.setListingId(crSbul.getListingId());
        return entity;
    }

    private ChangeRequestServiceBaseUrlListEntity getEntity(Long changeRequestSbulId) throws EntityRetrievalException {
        String hql = "FROM ChangeRequestServiceBaseUrlListEntity crSbul "
                + "JOIN FETCH crSbul.changeRequest "
                + "WHERE (NOT crSbul.deleted = true) "
                + "AND (crSbul.id = :changeRequestSbulId) ";

        List<ChangeRequestServiceBaseUrlListEntity> result = entityManager
                .createQuery(hql, ChangeRequestServiceBaseUrlListEntity.class)
                .setParameter("changeRequestSbulId", changeRequestSbulId)
                .getResultList();

        if (result == null || result.size() == 0) {
            throw new EntityRetrievalException(
                    "Data error. Change request service base url list not found in database.");
        } else if (result.size() > 1) {
            throw new EntityRetrievalException(
                    "Data error. Duplicate change request service base url list in database.");
        }

        if (result.size() == 0) {
            return null;
        }
        return result.get(0);
    }

    private ChangeRequestServiceBaseUrlListEntity getEntityByChangeRequestId(Long changeRequestId)
            throws EntityRetrievalException {
        String hql = "FROM ChangeRequestServiceBaseUrlListEntity crSbul "
                + "JOIN FETCH crSbul.changeRequest "
                + "WHERE (NOT crSbul.deleted = true) "
                + "AND (crSbul.changeRequest.id = :changeRequestId) ";

        List<ChangeRequestServiceBaseUrlListEntity> result = entityManager
                .createQuery(hql, ChangeRequestServiceBaseUrlListEntity.class)
                .setParameter("changeRequestId", changeRequestId)
                .getResultList();

        if (result == null || result.size() == 0) {
            throw new EntityRetrievalException(
                    "Data error. Change request service base url list not found in database.");
        } else if (result.size() > 1) {
            throw new EntityRetrievalException(
                    "Data error. Duplicate change request service base url list in database.");
        }

        if (result.size() == 0) {
            return null;
        }
        return result.get(0);
    }

}
