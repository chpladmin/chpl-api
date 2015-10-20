package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.CertificationResultDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.CertificationResultDTO;
import gov.healthit.chpl.entity.CertificationResultEntity;

@Repository(value="certificationResultDAO")
public class CertificationResultDAOImpl extends BaseDAOImpl implements CertificationResultDAO {

	@Override
	public void create(CertificationResultDTO result) throws EntityCreationException {
		
		CertificationResultEntity entity = null;
		try {
			if (result.getId() != null){
				entity = this.getEntityById(result.getId());
			}
		} catch (EntityRetrievalException e) {
			throw new EntityCreationException(e);
		}
		
		if (entity != null) {
			throw new EntityCreationException("An entity with this ID already exists.");
		} else {
			
			entity = new CertificationResultEntity();
			entity.setAutomatedMeasureCapable(result.getAutomatedMeasureCapable());
			entity.setAutomatedNumerator(result.getAutomatedNumerator());
			entity.setCertificationCriterionId(result.getCertificationCriterionId());
			entity.setCertifiedProductId(result.getCertifiedProductId());
			entity.setGap(result.getGap());
			entity.setInherited(result.getInherited());
			entity.setSedInherited(result.getSedInherited());
			entity.setSedSuccessful(result.getSedSuccessful());
			entity.setSuccess(result.getSuccessful());
			entity.setTestDataVersionId(result.getTestDataVersionId());
			entity.setTestProcedureVersionId(result.getTestProcedureVersionId());
			
			entity.setLastModifiedDate(new Date());
			entity.setLastModifiedUser(Util.getCurrentUser().getId());
			entity.setCreationDate(result.getCreationDate());
			entity.setDeleted(false);
			
			create(entity);	
		}
	}

	@Override
	public void update(CertificationResultDTO result) throws EntityRetrievalException {
	
		CertificationResultEntity entity = getEntityById(result.getId());
		
		entity.setAutomatedMeasureCapable(result.getAutomatedMeasureCapable());
		entity.setAutomatedNumerator(result.getAutomatedNumerator());
		entity.setCertificationCriterionId(result.getCertificationCriterionId());
		entity.setCertifiedProductId(result.getCertifiedProductId());
		entity.setCreationDate(result.getCreationDate());
		entity.setDeleted(result.getDeleted());
		entity.setGap(result.getGap());
		entity.setInherited(result.getInherited());
		//entity.setLastModifiedDate(result.getLastModifiedDate());
		entity.setLastModifiedUser(Util.getCurrentUser().getId());
		entity.setSedInherited(result.getSedInherited());
		entity.setSedSuccessful(result.getSedSuccessful());
		entity.setSuccess(result.getSuccessful());
		entity.setTestDataVersionId(result.getTestDataVersionId());
		entity.setTestProcedureVersionId(result.getTestProcedureVersionId());
		
		update(entity);	
	}

	@Override
	public void delete(Long resultId) {
		
		// TODO: How to delete this without leaving orphans
		Query query = entityManager.createQuery("UPDATE CertificationResultEntity SET deleted = true WHERE certification_result_id = :resultid");
		query.setParameter("resultid", resultId);
		query.executeUpdate();
		
	}

	@Override
	public void deleteByCertifiedProductId(Long certifiedProductId) {
		
		// TODO: How to delete this without leaving orphans
		Query query = entityManager.createQuery("UPDATE CertificationResultEntity SET deleted = true WHERE certified_product_id = :certifiedProductId");
		query.setParameter("certifiedProductId", certifiedProductId);
		query.executeUpdate();
		
	}
	
	@Override
	public List<CertificationResultDTO> findAll() {
			
		List<CertificationResultEntity> entities = getAllEntities();
		List<CertificationResultDTO> products = new ArrayList<>();
		
		for (CertificationResultEntity entity : entities) {
			CertificationResultDTO result = new CertificationResultDTO(entity);
			products.add(result);
		}
		return products;
		
	}

	@Override
	public CertificationResultDTO getById(Long resultId) throws EntityRetrievalException {
		
		CertificationResultDTO dto = null;
		CertificationResultEntity entity = getEntityById(resultId);
		
		if (entity != null){
			dto = new CertificationResultDTO(entity);
		}
		return dto;
	}
	
	private void create(CertificationResultEntity entity) {
		
		entityManager.persist(entity);
		entityManager.flush();
		
	}
	
	private void update(CertificationResultEntity entity) {
		
		entityManager.merge(entity);	
		entityManager.flush();
	
	}
	
	private List<CertificationResultEntity> getAllEntities() {
		
		List<CertificationResultEntity> result = entityManager.createQuery( "from CertificationResultEntity where (NOT deleted = true) ", CertificationResultEntity.class).getResultList();
		return result;
		
	}
	
	private CertificationResultEntity getEntityById(Long id) throws EntityRetrievalException {
		
		CertificationResultEntity entity = null;
			
		Query query = entityManager.createQuery( "from CertificationResultEntity where (NOT deleted = true) AND (cqm_result_id = :entityid) ", CertificationResultEntity.class );
		query.setParameter("entityid", id);
		List<CertificationResultEntity> result = query.getResultList();
		
		if (result.size() > 1){
			throw new EntityRetrievalException("Data error. Duplicate result id in database.");
		}
		
		if (result.size() > 0){
			entity = result.get(0);
		}
			
		return entity;
	}
	
	@Override
	public List<CertificationResultDTO> findByCertifiedProductId(
			Long certifiedProductId) {
		List<CertificationResultEntity> entities = getEntitiesByCertifiedProductId(certifiedProductId);
		List<CertificationResultDTO> cqmResults = new ArrayList<>();
		
		for (CertificationResultEntity entity : entities) {
			CertificationResultDTO cqmResult = new CertificationResultDTO(entity);
			cqmResults.add(cqmResult);
		}
		return cqmResults;
	}
	
	private List<CertificationResultEntity> getEntitiesByCertifiedProductId(Long certifiedProductId) {
		
		Query query = entityManager.createQuery( "from CertificationResultEntity where (NOT deleted = true) AND (certified_product_id = :entityid) ", CertificationResultEntity.class );
		query.setParameter("entityid", certifiedProductId);
		List<CertificationResultEntity> result = query.getResultList();
		return result;
	}
	

}
