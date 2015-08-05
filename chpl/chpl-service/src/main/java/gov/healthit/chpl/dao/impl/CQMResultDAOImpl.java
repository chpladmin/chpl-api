package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import gov.healthit.chpl.dao.CQMResultDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.CQMResultDTO;
import gov.healthit.chpl.entity.CQMResultEntity;

public class CQMResultDAOImpl extends BaseDAOImpl implements CQMResultDAO {

	@Override
	public void create(CQMResultDTO cqmResult) throws EntityCreationException {
		
		CQMResultEntity entity = null;
		try {
			if (cqmResult.getId() != null){
				entity = this.getEntityById(cqmResult.getId());
			}
		} catch (EntityRetrievalException e) {
			throw new EntityCreationException(e);
		}
		
		if (entity != null) {
			throw new EntityCreationException("An entity with this ID already exists.");
		} else {
			
			entity = new CQMResultEntity();
			entity.setCqmCriterionId(cqmResult.getCqmCriterionId());
			entity.setCqmVersionId(cqmResult.getCqmVersionId());
			entity.setCreationDate(cqmResult.getCreationDate());
			entity.setDeleted(cqmResult.getDeleted());
			entity.setId(cqmResult.getId());
			entity.setLastModifiedDate(cqmResult.getLastModifiedDate());
			entity.setLastModifiedUser(cqmResult.getLastModifiedUser());
			entity.setSuccess(cqmResult.getSuccess());
			
			create(entity);	
		}
		
	}
	
	@Override
	public void update(CQMResultDTO cqmResult) throws EntityRetrievalException {
		
		CQMResultEntity entity = this.getEntityById(cqmResult.getId());
		entity.setCqmCriterionId(cqmResult.getCqmCriterionId());
		entity.setCqmVersionId(cqmResult.getCqmVersionId());
		entity.setCreationDate(cqmResult.getCreationDate());
		entity.setDeleted(cqmResult.getDeleted());
		entity.setId(cqmResult.getId());
		entity.setLastModifiedDate(cqmResult.getLastModifiedDate());
		entity.setLastModifiedUser(cqmResult.getLastModifiedUser());
		entity.setSuccess(cqmResult.getSuccess());
		
		create(entity);	
		
	}

	@Override
	public void delete(Long cqmResultId) {
		// TODO: How to delete this without leaving orphans
		Query query = entityManager.createQuery("UPDATE CQMResultEntity SET deleted = true WHERE cqm_result_id = :resultid");
		query.setParameter("resultid", cqmResultId);
		query.executeUpdate();
	}

	@Override
	public List<CQMResultDTO> findAll() {
		
		List<CQMResultEntity> entities = getAllEntities();
		List<CQMResultDTO> cqmResults = new ArrayList<>();
		
		for (CQMResultEntity entity : entities) {
			CQMResultDTO cqmResult = new CQMResultDTO(entity);
			cqmResults.add(cqmResult);
		}
		return cqmResults;
	}

	@Override
	public CQMResultDTO getById(Long cqmResultId) throws EntityRetrievalException {
		CQMResultEntity entity = getEntityById(cqmResultId);
		CQMResultDTO dto = new CQMResultDTO(entity);
		return dto;
	}
	
	private void create(CQMResultEntity entity) {
		
		entityManager.persist(entity);
		
	}
	
	private void update(CQMResultEntity entity) {
		
		entityManager.merge(entity);	
	
	}
	
	private CQMResultEntity getEntityById(Long id) throws EntityRetrievalException {
		
		CQMResultEntity entity = null;
			
		Query query = entityManager.createQuery( "from CQMResultEntity where (NOT deleted = true) AND (cqm_result_id = :entityid) ", CQMResultEntity.class );
		query.setParameter("entityid", id);
		List<CQMResultEntity> result = query.getResultList();
		
		if (result.size() > 1){
			throw new EntityRetrievalException("Data error. CQM result id in database.");
		}
		
		if (result.size() < 0){
			entity = result.get(0);
		}
			
		return entity;
	}
	
	private List<CQMResultEntity> getAllEntities() {
		
		List<CQMResultEntity> result = entityManager.createQuery( "from CQMResultEntity where (NOT deleted = true) ", CQMResultEntity.class).getResultList();
		return result;
	}

}
