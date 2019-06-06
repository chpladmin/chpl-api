package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.ComplaintDAO;
import gov.healthit.chpl.dao.ComplaintDTO;
import gov.healthit.chpl.dto.ComplaintStatusTypeDTO;
import gov.healthit.chpl.dto.ComplaintTypeDTO;
import gov.healthit.chpl.entity.CertificationBodyEntity;
import gov.healthit.chpl.entity.ComplaintEntity;
import gov.healthit.chpl.entity.ComplaintStatusTypeEntity;
import gov.healthit.chpl.entity.ComplaintTypeEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;

@Component
public class ComplaintDAOImpl extends BaseDAOImpl implements ComplaintDAO {
    private CertificationBodyDAO certificationBodyDAO;

    @Autowired
    public ComplaintDAOImpl(final CertificationBodyDAO certificationBodyDAO) {
        this.certificationBodyDAO = certificationBodyDAO;
    }

    @Override
    public List<ComplaintTypeDTO> getComplaintTypes() {
        List<ComplaintTypeEntity> entities = getComplaintTypeEntities();
        List<ComplaintTypeDTO> dtos = new ArrayList<ComplaintTypeDTO>();
        for (ComplaintTypeEntity entity : entities) {
            dtos.add(new ComplaintTypeDTO(entity));
        }
        return dtos;
    }

    @Override
    public List<ComplaintStatusTypeDTO> getComplaintStatusTypes() {
        List<ComplaintStatusTypeEntity> entities = getComplaintStatusTypeEntities();
        List<ComplaintStatusTypeDTO> dtos = new ArrayList<ComplaintStatusTypeDTO>();
        for (ComplaintStatusTypeEntity entity : entities) {
            dtos.add(new ComplaintStatusTypeDTO(entity));
        }
        return dtos;
    }

    @Override
    public List<ComplaintDTO> getAllComplaints() {
        Query query = entityManager.createQuery(
                "FROM ComplaintEntity c " + "JOIN FETCH c.certificationBody " + "LEFT JOIN FETCH c.complaintType "
                        + "LEFT JOIN FETCH c.complaintStatusType " + "WHERE c.deleted = false ",
                ComplaintEntity.class);
        List<ComplaintEntity> results = query.getResultList();

        List<ComplaintDTO> complaintDTOs = new ArrayList<ComplaintDTO>();
        for (ComplaintEntity entity : results) {
            complaintDTOs.add(new ComplaintDTO(entity));
        }
        return complaintDTOs;
    }

    @Override
    public ComplaintDTO getComplaint(final Long complaintId) throws EntityRetrievalException {
        ComplaintEntity entity = getEntityById(complaintId);
        return new ComplaintDTO(entity);
    }

    @Override
    public ComplaintDTO create(ComplaintDTO complaintDTO) throws EntityRetrievalException {
        ComplaintEntity entity = new ComplaintEntity();

        if (complaintDTO.getCertificationBody() == null) {
            entity.setCertificationBody(null);
        } else {
            entity.setCertificationBody(getAcbEntityById(complaintDTO.getCertificationBody().getId()));
        }
        if (complaintDTO.getComplaintStatusType() == null) {
            entity.setComplaintStatusType(null);
        } else {
            entity.setComplaintStatusType(
                    getComplaintStatusTypeEntityById(complaintDTO.getComplaintStatusType().getId()));
        }
        if (complaintDTO.getComplaintType() == null) {
            entity.setComplaintType(null);
        } else {
            entity.setComplaintType(getComplaintTypeEntityById(complaintDTO.getComplaintType().getId()));
        }

        entity.setOncComplaintId(complaintDTO.getOncComplaintId());
        entity.setAcbComplaintId(complaintDTO.getAcbComplaintId());
        entity.setReceivedDate(complaintDTO.getReceivedDate());
        entity.setSummary(complaintDTO.getSummary());
        entity.setActions(complaintDTO.getActions());
        entity.setComplainantContacted(complaintDTO.isComplainantContacted());
        entity.setDeveloperContacted(complaintDTO.isDeveloperContacted());
        entity.setOncAtlContacted(complaintDTO.isOncAtlContacted());
        entity.setClosedDate(null);

        entity.setDeleted(false);
        entity.setLastModifiedUser(AuthUtil.getAuditId());

        entity.setDeleted(false);
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        entity.setCreationDate(new Date());
        entity.setLastModifiedDate(new Date());

        entityManager.persist(entity);
        entityManager.flush();
        return new ComplaintDTO(entity);
    }

    @Override
    public ComplaintDTO update(ComplaintDTO complaintDTO) throws EntityRetrievalException {
        ComplaintEntity entity = getEntityById(complaintDTO.getId());

        if (complaintDTO.getComplaintStatusType() == null) {
            entity.setComplaintStatusType(null);
        } else {
            entity.setComplaintStatusType(
                    getComplaintStatusTypeEntityById(complaintDTO.getComplaintStatusType().getId()));
        }

        if (complaintDTO.getComplaintType() == null) {
            entity.setComplaintType(null);
        } else {
            entity.setComplaintType(getComplaintTypeEntityById(complaintDTO.getComplaintType().getId()));
        }

        entity.setOncComplaintId(complaintDTO.getOncComplaintId());
        entity.setAcbComplaintId(complaintDTO.getAcbComplaintId());
        entity.setReceivedDate(complaintDTO.getReceivedDate());
        entity.setSummary(complaintDTO.getSummary());
        entity.setActions(complaintDTO.getActions());
        entity.setComplainantContacted(complaintDTO.isComplainantContacted());
        entity.setDeveloperContacted(complaintDTO.isDeveloperContacted());
        entity.setOncAtlContacted(complaintDTO.isOncAtlContacted());
        entity.setClosedDate(complaintDTO.getClosedDate());

        entity.setDeleted(false);
        entity.setLastModifiedUser(AuthUtil.getAuditId());

        entityManager.merge(entity);
        entityManager.flush();

        ComplaintEntity updatedEntity = getEntityById(complaintDTO.getId());
        return new ComplaintDTO(updatedEntity);
    }

    @Override
    public void delete(ComplaintDTO complaintDTO) throws EntityRetrievalException {
        ComplaintEntity entity = getEntityById(complaintDTO.getId());
        entity.setDeleted(true);
        entity.setLastModifiedUser(AuthUtil.getAuditId());

        entityManager.merge(entity);
        entityManager.flush();
    }

    private ComplaintEntity getEntityById(Long id) throws EntityRetrievalException {
        ComplaintEntity entity = null;

        Query query = entityManager.createQuery("FROM ComplaintEntity c " + "JOIN FETCH c.certificationBody "
                + "LEFT JOIN FETCH c.complaintType " + "LEFT JOIN FETCH c.complaintStatusType "
                + "WHERE c.deleted = false " + "AND c.id = :complaintId", ComplaintEntity.class);
        query.setParameter("complaintId", id);
        List<ComplaintEntity> result = query.getResultList();

        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate filter id in database.");
        } else if (result.size() == 1) {
            entity = result.get(0);
        }
        return entity;
    }

    private List<ComplaintTypeEntity> getComplaintTypeEntities() {
        Query query = entityManager.createQuery("from ComplaintTypeEntity where (NOT deleted = true) ",
                ComplaintTypeEntity.class);
        List<ComplaintTypeEntity> result = query.getResultList();
        return result;
    }

    private List<ComplaintStatusTypeEntity> getComplaintStatusTypeEntities() {
        Query query = entityManager.createQuery("from ComplaintStatusTypeEntity where (NOT deleted = true) ",
                ComplaintStatusTypeEntity.class);
        List<ComplaintStatusTypeEntity> result = query.getResultList();
        return result;
    }

    private CertificationBodyEntity getAcbEntityById(final Long entityId) throws EntityRetrievalException {
        CertificationBodyEntity entity = null;

        String queryStr = "SELECT acb from CertificationBodyEntity acb " + "LEFT OUTER JOIN FETCH acb.address "
                + "WHERE (acb.id = :entityid)" + " AND (acb.deleted = false)";

        Query query = entityManager.createQuery(queryStr, CertificationBodyEntity.class);
        query.setParameter("entityid", entityId);
        List<CertificationBodyEntity> result = query.getResultList();

        if (result == null || result.size() == 0) {
            String msg = msgUtil.getMessage("acb.notFound");
            throw new EntityRetrievalException(msg);
        } else if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate certificaiton body id in database.");
        } else if (result.size() == 1) {
            entity = result.get(0);
        }
        return entity;
    }

    private ComplaintTypeEntity getComplaintTypeEntityById(final Long entityId) throws EntityRetrievalException {
        ComplaintTypeEntity entity = null;

        String queryStr = "FROM ComplaintTypeEntity c " + "WHERE (c.id = :entityid) " + "AND (c.deleted = false)";

        Query query = entityManager.createQuery(queryStr, ComplaintTypeEntity.class);
        query.setParameter("entityid", entityId);
        List<ComplaintTypeEntity> result = query.getResultList();

        if (result == null || result.size() == 0) {
            String msg = msgUtil.getMessage("Data error.  Complaint Type [" + entityId + "] not found.");
            throw new EntityRetrievalException(msg);
        } else if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate Complaint Type id in database.");
        } else if (result.size() == 1) {
            entity = result.get(0);
        }
        return entity;
    }

    private ComplaintStatusTypeEntity getComplaintStatusTypeEntityById(final Long entityId)
            throws EntityRetrievalException {
        ComplaintStatusTypeEntity entity = null;

        String queryStr = "FROM ComplaintStatusTypeEntity c " + "WHERE (c.id = :entityid) " + "AND (c.deleted = false)";

        Query query = entityManager.createQuery(queryStr, ComplaintStatusTypeEntity.class);
        query.setParameter("entityid", entityId);
        List<ComplaintStatusTypeEntity> result = query.getResultList();

        if (result == null || result.size() == 0) {
            String msg = msgUtil.getMessage("Data error.  Complaint Status Type [" + entityId + "] not found.");
            throw new EntityRetrievalException(msg);
        } else if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate Complaint Status Typeid in database.");
        } else if (result.size() == 1) {
            entity = result.get(0);
        }
        return entity;
    }

}
