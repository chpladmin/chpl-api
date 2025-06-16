package gov.healthit.chpl.changerequest.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.changerequest.domain.ChangeRequestConverter;
import gov.healthit.chpl.changerequest.domain.ChangeRequestListingUrl;
import gov.healthit.chpl.changerequest.domain.ChangeRequestListingUrlType;
import gov.healthit.chpl.changerequest.entity.ChangeRequestEntity;
import gov.healthit.chpl.changerequest.entity.ChangeRequestListingUrlEntity;
import gov.healthit.chpl.changerequest.entity.ChangeRequestListingUrlTypeEntity;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.exception.EntityRetrievalException;

@Repository
public class ChangeRequestListingUrlDAO extends BaseDAOImpl {
    private ChangeRequestConverter changeRequestConverter;

    @Autowired
    public ChangeRequestListingUrlDAO(ChangeRequestConverter changeRequestConverter) {
        this.changeRequestConverter = changeRequestConverter;
    }

    public ChangeRequestListingUrlType getChangeRequestListingUrlType(String name) throws EntityRetrievalException {
        return getChangeRequestListingUrlTypeEntity(name).toDomain();
    }

    public Long create(Long changeRequestId, ChangeRequestListingUrl crListingUrl) throws EntityRetrievalException {
        System.out.println("Creating new change request listing url entity");
        ChangeRequestListingUrlEntity entity = getNewEntity(changeRequestId, crListingUrl);
        create(entity);
        System.out.println("Created change request listing url entity with ID " + entity.getId());
        return entity.getId();
    }

    public ChangeRequestListingUrl getByChangeRequestId(Long changeRequestId) throws EntityRetrievalException {
        return changeRequestConverter.convert(getEntityByChangeRequestId(changeRequestId));
    }

    public void update(ChangeRequestListingUrl crListingUpdate) throws EntityRetrievalException {
        ChangeRequestListingUrlEntity entity = getEntity(crListingUpdate.getId());
        entity.setUrl(crListingUpdate.getUrl());
        entity.setListingId(crListingUpdate.getListing().getId());
        update(entity);
    }

    private ChangeRequestListingUrlEntity getNewEntity(Long changeRequestId, ChangeRequestListingUrl crListingUpdate) {
        ChangeRequestListingUrlEntity entity = new ChangeRequestListingUrlEntity();
        entity.setChangeRequest(getSession().get(ChangeRequestEntity.class, changeRequestId));
        entity.setChangeRequestListingUrlType(getSession().get(ChangeRequestListingUrlTypeEntity.class, crListingUpdate.getChangeRequestListingUrlType().getId()));
        entity.setUrl(crListingUpdate.getUrl());
        entity.setListingId(crListingUpdate.getListing().getId());
        return entity;
    }

    private ChangeRequestListingUrlEntity getEntity(Long changeRequestListingUrlId) throws EntityRetrievalException {
        String hql = "FROM ChangeRequestListingUrlEntity crListingUrl "
                + "JOIN FETCH crListingUrl.changeRequest "
                + "JOIN FETCH crListingUrl.changeRequestListingUrlType "
                + "WHERE (NOT crListingUrl.deleted = true) "
                + "AND (crListingUrl.id = :crListingUrlId) ";

        List<ChangeRequestListingUrlEntity> result = entityManager
                .createQuery(hql, ChangeRequestListingUrlEntity.class)
                .setParameter("crListingUrlId", changeRequestListingUrlId)
                .getResultList();

        if (result == null || result.size() == 0) {
            throw new EntityRetrievalException(
                    "Data error. Change request listing url not found in database.");
        } else if (result.size() > 1) {
            throw new EntityRetrievalException(
                    "Data error. Duplicate change request lsiting url in database.");
        }

        if (result.size() == 0) {
            return null;
        }
        return result.get(0);
    }

    private ChangeRequestListingUrlEntity getEntityByChangeRequestId(Long changeRequestId)
            throws EntityRetrievalException {
        System.out.println("Getting Change Request Listing URL Entity with change request ID " + changeRequestId);

        String hql = "FROM ChangeRequestListingUrlEntity crListingUrl "
                + "JOIN FETCH crListingUrl.changeRequest "
                + "JOIN FETCH crListingUrl.changeRequestListingUrlType "
                + "WHERE (NOT crListingUrl.deleted = true) "
                + "AND (crListingUrl.changeRequest.id = :changeRequestId) ";

        List<ChangeRequestListingUrlEntity> result = entityManager
                .createQuery(hql, ChangeRequestListingUrlEntity.class)
                .setParameter("changeRequestId", changeRequestId)
                .getResultList();

        if (result.size() == 0) {
            return null;
        }
        return result.get(0);
    }

    private ChangeRequestListingUrlTypeEntity getChangeRequestListingUrlTypeEntity(String name)
            throws EntityRetrievalException {
        String hql = "FROM ChangeRequestListingUrlTypeEntity crlut "
                + "WHERE (NOT deleted = true) "
                + "AND (name = :name) ";

        List<ChangeRequestListingUrlTypeEntity> result = entityManager
                .createQuery(hql, ChangeRequestListingUrlTypeEntity.class)
                .setParameter("name", name)
                .getResultList();

        if (result == null || result.size() == 0) {
            throw new EntityRetrievalException(
                    "Data error. Change request listing url not found in database.");
        } else if (result.size() > 1) {
            throw new EntityRetrievalException(
                    "Data error. Duplicate change request listing url in database.");
        }

        if (result.size() == 0) {
            return null;
        }
        return result.get(0);
    }

}
