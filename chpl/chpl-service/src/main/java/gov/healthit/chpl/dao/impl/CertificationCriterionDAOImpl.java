package gov.healthit.chpl.dao.impl;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.CertificationResultDTO;
import gov.healthit.chpl.entity.CertificationCriterionEntity;
import gov.healthit.chpl.entity.CertificationResultEntity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

public class CertificationCriterionDAOImpl extends BaseDAOImpl implements CertificationCriterionDAO {
	
	@Override
	public void create(CertificationCriterionDTO dto) throws EntityCreationException, EntityRetrievalException {
		
		CertificationCriterionEntity entity = null;
		try {
			if (dto.getId() != null){
				entity = this.getEntityById(dto.getId());
			}
		} catch (EntityRetrievalException e) {
			throw new EntityCreationException(e);
		}
		
		if (entity != null) {
			throw new EntityCreationException("An entity with this ID already exists.");
		} else {
			
			entity = new CertificationCriterionEntity();
			entity.setAutomatedMeasureCapable(dto.getAutomatedMeasureCapable());
			entity.setAutomatedNumeratorCapable(dto.getAutomatedNumeratorCapable());
			entity.setCertificationEdition(dto.getCertificationEditionId());
			entity.setCreationDate(dto.getCreationDate());
			entity.setDeleted(dto.getDeleted());
			entity.setDescription(dto.getDescription());
			entity.setId(dto.getId());
			//entity.setLastModifiedDate(result.getLastModifiedDate());
			entity.setLastModifiedUser(Util.getCurrentUser().getId());
			entity.setNumber(dto.getNumber());
			entity.setParentCriterion(this.getEntityById(dto.getParentCriterionId()));
			entity.setRequiresSed(dto.getRequiresSed());
			entity.setTitle(dto.getTitle());
			
			create(entity);	
		}
	}

	@Override
	public void update(CertificationCriterionDTO dto) throws EntityRetrievalException, EntityCreationException {
		
		CertificationCriterionEntity entity = this.getEntityById(dto.getId());;
			
		entity = new CertificationCriterionEntity();
		entity.setAutomatedMeasureCapable(dto.getAutomatedMeasureCapable());
		entity.setAutomatedNumeratorCapable(dto.getAutomatedNumeratorCapable());
		entity.setCertificationEdition(dto.getCertificationEditionId());
		entity.setCreationDate(dto.getCreationDate());
		entity.setDeleted(dto.getDeleted());
		entity.setDescription(dto.getDescription());
		entity.setId(dto.getId());
		//entity.setLastModifiedDate(result.getLastModifiedDate());
		entity.setLastModifiedUser(Util.getCurrentUser().getId());
		entity.setNumber(dto.getNumber());
		entity.setParentCriterion(this.getEntityById(dto.getParentCriterionId()));
		entity.setRequiresSed(dto.getRequiresSed());
		entity.setTitle(dto.getTitle());
		
		update(entity);
	}
	
	@Override
	public void delete(Long criterionId) {
		
		// TODO: How to delete this without leaving orphans
		Query query = entityManager.createQuery("UPDATE CertificationResultEntity SET deleted = true WHERE certification_criterion_id = :entityid");
		query.setParameter("entityid", criterionId);
		query.executeUpdate();
		
	}
	
	@Override
	public List<CertificationCriterionDTO> findAll() {
		
		List<CertificationCriterionEntity> entities = getAllEntities();
		List<CertificationCriterionDTO> dtos = new ArrayList<>();
		
		for (CertificationCriterionEntity entity : entities) {
			CertificationCriterionDTO dto = new CertificationCriterionDTO(entity);
			dtos.add(dto);
		}
		return dtos;
	}
	
	@Override
	public CertificationCriterionDTO getById(Long criterionId) throws EntityRetrievalException {
		CertificationCriterionEntity entity = getEntityById(criterionId);
		CertificationCriterionDTO dto = new CertificationCriterionDTO(entity);
		return dto;
	}
	
	private void create(CertificationCriterionEntity entity) {
		
		entityManager.persist(entity);
		
	}
	
	private void update(CertificationCriterionEntity entity) {
		
		entityManager.merge(entity);	
	
	}
	
	private List<CertificationCriterionEntity> getAllEntities() {
		
		List<CertificationCriterionEntity> result = entityManager.createQuery( "from CertificationCriterionEntity where (NOT deleted = true) ", CertificationCriterionEntity.class).getResultList();
		return result;
		
	}
	
	private CertificationCriterionEntity getEntityById(Long id) throws EntityRetrievalException {
		
		CertificationCriterionEntity entity = null;
			
		Query query = entityManager.createQuery( "from CertificationCriterionEntity where (NOT deleted = true) AND (certification_criterion_id = :entityid) ", CertificationCriterionEntity.class );
		query.setParameter("entityid", id);
		List<CertificationCriterionEntity> result = query.getResultList();
		
		if (result.size() > 1){
			throw new EntityRetrievalException("Data error. Duplicate criterion id in database.");
		}
		
		if (result.size() < 0){
			entity = result.get(0);
		}
			
		return entity;
	}
	
}
