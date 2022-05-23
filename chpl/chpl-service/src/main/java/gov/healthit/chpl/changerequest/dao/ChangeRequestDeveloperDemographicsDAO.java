package gov.healthit.chpl.changerequest.dao;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestConverter;
import gov.healthit.chpl.changerequest.domain.ChangeRequestDeveloperDemographics;
import gov.healthit.chpl.changerequest.entity.ChangeRequestDeveloperDemographicsEntity;
import gov.healthit.chpl.changerequest.entity.ChangeRequestEntity;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;

@Component
public class ChangeRequestDeveloperDemographicsDAO extends BaseDAOImpl {


    public ChangeRequestDeveloperDemographics create(ChangeRequest cr, ChangeRequestDeveloperDemographics crDev)
            throws EntityRetrievalException {
        ChangeRequestDeveloperDemographicsEntity entity = getNewEntity(cr, crDev);
        create(entity);
        return ChangeRequestConverter.convert(getEntity(entity.getId()));
    }


    public ChangeRequestDeveloperDemographics getByChangeRequestId(Long changeRequestId) throws EntityRetrievalException {
        return ChangeRequestConverter.convert(getEntityByChangeRequestId(changeRequestId));
    }


    public ChangeRequestDeveloperDemographics update(ChangeRequestDeveloperDemographics crDev) throws EntityRetrievalException {
        ChangeRequestDeveloperDemographicsEntity entity = getEntity(crDev.getId());
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

    private ChangeRequestDeveloperDemographicsEntity getNewEntity(ChangeRequest cr, ChangeRequestDeveloperDemographics crDev) {
        ChangeRequestDeveloperDemographicsEntity entity = new ChangeRequestDeveloperDemographicsEntity();
        entity.setChangeRequest(getSession().load(ChangeRequestEntity.class, cr.getId()));
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
        entity.setDeleted(false);
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        entity.setCreationDate(new Date());
        entity.setLastModifiedDate(new Date());
        return entity;
    }

    private ChangeRequestDeveloperDemographicsEntity getEntity(Long changeRequestDevId)
            throws EntityRetrievalException {
        String hql = "FROM ChangeRequestDeveloperDemographicsEntity crDev "
                + "JOIN FETCH crDev.changeRequest "
                + "WHERE (NOT crDev.deleted = true) "
                + "AND (crDev.id = :changeRequestDevId) ";

        List<ChangeRequestDeveloperDemographicsEntity> result = entityManager
                .createQuery(hql, ChangeRequestDeveloperDemographicsEntity.class)
                .setParameter("changeRequestDevId", changeRequestDevId)
                .getResultList();

        if (result == null || result.size() == 0) {
            throw new EntityRetrievalException(
                    "Data error. Change request developer demographics not found in database.");
        } else if (result.size() > 1) {
            throw new EntityRetrievalException(
                    "Data error. Duplicate change request developer demographic in database.");
        }

        if (result.size() == 0) {
            return null;
        }
        return result.get(0);
    }

    private ChangeRequestDeveloperDemographicsEntity getEntityByChangeRequestId(Long changeRequestId)
            throws EntityRetrievalException {
        String hql = "FROM ChangeRequestDeveloperDemographicsEntity crDev "
                + "JOIN FETCH crDev.changeRequest "
                + "WHERE (NOT crDev.deleted = true) "
                + "AND (crDev.changeRequest.id = :changeRequestId) ";

        List<ChangeRequestDeveloperDemographicsEntity> result = entityManager
                .createQuery(hql, ChangeRequestDeveloperDemographicsEntity.class)
                .setParameter("changeRequestId", changeRequestId)
                .getResultList();

        if (result == null || result.size() == 0) {
            throw new EntityRetrievalException(
                    "Data error. Change request developer demographics not found in database.");
        } else if (result.size() > 1) {
            throw new EntityRetrievalException(
                    "Data error. Duplicate change request developer demographics in database.");
        }

        if (result.size() == 0) {
            return null;
        }
        return result.get(0);
    }

}
