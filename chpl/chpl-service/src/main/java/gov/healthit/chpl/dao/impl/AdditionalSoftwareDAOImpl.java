package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.AdditionalSoftwareDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.AdditionalSoftwareDTO;
import gov.healthit.chpl.entity.AdditionalSoftwareEntity;

@Repository("additionalSoftwareDAO")
public class AdditionalSoftwareDAOImpl extends BaseDAOImpl implements AdditionalSoftwareDAO {

	@Override
	public AdditionalSoftwareDTO create(AdditionalSoftwareDTO dto)
			throws EntityCreationException {
		
		AdditionalSoftwareEntity entity = null;
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
			
			entity = new AdditionalSoftwareEntity();
			
			entity.setName(dto.getName());
			entity.setVersion(dto.getVersion());
			entity.setCertifiedProductId(dto.getCertifiedProductId());
			entity.setJustification(dto.getJustification());
			entity.setCertifiedProductSelfId(dto.getCertifiedProductSelfId());
			entity.setCreationDate(new Date());
			entity.setDeleted(false);
			if(dto.getLastModifiedDate() != null) {
				entity.setLastModifiedDate(dto.getLastModifiedDate());
			} else {
				entity.setLastModifiedDate(new Date());
			}
			entity.setLastModifiedUser(Util.getCurrentUser().getId());
			
			create(entity);
			
		}
		AdditionalSoftwareDTO result = null;
		if (entity != null){
			result = new AdditionalSoftwareDTO(entity);
		}
		return result;
	}

	@Override
	public void delete(Long id) {
		// TODO: How to delete this without leaving orphans
		Query query = entityManager.createQuery("UPDATE AdditionalSoftwareEntity SET deleted = true WHERE additional_software_id = :resultid");
		query.setParameter("resultid", id);
		query.executeUpdate();
	}
	
	@Override
	public void deleteByCertifiedProduct(Long productId) {
		// TODO: How to delete this without leaving orphans
		Query query = entityManager.createQuery("UPDATE AdditionalSoftwareEntity SET deleted = true WHERE certified_product_id = :productId");
		query.setParameter("productId", productId);
		query.executeUpdate();
	}

	@Override
	public List<AdditionalSoftwareDTO> findAll() {
		
		List<AdditionalSoftwareEntity> entities = getAllEntities();
		List<AdditionalSoftwareDTO> additionalSoftwares = new ArrayList<>();
		
		for (AdditionalSoftwareEntity entity : entities) {
			AdditionalSoftwareDTO result = new AdditionalSoftwareDTO(entity);
			additionalSoftwares.add(result);
		}
		return additionalSoftwares;
		
	}

	@Override
	public List<AdditionalSoftwareDTO> findByCertifiedProductId(Long id) {
		
		List<AdditionalSoftwareEntity> entities = getEntitiesByCertifiedProductId(id);
		List<AdditionalSoftwareDTO> cqmResults = new ArrayList<>();
		
		for (AdditionalSoftwareEntity entity : entities) {
			AdditionalSoftwareDTO cqmResult = new AdditionalSoftwareDTO(entity);
			cqmResults.add(cqmResult);
		}
		return cqmResults;
	}

	@Override
	public AdditionalSoftwareDTO getById(Long id)
			throws EntityRetrievalException {
		
		AdditionalSoftwareEntity entity = getEntityById(id);
		AdditionalSoftwareDTO dto = null;
		if (entity != null){
			dto = new AdditionalSoftwareDTO(entity);
		}
		return dto;
	}

	@Override
	public AdditionalSoftwareDTO getByName(String name) {
		AdditionalSoftwareEntity entity = getEntityByName(name);
		if(entity == null || entity.getId() == null) {
			return null;
		}
		AdditionalSoftwareDTO dto = new AdditionalSoftwareDTO(entity);
		return dto;
	}
	
	@Override
	public void update(AdditionalSoftwareDTO dto)
			throws EntityRetrievalException {
		
		AdditionalSoftwareEntity entity =  this.getEntityById(dto.getId());
		
		entity.setCertifiedProductId(dto.getCertifiedProductId());
		entity.setCertifiedProductSelfId(dto.getCertifiedProductSelfId());
		entity.setCreationDate(dto.getCreationDate());
		entity.setDeleted(dto.getDeleted());
		entity.setId(dto.getId());
		entity.setJustification(dto.getJustification());
		entity.setLastModifiedDate(dto.getLastModifiedDate());
		entity.setLastModifiedUser(Util.getCurrentUser().getId());
		entity.setName(dto.getName());
		entity.setVersion(dto.getVersion());
		
		update(entity);
		
	}
	
	public List<AdditionalSoftwareDTO> findByCertificationResultId(Long id){
		
		String queryStr = "SELECT b.* FROM "
				+ "(SELECT * FROM openchpl.certification_result_additional_software_map WHERE certification_result_id = :resultid) a "
				+ "INNER JOIN "
				+ "(SELECT * FROM openchpl.additional_software) b "
				+ "ON a.additional_software_id = b.additional_software_id;";
		
		Query query = entityManager.createNativeQuery(queryStr, AdditionalSoftwareEntity.class);
		query.setParameter("resultid", id);
		
		List<AdditionalSoftwareEntity> results = query.getResultList();
		
		List<AdditionalSoftwareDTO> additionalSoftware = new ArrayList<>();
		
		for (AdditionalSoftwareEntity entity : results){
			additionalSoftware.add(new AdditionalSoftwareDTO(entity));
		}
		return additionalSoftware;
	}
	
	public List<AdditionalSoftwareDTO> findByCQMResultId(Long id){
		
		String queryStr = "SELECT b.* FROM "
				+ "(SELECT * FROM openchpl.cqm_result_additional_software_map WHERE cqm_result_id = :resultid) a "
				+ "INNER JOIN "
				+ "(SELECT * FROM openchpl.additional_software) b "
				+ "ON a.additional_software_id = b.additional_software_id;";
		
		Query query = entityManager.createNativeQuery(queryStr, AdditionalSoftwareEntity.class);
		query.setParameter("resultid", id);
		
		List<AdditionalSoftwareEntity> results = query.getResultList();
		
		List<AdditionalSoftwareDTO> additionalSoftware = new ArrayList<>();
		
		for (AdditionalSoftwareEntity entity : results){
			additionalSoftware.add(new AdditionalSoftwareDTO(entity));
		}
		return additionalSoftware;
		
	}	
	
	private void create(AdditionalSoftwareEntity entity) {
		
		entityManager.persist(entity);
		entityManager.flush();
		
	}
	
	private void update(AdditionalSoftwareEntity entity) {
		
		entityManager.merge(entity);
		entityManager.flush();
	
	}
	
	private AdditionalSoftwareEntity getEntityById(Long id) throws EntityRetrievalException {
		
		AdditionalSoftwareEntity entity = null;
			
		Query query = entityManager.createQuery( "from AdditionalSoftwareEntity where (NOT deleted = true) AND (additional_software_id = :entityid) ", AdditionalSoftwareEntity.class );
		query.setParameter("entityid", id);
		List<AdditionalSoftwareEntity> result = query.getResultList();
		
		if (result.size() > 1){
			throw new EntityRetrievalException("Data error. Duplicate criterion id in database.");
		}
		
		if (result.size() > 0){
			entity = result.get(0);
		}
		return entity;
	}
	
	private AdditionalSoftwareEntity getEntityByName(String name) {
		
		AdditionalSoftwareEntity entity = null;
			
		Query query = entityManager.createQuery( "from AdditionalSoftwareEntity where (NOT deleted = true) AND (name = :name) ", AdditionalSoftwareEntity.class );
		query.setParameter("name", name);
		List<AdditionalSoftwareEntity> result = query.getResultList();
		
		if (result.size() > 0){
			entity = result.get(0);
		}
		return entity;
	}

	private List<AdditionalSoftwareEntity> getEntitiesByCertifiedProductId(Long certifiedProductId) {
		
		Query query = entityManager.createQuery( "from AdditionalSoftwareEntity where (NOT deleted = true) AND (certified_product_id = :entityid) ", AdditionalSoftwareEntity.class );
		query.setParameter("entityid", certifiedProductId);
		List<AdditionalSoftwareEntity> result = query.getResultList();
		return result;
	}
	
	private List<AdditionalSoftwareEntity> getAllEntities() {
		
		List<AdditionalSoftwareEntity> result = entityManager.createQuery( "from AdditionalSoftwareEntity where (NOT deleted = true) ", AdditionalSoftwareEntity.class).getResultList();
		return result;
	}
	
}
