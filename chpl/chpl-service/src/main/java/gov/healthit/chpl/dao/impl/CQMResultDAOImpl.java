package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.CQMResultDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.CQMResultDTO;
import gov.healthit.chpl.dto.CQMResultAdditionalSoftwareMapDTO;
import gov.healthit.chpl.entity.CQMResultEntity;
import gov.healthit.chpl.entity.CQMResultAdditionalSoftwareMapEntity;

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
	public void update(CQMResultDTO cqmResult) throws EntityRetrievalException {
		
		CQMResultEntity entity = this.getEntityById(cqmResult.getId());
		entity.setCqmCriterionId(cqmResult.getCqmCriterionId());
		entity.setCreationDate(cqmResult.getCreationDate());
		entity.setDeleted(cqmResult.getDeleted());
		entity.setId(cqmResult.getId());
		//entity.setLastModifiedDate(cqmResult.getLastModifiedDate());
		entity.setLastModifiedUser(Util.getCurrentUser().getId());;
		entity.setSuccess(cqmResult.getSuccess());
		
		update(entity);
		
	}

	@Override
	public void delete(Long cqmResultId) {
		// TODO: How to delete this without leaving orphans
		Query query = entityManager.createQuery("UPDATE CQMResultEntity SET deleted = true WHERE cqm_result_id = :resultid");
		query.setParameter("resultid", cqmResultId);
		query.executeUpdate();
	}
	
	@Override
	public void deleteByCertifiedProductId(Long productId) {
		Query query = entityManager.createQuery("UPDATE CQMResultEntity SET deleted = true WHERE certified_product_id = :productId");
		query.setParameter("productId", productId);
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
	public CQMResultAdditionalSoftwareMapDTO createAdditionalSoftwareMapping(CQMResultAdditionalSoftwareMapDTO dto) throws EntityCreationException{
		CQMResultAdditionalSoftwareMapEntity mapping = getCQMResultAdditionalSoftwareMap(dto.getCqmResultId(), dto.getAdditionalSoftwareId());
		if(mapping != null) {
			throw new EntityCreationException("A CQM Result - Additional Software mapping entity with this ID pair already exists.");
		}
		
		mapping = new CQMResultAdditionalSoftwareMapEntity();
		mapping.setCQMResultId(dto.getCqmResultId());
		mapping.setAdditionalSoftwareId(dto.getAdditionalSoftwareId());
		mapping.setCreationDate(new Date());
		mapping.setDeleted(false);
		mapping.setLastModifiedDate(new Date());
		mapping.setLastModifiedUser(Util.getCurrentUser().getId());
		entityManager.persist(mapping);
		entityManager.flush();
		
		return new CQMResultAdditionalSoftwareMapDTO(mapping);
		
	}
	
	@Override
	public CQMResultAdditionalSoftwareMapDTO updateAdditionalSoftwareMapping(CQMResultAdditionalSoftwareMapDTO dto){
		
		CQMResultAdditionalSoftwareMapEntity mapping = getCQMResultAdditionalSoftwareMap(dto.getCqmResultId(), dto.getAdditionalSoftwareId());
		if(mapping == null) {
			return null;
		}
		mapping.setCQMResultId(dto.getCqmResultId());
		mapping.setAdditionalSoftwareId(dto.getAdditionalSoftwareId());
		mapping.setDeleted(dto.getDeleted());
		mapping.setLastModifiedDate(dto.getLastModifiedDate());
		mapping.setLastModifiedUser(Util.getCurrentUser().getId());
		entityManager.persist(mapping);
		entityManager.flush();
		return new CQMResultAdditionalSoftwareMapDTO(mapping);
	}
	
	@Override
	public void deleteAdditionalSoftwareMapping(Long CQMResultId, Long additionalSoftwareId){
		CQMResultAdditionalSoftwareMapEntity toDelete = getCQMResultAdditionalSoftwareMap(CQMResultId, additionalSoftwareId);
		if(toDelete != null) {
			toDelete.setDeleted(true);
			toDelete.setLastModifiedDate(new Date());
			toDelete.setLastModifiedUser(Util.getCurrentUser().getId());
			entityManager.persist(toDelete);
			entityManager.flush();
		}
	}
	
	@Override
	public CQMResultAdditionalSoftwareMapDTO getAdditionalSoftwareMapping(Long CQMResultId, Long additionalSoftwareId){
		
		CQMResultAdditionalSoftwareMapEntity mapping = getCQMResultAdditionalSoftwareMap(CQMResultId, additionalSoftwareId);
		if (mapping == null){
			return null;
		}
		return new CQMResultAdditionalSoftwareMapDTO(mapping);
	}
	
	private CQMResultAdditionalSoftwareMapEntity getCQMResultAdditionalSoftwareMap(Long cqmResultId, Long additionalSoftwareId){
		Query query = entityManager.createQuery( "FROM CQMResultAdditionalSoftwareMapEntity where "
				+ "(NOT deleted = true) "
				+ "AND cqm_result_id = :cqmResultId "
				+ "AND additional_software_id = :additionalSoftwareId", CQMResultAdditionalSoftwareMapEntity.class);
		query.setParameter("cqmResultId", cqmResultId);
		query.setParameter("additionalSoftwareId", additionalSoftwareId);
		
		Object result = null;
		try {
			result = query.getSingleResult();
		}
		catch(NoResultException ex) {}
		catch(NonUniqueResultException ex) {}
		
		if(result == null) {
			return null;
		}
		return (CQMResultAdditionalSoftwareMapEntity)result;
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

}
