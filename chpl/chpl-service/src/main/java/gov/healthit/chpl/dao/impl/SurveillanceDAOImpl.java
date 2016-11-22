package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityNotFoundException;
import javax.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.SurveillanceDAO;
import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.domain.SurveillanceNonconformity;
import gov.healthit.chpl.domain.SurveillanceNonconformityDocument;
import gov.healthit.chpl.domain.SurveillanceNonconformityStatus;
import gov.healthit.chpl.domain.SurveillanceRequirement;
import gov.healthit.chpl.domain.SurveillanceRequirementType;
import gov.healthit.chpl.domain.SurveillanceResultType;
import gov.healthit.chpl.domain.SurveillanceType;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.entity.NonconformityStatusEntity;
import gov.healthit.chpl.entity.PendingSurveillanceEntity;
import gov.healthit.chpl.entity.PendingSurveillanceNonconformityEntity;
import gov.healthit.chpl.entity.PendingSurveillanceRequirementEntity;
import gov.healthit.chpl.entity.SurveillanceEntity;
import gov.healthit.chpl.entity.SurveillanceNonconformityDocumentationEntity;
import gov.healthit.chpl.entity.SurveillanceNonconformityEntity;
import gov.healthit.chpl.entity.SurveillanceRequirementEntity;
import gov.healthit.chpl.entity.SurveillanceRequirementTypeEntity;
import gov.healthit.chpl.entity.SurveillanceResultTypeEntity;
import gov.healthit.chpl.entity.SurveillanceTypeEntity;

@Repository("surveillanceDAO")
public class SurveillanceDAOImpl extends BaseDAOImpl implements SurveillanceDAO {
	private static final Logger logger = LogManager.getLogger(SurveillanceDAOImpl.class);
	
	@Autowired CertificationCriterionDAO criterionDao;
	
	public Long insertSurveillance(Surveillance surv) {
		SurveillanceEntity toInsert = new SurveillanceEntity();
		populateSurveillanceEntity(toInsert, surv);
		toInsert.setLastModifiedUser(Util.getCurrentUser().getId());
		toInsert.setDeleted(false);
		entityManager.persist(toInsert);
		entityManager.flush();
		
		for(SurveillanceRequirement req : surv.getRequirements()) {
			SurveillanceRequirementEntity toInsertReq = new SurveillanceRequirementEntity();
			populateSurveillanceRequirementEntity(toInsertReq, req);
			toInsertReq.setSurveillanceId(toInsert.getId());
			toInsertReq.setLastModifiedUser(Util.getCurrentUser().getId());
			toInsertReq.setDeleted(false);
			entityManager.persist(toInsertReq);
			entityManager.flush();
			
			for(SurveillanceNonconformity nc : req.getNonconformities()) {
				SurveillanceNonconformityEntity toInsertNc = new SurveillanceNonconformityEntity();
				populateSurveillanceNonconformityEntity(toInsertNc, nc);
				toInsertNc.setSurveillanceRequirementId(toInsertReq.getId());
				toInsertNc.setDeleted(false);
				toInsertNc.setLastModifiedUser(Util.getCurrentUser().getId());
				
				entityManager.persist(toInsertNc);
				entityManager.flush();
			}
		}
		return toInsert.getId();
	}
	
	public Long insertNonconformityDocument(Long nonconformityId, SurveillanceNonconformityDocument doc) {
		SurveillanceNonconformityDocumentationEntity docEntity = new SurveillanceNonconformityDocumentationEntity();
		docEntity.setNonconformityId(nonconformityId);
		docEntity.setFileData(doc.getFileContents());
		docEntity.setFileType(doc.getFileType());
		docEntity.setFileName(doc.getFileName());
		docEntity.setDeleted(false);
		docEntity.setLastModifiedUser(Util.getCurrentUser().getId());
		
		entityManager.persist(docEntity);
		entityManager.flush();
		
		return docEntity.getId();
	}
	
	public Long updateSurveillance(Surveillance newSurv) {
		SurveillanceEntity oldSurv = fetchSurveillanceById(newSurv.getId());
		populateSurveillanceEntity(oldSurv, newSurv);
		oldSurv.setLastModifiedUser(Util.getCurrentUser().getId());
		oldSurv.setDeleted(false);
		entityManager.merge(oldSurv);
		entityManager.flush();
		
		//look for reqs that are in the updateSurv but not the newSurv and mark as deleted
		for(SurveillanceRequirementEntity oldReq : oldSurv.getSurveilledRequirements()) {
			boolean isFoundInUpdate = false;
			for(SurveillanceRequirement newReq : newSurv.getRequirements()) {
				if(newReq.getId() != null && 
						newReq.getId().longValue() == oldReq.getId().longValue()) {
					isFoundInUpdate = true;
				}
			}
			
			if(!isFoundInUpdate) {
				//delete nonconformities and documents for this requirement first
				for(SurveillanceNonconformityEntity nc : oldReq.getNonconformities()) {
					if(nc.getDocuments() != null) {
						for(SurveillanceNonconformityDocumentationEntity ncDoc : nc.getDocuments()) {
							ncDoc.setLastModifiedUser(Util.getCurrentUser().getId());
							ncDoc.setDeleted(true);
							entityManager.merge(ncDoc);
							entityManager.flush();
						}
					}
					nc.setLastModifiedUser(Util.getCurrentUser().getId());
					nc.setDeleted(true);
					entityManager.merge(nc);
					entityManager.flush();
				}
				//delete the req
				oldReq.setLastModifiedUser(Util.getCurrentUser().getId());
				oldReq.setDeleted(true);
				entityManager.merge(oldReq);
				entityManager.flush();
			}
		}
		
		//look through the incoming reqs and add or update as necessary
		for(SurveillanceRequirement newReq : newSurv.getRequirements()) {
			if(newReq.getId() != null && newReq.getId().longValue() > 0) {
				//update existing req
				for(SurveillanceRequirementEntity oldReq : oldSurv.getSurveilledRequirements()) {
					if(oldReq.getId().longValue() == newReq.getId().longValue()) {
						populateSurveillanceRequirementEntity(oldReq, newReq);
						oldReq.setLastModifiedUser(Util.getCurrentUser().getId());
						oldReq.setDeleted(false);
						entityManager.merge(oldReq);
						entityManager.flush();
						
						//look for nonconformites that are in updateReq but not in newReq and mark as deleted
						for(SurveillanceNonconformityEntity oldNc : oldReq.getNonconformities()) {
							boolean isFoundInUpdate = false;
							for(SurveillanceNonconformity newNc : newReq.getNonconformities()) {
								if(newNc.getId() != null && 
										newNc.getId().longValue() == oldNc.getId().longValue()) {
									isFoundInUpdate = true;
								}
							}
							if(!isFoundInUpdate) {
								if(oldNc.getDocuments() != null) {
									for(SurveillanceNonconformityDocumentationEntity ncDoc : oldNc.getDocuments()) {
										ncDoc.setLastModifiedUser(Util.getCurrentUser().getId());
										ncDoc.setDeleted(true);
										entityManager.merge(ncDoc);
										entityManager.flush();
									}
								}
								oldNc.setLastModifiedUser(Util.getCurrentUser().getId());
								oldNc.setDeleted(true);
								entityManager.merge(oldNc);
								entityManager.flush();
							}
						}
						
						//look through newReq nonconformities and add or update as necessary
						for(SurveillanceNonconformity newNc : newReq.getNonconformities()) {
							if(newNc.getId() != null && newNc.getId().longValue() > 0) {
								//update existing nonconformity
								for(SurveillanceNonconformityEntity oldNc : oldReq.getNonconformities()) {
									if(oldNc.getId().longValue() == newNc.getId().longValue()) {
										populateSurveillanceNonconformityEntity(oldNc, newNc);
										oldNc.setLastModifiedUser(Util.getCurrentUser().getId());
										oldNc.setDeleted(false);
										entityManager.merge(oldNc);
										entityManager.flush();
									}
								}
							} else {
								//add new nonconformity
								SurveillanceNonconformityEntity toInsertNc = new SurveillanceNonconformityEntity();
								populateSurveillanceNonconformityEntity(toInsertNc, newNc);
								toInsertNc.setSurveillanceRequirementId(oldReq.getId());
								toInsertNc.setDeleted(false);
								toInsertNc.setLastModifiedUser(Util.getCurrentUser().getId());
								entityManager.persist(toInsertNc);
								entityManager.flush();
							}
						}
					}
				}
			} else {
				//add new req
				SurveillanceRequirementEntity toInsertReq = new SurveillanceRequirementEntity();
				populateSurveillanceRequirementEntity(toInsertReq, newReq);
				toInsertReq.setSurveillanceId(oldSurv.getId());
				toInsertReq.setLastModifiedUser(Util.getCurrentUser().getId());
				toInsertReq.setDeleted(false);
				entityManager.persist(toInsertReq);
				entityManager.flush();
				//add new nonconformities
				for(SurveillanceNonconformity nc : newReq.getNonconformities()) {
					SurveillanceNonconformityEntity toInsertNc = new SurveillanceNonconformityEntity();
					populateSurveillanceNonconformityEntity(toInsertNc, nc);
					toInsertNc.setSurveillanceRequirementId(toInsertReq.getId());
					toInsertNc.setDeleted(false);
					toInsertNc.setLastModifiedUser(Util.getCurrentUser().getId());
					
					entityManager.persist(toInsertNc);
					entityManager.flush();
				}
			}
		}
		
		return newSurv.getId();
	}
	
	public SurveillanceEntity getSurveillanceByCertifiedProductAndFriendlyId(Long certifiedProductId, String survFriendlyId) {
		Query query = entityManager.createQuery("from SurveillanceEntity surv "
				+ "where surv.friendlyId = :friendlyId "
				+ "and surv.certifiedProductId = :cpId "
				+ "and surv.deleted <> true",
				SurveillanceEntity.class);
		query.setParameter("friendlyId", survFriendlyId);
		query.setParameter("cpId", certifiedProductId);
		List<SurveillanceEntity> matches = query.getResultList();
		
		if(matches != null && matches.size() > 0) {
			return matches.get(0);
		}
		return null;
	}
	
	public SurveillanceEntity getSurveillanceById(Long id) {
		SurveillanceEntity result = fetchSurveillanceById(id);
		return result;
	}
	
	public SurveillanceNonconformityDocumentationEntity getDocumentById(Long documentId) throws EntityNotFoundException {
		SurveillanceNonconformityDocumentationEntity doc = entityManager.find(SurveillanceNonconformityDocumentationEntity.class, documentId);
		if(doc == null) {
			logger.error("Could not find documentation with id " + documentId);
			throw new EntityNotFoundException("Could not find documentation with id " + documentId);
		}
		return doc;
	}
	
	public List<SurveillanceEntity> getSurveillanceByCertifiedProductId(Long id) {
		List<SurveillanceEntity> results = fetchSurveillanceByCertifiedProductId(id);
		return results;
	}
	
	public Long insertPendingSurveillance(Surveillance surv) {
		PendingSurveillanceEntity toInsert = new PendingSurveillanceEntity();
		toInsert.setSurvFriendlyIdToReplace(surv.getSurveillanceIdToReplace());
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
	
	public void deleteNonconformityDocument(Long documentId) throws EntityNotFoundException {
		SurveillanceNonconformityDocumentationEntity doc = entityManager.find(SurveillanceNonconformityDocumentationEntity.class, documentId);
		if(doc == null) {
			logger.error("Could not find documentation with id " + documentId);
			throw new EntityNotFoundException("Could not find documentation with id " + documentId);
		}
		doc.setDeleted(true);
		doc.setLastModifiedUser(Util.getCurrentUser().getId());
		entityManager.merge(doc);
		entityManager.flush();
	}
	
	public void deleteSurveillance(Surveillance surv) throws EntityNotFoundException {
		logger.debug("Looking for surveillance with id " + surv.getId() + " to delete.");
		SurveillanceEntity toDelete = fetchSurveillanceById(surv.getId());
		if(toDelete == null) {
			throw new EntityNotFoundException("Could not find surveillance with id " + surv.getId());
		}
		
		if(toDelete.getSurveilledRequirements() != null) {
			for(SurveillanceRequirementEntity reqToDelete : toDelete.getSurveilledRequirements()) {
				if(reqToDelete.getNonconformities() != null) {
					for(SurveillanceNonconformityEntity ncToDelete : reqToDelete.getNonconformities()) {
						if(ncToDelete.getDocuments() != null) {
							for(SurveillanceNonconformityDocumentationEntity docsToDelete : ncToDelete.getDocuments()) {
								docsToDelete.setDeleted(true);
								docsToDelete.setLastModifiedUser(Util.getCurrentUser().getId());
								entityManager.merge(ncToDelete);
								entityManager.flush();
							}
						}
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
	
	public List<SurveillanceType> getAllSurveillanceTypes() {
		Query query = entityManager.createQuery("from SurveillanceTypeEntity where deleted <> true",
				SurveillanceTypeEntity.class);
		List<SurveillanceTypeEntity> resultEntities = query.getResultList();
		List<SurveillanceType> results = new ArrayList<SurveillanceType>();
		for(SurveillanceTypeEntity resultEntity : resultEntities) {
			SurveillanceType result = convert(resultEntity);
			results.add(result);
		}
		return results;
	}
	
	public SurveillanceType findSurveillanceType(String type) {
		logger.debug("Searchig for surveillance type '" + type + "'.");
		Query query = entityManager.createQuery(
				"from SurveillanceTypeEntity where UPPER(name) LIKE :name and deleted <> true", 
				SurveillanceTypeEntity.class);
		query.setParameter("name", type.toUpperCase());
		List<SurveillanceTypeEntity> matches = query.getResultList();
		
		SurveillanceTypeEntity resultEntity = null;
		if(matches != null && matches.size() > 0) {
			resultEntity = matches.get(0);
			logger.debug("Found surveillance type '" + type + "' having id '" + resultEntity.getId() + "'.");
		}
		
		SurveillanceType result = convert(resultEntity);
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
		
		SurveillanceType result = convert(resultEntity);
		return result;
	}
	
	public List<SurveillanceRequirementType> getAllSurveillanceRequirementTypes() {
		Query query = entityManager.createQuery(
				"from SurveillanceRequirementTypeEntity where deleted <> true", 
				SurveillanceRequirementTypeEntity.class);
		List<SurveillanceRequirementTypeEntity> resultEntities = query.getResultList();
		List<SurveillanceRequirementType> results = new ArrayList<SurveillanceRequirementType>();
		for(SurveillanceRequirementTypeEntity resultEntity : resultEntities) {
			SurveillanceRequirementType result = convert(resultEntity);
			results.add(result);
		}
		return results;
	}
	
	public SurveillanceRequirementType findSurveillanceRequirementType(String type) {
		logger.debug("Searchig for surveillance requirement type '" + type + "'.");
		Query query = entityManager.createQuery(
				"from SurveillanceRequirementTypeEntity where UPPER(name) LIKE :name and deleted <> true", 
				SurveillanceRequirementTypeEntity.class);
		query.setParameter("name", type.toUpperCase());
		List<SurveillanceRequirementTypeEntity> matches = query.getResultList();
		
		SurveillanceRequirementTypeEntity resultEntity = null;
		if(matches != null && matches.size() > 0) {
			resultEntity = matches.get(0);
			logger.debug("Found surveillance requirement type '" + type + "' having id '" + resultEntity.getId() + "'.");
		}
		
		SurveillanceRequirementType result = convert(resultEntity);
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
		
		SurveillanceRequirementType result = convert(resultEntity);
		return result;
	}
	
	public List<SurveillanceResultType> getAllSurveillanceResultTypes() {
		Query query = entityManager.createQuery(
				"from SurveillanceResultTypeEntity where deleted <> true", 
				SurveillanceResultTypeEntity.class);
		List<SurveillanceResultTypeEntity> resultEntities = query.getResultList();
		List<SurveillanceResultType> results = new ArrayList<SurveillanceResultType>();
		for(SurveillanceResultTypeEntity resultEntity : resultEntities) {
			SurveillanceResultType result = convert(resultEntity);
			results.add(result);
		}
		return results;
	}
	
	public SurveillanceResultType findSurveillanceResultType(String type) {
		logger.debug("Searching for surveillance result type '" + type + "'.");
		Query query = entityManager.createQuery(
				"from SurveillanceResultTypeEntity where UPPER(name) LIKE :name and deleted <> true", 
				SurveillanceResultTypeEntity.class);
		query.setParameter("name", type.toUpperCase());
		List<SurveillanceResultTypeEntity> matches = query.getResultList();
		
		SurveillanceResultTypeEntity resultEntity = null;
		if(matches != null && matches.size() > 0) {
			resultEntity = matches.get(0);
			logger.debug("Found surveillance result type '" + type + "' having id '" + resultEntity.getId() + "'.");
		}
		
		SurveillanceResultType result = convert(resultEntity);
		return result;
	}
	
	public SurveillanceResultType findSurveillanceResultType(Long id) {
		logger.debug("Searching for surveillance result type by id '" + id + "'.");
		Query query = entityManager.createQuery(
				"from SurveillanceResultTypeEntity where id = :id and deleted <> true", 
				SurveillanceResultTypeEntity.class);
		query.setParameter("id", id);
		List<SurveillanceResultTypeEntity> matches = query.getResultList();
		
		SurveillanceResultTypeEntity resultEntity = null;
		if(matches != null && matches.size() > 0) {
			resultEntity = matches.get(0);
		}
		
		SurveillanceResultType result = convert(resultEntity);
		return result;
	}
	
	public List<SurveillanceNonconformityStatus> getAllSurveillanceNonconformityStatusTypes() {
		Query query = entityManager.createQuery(
				"from NonconformityStatusEntity where deleted <> true", 
				NonconformityStatusEntity.class);
		List<NonconformityStatusEntity> resultEntities = query.getResultList();
		List<SurveillanceNonconformityStatus> results = new ArrayList<SurveillanceNonconformityStatus>();
		for(NonconformityStatusEntity resultEntity : resultEntities) {
			SurveillanceNonconformityStatus result = convert(resultEntity);
			results.add(result);
		}
		return results;
	}
	
	public SurveillanceNonconformityStatus findSurveillanceNonconformityStatusType(String type) {
		logger.debug("Searching for nonconformity status type '" + type + "'.");
		Query query = entityManager.createQuery(
				"from NonconformityStatusEntity where UPPER(name) LIKE :name and deleted <> true", 
				NonconformityStatusEntity.class);
		query.setParameter("name", type.toUpperCase());
		List<NonconformityStatusEntity> matches = query.getResultList();
		
		NonconformityStatusEntity resultEntity = null;
		if(matches != null && matches.size() > 0) {
			resultEntity = matches.get(0);
			logger.debug("Found nonconformity status type '" + type + "' having id '" + resultEntity.getId() + "'.");
		}
		
		SurveillanceNonconformityStatus result = convert(resultEntity);
		return result;
	}
	
	public SurveillanceNonconformityStatus findSurveillanceNonconformityStatusType(Long id) {
		logger.debug("Searching for nonconformity status type by id '" + id + "'.");
		Query query = entityManager.createQuery(
				"from NonconformityStatusEntity where id = :id and deleted <> true", 
				NonconformityStatusEntity.class);
		query.setParameter("id", id);
		List<NonconformityStatusEntity> matches = query.getResultList();
		
		NonconformityStatusEntity resultEntity = null;
		if(matches != null && matches.size() > 0) {
			resultEntity = matches.get(0);
		}
		
		SurveillanceNonconformityStatus result = convert(resultEntity);
		return result;
	}
	
	private SurveillanceEntity fetchSurveillanceById(Long id) {
		entityManager.clear();
		Query query = entityManager.createQuery("SELECT DISTINCT surv " 
				+ "FROM SurveillanceEntity surv "
				+ "JOIN FETCH surv.certifiedProduct "
				+ "JOIN FETCH surv.surveillanceType "
				+ "LEFT OUTER JOIN FETCH surv.surveilledRequirements reqs "		
				+ "LEFT OUTER JOIN FETCH reqs.surveillanceRequirementType "
				+ "LEFT OUTER JOIN FETCH reqs.certificationCriterionEntity "
				+ "LEFT OUTER JOIN FETCH reqs.surveillanceResultTypeEntity "
				+ "LEFT OUTER JOIN FETCH reqs.nonconformities ncs "
				+ "LEFT OUTER JOIN FETCH ncs.certificationCriterionEntity "
				+ "LEFT OUTER JOIN FETCH ncs.nonconformityStatus "
				+ "LEFT OUTER JOIN FETCH ncs.documents "
				+ "WHERE surv.deleted <> true "
				+ "AND surv.id = :entityid", 
				SurveillanceEntity.class);
		query.setParameter("entityid", id);
		
		List<SurveillanceEntity> results = query.getResultList();
		if(results != null && results.size() > 0) {
			return results.get(0);
		}
		return null;
	}
	
	private List<SurveillanceEntity> fetchSurveillanceByCertifiedProductId(Long id) {
		entityManager.clear();
		Query query = entityManager.createQuery("SELECT DISTINCT surv " 
				+ "FROM SurveillanceEntity surv "
				+ "JOIN FETCH surv.certifiedProduct "
				+ "JOIN FETCH surv.surveillanceType "
				+ "LEFT OUTER JOIN FETCH surv.surveilledRequirements reqs "				
				+ "LEFT OUTER JOIN FETCH reqs.surveillanceRequirementType "
				+ "LEFT OUTER JOIN FETCH reqs.certificationCriterionEntity "
				+ "LEFT OUTER JOIN FETCH reqs.surveillanceResultTypeEntity "
				+ "LEFT OUTER JOIN FETCH reqs.nonconformities ncs "
				+ "LEFT OUTER JOIN FETCH ncs.certificationCriterionEntity "
				+ "LEFT OUTER JOIN FETCH ncs.nonconformityStatus "
				+ "LEFT OUTER JOIN FETCH ncs.documents "
				+ "WHERE surv.deleted <> true "
				+ "AND surv.certifiedProductId = :cpId", 
				SurveillanceEntity.class);
		query.setParameter("cpId", id);
		
		List<SurveillanceEntity> results = query.getResultList();
		return results;
	}
	
	private PendingSurveillanceEntity fetchPendingSurveillanceById(Long id) {
		entityManager.clear();
		Query query = entityManager.createQuery("SELECT DISTINCT surv " 
				+ "FROM PendingSurveillanceEntity surv "
				+ "JOIN FETCH surv.certifiedProduct "
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
	
	private SurveillanceType convert(SurveillanceTypeEntity entity) {
		SurveillanceType result = null;
		if(entity != null) {
			result = new SurveillanceType();
			result.setId(entity.getId());
			result.setName(entity.getName());
		}
		return result;
	}
	
	private SurveillanceRequirementType convert(SurveillanceRequirementTypeEntity entity) {
		SurveillanceRequirementType result = null;
		if(entity != null) {
			result = new SurveillanceRequirementType();
			result.setId(entity.getId());
			result.setName(entity.getName());
		}
		return result;
	}
	
	private SurveillanceResultType convert(SurveillanceResultTypeEntity entity) {
		SurveillanceResultType result = null;
		if(entity != null) {
			result = new SurveillanceResultType();
			result.setId(entity.getId());
			result.setName(entity.getName());
		}
		return result;
	}
	
	private SurveillanceNonconformityStatus convert(NonconformityStatusEntity entity) {
		SurveillanceNonconformityStatus result = null;
		if(entity != null) {
			result = new SurveillanceNonconformityStatus();
			result.setId(entity.getId());
			result.setName(entity.getName());
		}
		return result;
	}
	
	private void populateSurveillanceEntity(SurveillanceEntity to, Surveillance from) {
		if(from.getCertifiedProduct() != null) {
			to.setCertifiedProductId(from.getCertifiedProduct().getId());
		}
		to.setEndDate(from.getEndDate());
		to.setNumRandomizedSites(from.getRandomizedSitesUsed());
		to.setStartDate(from.getStartDate());
		if(from.getType() != null) {
			to.setSurveillanceTypeId(from.getType().getId());
		}
	}
	
	private void populateSurveillanceRequirementEntity(SurveillanceRequirementEntity to, SurveillanceRequirement from) {
		if(from.getRequirement() != null) {
			CertificationCriterionDTO crit = criterionDao.getByName(from.getRequirement());
			if(crit != null) {
				to.setCertificationCriterionId(crit.getId());
			} else {
				to.setSurveilledRequirement(from.getRequirement());
			}
		}
		if(from.getType() != null) {
			to.setSurveillanceRequirementTypeId(from.getType().getId());
		}
		if(from.getResult() != null) {
			to.setSurveillanceResultTypeId(from.getResult().getId());
		}
	}
	
	private void populateSurveillanceNonconformityEntity(SurveillanceNonconformityEntity to, SurveillanceNonconformity from) {
		if(from.getNonconformityType() != null) {
			CertificationCriterionDTO crit = criterionDao.getByName(from.getNonconformityType());
			if(crit != null) {
				to.setCertificationCriterionId(crit.getId());
			} else {
				to.setType(from.getNonconformityType());
			}
		}
		to.setCapApproval(from.getCapApprovalDate());
		to.setCapEndDate(from.getCapEndDate());
		to.setCapMustCompleteDate(from.getCapMustCompleteDate());
		to.setCapStart(from.getCapStartDate());
		to.setDateOfDetermination(from.getDateOfDetermination());
		to.setDeveloperExplanation(from.getDeveloperExplanation());
		to.setFindings(from.getFindings());
		to.setResolution(from.getResolution());
		to.setSitesPassed(from.getSitesPassed());
		if(from.getStatus() != null) {
			to.setNonconformityStatusId(from.getStatus().getId());
		}
		to.setSummary(from.getSummary());
		to.setTotalSites(from.getTotalSites());
		to.setType(from.getNonconformityType());
	}
}
