package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.caching.ClearAllCaches;
import gov.healthit.chpl.dao.CertificationEditionDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.CertificationEditionDTO;
import gov.healthit.chpl.entity.CertificationEditionEntity;

@Repository("certificationEditionDAO")
public class CertificationEditionDAOImpl extends BaseDAOImpl implements CertificationEditionDAO {

	@Override
	public void create(CertificationEditionDTO dto)
			throws EntityCreationException, EntityRetrievalException {
		
		CertificationEditionEntity entity = null;
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
			
			entity = new CertificationEditionEntity();
			//entity.setCertificationCriterions();
			entity.setCreationDate(dto.getCreationDate());
			entity.setDeleted(dto.getDeleted());
			entity.setId(dto.getId());
			entity.setYear(dto.getYear());
			//entity.setLastModifiedDate(result.getLastModifiedDate());
			entity.setLastModifiedUser(Util.getCurrentUser().getId());
			
			create(entity);	
		}
		
	}

	@Override
	public void update(CertificationEditionDTO dto)
			throws EntityRetrievalException {
		
		CertificationEditionEntity entity = this.getEntityById(dto.getId());
		//entity.setCertificationCriterions();
		entity.setCreationDate(dto.getCreationDate());
		entity.setDeleted(dto.getDeleted());
		entity.setId(dto.getId());
		entity.setYear(dto.getYear());
		//entity.setLastModifiedDate(result.getLastModifiedDate());
		entity.setLastModifiedUser(Util.getCurrentUser().getId());
			
		update(entity);
	}

	@Override
	public void delete(Long id) {
		Query query = entityManager.createQuery("UPDATE CertificationEditionEntity SET deleted = true WHERE certification_edition_id = :entityid");
		query.setParameter("entityid", id);
		query.executeUpdate();
	}

	@Override
	public List<CertificationEditionDTO> findAll() {
		List<CertificationEditionEntity> entities = getAllEntities();
		List<CertificationEditionDTO> dtos = new ArrayList<>();
		
		for (CertificationEditionEntity entity : entities) {
			CertificationEditionDTO dto = new CertificationEditionDTO(entity);
			dtos.add(dto);
		}
		return dtos;
	}

	@Override
	public CertificationEditionDTO getById(Long criterionEditionId)
			throws EntityRetrievalException {
		
		CertificationEditionDTO dto = null;
		CertificationEditionEntity entity = getEntityById(criterionEditionId);
		
		if (entity != null){
			dto = new CertificationEditionDTO(entity);
		}
		return dto;
	}
	
	@Override
	public CertificationEditionDTO getByYear(String year) {
		CertificationEditionDTO result = null;
		CertificationEditionEntity yearEntity = getEntityByYear(year);
		if(yearEntity != null) {
			result = new CertificationEditionDTO(yearEntity);
		}
		return result;
	}
	
	private void create(CertificationEditionEntity entity) {
		
		entityManager.persist(entity);
		entityManager.flush();
		
	}
	
	private void update(CertificationEditionEntity entity) {
		
		entityManager.merge(entity);	
		entityManager.flush();
	
	}
	
	private List<CertificationEditionEntity> getAllEntities() {
		
		List<CertificationEditionEntity> result = entityManager.createQuery( "from CertificationEditionEntity where (NOT deleted = true) ", CertificationEditionEntity.class).getResultList();
		return result;
		
	}
	
	private CertificationEditionEntity getEntityById(Long id) throws EntityRetrievalException {
		
		CertificationEditionEntity entity = null;
			
		Query query = entityManager.createQuery( "from CertificationEditionEntity where (NOT deleted = true) AND (certification_edition_id = :entityid) ", CertificationEditionEntity.class );
		query.setParameter("entityid", id);
		List<CertificationEditionEntity> result = query.getResultList();
		
		if (result.size() > 1){
			throw new EntityRetrievalException("Data error. Duplicate criterion edition id in database.");
		}
		
		if (result.size() > 0){
			entity = result.get(0);
		}
			
		return entity;
	}
	
	private CertificationEditionEntity getEntityByYear(String year) {
		
		CertificationEditionEntity entity = null;
			
		Query query = entityManager.createQuery( "from CertificationEditionEntity where (NOT deleted = true) AND (year = :year) ", CertificationEditionEntity.class );
		query.setParameter("year", year);
		List<CertificationEditionEntity> result = query.getResultList();
		
		if (result.size() > 0){
			entity = result.get(0);
		}
			
		return entity;
	}
	
}
