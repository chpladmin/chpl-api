package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.CQMResultDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.CQMResultCriteriaDTO;
import gov.healthit.chpl.dto.CQMResultDTO;
import gov.healthit.chpl.entity.CQMResultCriteriaEntity;
import gov.healthit.chpl.entity.CQMResultEntity;

@Repository(value="cqmResultDAO")
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
			entity.setCertifiedProductId(cqmResult.getCertifiedProductId());
			entity.setSuccess(cqmResult.getSuccess());
			
			entity.setLastModifiedUser(Util.getCurrentUser().getId());
			entity.setLastModifiedDate(new Date());
			entity.setCreationDate(cqmResult.getCreationDate());
			entity.setDeleted(false);
			
			create(entity);	
		}
		
	}
	
	@Override
	public CQMResultCriteriaDTO createCriteriaMapping(CQMResultCriteriaDTO criteria) {
		CQMResultCriteriaEntity newMapping = new CQMResultCriteriaEntity();
		newMapping.setCertificationCriterionId(criteria.getCriterionId());
		newMapping.setCqmResultId(criteria.getCqmResultId());
		newMapping.setCreationDate(new Date());
		newMapping.setDeleted(false);
		newMapping.setLastModifiedDate(new Date());
		newMapping.setLastModifiedUser(Util.getCurrentUser().getId());
		entityManager.persist(newMapping);
		entityManager.flush();
		return new CQMResultCriteriaDTO(newMapping);
	}
	
	@Override
	public void update(CQMResultDTO cqmResult) throws EntityRetrievalException {
		
		CQMResultEntity entity = this.getEntityById(cqmResult.getId());
		entity.setCqmCriterionId(cqmResult.getCqmCriterionId());
		entity.setCreationDate(cqmResult.getCreationDate());
		entity.setDeleted(cqmResult.getDeleted());
		entity.setId(cqmResult.getId());
		entity.setLastModifiedDate(new Date());
		entity.setLastModifiedUser(Util.getCurrentUser().getId());
		entity.setSuccess(cqmResult.getSuccess());
		
		update(entity);
		
	}

	@Override
	public CQMResultCriteriaDTO updateCriteriaMapping(CQMResultCriteriaDTO dto) {
		CQMResultCriteriaEntity toUpdate = getCqmCriteriaById(dto.getId());
		if(toUpdate == null) {
			return null;
		}
		toUpdate.setCqmResultId(dto.getCqmResultId());
		toUpdate.setCertificationCriterionId(dto.getCriterionId());
		toUpdate.setLastModifiedDate(new Date());
		toUpdate.setLastModifiedUser(Util.getCurrentUser().getId());
		entityManager.merge(toUpdate);
		entityManager.flush();
		return new CQMResultCriteriaDTO(toUpdate);	
	}
	
	@Override
	public void delete(Long cqmResultId) {
		// TODO: How to delete this without leaving orphans
		deleteMappingsForCqmResult(cqmResultId);
		Query query = entityManager.createQuery("UPDATE CQMResultEntity SET deleted = true WHERE cqm_result_id = :resultid");
		query.setParameter("resultid", cqmResultId);
		query.executeUpdate();
	}
	
	@Override
	public void deleteByCertifiedProductId(Long productId) {
		List<CQMResultDTO> cqmResults = findByCertifiedProductId(productId);
		for(CQMResultDTO cqmResult : cqmResults) {
			deleteMappingsForCqmResult(cqmResult.getId());
		}
		Query query = entityManager.createQuery("UPDATE CQMResultEntity SET deleted = true WHERE certified_product_id = :productId");
		query.setParameter("productId", productId);
		query.executeUpdate();
	}

	@Override
	public void deleteCriteriaMapping(Long mappingId){
		CQMResultCriteriaEntity toDelete = getCqmCriteriaById(mappingId);
		if(toDelete != null) {
			toDelete.setDeleted(true);
			toDelete.setLastModifiedDate(new Date());
			toDelete.setLastModifiedUser(Util.getCurrentUser().getId());
			entityManager.persist(toDelete);
			entityManager.flush();
		}
	}
	
	@Override
	public void deleteMappingsForCqmResult(Long cqmResultId){
		Query query = entityManager.createQuery("UPDATE CQMResultCriteriaEntity SET deleted = true WHERE cqm_result_id = :resultid");
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
	public List<CQMResultDTO> findByCertifiedProductId(Long certifiedProductId){
		
		List<CQMResultEntity> entities = getEntitiesByCertifiedProductId(certifiedProductId);
		List<CQMResultDTO> cqmResults = new ArrayList<>();
		
		for (CQMResultEntity entity : entities) {
			CQMResultDTO cqmResult = new CQMResultDTO(entity);
			cqmResults.add(cqmResult);
		}
		return cqmResults;
		
	}

	@Override
	public CQMResultDTO getById(Long cqmResultId) throws EntityRetrievalException {
		
		CQMResultDTO dto = null;
		CQMResultEntity entity = getEntityById(cqmResultId);
		
		if (entity != null){
			dto = new CQMResultDTO(entity);
		}
		
		return dto;
	}
	
	@Override
	public List<CQMResultCriteriaDTO> getCriteriaForCqmResult(Long cqmResultId){
		
		List<CQMResultCriteriaEntity> entities = getCertCriteriaForCqmResult(cqmResultId);
		List<CQMResultCriteriaDTO> dtos = new ArrayList<CQMResultCriteriaDTO>();
		
		for (CQMResultCriteriaEntity entity : entities){
			CQMResultCriteriaDTO dto = new CQMResultCriteriaDTO(entity);
			dtos.add(dto);	
		}
		return dtos;
	}
	
	private void create(CQMResultEntity entity) {
		
		entityManager.persist(entity);
		entityManager.flush();
	}
	
	private void update(CQMResultEntity entity) {
		
		entityManager.merge(entity);	
		entityManager.flush();
	}
	
	private CQMResultEntity getEntityById(Long id) throws EntityRetrievalException {
		
		CQMResultEntity entity = null;
			
		Query query = entityManager.createQuery( "from CQMResultEntity where (NOT deleted = true) AND (cqm_result_id = :entityid) ", CQMResultEntity.class );
		query.setParameter("entityid", id);
		List<CQMResultEntity> result = query.getResultList();
		
		if (result.size() > 1){
			throw new EntityRetrievalException("Data error. Duplicate CQM result id in database.");
		}
		
		if (result.size() > 0){
			entity = result.get(0);
		}
			
		return entity;
	}
	
	private List<CQMResultEntity> getAllEntities() {
		
		List<CQMResultEntity> result = entityManager.createQuery( "from CQMResultEntity where (NOT deleted = true) ", CQMResultEntity.class).getResultList();
		return result;
	}
	
	private List<CQMResultEntity> getEntitiesByCertifiedProductId(Long certifiedProductId) {
		
		Query query = entityManager.createQuery( "from CQMResultEntity where (NOT deleted = true) AND (certified_product_id = :entityid) ", CQMResultEntity.class );
		query.setParameter("entityid", certifiedProductId);
		List<CQMResultEntity> result = query.getResultList();
		return result;
	}

	private CQMResultCriteriaEntity getCqmCriteriaById(Long id) {
		CQMResultCriteriaEntity entity = null;
		
		Query query = entityManager.createQuery( "from CQMResultCriteriaEntity "
				+ "where (NOT deleted = true) AND (id = :entityid) ", 
				CQMResultCriteriaEntity.class );
		query.setParameter("entityid", id);
		List<CQMResultCriteriaEntity> result = query.getResultList();

		if (result.size() > 0){
			entity = result.get(0);
		}
		return entity;
	}
	
	private List<CQMResultCriteriaEntity> getCertCriteriaForCqmResult(Long cqmResultId){
		Query query = entityManager.createQuery( "from CQMResultCriteriaEntity "
				+ "where (NOT deleted = true) AND (cqm_result_id = :cqmResultId) ", 
				CQMResultCriteriaEntity.class );
		query.setParameter("cqmResultId", cqmResultId);
		
		List<CQMResultCriteriaEntity> result = query.getResultList();
		if(result == null) {
			return null;
		}
		return result;
	}
}
