package gov.healthit.chpl.changerequest.dao;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestConverter;
import gov.healthit.chpl.changerequest.domain.ChangeRequestDeveloperDemographic;
import gov.healthit.chpl.changerequest.entity.ChangeRequestDeveloperDemographicEntity;
import gov.healthit.chpl.changerequest.entity.ChangeRequestEntity;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;

@Repository("changeRequestDeveloperDemographicDAO")
public class ChangeRequestDeveloperDemographicDAO extends BaseDAOImpl {


    public ChangeRequestDeveloperDemographic create(ChangeRequest cr, ChangeRequestDeveloperDemographic crDev)
            throws EntityRetrievalException {
        ChangeRequestDeveloperDemographicEntity entity = getNewEntity(cr, crDev);
        create(entity);
        return ChangeRequestConverter.convert(getEntity(entity.getId()));
    }


    public ChangeRequestDeveloperDemographic getByChangeRequestId(Long changeRequestId) throws EntityRetrievalException {
        return ChangeRequestConverter.convert(getEntityByChangeRequestId(changeRequestId));
    }


    public ChangeRequestDeveloperDemographic update(ChangeRequestDeveloperDemographic crDev) throws EntityRetrievalException {
        ChangeRequestDeveloperDemographicEntity entity = getEntity(crDev.getId());
        if (crDev.getSelfDeveloper() != null) {
            entity.setSelfDeveloper(crDev.getSelfDeveloper());
        }
        entity.setWebsite(crDev.getWebsite());
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

    private ChangeRequestDeveloperDemographicEntity getNewEntity(ChangeRequest cr, ChangeRequestDeveloperDemographic crDev) {
        ChangeRequestDeveloperDemographicEntity entity = new ChangeRequestDeveloperDemographicEntity();
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

    private ChangeRequestDeveloperDemographicEntity getEntity(Long changeRequestDevId)
            throws EntityRetrievalException {
        String hql = "FROM ChangeRequestDeveloperDemographicEntity crDev "
                + "JOIN FETCH crDev.changeRequest "
                + "WHERE (NOT crDev.deleted = true) "
                + "AND (crDev.id = :changeRequestDevId) ";

        List<ChangeRequestDeveloperDemographicEntity> result = entityManager
                .createQuery(hql, ChangeRequestDeveloperDemographicEntity.class)
                .setParameter("changeRequestDevId", changeRequestDevId)
                .getResultList();

        if (result == null || result.size() == 0) {
            throw new EntityRetrievalException(
                    "Data error. Change request developer demographic not found in database.");
        } else if (result.size() > 1) {
            throw new EntityRetrievalException(
                    "Data error. Duplicate change request developer demographic in database.");
        }

        if (result.size() == 0) {
            return null;
        }
        return result.get(0);
    }

    private ChangeRequestDeveloperDemographicEntity getEntityByChangeRequestId(Long changeRequestId)
            throws EntityRetrievalException {
        String hql = "FROM ChangeRequestDeveloperDemographicEntity crDev "
                + "JOIN FETCH crDev.changeRequest "
                + "WHERE (NOT crDev.deleted = true) "
                + "AND (crDev.changeRequest.id = :changeRequestId) ";

        List<ChangeRequestDeveloperDemographicEntity> result = entityManager
                .createQuery(hql, ChangeRequestDeveloperDemographicEntity.class)
                .setParameter("changeRequestId", changeRequestId)
                .getResultList();

        if (result == null || result.size() == 0) {
            throw new EntityRetrievalException(
                    "Data error. Change request developer demographic not found in database.");
        } else if (result.size() > 1) {
            throw new EntityRetrievalException(
                    "Data error. Duplicate change request developer demographic in database.");
        }

        if (result.size() == 0) {
            return null;
        }
        return result.get(0);
    }

}
