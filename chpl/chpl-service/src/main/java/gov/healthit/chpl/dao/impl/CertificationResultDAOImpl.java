package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
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
			entity.setCreationDate(result.getCreationDate());
			entity.setDeleted(result.getDeleted());
			entity.setGap(result.getGap());
			entity.setId(result.getId());
			entity.setInherited(result.getInherited());
			//entity.setLastModifiedDate(result.getLastModifiedDate());
			entity.setLastModifiedUser(Util.getCurrentUser().getId());
			entity.setSedInherited(result.getSedInherited());
			entity.setSedSuccessful(result.getSedSuccessful());
			entity.setSuccessful(result.getSuccessful());
			entity.setTestDataVersionId(result.getTestDataVersionId());
			entity.setTestProcedureVersionId(result.getTestProcedureVersionId());
			
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
		entity.setLastModifiedDate(result.getLastModifiedDate());
		entity.setLastModifiedUser(result.getLastModifiedUser());
		entity.setSedInherited(result.getSedInherited());
		entity.setSedSuccessful(result.getSedSuccessful());
		entity.setSuccessful(result.getSuccessful());
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
		CertificationResultEntity entity = getEntityById(resultId);
		CertificationResultDTO dto = new CertificationResultDTO(entity);
		return dto;
	}
	
	private void create(CertificationResultEntity entity) {
		
		entityManager.persist(entity);
		
	}
	
	private void update(CertificationResultEntity entity) {
		
		entityManager.merge(entity);	
	
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
		
		if (result.size() < 0){
			entity = result.get(0);
		}
			
		return entity;
	}

}
