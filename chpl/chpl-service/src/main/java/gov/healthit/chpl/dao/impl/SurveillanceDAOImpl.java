package gov.healthit.chpl.dao.impl;

import java.util.List;

import javax.persistence.EntityNotFoundException;
import javax.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.SurveillanceDAO;
import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.domain.SurveillanceNonconformity;
import gov.healthit.chpl.domain.SurveillanceNonconformityStatus;
import gov.healthit.chpl.domain.SurveillanceRequirement;
import gov.healthit.chpl.domain.SurveillanceRequirementType;
import gov.healthit.chpl.domain.SurveillanceResultType;
import gov.healthit.chpl.domain.SurveillanceType;
import gov.healthit.chpl.entity.NonconformityStatusEntity;
import gov.healthit.chpl.entity.PendingSurveillanceEntity;
import gov.healthit.chpl.entity.PendingSurveillanceNonconformityEntity;
import gov.healthit.chpl.entity.PendingSurveillanceRequirementEntity;
import gov.healthit.chpl.entity.SurveillanceRequirementTypeEntity;
import gov.healthit.chpl.entity.SurveillanceResultTypeEntity;
import gov.healthit.chpl.entity.SurveillanceTypeEntity;

@Repository("surveillanceDAO")
public class SurveillanceDAOImpl extends BaseDAOImpl implements SurveillanceDAO {
	private static final Logger logger = LogManager.getLogger(SurveillanceDAOImpl.class);
	
	public Surveillance insertSurveillance(Surveillance surv) {
		//TODO 
		return null;
	}
	
	public Long insertPendingSurveillance(Surveillance surv) {
		PendingSurveillanceEntity toInsert = new PendingSurveillanceEntity();
		toInsert.setSurvIdToReplace(surv.getSurveillanceIdToReplace());
		if(surv.getCertifiedProduct() != null) {
			toInsert.setCertifiedProductId(surv.getCertifiedProduct().getId());
			toInsert.setCertifiedProductUniqueId(surv.getCertifiedProduct().getChplProductNumber());
		}
		toInsert.setNumRandomizedSites(surv.getRandomizedSitesUsed());
		toInsert.setEndDate(surv.getEndDate());
		toInsert.setStartDate(surv.getStartDate());
		if(surv.getType() != null) {
			toInsert.setSurveillanceType(surv.getType().getName());
		}
		toInsert.setLastModifiedUser(Util.getCurrentUser().getId());
		toInsert.setDeleted(false);
		entityManager.persist(toInsert);
		entityManager.flush();
		
		for(SurveillanceRequirement req : surv.getRequirements()) {
			PendingSurveillanceRequirementEntity toInsertReq = new PendingSurveillanceRequirementEntity();
			if(req.getResult() != null) {
				toInsertReq.setResult(req.getResult().getName());
			}
			if(req.getType() != null) {
				toInsertReq.setRequirementType(req.getType().getName());
			}
			toInsertReq.setSurveilledRequirement(req.getRequirement());
			toInsertReq.setPendingSurveillanceId(toInsert.getId());
			toInsertReq.setLastModifiedUser(Util.getCurrentUser().getId());
			toInsertReq.setDeleted(false);
			
			entityManager.persist(toInsertReq);
			entityManager.flush();
			
			for(SurveillanceNonconformity nc : req.getNonconformities()) {
				PendingSurveillanceNonconformityEntity toInsertNc = new PendingSurveillanceNonconformityEntity();
				
				toInsertNc.setCapApproval(nc.getCapApprovalDate());
				toInsertNc.setCapEndDate(nc.getCapEndDate());
				toInsertNc.setCapMustCompleteDate(nc.getCapMustCompleteDate());
				toInsertNc.setCapStart(nc.getCapStartDate());
				toInsertNc.setDateOfDetermination(nc.getDateOfDetermination());
				toInsertNc.setDeveloperExplanation(nc.getDeveloperExplanation());
				toInsertNc.setFindings(nc.getFindings());
				toInsertNc.setPendingSurveillanceRequirementId(toInsertReq.getId());
				toInsertNc.setResolution(nc.getResolution());
				toInsertNc.setSitesPassed(nc.getSitesPassed());
				if(nc.getStatus() != null) {
					toInsertNc.setStatus(nc.getStatus().getName());
				}
				toInsertNc.setSummary(nc.getSummary());
				toInsertNc.setTotalSites(nc.getTotalSites());
				toInsertNc.setType(nc.getNonconformityType());
				toInsertNc.setDeleted(false);
				toInsertNc.setLastModifiedUser(Util.getCurrentUser().getId());
				
				entityManager.persist(toInsertNc);
				entityManager.flush();
			}
		}
		return toInsert.getId();
	}
	
	public void deleteSurveillance(Surveillance surv) throws EntityNotFoundException {
		logger.debug("Looking for surveillance with id " + surv.getId() + " to delete.");
		
	}
	
	public void deletePendingSurveillance(Surveillance surv) throws EntityNotFoundException {
		PendingSurveillanceEntity toDelete = fetchPendingSurveillanceById(surv.getId());
		if(toDelete == null) {
			throw new EntityNotFoundException("Could not find pending surveillance with id " + surv.getId());
		}
		
		if(toDelete.getSurveilledRequirements() != null) {
			for(PendingSurveillanceRequirementEntity reqToDelete : toDelete.getSurveilledRequirements()) {
				if(reqToDelete.getNonconformities() != null) {
					for(PendingSurveillanceNonconformityEntity ncToDelete : reqToDelete.getNonconformities()) {
						ncToDelete.setDeleted(true);
						ncToDelete.setLastModifiedUser(Util.getCurrentUser().getId());
						entityManager.merge(ncToDelete);
						entityManager.flush();
					}
				}
				reqToDelete.setDeleted(true);
				reqToDelete.setLastModifiedUser(Util.getCurrentUser().getId());
				entityManager.merge(reqToDelete);
				entityManager.flush();
			}
		}
		toDelete.setDeleted(true);
		toDelete.setLastModifiedUser(Util.getCurrentUser().getId());
		entityManager.merge(toDelete);
		entityManager.flush();
	}
	
	public PendingSurveillanceEntity getPendingSurveillanceById(Long id) {
		PendingSurveillanceEntity entity = fetchPendingSurveillanceById(id);
		return entity;
	}

	public List<PendingSurveillanceEntity> getPendingSurveillanceByAcb(Long acbId) {
		List<PendingSurveillanceEntity> results = fetchPendingSurveillanceByAcbId(acbId);
		return results;
	}
	
	public SurveillanceType findSurveillanceType(String type) {
		logger.debug("Searchig for surveillance type '" + type + "'.");
		Query query = entityManager.createQuery(
				"from SurveillanceTypeEntity where name LIKE :name and deleted <> true", 
				SurveillanceTypeEntity.class);
		query.setParameter("name", type);
		List<SurveillanceTypeEntity> matches = query.getResultList();
		
		SurveillanceTypeEntity resultEntity = null;
		if(matches != null && matches.size() > 0) {
			resultEntity = matches.get(0);
			logger.debug("Found surveillance type '" + type + "' having id '" + resultEntity.getId() + "'.");
		}
		
		SurveillanceType result = null;
		if(resultEntity != null) {
			result = new SurveillanceType();
			result.setId(resultEntity.getId());
			result.setName(resultEntity.getName());
		}
		return result;
	}
	
	public SurveillanceType findSurveillanceType(Long id) {
		logger.debug("Searchig for surveillance type with id '" + id + "'.");
		Query query = entityManager.createQuery(
				"from SurveillanceTypeEntity where id = :id and deleted <> true", 
				SurveillanceTypeEntity.class);
		query.setParameter("id", id);
		List<SurveillanceTypeEntity> matches = query.getResultList();
		
		SurveillanceTypeEntity resultEntity = null;
		if(matches != null && matches.size() > 0) {
			resultEntity = matches.get(0);
		}
		
		SurveillanceType result = null;
		if(resultEntity != null) {
			result = new SurveillanceType();
			result.setId(resultEntity.getId());
			result.setName(resultEntity.getName());
		}
		return result;
	}
	
	public SurveillanceRequirementType findSurveillanceRequirementType(String type) {
		logger.debug("Searchig for surveillance requirement type '" + type + "'.");
		Query query = entityManager.createQuery(
				"from SurveillanceRequirementTypeEntity where name LIKE :name and deleted <> true", 
				SurveillanceRequirementTypeEntity.class);
		query.setParameter("name", type);
		List<SurveillanceRequirementTypeEntity> matches = query.getResultList();
		
		SurveillanceRequirementTypeEntity resultEntity = null;
		if(matches != null && matches.size() > 0) {
			resultEntity = matches.get(0);
			logger.debug("Found surveillance requirement type '" + type + "' having id '" + resultEntity.getId() + "'.");
		}
		
		SurveillanceRequirementType result = null;
		if(resultEntity != null) {
			result = new SurveillanceRequirementType();
			result.setId(resultEntity.getId());
			result.setName(resultEntity.getName());
		}
		return result;
	}
	
	public SurveillanceRequirementType findSurveillanceRequirementType(Long id) {
		logger.debug("Searchig for surveillance requirement type by id '" + id + "'.");
		Query query = entityManager.createQuery(
				"from SurveillanceRequirementTypeEntity where id = :id and deleted <> true", 
				SurveillanceRequirementTypeEntity.class);
		query.setParameter("id", id);
		List<SurveillanceRequirementTypeEntity> matches = query.getResultList();
		
		SurveillanceRequirementTypeEntity resultEntity = null;
		if(matches != null && matches.size() > 0) {
			resultEntity = matches.get(0);
		}
		
		SurveillanceRequirementType result = null;
		if(resultEntity != null) {
			result = new SurveillanceRequirementType();
			result.setId(resultEntity.getId());
			result.setName(resultEntity.getName());
		}
		return result;
	}
	
	public SurveillanceResultType findSurveillanceResultType(String type) {
		logger.debug("Searchig for surveillance result type '" + type + "'.");
		Query query = entityManager.createQuery(
				"from SurveillanceResultTypeEntity where name LIKE :name and deleted <> true", 
				SurveillanceResultTypeEntity.class);
		query.setParameter("name", type);
		List<SurveillanceResultTypeEntity> matches = query.getResultList();
		
		SurveillanceResultTypeEntity resultEntity = null;
		if(matches != null && matches.size() > 0) {
			resultEntity = matches.get(0);
			logger.debug("Found surveillance result type '" + type + "' having id '" + resultEntity.getId() + "'.");
		}
		
		SurveillanceResultType result = null;
		if(resultEntity != null) {
			result = new SurveillanceResultType();
			result.setId(resultEntity.getId());
			result.setName(resultEntity.getName());
		}
		return result;
	}
	
	public SurveillanceResultType findSurveillanceResultType(Long id) {
		logger.debug("Searchig for surveillance result type by id '" + id + "'.");
		Query query = entityManager.createQuery(
				"from SurveillanceResultTypeEntity where id = :id and deleted <> true", 
				SurveillanceResultTypeEntity.class);
		query.setParameter("id", id);
		List<SurveillanceResultTypeEntity> matches = query.getResultList();
		
		SurveillanceResultTypeEntity resultEntity = null;
		if(matches != null && matches.size() > 0) {
			resultEntity = matches.get(0);
		}
		
		SurveillanceResultType result = null;
		if(resultEntity != null) {
			result = new SurveillanceResultType();
			result.setId(resultEntity.getId());
			result.setName(resultEntity.getName());
		}
		return result;
	}
	
	public SurveillanceNonconformityStatus findSurveillanceNonconformityStatusType(String type) {
		logger.debug("Searchig for nonconformity status type '" + type + "'.");
		Query query = entityManager.createQuery(
				"from NonconformityStatusEntity where name LIKE :name and deleted <> true", 
				NonconformityStatusEntity.class);
		query.setParameter("name", type);
		List<NonconformityStatusEntity> matches = query.getResultList();
		
		NonconformityStatusEntity resultEntity = null;
		if(matches != null && matches.size() > 0) {
			resultEntity = matches.get(0);
			logger.debug("Found nonconformity status type '" + type + "' having id '" + resultEntity.getId() + "'.");
		}
		
		SurveillanceNonconformityStatus result = null;
		if(resultEntity != null) {
			result = new SurveillanceNonconformityStatus();
			result.setId(resultEntity.getId());
			result.setName(resultEntity.getName());
		}
		return result;
	}
	
	public SurveillanceNonconformityStatus findSurveillanceNonconformityStatusType(Long id) {
		logger.debug("Searchig for nonconformity status type by id '" + id + "'.");
		Query query = entityManager.createQuery(
				"from NonconformityStatusEntity where id = :id and deleted <> true", 
				NonconformityStatusEntity.class);
		query.setParameter("id", id);
		List<NonconformityStatusEntity> matches = query.getResultList();
		
		NonconformityStatusEntity resultEntity = null;
		if(matches != null && matches.size() > 0) {
			resultEntity = matches.get(0);
		}
		
		SurveillanceNonconformityStatus result = null;
		if(resultEntity != null) {
			result = new SurveillanceNonconformityStatus();
			result.setId(resultEntity.getId());
			result.setName(resultEntity.getName());
		}
		return result;
	}
	
	private PendingSurveillanceEntity fetchPendingSurveillanceById(Long id) {
		entityManager.clear();
		Query query = entityManager.createQuery("SELECT DISTINCT surv " 
				+ "FROM PendingSurveillanceEntity surv "
				+ "LEFT OUTER JOIN FETCH surv.surveilledRequirements reqs "
				+ "LEFT OUTER JOIN FETCH reqs.nonconformities ncs "
				+ "WHERE surv.deleted <> true "
				+ "AND surv.id = :entityid", 
				PendingSurveillanceEntity.class);
		query.setParameter("entityid", id);
		
		List<PendingSurveillanceEntity> results = query.getResultList();
		if(results != null && results.size() > 0) {
			return results.get(0);
		}
		return null;
	}
	
	private List<PendingSurveillanceEntity> fetchPendingSurveillanceByAcbId(Long acbId) {
		entityManager.clear();
		Query query = entityManager.createQuery("SELECT DISTINCT surv " 
				+ "FROM PendingSurveillanceEntity surv "
				+ "JOIN FETCH surv.certifiedProduct cp "
				+ "LEFT OUTER JOIN FETCH surv.surveilledRequirements reqs "
				+ "LEFT OUTER JOIN FETCH reqs.nonconformities ncs "
				+ "WHERE surv.deleted <> true "
				+ "AND cp.certificationBodyId = :acbId", 
				PendingSurveillanceEntity.class);
		query.setParameter("acbId", acbId);
		
		List<PendingSurveillanceEntity> results = query.getResultList();
		return results;
	}
}
