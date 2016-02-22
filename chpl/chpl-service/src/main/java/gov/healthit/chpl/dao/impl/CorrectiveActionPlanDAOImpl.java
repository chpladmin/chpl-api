package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.CorrectiveActionPlanDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.CorrectiveActionPlanDTO;
import gov.healthit.chpl.entity.CorrectiveActionPlanEntity;

@Repository("correctiveActionPlanDAO")
public class CorrectiveActionPlanDAOImpl extends BaseDAOImpl implements CorrectiveActionPlanDAO {
	
	@Override
	public CorrectiveActionPlanDTO create(CorrectiveActionPlanDTO toCreate) throws EntityCreationException,
		EntityRetrievalException {
		CorrectiveActionPlanEntity entity = null;
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
			entity = new CorrectiveActionPlanEntity();
			entity.setRequiredCompletionDate(toCreate.getRequiredCompletionDate());
			entity.setActualCompletionDate(toCreate.getActualCompletionDate());
			entity.setApprovalDate(toCreate.getApprovalDate());
			entity.setStartDate(toCreate.getStartDate());
			entity.setNonComplianceDeterminationDate(toCreate.getNonComplianceDeterminationDate());
			entity.setCertifiedProductId(toCreate.getCertifiedProductId());
			entity.setSurveillanceStartDate(toCreate.getSurveillanceStartDate());
			entity.setSurveillanceEndDate(toCreate.getSurveillanceEndDate());
			entity.setSurveillanceResult(toCreate.getSurveillanceResult());
			
			entity.setCreationDate(new Date());
			entity.setDeleted(false);
			entity.setLastModifiedDate(new Date());
			entity.setLastModifiedUser(Util.getCurrentUser().getId());
			create(entity);
			return new CorrectiveActionPlanDTO(entity);
		}
	}

	@Override
	public CorrectiveActionPlanDTO update(CorrectiveActionPlanDTO toUpdate) throws EntityRetrievalException {
		CorrectiveActionPlanEntity entity = getEntityById(toUpdate.getId());
		if(entity == null) {
			throw new EntityRetrievalException("Entity with id " + toUpdate.getId() + " does not exist.");
		}
		
		if(toUpdate.getCertifiedProductId() != null) {
			entity.setCertifiedProductId(toUpdate.getCertifiedProductId());
		}
		
		entity.setRequiredCompletionDate(toUpdate.getRequiredCompletionDate());
		entity.setActualCompletionDate(toUpdate.getActualCompletionDate());
		entity.setApprovalDate(toUpdate.getApprovalDate());
		entity.setStartDate(toUpdate.getStartDate());
		entity.setNonComplianceDeterminationDate(toUpdate.getNonComplianceDeterminationDate());
		entity.setCertifiedProductId(toUpdate.getCertifiedProductId());
		entity.setSurveillanceStartDate(toUpdate.getSurveillanceStartDate());
		entity.setSurveillanceEndDate(toUpdate.getSurveillanceEndDate());
		entity.setSurveillanceResult(toUpdate.getSurveillanceResult());
		
		entity.setLastModifiedDate(new Date());
		entity.setLastModifiedUser(Util.getCurrentUser().getId());
		
		update(entity);
		return new CorrectiveActionPlanDTO(entity);
	}
	
	@Override
	public CorrectiveActionPlanDTO getById(Long id) throws EntityRetrievalException {
		CorrectiveActionPlanDTO dto = null;
		CorrectiveActionPlanEntity entity = getEntityById(id);
		if(entity != null) {
			dto = new CorrectiveActionPlanDTO(entity);
		}
		return dto;
	}


	@Override
	public List<CorrectiveActionPlanDTO> getAllForCertifiedProduct(Long certifiedProductId) {
		List<CorrectiveActionPlanEntity> entities = getEntitiesByCertifiedProductId(certifiedProductId);
		List<CorrectiveActionPlanDTO> dtos = new ArrayList<CorrectiveActionPlanDTO>();
		
		for(CorrectiveActionPlanEntity entity : entities) {
			CorrectiveActionPlanDTO dto = new CorrectiveActionPlanDTO(entity);
			dtos.add(dto);
		}
		return dtos;
	}

	@Override
	public void delete(Long id) throws EntityRetrievalException {
		CorrectiveActionPlanEntity entity = getEntityById(id);
		entity.setDeleted(true);
		entity.setLastModifiedDate(new Date());
		entity.setLastModifiedUser(Util.getCurrentUser().getId());
		update(entity);
	}
	
	private void create(CorrectiveActionPlanEntity entity) {
		
		entityManager.persist(entity);
		entityManager.flush();
		
	}
	
	private void update(CorrectiveActionPlanEntity entity) {
		
		entityManager.merge(entity);	
		entityManager.flush();
	}
	
	public CorrectiveActionPlanEntity getEntityById(Long id) throws EntityRetrievalException {
		
		CorrectiveActionPlanEntity entity = null;
			
		Query query = entityManager.createQuery( "from CorrectiveActionPlanEntity where (NOT deleted = true) AND (corrective_action_plan_id = :entityid) ", CorrectiveActionPlanEntity.class );
		query.setParameter("entityid", id);
		List<CorrectiveActionPlanEntity> result = query.getResultList();
		
		if (result.size() > 1){
			throw new EntityRetrievalException("Data error. Duplicate corrective action plan id in database.");
		}
		
		if (result.size() > 0){
			entity = result.get(0);
		}
		
		return entity;
	}
	
	
	private List<CorrectiveActionPlanEntity> getEntitiesByCertifiedProductId(Long id) {
		
		Query query = entityManager.createQuery( "from CorrectiveActionPlanEntity where (NOT deleted = true) AND (certified_product_id = :certified_product_id) ", CorrectiveActionPlanEntity.class );
		query.setParameter("certified_product_id", id);
		List<CorrectiveActionPlanEntity> result = query.getResultList();
		
		return result;
	}	
}