package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.CorrectiveActionPlanDAO;
import gov.healthit.chpl.dao.CorrectiveActionPlanDocumentationDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.CorrectiveActionPlanDocumentationDTO;
import gov.healthit.chpl.entity.CorrectiveActionPlanDocumentationEntity;
import gov.healthit.chpl.entity.CorrectiveActionPlanEntity;

@Repository("correctiveActionPlanDocumentationDAO")
public class CorrectiveActionPlanDocumentationDAOImpl extends BaseDAOImpl implements CorrectiveActionPlanDocumentationDAO {
	
	@Autowired CorrectiveActionPlanDAO capDao;
	
	@Override
	public CorrectiveActionPlanDocumentationDTO create(CorrectiveActionPlanDocumentationDTO toCreate) throws EntityCreationException,
		EntityRetrievalException {
		CorrectiveActionPlanDocumentationEntity entity = null;
		try {
			if(toCreate.getId() != null) {
				entity = getEntityById(toCreate.getId());
			}
		} catch(EntityRetrievalException e) {
			throw new EntityCreationException(e);
		}
		
		if(entity != null) {
			throw new EntityCreationException("An entity with this id already exists.");
		} else {
			CorrectiveActionPlanEntity plan = capDao.getEntityById(toCreate.getCorrectiveActionPlanId());
			entity = new CorrectiveActionPlanDocumentationEntity();
			entity.setCorrectiveActionPlan(plan);
			entity.setFileData(toCreate.getFileData());
			entity.setFileName(toCreate.getFileName());
			entity.setFileType(toCreate.getFileType());
			entity.setCreationDate(new Date());
			entity.setDeleted(false);
			entity.setLastModifiedDate(new Date());
			entity.setLastModifiedUser(Util.getCurrentUser().getId());
			create(entity);
			return new CorrectiveActionPlanDocumentationDTO(entity);
		}
	}

	@Override
	public CorrectiveActionPlanDocumentationDTO getById(Long id) throws EntityRetrievalException {
		CorrectiveActionPlanDocumentationDTO dto = null;
		CorrectiveActionPlanDocumentationEntity entity = getEntityById(id);
		if(entity != null) {
			dto = new CorrectiveActionPlanDocumentationDTO(entity);
		}
		return dto;
	}


	@Override
	public List<CorrectiveActionPlanDocumentationDTO> getAllForCorrectiveActionPlan(Long correctiveActionPlanId) {
		List<CorrectiveActionPlanDocumentationEntity> entities = getEntitiesByCorrectiveActionPlan(correctiveActionPlanId);
		List<CorrectiveActionPlanDocumentationDTO> dtos = new ArrayList<CorrectiveActionPlanDocumentationDTO>();
		
		for(CorrectiveActionPlanDocumentationEntity entity : entities) {
			CorrectiveActionPlanDocumentationDTO dto = new CorrectiveActionPlanDocumentationDTO(entity);
			dtos.add(dto);
		}
		return dtos;
	}

	@Override
	public void delete(Long id) throws EntityRetrievalException {
		CorrectiveActionPlanDocumentationEntity entity = getEntityById(id);
		entity.setDeleted(true);
		entity.setLastModifiedDate(new Date());
		entity.setLastModifiedUser(Util.getCurrentUser().getId());
		update(entity);
	}
	
	private void create(CorrectiveActionPlanDocumentationEntity entity) {
		
		entityManager.persist(entity);
		entityManager.flush();
		
	}
	
	private void update(CorrectiveActionPlanDocumentationEntity entity) {
		
		entityManager.merge(entity);	
		entityManager.flush();
	}
	
	private CorrectiveActionPlanDocumentationEntity getEntityById(Long id) throws EntityRetrievalException {
		
		CorrectiveActionPlanDocumentationEntity entity = null;
			
		Query query = entityManager.createQuery( "from CorrectiveActionPlanDocumentationEntity capDoc "
				+ "JOIN FETCH capDoc.correctiveActionPlan cap " 
				+ "where (NOT capDoc.deleted = true) AND (capDoc.id = :entityid) ", CorrectiveActionPlanDocumentationEntity.class );
		query.setParameter("entityid", id);
		List<CorrectiveActionPlanDocumentationEntity> result = query.getResultList();
		
		if (result.size() > 1){
			throw new EntityRetrievalException("Data error. Duplicate corrective action plan documentation id in database.");
		}
		
		if (result.size() > 0){
			entity = result.get(0);
		}
		
		return entity;
	}
	
	
	
	private List<CorrectiveActionPlanDocumentationEntity> getEntitiesByCorrectiveActionPlan(Long id) {
		
		Query query = entityManager.createQuery( "from CorrectiveActionPlanDocumentationEntity capDoc "
				+ "JOIN FETCH capDoc.correctiveActionPlan cap "
				+ "where (NOT capDoc.deleted = true) AND (cap.id = :corrective_action_plan_id) ", CorrectiveActionPlanDocumentationEntity.class );
		query.setParameter("corrective_action_plan_id", id);
		List<CorrectiveActionPlanDocumentationEntity> result = query.getResultList();
		
		return result;
	}	
}