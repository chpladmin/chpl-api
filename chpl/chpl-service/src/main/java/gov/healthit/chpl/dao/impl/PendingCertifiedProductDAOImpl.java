package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.PendingCertifiedProductDAO;
import gov.healthit.chpl.dto.CertificationStatusDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.entity.PendingCertificationCriterionEntity;
import gov.healthit.chpl.entity.PendingCertifiedProductEntity;
import gov.healthit.chpl.entity.PendingCqmCriterionEntity;

@Repository(value="pendingCertifiedProductDAO")
public class PendingCertifiedProductDAOImpl extends BaseDAOImpl implements PendingCertifiedProductDAO {
	
	@Override
	@Transactional
	public PendingCertifiedProductDTO create(PendingCertifiedProductEntity toCreate) {		
		if(toCreate.getLastModifiedDate() == null) {
			toCreate.setLastModifiedDate(new Date());
		}		
		if(toCreate.getLastModifiedUser() == null) {
			toCreate.setLastModifiedUser(Util.getCurrentUser().getId());
		}
		toCreate.setCreationDate(new Date());
		toCreate.setDeleted(false);
		
		entityManager.persist(toCreate);
		
		for(PendingCertificationCriterionEntity criterion : toCreate.getCertificationCriterion()) {
			criterion.setPendingCertifiedProductId(toCreate.getId());
			if(criterion.getLastModifiedDate() == null) {
				criterion.setLastModifiedDate(new Date());
			}		
			if(criterion.getLastModifiedUser() == null) {
				criterion.setLastModifiedUser(Util.getCurrentUser().getId());
			}
			criterion.setCreationDate(new Date());
			criterion.setDeleted(false);
			entityManager.persist(criterion);
		}
		
		for(PendingCqmCriterionEntity cqm : toCreate.getCqmCriterion()) {
			cqm.setPendingCertifiedProductId(toCreate.getId());
			if(cqm.getLastModifiedDate() == null) {
				cqm.setLastModifiedDate(new Date());
			}		
			if(cqm.getLastModifiedUser() == null) {
				cqm.setLastModifiedUser(Util.getCurrentUser().getId());
			}
			cqm.setCreationDate(new Date());
			cqm.setDeleted(false);
			entityManager.persist(cqm);
		}
		
		return new PendingCertifiedProductDTO(toCreate);
	}

	@Override
	@Transactional
	public void delete(Long pendingProductId, CertificationStatusDTO reason) throws EntityRetrievalException {
		PendingCertifiedProductEntity entity = getEntityById(pendingProductId);
		if(entity == null) {
			throw new EntityRetrievalException("No pending certified product exists with id " + pendingProductId);
		}
		entity.setStatus(reason.getId());
		entity.setDeleted(true);
		entity.setLastModifiedDate(new Date());
		entity.setLastModifiedUser(Util.getCurrentUser().getId());
		
		entityManager.persist(entity);
	}
	

	@Override
	public void updateStatus(Long pendingProductId, CertificationStatusDTO status) throws EntityRetrievalException {
		PendingCertifiedProductEntity entity = getEntityById(pendingProductId);
		if(entity == null) {
			throw new EntityRetrievalException("No pending certified product exists with id " + pendingProductId);
		}
		entity.setStatus(status.getId());
		entity.setLastModifiedDate(new Date());
		entity.setLastModifiedUser(Util.getCurrentUser().getId());
		
		entityManager.persist(entity);
	}

	
	public List<PendingCertifiedProductDTO> findAll() {
		List<PendingCertifiedProductEntity> entities = getAllEntities();
		List<PendingCertifiedProductDTO> dtos = new ArrayList<>();
		
		for (PendingCertifiedProductEntity entity : entities) {
			PendingCertifiedProductDTO dto = new PendingCertifiedProductDTO(entity);
			dtos.add(dto);
		}
		return dtos;
	}
	
	public List<PendingCertifiedProductDTO> findByStatus(Long statusId) {
		List<PendingCertifiedProductEntity> entities = getEntitiesByStatus(statusId);
		List<PendingCertifiedProductDTO> dtos = new ArrayList<>();
		
		for (PendingCertifiedProductEntity entity : entities) {
			PendingCertifiedProductDTO dto = new PendingCertifiedProductDTO(entity);
			dtos.add(dto);
		}
		return dtos;
	}
	
	public PendingCertifiedProductDTO findById(Long pcpId) throws EntityRetrievalException {
		PendingCertifiedProductEntity entity = getEntityById(pcpId);
		if(entity == null) {
			return null;
		}
		return new PendingCertifiedProductDTO(entity);
	}
	
	public List<PendingCertifiedProductDTO> findByAcbId(Long acbId) {
		List<PendingCertifiedProductEntity> entities = getEntityByAcbId(acbId);
		List<PendingCertifiedProductDTO> dtos = new ArrayList<>();

		for (PendingCertifiedProductEntity entity : entities) {
			PendingCertifiedProductDTO dto = new PendingCertifiedProductDTO(entity);
			dtos.add(dto);
		}
		return dtos;
	}
	
	public Long findIdByOncId(String id) throws EntityRetrievalException {
		PendingCertifiedProductEntity entity = getEntityByOncId(id);
		if(entity == null) {
			return null;
		}
		return entity.getId();
	}
	
	private void update(PendingCertifiedProductEntity product) {
		
		entityManager.merge(product);	
	
	}
	
	private List<PendingCertifiedProductEntity> getAllEntities() {
		
		List<PendingCertifiedProductEntity> result = entityManager.createQuery( "SELECT pcp from PendingCertifiedProductEntity pcp "
				+ " WHERE (not pcp.deleted = true)", PendingCertifiedProductEntity.class).getResultList();
		return result;
		
	}
	
	private PendingCertifiedProductEntity getEntityById(Long entityId) throws EntityRetrievalException {
		
		PendingCertifiedProductEntity entity = null;
		
		Query query = entityManager.createQuery( "SELECT pcp from PendingCertifiedProductEntity pcp "
				+ " where (pending_certified_product_id = :entityid) "
				+ " and (not pcp.deleted = true)", PendingCertifiedProductEntity.class );
		query.setParameter("entityid", entityId);
		List<PendingCertifiedProductEntity> result = query.getResultList();
		
		if (result.size() > 1){
			throw new EntityRetrievalException("Data error. Duplicate Certified Product id in database.");
		}
		
		if (result.size() > 0){
			entity = result.get(0);
		}
		
		return entity;
	}
	
	private PendingCertifiedProductEntity getEntityByOncId(String id) throws EntityRetrievalException {
		
		PendingCertifiedProductEntity entity = null;
		
		Query query = entityManager.createQuery( "SELECT pcp from PendingCertifiedProductEntity pcp "
				+ " where (unique_id = :id) "
				+ " and (not pcp.deleted = true)", PendingCertifiedProductEntity.class );
		query.setParameter("id", id);
		List<PendingCertifiedProductEntity> result = query.getResultList();
		
		if (result.size() > 1){
			throw new EntityRetrievalException("Data error. Duplicate ONC id in database.");
		}
		
		if (result.size() > 0){
			entity = result.get(0);
		}
		
		return entity;
	}
	
	private List<PendingCertifiedProductEntity> getEntityByAcbId(Long acbId) {
				
		Query query = entityManager.createQuery( "SELECT pcp from PendingCertifiedProductEntity pcp "
				+ " where (certification_body_id = :acbId) "
				+ " and not (pcp.deleted = true)", PendingCertifiedProductEntity.class );
		query.setParameter("acbId", acbId);
		List<PendingCertifiedProductEntity> result = query.getResultList();
		return result;
	}
	
	private List<PendingCertifiedProductEntity> getEntitiesByStatus(Long statusId) {
		
		Query query = entityManager.createQuery( "SELECT pcp from PendingCertifiedProductEntity pcp "
				+ " where (certification_status_id = :statusId) "
				+ " and not (pcp.deleted = true)", PendingCertifiedProductEntity.class );
		query.setParameter("statusId", statusId);
		List<PendingCertifiedProductEntity> result = query.getResultList();
		return result;
	}
}
