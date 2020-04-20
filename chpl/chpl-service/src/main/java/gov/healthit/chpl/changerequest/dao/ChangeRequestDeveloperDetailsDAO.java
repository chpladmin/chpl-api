package gov.healthit.chpl.changerequest.dao;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestConverter;
import gov.healthit.chpl.changerequest.domain.ChangeRequestDeveloperDetails;
import gov.healthit.chpl.changerequest.entity.ChangeRequestDeveloperDetailsEntity;
import gov.healthit.chpl.changerequest.entity.ChangeRequestEntity;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;

@Repository("changeRequestDeveloperDetailsDAO")
public class ChangeRequestDeveloperDetailsDAO extends BaseDAOImpl {


    public ChangeRequestDeveloperDetails create(ChangeRequest cr, ChangeRequestDeveloperDetails crDev)
            throws EntityRetrievalException {
        ChangeRequestDeveloperDetailsEntity entity = getNewEntity(cr, crDev);
        create(entity);
        return ChangeRequestConverter.convert(getEntity(entity.getId()));
    }


    public ChangeRequestDeveloperDetails getByChangeRequestId(Long changeRequestId) throws EntityRetrievalException {
        return ChangeRequestConverter.convert(getEntityByChangeRequestId(changeRequestId));
    }


    public ChangeRequestDeveloperDetails update(ChangeRequestDeveloperDetails crDev) throws EntityRetrievalException {
        ChangeRequestDeveloperDetailsEntity entity = getEntity(crDev.getId());
        if (crDev.getSelfDeveloper() != null) {
            entity.setSelfDeveloper(crDev.getSelfDeveloper());
        }
        if (crDev.getAddress() != null) {
            entity.setStreetLine1(crDev.getAddress().getLine1());
            entity.setStreetLine2(crDev.getAddress().getLine2());
            entity.setCity(crDev.getAddress().getCity());
            entity.setState(crDev.getAddress().getState());
            entity.setZipcode(crDev.getAddress().getZipcode());
            entity.setCountry(crDev.getAddress().getCountry());
        }
        if (crDev.getContact() != null) {
            entity.setContactFullName(crDev.getContact().getFullName());
            entity.setContactEmail(crDev.getContact().getEmail());
            entity.setContactPhoneNumber(crDev.getContact().getPhoneNumber());
            entity.setContactTitle(crDev.getContact().getTitle());
        }
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        update(entity);
        return ChangeRequestConverter.convert(getEntity(entity.getId()));
    }

    private ChangeRequestDeveloperDetailsEntity getNewEntity(ChangeRequest cr, ChangeRequestDeveloperDetails crDev) {
        ChangeRequestDeveloperDetailsEntity entity = new ChangeRequestDeveloperDetailsEntity();
        entity.setChangeRequest(getSession().load(ChangeRequestEntity.class, cr.getId()));
        if (crDev.getSelfDeveloper() != null) {
            entity.setSelfDeveloper(crDev.getSelfDeveloper());
        }
        if (crDev.getAddress() != null) {
            entity.setStreetLine1(crDev.getAddress().getLine1());
            entity.setStreetLine2(crDev.getAddress().getLine2());
            entity.setCity(crDev.getAddress().getCity());
            entity.setState(crDev.getAddress().getState());
            entity.setZipcode(crDev.getAddress().getZipcode());
            entity.setCountry(crDev.getAddress().getCountry());
        }
        if (crDev.getContact() != null) {
            entity.setContactFullName(crDev.getContact().getFullName());
            entity.setContactEmail(crDev.getContact().getEmail());
            entity.setContactPhoneNumber(crDev.getContact().getPhoneNumber());
            entity.setContactTitle(crDev.getContact().getTitle());
        }
        entity.setDeleted(false);
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        entity.setCreationDate(new Date());
        entity.setLastModifiedDate(new Date());
        return entity;
    }

    private ChangeRequestDeveloperDetailsEntity getEntity(Long changeRequestDevId)
            throws EntityRetrievalException {
        String hql = "FROM ChangeRequestDeveloperDetailsEntity crDev "
                + "JOIN FETCH crDev.changeRequest "
                + "WHERE (NOT crDev.deleted = true) "
                + "AND (crDev.id = :changeRequestDevId) ";

        List<ChangeRequestDeveloperDetailsEntity> result = entityManager
                .createQuery(hql, ChangeRequestDeveloperDetailsEntity.class)
                .setParameter("changeRequestDevId", changeRequestDevId)
                .getResultList();

        if (result == null || result.size() == 0) {
            throw new EntityRetrievalException(
                    "Data error. Change request developer details not found in database.");
        } else if (result.size() > 1) {
            throw new EntityRetrievalException(
                    "Data error. Duplicate change request developer details in database.");
        }

        if (result.size() == 0) {
            return null;
        }
        return result.get(0);
    }

    private ChangeRequestDeveloperDetailsEntity getEntityByChangeRequestId(Long changeRequestId)
            throws EntityRetrievalException {
        String hql = "FROM ChangeRequestDeveloperDetailsEntity crDev "
                + "JOIN FETCH crDev.changeRequest "
                + "WHERE (NOT crDev.deleted = true) "
                + "AND (crDev.changeRequest.id = :changeRequestId) ";

        List<ChangeRequestDeveloperDetailsEntity> result = entityManager
                .createQuery(hql, ChangeRequestDeveloperDetailsEntity.class)
                .setParameter("changeRequestId", changeRequestId)
                .getResultList();

        if (result == null || result.size() == 0) {
            throw new EntityRetrievalException(
                    "Data error. Change request developer details not found in database.");
        } else if (result.size() > 1) {
            throw new EntityRetrievalException(
                    "Data error. Duplicate change request developer details in database.");
        }

        if (result.size() == 0) {
            return null;
        }
        return result.get(0);
    }

}
