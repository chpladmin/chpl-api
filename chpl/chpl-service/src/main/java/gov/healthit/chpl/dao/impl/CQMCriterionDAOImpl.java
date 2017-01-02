package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.CQMCriterionDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.CQMCriterionDTO;
import gov.healthit.chpl.entity.CQMCriterionEntity;
import gov.healthit.chpl.entity.CQMVersionEntity;


@Repository(value="cqmCriterionDAO")
public class CQMCriterionDAOImpl extends BaseDAOImpl implements CQMCriterionDAO {

	@Override
	public CQMCriterionDTO create(CQMCriterionDTO dto)
			throws EntityCreationException, EntityRetrievalException {
		CQMCriterionEntity entity = null;
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
			
			entity = new CQMCriterionEntity();
			entity.setCmsId(dto.getCmsId());
			entity.setCqmCriterionTypeId(dto.getCqmCriterionTypeId());
			entity.setCqmDomain(dto.getCqmDomain());
			entity.setCqmVersionId(dto.getCqmVersionId());
			entity.setCreationDate(dto.getCreationDate());
			entity.setDeleted(dto.getDeleted());
			entity.setDescription(dto.getDescription());
			//entity.setId(dto.getId());
			entity.setLastModifiedDate(dto.getLastModifiedDate());
			entity.setLastModifiedUser(Util.getCurrentUser().getId());
			entity.setNqfNumber(dto.getNqfNumber());
			entity.setNumber(dto.getNumber());
			entity.setTitle(dto.getTitle());
			entity.setRetired(dto.getRetired());
						
			create(entity);	
		}
		
		CQMCriterionDTO result = null;
		if (entity != null){
			result = new CQMCriterionDTO(entity);
		}
		return result;
	}

	@Override
	public void update(CQMCriterionDTO dto)
			throws EntityRetrievalException, EntityCreationException {
		
		
		CQMCriterionEntity entity = this.getEntityById(dto.getId());
		
		entity.setCmsId(dto.getCmsId());
		entity.setCqmCriterionTypeId(dto.getCqmCriterionTypeId());
		entity.setCqmDomain(dto.getCqmDomain());
		entity.setCqmVersionId(dto.getCqmVersionId());
		entity.setCreationDate(dto.getCreationDate());
		entity.setDeleted(dto.getDeleted());
		entity.setDescription(dto.getDescription());
		//entity.setId(dto.getId());
		entity.setLastModifiedDate(dto.getLastModifiedDate());
		entity.setLastModifiedUser(Util.getCurrentUser().getId());
		entity.setNqfNumber(dto.getNqfNumber());
		entity.setNumber(dto.getNumber());
		entity.setTitle(dto.getTitle());
		entity.setRetired(dto.getRetired());
						
		update(entity);	
		
	}

	@Override
	public void delete(Long criterionId) {
		
		Query query = entityManager.createQuery("UPDATE CQMCriterionEntity SET deleted = true WHERE cqm_criterion_id = :entityid");
		query.setParameter("entityid", criterionId);
		query.executeUpdate();

	}

	@Override
	public List<CQMCriterionDTO> findAll() {
		
		List<CQMCriterionEntity> entities = getAllEntities();
		List<CQMCriterionDTO> dtos = new ArrayList<>();
		
		for (CQMCriterionEntity entity : entities) {
			CQMCriterionDTO dto = new CQMCriterionDTO(entity);
			dtos.add(dto);
		}
		return dtos;
	}
	
	@Override
	public CQMCriterionDTO getById(Long criterionId)
			throws EntityRetrievalException {
		
		CQMCriterionDTO dto = null;
		CQMCriterionEntity entity = getEntityById(criterionId);
		
		if (entity != null){
			dto = new CQMCriterionDTO(entity);
		}
		return dto;
	}
	
	@Override
	public CQMCriterionDTO getCMSByNumber(String number) {
		CQMCriterionEntity entity = getCMSEntityByNumber(number);
		if(entity == null) {
			return null;
		}
		return new CQMCriterionDTO(entity);
	}
	
	@Override
	public CQMCriterionDTO getCMSByNumberAndVersion(String number, String version) {
		CQMCriterionEntity entity = getCMSEntityByNumberAndVersion(number, version);
		if(entity == null) {
			return null;
		}
		return new CQMCriterionDTO(entity);
	}
	
	@Override
	public CQMCriterionDTO getNQFByNumber(String number) {
		CQMCriterionEntity entity = getNQFEntityByNumber(number);
		if(entity == null) {
			return null;
		}
		return new CQMCriterionDTO(entity);
	}
	
	private void create(CQMCriterionEntity entity) {
		
		entityManager.persist(entity);
		entityManager.flush();
	}
	
	private void update(CQMCriterionEntity entity) {
		
		entityManager.merge(entity);	
		entityManager.flush();
	}
	
	private List<CQMCriterionEntity> getAllEntities() {
		
		List<CQMCriterionEntity> result = entityManager.createQuery( "from CQMCriterionEntity where (NOT deleted = true) ", CQMCriterionEntity.class).getResultList();
		return result;
		
	}
	
	private CQMCriterionEntity getEntityById(Long id) throws EntityRetrievalException {
		
		CQMCriterionEntity entity = null;
			
		Query query = entityManager.createQuery( "from CQMCriterionEntity where (NOT deleted = true) AND (cqm_criterion_id = :entityid) ", CQMCriterionEntity.class );
		query.setParameter("entityid", id);
		List<CQMCriterionEntity> result = query.getResultList();
		
		if (result.size() > 1){
			throw new EntityRetrievalException("Data error. Duplicate criterion id in database.");
		}
		
		if (result.size() > 0){
			entity = result.get(0);
		}
			
		return entity;
	}
	
	@Override
	public CQMCriterionEntity getNQFEntityByNumber(String number) {
		
		CQMCriterionEntity entity = null;
			
		Query query = entityManager.createQuery( "from CQMCriterionEntity where (NOT deleted = true) AND (nqf_number = :number) ", CQMCriterionEntity.class );
		query.setParameter("number", number);
		List<CQMCriterionEntity> result = query.getResultList();
		
		if (result.size() > 0){
			entity = result.get(0);
		}
			
		return entity;
	}
	
	@Override
	public CQMCriterionEntity getCMSEntityByNumber(String number) {
		
		CQMCriterionEntity entity = null;
			
		Query query = entityManager.createQuery( "from CQMCriterionEntity where (NOT deleted = true) AND (cms_id = :number) ", CQMCriterionEntity.class );
		query.setParameter("number", number);
		List<CQMCriterionEntity> result = query.getResultList();
		
		if (result.size() > 0){
			entity = result.get(0);
		}
			
		return entity;
	}
	
	@Override
	public CQMCriterionEntity getCMSEntityByNumberAndVersion(String number, String version) {
		//first find the version id
		CQMVersionEntity versionEntity = null;
		Query versionQuery = entityManager.createQuery("from CQMVersionEntity where (NOT deleted = true) AND (version = :version)", CQMVersionEntity.class);
		versionQuery.setParameter("version", version);
		List<CQMVersionEntity> versionResult = versionQuery.getResultList();
		if(versionResult.size() > 0) {
			versionEntity = versionResult.get(0);
		}
		
		CQMCriterionEntity entity = null;
		if(versionEntity != null && versionEntity.getId() != null ) {
				
			Query query = entityManager.createQuery( "from CQMCriterionEntity where (NOT deleted = true) AND (cms_id = :number) AND (cqm_version_id = :versionId) ", CQMCriterionEntity.class );
			query.setParameter("number", number);
			query.setParameter("versionId", versionEntity.getId());
			
			List<CQMCriterionEntity> result = query.getResultList();
			if (result.size() > 0){
				entity = result.get(0);
			}
		}
		return entity;
	}
}
