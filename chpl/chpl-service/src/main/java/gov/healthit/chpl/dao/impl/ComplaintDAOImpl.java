package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Component;

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
        Query query = entityManager
                .createQuery(
                        "FROM ComplaintEntity c " + "JOIN FETCH c.certificationBody " + "JOIN FETCH c.complaintType "
                                + "JOIN FETCH c.complaintStatusType " + "WHERE c.deleted = false ",
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
    public ComplaintDTO create(ComplaintDTO complaintDTO) {
        ComplaintEntity entity = new ComplaintEntity();
        entity = populateEntity(entity, complaintDTO);
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
        entity = populateEntity(entity, complaintDTO);
        entity.setDeleted(false);
        entity.setLastModifiedUser(AuthUtil.getAuditId());

        entityManager.merge(entity);
        entityManager.flush();

        return new ComplaintDTO(entity);
    }

    @Override
    public void delete(ComplaintDTO complaintDTO) throws EntityRetrievalException {
        ComplaintEntity entity = getEntityById(complaintDTO.getId());
        entity.setDeleted(true);
        entity.setLastModifiedUser(AuthUtil.getAuditId());

        entityManager.merge(entity);
        entityManager.flush();
    }

    private ComplaintEntity populateEntity(ComplaintEntity entity, ComplaintDTO dto) {
        entity.setCertificationBody(new CertificationBodyEntity());
        entity.getCertificationBody().setId(dto.getCertificationBody().getId());
        entity.getCertificationBody().setName(dto.getCertificationBody().getName());
        entity.setComplaintType(new ComplaintTypeEntity());
        entity.getComplaintType().setId(dto.getComplaintType().getId());
        entity.getComplaintType().setName(dto.getComplaintType().getName());
        entity.setComplaintStatusType(new ComplaintStatusTypeEntity());
        entity.getComplaintStatusType().setId(dto.getComplaintStatusType().getId());
        entity.getComplaintStatusType().setName(dto.getComplaintStatusType().getName());
        entity.setOncComplaintId(dto.getOncComplaintId());
        entity.setReceivedDate(dto.getReceivedDate());
        entity.setSummary(dto.getSummary());
        entity.setActions(dto.getActions());
        entity.setComplainantContacted(dto.isComplainantContacted());
        entity.setDeveloperContacted(dto.isDeveloperContacted());
        entity.setOncAtlContacted(dto.isOncAtlContacted());
        entity.setClosedDate(dto.getClosedDate());
        return entity;
    }

    private ComplaintEntity getEntityById(Long id) throws EntityRetrievalException {
        ComplaintEntity entity = null;

        Query query = entityManager.createQuery(
                "FROM ComplaintEntity c " + "JOIN FETCH c.certificationBody " + "JOIN FETCH c.complaintType "
                        + "JOIN FETCH c.complaintStatusType " + "WHERE c.deleted = false " + "AND c.id = :complaintId",
                ComplaintEntity.class);
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

}
