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
import gov.healthit.chpl.dto.CertificationResultAdditionalSoftwareDTO;
import gov.healthit.chpl.dto.CertificationResultDTO;
import gov.healthit.chpl.entity.CertificationResultAdditionalSoftwareEntity;
import gov.healthit.chpl.entity.CertificationResultEntity;

@Repository(value="certificationResultDAO")
public class CertificationResultDAOImpl extends BaseDAOImpl implements CertificationResultDAO {
	
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
			entity.setCertificationCriterionId(result.getCertificationCriterionId());
			entity.setCertifiedProductId(result.getCertifiedProductId());
			entity.setGap(result.getGap());
			entity.setSed(result.getSed());
			entity.setG1Success(result.getG1Success());
			entity.setG2Success(result.getG2Success());
			entity.setUcdProcessSelected(result.getUcdProcessSelected());
			entity.setUcdProcessDetails(result.getUcdProcessDetails());
			entity.setSuccess(result.getSuccessful());
			
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
		entity.setCertificationCriterionId(result.getCertificationCriterionId());
		entity.setCertifiedProductId(result.getCertifiedProductId());
		entity.setGap(result.getGap());
		entity.setSed(result.getSed());
		entity.setG1Success(result.getG1Success());
		entity.setG2Success(result.getG2Success());
		entity.setUcdProcessSelected(result.getUcdProcessSelected());
		entity.setUcdProcessDetails(result.getUcdProcessDetails());
		entity.setSuccess(result.getSuccessful());
		
		if(result.getDeleted() != null) {
			entity.setDeleted(result.getDeleted());
		}
		entity.setLastModifiedUser(Util.getCurrentUser().getId());
		entity.setLastModifiedDate(new Date());
		
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

	/******************************************************
	 * Additional Software for Certification Results
	 * 
	 *******************************************************/
	
	@Override
	public List<CertificationResultAdditionalSoftwareDTO> getAdditionalSoftwareForCertificationResult(Long certificationResultId){
		
		List<CertificationResultAdditionalSoftwareEntity> entities = getAdditionalSoftwareForCertification(certificationResultId);
		List<CertificationResultAdditionalSoftwareDTO> dtos = new ArrayList<CertificationResultAdditionalSoftwareDTO>();
		
		for (CertificationResultAdditionalSoftwareEntity entity : entities){
			CertificationResultAdditionalSoftwareDTO dto = new CertificationResultAdditionalSoftwareDTO(entity);
			dtos.add(dto);	
		}
		return dtos;
	}
	
	public CertificationResultAdditionalSoftwareDTO addAdditionalSoftwareMapping(CertificationResultAdditionalSoftwareDTO dto) throws EntityCreationException {
		CertificationResultAdditionalSoftwareEntity mapping = new CertificationResultAdditionalSoftwareEntity();
		mapping = new CertificationResultAdditionalSoftwareEntity();
		mapping.setCertificationResultId(dto.getCertificationResultId());
		mapping.setCertifiedProductId(dto.getCertifiedProductId());
		mapping.setName(dto.getName());
		mapping.setVersion(dto.getVersion());
		mapping.setJustification(dto.getJustification());
		mapping.setCreationDate(new Date());
		mapping.setDeleted(false);
		mapping.setLastModifiedDate(new Date());
		mapping.setLastModifiedUser(Util.getCurrentUser().getId());
		entityManager.persist(mapping);
		entityManager.flush();
		
		return new CertificationResultAdditionalSoftwareDTO(mapping);
	}
	
	public CertificationResultAdditionalSoftwareDTO updateAdditionalSoftwareMapping(CertificationResultAdditionalSoftwareDTO dto){
		CertificationResultAdditionalSoftwareEntity mapping = getCertificationResultAdditionalSoftwareById(dto.getId());
		if(mapping == null) {
			return null;
		}
		mapping.setCertificationResultId(dto.getCertificationResultId());
		mapping.setCertifiedProductId(dto.getCertifiedProductId());
		mapping.setName(dto.getName());
		mapping.setVersion(dto.getVersion());
		mapping.setJustification(dto.getJustification());
		if(dto.getDeleted() != null) {
			mapping.setDeleted(dto.getDeleted());
		}
		mapping.setLastModifiedDate(new Date());
		mapping.setLastModifiedUser(Util.getCurrentUser().getId());
		entityManager.persist(mapping);
		entityManager.flush();
		return new CertificationResultAdditionalSoftwareDTO(mapping);
	}

	public void deleteAdditionalSoftwareMapping(Long mappingId){
		CertificationResultAdditionalSoftwareEntity toDelete = getCertificationResultAdditionalSoftwareById(mappingId);
		if(toDelete != null) {
			toDelete.setDeleted(true);
			toDelete.setLastModifiedDate(new Date());
			toDelete.setLastModifiedUser(Util.getCurrentUser().getId());
			entityManager.persist(toDelete);
			entityManager.flush();
		}
	}
	
	private CertificationResultAdditionalSoftwareEntity getCertificationResultAdditionalSoftwareById(Long id) {
		CertificationResultAdditionalSoftwareEntity entity = null;
		
		Query query = entityManager.createQuery( "from CertificationResultAdditionalSoftwareEntity "
				+ "where (NOT deleted = true) AND (certification_result_additional_software_id = :entityid) ", 
				CertificationResultAdditionalSoftwareEntity.class );
		query.setParameter("entityid", id);
		List<CertificationResultAdditionalSoftwareEntity> result = query.getResultList();

		if (result.size() > 0){
			entity = result.get(0);
		}
		return entity;
	}
	
	private List<CertificationResultAdditionalSoftwareEntity> getAdditionalSoftwareForCertification(Long certificationResultId){
		Query query = entityManager.createQuery( "from CertificationResultAdditionalSoftwareEntity "
				+ "where (NOT deleted = true) AND (certification_result_id = :certificationResultId) ", 
				CertificationResultAdditionalSoftwareEntity.class );
		query.setParameter("certificationResultId", certificationResultId);
		
		List<CertificationResultAdditionalSoftwareEntity> result = query.getResultList();
		if(result == null) {
			return null;
		}
		return result;
	}
	
}
