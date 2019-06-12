package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.ComplaintDAO;
import gov.healthit.chpl.dao.ComplaintDTO;
import gov.healthit.chpl.dto.ComplaintStatusTypeDTO;
import gov.healthit.chpl.dto.ComplaintTypeDTO;
import gov.healthit.chpl.entity.ComplaintEntity;
import gov.healthit.chpl.entity.ComplaintStatusTypeEntity;
import gov.healthit.chpl.entity.ComplaintTypeEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.ChplProductNumberUtil;

@Component
public class ComplaintDAOImpl extends BaseDAOImpl implements ComplaintDAO {

    private ChplProductNumberUtil chplProductNumberUtil;

    @Autowired
    public ComplaintDAOImpl(final ChplProductNumberUtil chplProductNumberUtil) {
        this.chplProductNumberUtil = chplProductNumberUtil;
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
        Query query = entityManager.createQuery("FROM ComplaintEntity c " + "JOIN FETCH c.certificationBody "
                + "LEFT JOIN FETCH c.complaintType " + "LEFT JOIN FETCH c.complaintStatusType "
                + "LEFT JOIN FETCH c.listings " + "WHERE c.deleted = false ", ComplaintEntity.class);
        List<ComplaintEntity> results = query.getResultList();

        List<ComplaintDTO> complaintDTOs = new ArrayList<ComplaintDTO>();
        for (ComplaintEntity entity : results) {
            ChplProductNumberUtil
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

        entity.setCertificationBodyId(complaintDTO.getCertificationBody().getId());
        entity.setComplaintTypeId(complaintDTO.getComplaintType().getId());
        entity.setComplaintStatusTypeId(complaintDTO.getComplaintStatusType().getId());
        entity.setOncComplaintId(complaintDTO.getOncComplaintId());
        entity.setAcbComplaintId(complaintDTO.getAcbComplaintId());
        entity.setReceivedDate(complaintDTO.getReceivedDate());
        entity.setSummary(complaintDTO.getSummary());
        entity.setActions(complaintDTO.getActions());
        entity.setComplainantContacted(complaintDTO.isComplainantContacted());
        entity.setDeveloperContacted(complaintDTO.isDeveloperContacted());
        entity.setOncAtlContacted(complaintDTO.isOncAtlContacted());
        entity.setFlagForOncReview(complaintDTO.isFlagForOncReview());
        entity.setClosedDate(null);
        entity.setDeleted(false);
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        entity.setCreationDate(new Date());
        entity.setLastModifiedDate(new Date());

        entityManager.persist(entity);
        entityManager.flush();
        entityManager.clear();
        return new ComplaintDTO(getEntityById(entity.getId()));
    }

    @Override
    public ComplaintDTO update(ComplaintDTO complaintDTO) throws EntityRetrievalException {
        ComplaintEntity entity = getEntityById(complaintDTO.getId());

        entity.setCertificationBodyId(complaintDTO.getCertificationBody().getId());
        entity.setComplaintTypeId(complaintDTO.getComplaintType().getId());
        entity.setComplaintStatusTypeId(complaintDTO.getComplaintStatusType().getId());
        entity.setOncComplaintId(complaintDTO.getOncComplaintId());
        entity.setAcbComplaintId(complaintDTO.getAcbComplaintId());
        entity.setReceivedDate(complaintDTO.getReceivedDate());
        entity.setSummary(complaintDTO.getSummary());
        entity.setActions(complaintDTO.getActions());
        entity.setComplainantContacted(complaintDTO.isComplainantContacted());
        entity.setDeveloperContacted(complaintDTO.isDeveloperContacted());
        entity.setOncAtlContacted(complaintDTO.isOncAtlContacted());
        entity.setFlagForOncReview(complaintDTO.isFlagForOncReview());
        entity.setClosedDate(complaintDTO.getClosedDate());
        entity.setDeleted(false);
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        entity.setLastModifiedDate(new Date());

        entityManager.merge(entity);
        entityManager.flush();
        entityManager.clear();

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
        entityManager.clear();
    }

    private ComplaintEntity getEntityById(Long id) throws EntityRetrievalException {
        ComplaintEntity entity = null;

        Query query = entityManager.createQuery("FROM ComplaintEntity c " + "JOIN FETCH c.certificationBody "
                + "LEFT JOIN FETCH c.complaintType " + "LEFT JOIN FETCH c.complaintStatusType "
                + "LEFT JOIN FETCH c.listings " + "WHERE c.deleted = false " + "AND c.id = :complaintId",
                ComplaintEntity.class);
        query.setParameter("complaintId", id);
        List<ComplaintEntity> result = query.getResultList();

        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate complaint id in database.");
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
}
