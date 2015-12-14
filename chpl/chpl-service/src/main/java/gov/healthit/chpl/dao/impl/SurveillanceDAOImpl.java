package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.SurveillanceDAO;
import gov.healthit.chpl.dto.SurveillanceDTO;
import gov.healthit.chpl.entity.SurveillanceEntity;

@Repository("surveillanceDAO")
public class SurveillanceDAOImpl extends BaseDAOImpl implements SurveillanceDAO {
	
	@Override
	public SurveillanceDTO create(SurveillanceDTO toCreate) throws EntityCreationException,
		EntityRetrievalException {
		SurveillanceEntity entity = null;
		try {
			if(toCreate.getId() != null) {
				entity = getEntityById(toCreate.getId());
			}
		} catch(EntityRetrievalException e) {
			throw new EntityCreationException(e);
		}
		
		if(entity != null) {
			throw new EntityCreationException("An entity with this id already exists.");
		} else {
			entity = new SurveillanceEntity();
			entity.setCertifiedProductId(toCreate.getCertifiedProductId());
			entity.setCreationDate(new Date());
			entity.setDeleted(false);
			entity.setEndDate(toCreate.getEndDate());
			entity.setStartDate(toCreate.getStartDate());
			entity.setLastModifiedDate(new Date());
			entity.setLastModifiedUser(Util.getCurrentUser().getId());
			
			create(entity);
			return new SurveillanceDTO(entity);
		}
	}

	@Override
	public SurveillanceDTO update(SurveillanceDTO toUpdate) throws EntityRetrievalException {
		SurveillanceEntity entity = getEntityById(toUpdate.getId());
		if(entity == null) {
			throw new EntityRetrievalException("Entity with id " + toUpdate.getId() + " does not exist.");
		}
		
		if(toUpdate.getCertifiedProductId() != null) {
			entity.setCertifiedProductId(toUpdate.getCertifiedProductId());
		}
		entity.setStartDate(toUpdate.getStartDate());
		entity.setEndDate(toUpdate.getEndDate());
		entity.setLastModifiedDate(new Date());
		entity.setLastModifiedUser(Util.getCurrentUser().getId());
		
		update(entity);
		return new SurveillanceDTO(entity);
	}
	
	@Override
	public SurveillanceDTO getById(Long id) throws EntityRetrievalException {
		SurveillanceDTO dto = null;
		SurveillanceEntity entity = getEntityById(id);
		if(entity != null) {
			dto = new SurveillanceDTO(entity);
		}
		return dto;
	}


	@Override
	public List<SurveillanceDTO> getAllForCertifiedProduct(Long certifiedProductId) {
		List<SurveillanceEntity> entities = getEntitiesByCertifiedProductId(certifiedProductId);
		List<SurveillanceDTO> dtos = new ArrayList<SurveillanceDTO>();
		
		for(SurveillanceEntity entity : entities) {
			SurveillanceDTO dto = new SurveillanceDTO(entity);
			dtos.add(dto);
		}
		return dtos;
	}

	@Override
	public void delete(Long id) throws EntityRetrievalException {
		SurveillanceEntity entity = getEntityById(id);
		entity.setDeleted(true);
		entity.setLastModifiedDate(new Date());
		entity.setLastModifiedUser(Util.getCurrentUser().getId());
		update(entity);
	}
	
	private void create(SurveillanceEntity entity) {
		
		entityManager.persist(entity);
		entityManager.flush();
		
	}
	
	private void update(SurveillanceEntity entity) {
		
		entityManager.merge(entity);	
		entityManager.flush();
	}
	
	public SurveillanceEntity getEntityById(Long id) throws EntityRetrievalException {
		
		SurveillanceEntity entity = null;
			
		Query query = entityManager.createQuery( "from SurveillanceEntity where (NOT deleted = true) AND (surveillance_id = :entityid) ", SurveillanceEntity.class );
		query.setParameter("entityid", id);
		List<SurveillanceEntity> result = query.getResultList();
		
		if (result.size() > 1){
			throw new EntityRetrievalException("Data error. Duplicate surveillance id in database.");
		}
		
		if (result.size() > 0){
			entity = result.get(0);
		}
		
		return entity;
	}
	
	
	private List<SurveillanceEntity> getEntitiesByCertifiedProductId(Long id) {
		
		Query query = entityManager.createQuery( "from SurveillanceEntity where (NOT deleted = true) AND (certified_product_id = :certified_product_id) ", SurveillanceEntity.class );
		query.setParameter("certified_product_id", id);
		List<SurveillanceEntity> result = query.getResultList();
		
		return result;
	}	
}