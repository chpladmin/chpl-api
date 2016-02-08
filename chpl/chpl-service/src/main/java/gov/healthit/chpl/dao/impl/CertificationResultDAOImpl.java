package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.AdditionalSoftwareDAO;
import gov.healthit.chpl.dao.CertificationResultDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.CertificationResultAdditionalSoftwareMapDTO;
import gov.healthit.chpl.dto.CertificationResultDTO;
import gov.healthit.chpl.entity.CertificationResultAdditionalSoftwareMapEntity;
import gov.healthit.chpl.entity.CertificationResultEntity;

@Repository(value="certificationResultDAO")
public class CertificationResultDAOImpl extends BaseDAOImpl implements CertificationResultDAO {

	
	@Autowired
	private AdditionalSoftwareDAO additionalSoftwareDAO;
	
	
	@Override
	public CertificationResultDTO create(CertificationResultDTO result) throws EntityCreationException {
		
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
		
		return new CertificationResultDTO(entity);
		
	}

	@Override
	public CertificationResultDTO update(CertificationResultDTO result) throws EntityRetrievalException {
	
		CertificationResultEntity entity = getEntityById(result.getId());
		
		entity.setAutomatedMeasureCapable(result.getAutomatedMeasureCapable());
		entity.setAutomatedNumerator(result.getAutomatedNumerator());
		entity.setCertificationCriterionId(result.getCertificationCriterionId());
		entity.setCertifiedProductId(result.getCertifiedProductId());
		entity.setCreationDate(result.getCreationDate());
		entity.setDeleted(result.getDeleted());
		entity.setGap(result.getGap());
		entity.setInherited(result.getInherited());
		entity.setLastModifiedUser(Util.getCurrentUser().getId());
		entity.setSedInherited(result.getSedInherited());
		entity.setSedSuccessful(result.getSedSuccessful());
		entity.setSuccess(result.getSuccessful());
		entity.setTestDataVersionId(result.getTestDataVersionId());
		entity.setTestProcedureVersionId(result.getTestProcedureVersionId());
		
		update(entity);
		
		return new CertificationResultDTO(entity);
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
			
		Query query = entityManager.createQuery( "from CertificationResultEntity where (NOT deleted = true) AND (certification_result_id = :entityid) ", CertificationResultEntity.class );
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
	
	public CertificationResultAdditionalSoftwareMapDTO createAdditionalSoftwareMapping(CertificationResultAdditionalSoftwareMapDTO dto) throws EntityCreationException{
		CertificationResultAdditionalSoftwareMapEntity mapping = getCertificationResultAdditionalSoftwareMap(dto.getCertificationResultId(), dto.getAdditionalSoftwareId());
		if(mapping != null) {
			throw new EntityCreationException("A Certification Result - Additional Software mapping entity with this ID pair already exists.");
		}
		
		mapping = new CertificationResultAdditionalSoftwareMapEntity();
		mapping.setCertificationResultId(dto.getCertificationResultId());
		mapping.setAdditionalSoftwareId(dto.getAdditionalSoftwareId());
		mapping.setCreationDate(new Date());
		mapping.setDeleted(false);
		mapping.setLastModifiedDate(new Date());
		mapping.setLastModifiedUser(Util.getCurrentUser().getId());
		entityManager.persist(mapping);
		entityManager.flush();
		
		return new CertificationResultAdditionalSoftwareMapDTO(mapping);
		
	}
	
	public CertificationResultAdditionalSoftwareMapDTO updateAdditionalSoftwareMapping(CertificationResultAdditionalSoftwareMapDTO dto){
		
		CertificationResultAdditionalSoftwareMapEntity mapping = getCertificationResultAdditionalSoftwareMap(dto.getCertificationResultId(), dto.getAdditionalSoftwareId());
		if(mapping == null) {
			return null;
		}
		mapping.setCertificationResultId(dto.getCertificationResultId());
		mapping.setAdditionalSoftwareId(dto.getAdditionalSoftwareId());
		mapping.setDeleted(dto.getDeleted());
		mapping.setLastModifiedDate(dto.getLastModifiedDate());
		mapping.setLastModifiedUser(Util.getCurrentUser().getId());
		entityManager.persist(mapping);
		entityManager.flush();
		return new CertificationResultAdditionalSoftwareMapDTO(mapping);
	}
	
	public void deleteAdditionalSoftwareMapping(Long certificationResultId, Long additionalSoftwareId){
		CertificationResultAdditionalSoftwareMapEntity toDelete = getCertificationResultAdditionalSoftwareMap(certificationResultId, additionalSoftwareId);
		if(toDelete != null) {
			toDelete.setDeleted(true);
			toDelete.setLastModifiedDate(new Date());
			toDelete.setLastModifiedUser(Util.getCurrentUser().getId());
			entityManager.persist(toDelete);
			entityManager.flush();
		}
	}
	
	
	public CertificationResultAdditionalSoftwareMapDTO getAdditionalSoftwareMapping(Long certificationResultId, Long additionalSoftwareId){
		
		CertificationResultAdditionalSoftwareMapEntity mapping = getCertificationResultAdditionalSoftwareMap(certificationResultId, additionalSoftwareId);
		if (mapping == null){
			return null;
		}
		return new CertificationResultAdditionalSoftwareMapDTO(mapping);
	}
	
	@Override
	public List<CertificationResultAdditionalSoftwareMapDTO> getCertificationResultAdditionalSoftwareMappings(Long certificationResultId){
		
		List<CertificationResultAdditionalSoftwareMapEntity> entities = getCertificationResultAdditionalSoftwareMapEntities(certificationResultId);
		List<CertificationResultAdditionalSoftwareMapDTO> dtos = new ArrayList<CertificationResultAdditionalSoftwareMapDTO>();
		
		for (CertificationResultAdditionalSoftwareMapEntity entity : entities){
			
			CertificationResultAdditionalSoftwareMapDTO dto = new CertificationResultAdditionalSoftwareMapDTO(entity);
			dtos.add(dto);	
		}
		return dtos;
	}
	
	
	
	private CertificationResultAdditionalSoftwareMapEntity getCertificationResultAdditionalSoftwareMap(Long certificationResultId, Long additionalSoftwareId){
		Query query = entityManager.createQuery( "FROM CertificationResultAdditionalSoftwareMapEntity where "
				+ "(NOT deleted = true) "
				+ "AND certification_result_id = :certificationResultId "
				+ "AND additional_software_id = :additionalSoftwareId", CertificationResultAdditionalSoftwareMapEntity.class);
		query.setParameter("certificationResultId", certificationResultId);
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
		return (CertificationResultAdditionalSoftwareMapEntity) result;
	}
	
	
	private List<CertificationResultAdditionalSoftwareMapEntity> getCertificationResultAdditionalSoftwareMapEntities(Long certificationResultId){
		Query query = entityManager.createQuery( "FROM CertificationResultAdditionalSoftwareMapEntity where "
				+ "(NOT deleted = true) "
				+ "AND certification_result_id = :certificationResultId ", CertificationResultAdditionalSoftwareMapEntity.class);
		query.setParameter("certificationResultId", certificationResultId);
		
		List<CertificationResultAdditionalSoftwareMapEntity> result = query.getResultList();
		if(result == null) {
			return null;
		}
		return result;
	}
	
}
