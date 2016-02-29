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
import gov.healthit.chpl.domain.CertificationResultTestStandard;
import gov.healthit.chpl.dto.CertificationStatusDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.entity.CertificationResultTestStandardEntity;
import gov.healthit.chpl.entity.PendingCertificationResultAdditionalSoftwareEntity;
import gov.healthit.chpl.entity.PendingCertificationResultEntity;
import gov.healthit.chpl.entity.PendingCertificationResultTestDataEntity;
import gov.healthit.chpl.entity.PendingCertificationResultTestFunctionalityEntity;
import gov.healthit.chpl.entity.PendingCertificationResultTestProcedureEntity;
import gov.healthit.chpl.entity.PendingCertificationResultTestStandardEntity;
import gov.healthit.chpl.entity.PendingCertificationResultTestToolEntity;
import gov.healthit.chpl.entity.PendingCertificationResultUcdProcessEntity;
import gov.healthit.chpl.entity.PendingCertifiedProductEntity;
import gov.healthit.chpl.entity.PendingCertifiedProductQmsStandardEntity;
import gov.healthit.chpl.entity.PendingCqmCriterionEntity;

@Repository(value="pendingCertifiedProductDAO")
public class PendingCertifiedProductDAOImpl extends BaseDAOImpl implements PendingCertifiedProductDAO {
	
	@Override
	@Transactional
	public PendingCertifiedProductDTO create(PendingCertifiedProductEntity toCreate) {		
		toCreate.setLastModifiedDate(new Date());
		toCreate.setLastModifiedUser(Util.getCurrentUser().getId());
		toCreate.setCreationDate(new Date());
		toCreate.setDeleted(false);
		
		entityManager.persist(toCreate);
		
		for(PendingCertifiedProductQmsStandardEntity qmsStandard : toCreate.getQmsStandards()) {
			qmsStandard.setPendingCertifiedProductId(toCreate.getId());
			qmsStandard.setLastModifiedDate(new Date());	
			qmsStandard.setLastModifiedUser(Util.getCurrentUser().getId());
			qmsStandard.setCreationDate(new Date());
			qmsStandard.setDeleted(false);
			entityManager.persist(qmsStandard);
		}
		
		for(PendingCertificationResultEntity criterion : toCreate.getCertificationCriterion()) {
			criterion.setPendingCertifiedProductId(toCreate.getId());
			criterion.setLastModifiedDate(new Date());	
			criterion.setLastModifiedUser(Util.getCurrentUser().getId());
			criterion.setCreationDate(new Date());
			criterion.setDeleted(false);
			entityManager.persist(criterion);
			
			if(criterion.getUcdProcesses() != null && criterion.getUcdProcesses().size() > 0) {
				for(PendingCertificationResultUcdProcessEntity ucd : criterion.getUcdProcesses()) {
					ucd.setPendingCertificationResultId(criterion.getId());
					ucd.setLastModifiedDate(new Date());	
					ucd.setLastModifiedUser(Util.getCurrentUser().getId());
					ucd.setCreationDate(new Date());
					ucd.setDeleted(false);
					entityManager.persist(ucd);
				}				
			}
			
			if(criterion.getTestStandards() != null && criterion.getTestStandards().size() > 0) {
				for(PendingCertificationResultTestStandardEntity tsEntity : criterion.getTestStandards()) {
					tsEntity.setPendingCertificationResultId(criterion.getId());
					tsEntity.setLastModifiedDate(new Date());	
					tsEntity.setLastModifiedUser(Util.getCurrentUser().getId());
					tsEntity.setCreationDate(new Date());
					tsEntity.setDeleted(false);
					entityManager.persist(tsEntity);
				}
			}
			if(criterion.getTestFunctionality() != null && criterion.getTestFunctionality().size() > 0) {
				for(PendingCertificationResultTestFunctionalityEntity tfEntity : criterion.getTestFunctionality()) {
					tfEntity.setPendingCertificationResultId(criterion.getId());
					tfEntity.setLastModifiedDate(new Date());	
					tfEntity.setLastModifiedUser(Util.getCurrentUser().getId());
					tfEntity.setCreationDate(new Date());
					tfEntity.setDeleted(false);
					entityManager.persist(tfEntity);
				}
			}
			
			if(criterion.getAdditionalSoftware() != null && criterion.getAdditionalSoftware().size() > 0) {
				for(PendingCertificationResultAdditionalSoftwareEntity asEntity : criterion.getAdditionalSoftware()) {
					asEntity.setPendingCertificationResultId(criterion.getId());
					asEntity.setLastModifiedDate(new Date());	
					asEntity.setLastModifiedUser(Util.getCurrentUser().getId());
					asEntity.setCreationDate(new Date());
					asEntity.setDeleted(false);
					entityManager.persist(asEntity);
				}
			}
			
			if(criterion.getTestProcedures() != null && criterion.getTestProcedures().size() > 0) {
				for(PendingCertificationResultTestProcedureEntity tpEntity : criterion.getTestProcedures()) {
					tpEntity.setPendingCertificationResultId(criterion.getId());
					tpEntity.setLastModifiedDate(new Date());	
					tpEntity.setLastModifiedUser(Util.getCurrentUser().getId());
					tpEntity.setCreationDate(new Date());
					tpEntity.setDeleted(false);
					entityManager.persist(tpEntity);
				}
			}
			
			if(criterion.getTestData() != null && criterion.getTestData().size() > 0) {
				for(PendingCertificationResultTestDataEntity tdEntity : criterion.getTestData()) {
					tdEntity.setPendingCertificationResultId(criterion.getId());
					tdEntity.setLastModifiedDate(new Date());	
					tdEntity.setLastModifiedUser(Util.getCurrentUser().getId());
					tdEntity.setCreationDate(new Date());
					tdEntity.setDeleted(false);
					entityManager.persist(tdEntity);
				}
			}
			
			if(criterion.getTestTools() != null && criterion.getTestTools().size() > 0) {
				for(PendingCertificationResultTestToolEntity ttEntity : criterion.getTestTools()) {
					ttEntity.setPendingCertificationResultId(criterion.getId());
					ttEntity.setLastModifiedDate(new Date());	
					ttEntity.setLastModifiedUser(Util.getCurrentUser().getId());
					ttEntity.setCreationDate(new Date());
					ttEntity.setDeleted(false);
					entityManager.persist(ttEntity);
				}
			}
			
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
		
		List<PendingCertifiedProductEntity> result = entityManager.createQuery( 
				"SELECT pcp from PendingCertifiedProductEntity pcp " 
				+ "WHERE (not pcp.deleted = true)", PendingCertifiedProductEntity.class).getResultList();
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
