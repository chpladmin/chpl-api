package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.CertificationEventDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.CertificationEventDTO;
import gov.healthit.chpl.entity.CertificationEventEntity;

@Repository("certificationEventDAO")
public class CertificationEventDAOImpl extends BaseDAOImpl implements CertificationEventDAO {
	

	@Override
	public CertificationEventDTO create(CertificationEventDTO dto)
			throws EntityCreationException, EntityRetrievalException {
		
		CertificationEventEntity entity = null;
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
			entity = new CertificationEventEntity();
			
			entity.setCertifiedProductId(dto.getCertifiedProductId());
			
			if (dto.getCity() != null ){
				entity.setCity(dto.getCity());
			}
			
			if (dto.getState() != null){
				entity.setState(dto.getState());
			}
			
			if (dto.getEventDate() != null){
				entity.setEventDate(dto.getEventDate());
			}
			
			if(dto.getEventTypeId() != null){
				entity.setEventTypeId(dto.getEventTypeId());
			}
			
			if(dto.getLastModifiedUser() != null) {
				entity.setLastModifiedUser(dto.getLastModifiedUser());
			} else {
				entity.setLastModifiedUser(Util.getCurrentUser().getId());
			}		
			
			if(dto.getLastModifiedDate() != null) {
				entity.setLastModifiedDate(dto.getLastModifiedDate());
			} else {
				entity.setLastModifiedDate(new Date());
			}
			
			if(dto.getCreationDate() != null) {
				entity.setCreationDate(dto.getCreationDate());
			} else {
				entity.setCreationDate(new Date());
			}
			
			if(dto.getDeleted() != null) {
				entity.setDeleted(dto.getDeleted());
			} else {
				entity.setDeleted(false);
			}
			create(entity);
			return new CertificationEventDTO(entity);
		}		
	}

	@Override
	public CertificationEventDTO update(CertificationEventDTO dto)
			throws EntityRetrievalException {
		CertificationEventEntity entity = this.getEntityById(dto.getId());
		
		if(entity == null) {
			throw new EntityRetrievalException("Entity with id " + dto.getId() + " does not exist");
		}
		
		if (dto.getCity() != null ){
			entity.setCity(dto.getCity());
		}
		
		if (dto.getState() != null){
			entity.setState(dto.getState());
		}
		
		if (dto.getEventDate() != null){
			entity.setEventDate(dto.getEventDate());
		}
		
		if(dto.getEventTypeDTO() != null){
			entity.setEventTypeId(dto.getEventTypeDTO().getId());
		}
		
		if(dto.getLastModifiedUser() != null) {
			entity.setLastModifiedUser(dto.getLastModifiedUser());
		} else {
			entity.setLastModifiedUser(Util.getCurrentUser().getId());
		}		
		
		if(dto.getLastModifiedDate() != null) {
			entity.setLastModifiedDate(dto.getLastModifiedDate());
		} else {
			entity.setLastModifiedDate(new Date());
		}
		
		if(dto.getCreationDate() != null) {
			entity.setCreationDate(dto.getCreationDate());
		} else {
			entity.setCreationDate(new Date());
		}
		
		if(dto.getDeleted() != null) {
			entity.setDeleted(dto.getDeleted());
		} else {
			entity.setDeleted(false);
		}
		
		update(entity);
		return new CertificationEventDTO(entity);
	}

	@Override
	public void delete(Long id) throws EntityRetrievalException {
		
		CertificationEventEntity toDelete = getEntityById(id);
		
		if(toDelete != null) {
			toDelete.setDeleted(true);
			toDelete.setLastModifiedDate(new Date());
			toDelete.setLastModifiedUser(Util.getCurrentUser().getId());
			update(toDelete);
		}
	}

	@Override
	public CertificationEventDTO getById(Long id)
			throws EntityRetrievalException {
		
		CertificationEventEntity entity = getEntityById(id);
		CertificationEventDTO dto = new CertificationEventDTO(entity);
		return dto;
		
	}

	@Override
	public List<CertificationEventDTO> findAll() {
		
		List<CertificationEventEntity> entities = getAllEntities();
		List<CertificationEventDTO> dtos = new ArrayList<>();
		
		for (CertificationEventEntity entity : entities) {
			CertificationEventDTO dto = new CertificationEventDTO(entity);
			dtos.add(dto);
		}
		return dtos;
		
	}

	@Override
	public List<CertificationEventDTO> findByCertifiedProductId(
			Long certifiedProductId) {
		
		List<CertificationEventEntity> entities = getEntitiesByCertifiedProductId(certifiedProductId);
		List<CertificationEventDTO> dtos = new ArrayList<>();
		
		for (CertificationEventEntity entity : entities) {
			CertificationEventDTO dto = new CertificationEventDTO(entity);
			dtos.add(dto);
		}
		return dtos;
	}


	private void create(CertificationEventEntity entity) {
		
		entityManager.persist(entity);
		entityManager.flush();
		
	}
	
	private void update(CertificationEventEntity entity) {
		
		entityManager.merge(entity);	
		entityManager.flush();
	}
	
	private List<CertificationEventEntity> getAllEntities() {
		
		List<CertificationEventEntity> result = entityManager.createQuery( "from CertificationEventEntity where (NOT deleted = true) ", CertificationEventEntity.class).getResultList();
		return result;
		
	}
	
	private CertificationEventEntity getEntityById(Long id) throws EntityRetrievalException {
		
		CertificationEventEntity entity = null;
			
		Query query = entityManager.createQuery( "from CertificationEventEntity where (NOT deleted = true) AND (certification_event_id = :entityid) ", CertificationEventEntity.class );
		query.setParameter("entityid", id);
		List<CertificationEventEntity> result = query.getResultList();
		
		if (result.size() > 1){
			throw new EntityRetrievalException("Data error. Duplicate certification event id in database.");
		}
		
		if (result.size() > 0){
			entity = result.get(0);
		}
		
		return entity;
	}
	
	
	private List<CertificationEventEntity> getEntitiesByCertifiedProductId(Long id) {
		
		Query query = entityManager.createQuery( "from CertificationEventEntity where (NOT deleted = true) AND (certified_product_id = :certified_product_id) ", CertificationEventEntity.class );
		query.setParameter("certified_product_id", id);
		List<CertificationEventEntity> result = query.getResultList();
		
		return result;
	}
	
	
}
