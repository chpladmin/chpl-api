package gov.healthit.chpl.manager.impl;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;
import javax.persistence.EntityNotFoundException;

import org.apache.commons.io.FileExistsException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.core.env.Environment;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.auth.SendMailUtil;
import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.SurveillanceDAO;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.domain.SurveillanceNonconformity;
import gov.healthit.chpl.domain.SurveillanceNonconformityDocument;
import gov.healthit.chpl.domain.SurveillanceNonconformityStatus;
import gov.healthit.chpl.domain.SurveillanceRequirement;
import gov.healthit.chpl.domain.SurveillanceRequirementType;
import gov.healthit.chpl.domain.SurveillanceResultType;
import gov.healthit.chpl.domain.SurveillanceType;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.entity.CertifiedProductEntity;
import gov.healthit.chpl.entity.PendingSurveillanceEntity;
import gov.healthit.chpl.entity.PendingSurveillanceNonconformityEntity;
import gov.healthit.chpl.entity.PendingSurveillanceRequirementEntity;
import gov.healthit.chpl.entity.SurveillanceEntity;
import gov.healthit.chpl.entity.SurveillanceNonconformityDocumentationEntity;
import gov.healthit.chpl.entity.SurveillanceNonconformityEntity;
import gov.healthit.chpl.entity.SurveillanceRequirementEntity;
import gov.healthit.chpl.manager.SurveillanceManager;
import gov.healthit.chpl.validation.surveillance.SurveillanceValidator;

@Service
public class SurveillanceManagerImpl implements SurveillanceManager {
	private static final Logger logger = LogManager.getLogger(SurveillanceManagerImpl.class);
	@Autowired private SendMailUtil sendMailService;
	@Autowired private Environment env;
	
	@Autowired SurveillanceDAO survDao;
	@Autowired CertifiedProductDAO cpDao;
	@Autowired SurveillanceValidator validator;
	
	@Override
	@Transactional(readOnly = true)
	public Surveillance getById(Long survId) throws EntityNotFoundException {
		SurveillanceEntity surv = survDao.getSurveillanceById(survId);
		if(surv == null) {
			throw new EntityNotFoundException("Could not find surveillance with id " + survId);
		}
		Surveillance result = convertToDomain(surv);
		validator.validate(result);
		return result;
	}
	
	@Override
	@Transactional(readOnly = true)
	public Surveillance getByFriendlyIdAndProduct(Long certifiedProductId, String survFriendlyId) {
		SurveillanceEntity surv = survDao.getSurveillanceByCertifiedProductAndFriendlyId(certifiedProductId, survFriendlyId);
		if(surv == null) {
			throw new EntityNotFoundException("Could not find surveillance for certified product " + certifiedProductId + " with friendly id " + survFriendlyId);
		}
		Surveillance result = convertToDomain(surv);
		validator.validate(result);
		return result;
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<Surveillance> getByCertifiedProduct(Long cpId) {
		List<SurveillanceEntity> survResults = survDao.getSurveillanceByCertifiedProductId(cpId);
		List<Surveillance> results = new ArrayList<Surveillance>();
		if(survResults != null) {
			for(SurveillanceEntity survResult : survResults) {
				Surveillance surv = convertToDomain(survResult);
				validator.validate(surv);
				results.add(surv);
			}
		}
		return results;
	}
	
	@Override
	@Transactional(readOnly = true)
	public SurveillanceNonconformityDocument getDocumentById(Long docId, boolean getFileContents) {
		SurveillanceNonconformityDocumentationEntity docEntity = null;
		
		try {
			docEntity = survDao.getDocumentById(docId);
		} catch(EntityNotFoundException ex) {
			logger.error("No document with id " + docId + " was found.");
		}
		
		SurveillanceNonconformityDocument doc = null;
		if(docEntity != null) {
			doc = convertToDomain(docEntity, getFileContents);
		}
		return doc;
	}
	
	@Override
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or "
			+ "((hasRole('ROLE_ACB_STAFF') or hasRole('ROLE_ACB_ADMIN')) "
			+ "and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin))")
	@CacheEvict(value = {CacheNames.SEARCH, CacheNames.BASIC_SEARCH, CacheNames.COUNT_MULTI_FILTER_SEARCH_RESULTS}, allEntries=true)
	public Long createSurveillance(Long acbId, Surveillance surv) {
		Long insertedId = null;
		
		try {
			insertedId = survDao.insertSurveillance(surv);
		} catch(Exception ex) {
			logger.error("Error inserting surveillance.", ex);
			throw ex;
		}
		
		return insertedId;
	}
	
	@Override
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or "
			+ "((hasRole('ROLE_ACB_STAFF') or hasRole('ROLE_ACB_ADMIN')) "
			+ "and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin))")
	public Long addDocumentToNonconformity(Long acbId, Long nonconformityId, SurveillanceNonconformityDocument doc) {
		Long insertedId = null;
		
		try {
			insertedId = survDao.insertNonconformityDocument(nonconformityId, doc);
		} catch(Exception ex) {
			logger.error("Error inserting nonconformity document.", ex);
			throw ex;
		}
		
		return insertedId;
	}
	
	@Override
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or "
			+ "((hasRole('ROLE_ACB_STAFF') or hasRole('ROLE_ACB_ADMIN')) "
			+ "and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin))")
	@CacheEvict(value = {CacheNames.SEARCH, CacheNames.BASIC_SEARCH, CacheNames.COUNT_MULTI_FILTER_SEARCH_RESULTS}, allEntries=true)
	public void updateSurveillance(Long acbId, Surveillance surv) {
		try {
			survDao.updateSurveillance(surv);
		} catch(Exception ex) {
			logger.error("Error updating surveillance.", ex);
			throw ex;
		}
	}
	
	@Override
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or "
			+ "((hasRole('ROLE_ACB_STAFF') or hasRole('ROLE_ACB_ADMIN')) "
			+ "and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin))")
	@CacheEvict(value = {CacheNames.SEARCH, CacheNames.BASIC_SEARCH, CacheNames.COUNT_MULTI_FILTER_SEARCH_RESULTS}, allEntries=true)
	public void deleteSurveillance(Long acbId, Long survId) {		
		Surveillance surv = new Surveillance();
		surv.setId(survId);
		
		try {
			survDao.deleteSurveillance(surv);
		} catch(Exception ex) {
			logger.error("Error marking surveillance with id " + survId + " as deleted.", ex);
			throw ex;
		}
	}
	
	@Override
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or "
			+ "((hasRole('ROLE_ACB_STAFF') or hasRole('ROLE_ACB_ADMIN')) "
			+ "and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin))")
	public void deleteNonconformityDocument(Long acbId, Long documentId) {
		try {
			survDao.deleteNonconformityDocument(documentId);
		} catch(Exception ex) {
			logger.error("Error marking document with id " + documentId + " as deleted.", ex);
			throw ex;
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	@PreAuthorize("(hasRole('ROLE_ACB_STAFF') or hasRole('ROLE_ACB_ADMIN')) "
			+ "and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin)")
	public List<Surveillance> getPendingByAcb(Long acbId) {
		List<PendingSurveillanceEntity> pendingResults = survDao.getPendingSurveillanceByAcb(acbId);
		List<Surveillance> results = new ArrayList<Surveillance>();
		if(pendingResults != null) {
			for(PendingSurveillanceEntity pr : pendingResults) {
				Surveillance surv = convertToDomain(pr);
				validator.validate(surv);
				results.add(surv);
			}
		}
		return results;
	}
	
	@Override
	@Transactional(readOnly = true)
	@PreAuthorize("(hasRole('ROLE_ACB_STAFF') or hasRole('ROLE_ACB_ADMIN')) "
			+ "and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin)")
	public Surveillance getPendingById(Long acbId, Long survId) throws EntityNotFoundException {
		PendingSurveillanceEntity pending = survDao.getPendingSurveillanceById(survId);
		if(pending == null) {
			throw new EntityNotFoundException("Could not find pending surveillance with id " + survId);
		}
		Surveillance surv = convertToDomain(pending);
		validator.validate(surv);
		return surv;
	}
	
	@Override
	@Transactional
	@PreAuthorize("(hasRole('ROLE_ACB_STAFF') or hasRole('ROLE_ACB_ADMIN')) "
			+ "and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin)")
	public Long createPendingSurveillance(Long acbId, Surveillance surv) {	
		Long insertedId = null;
		
		try {
			insertedId = survDao.insertPendingSurveillance(surv);
		} catch(Exception ex) {
			logger.error("Error inserting pending surveillance.", ex);
		}
		
		return insertedId;
	}
	
	@Override
	@Transactional
	@PreAuthorize("(hasRole('ROLE_ACB_STAFF') or hasRole('ROLE_ACB_ADMIN')) "
			+ "and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin)")
	public void deletePendingSurveillance(Long acbId, Long survId) {		
		Surveillance surv = new Surveillance();
		surv.setId(survId);
		
		try {
			survDao.deletePendingSurveillance(surv);
		} catch(Exception ex) {
			logger.error("Error marking pending surveillance with id " + survId + " as deleted.", ex);
		}
	}
	
	@Override
	@Transactional
	@PreAuthorize("hasRole('ROLE_ACB_STAFF') or hasRole('ROLE_ACB_ADMIN')")
	public void deletePendingSurveillance(List<CertificationBodyDTO> userAcbs, Long survId)
		throws EntityNotFoundException, AccessDeniedException {
		PendingSurveillanceEntity surv = survDao.getPendingSurveillanceById(survId);
		if(surv == null) {
			throw new EntityNotFoundException("Could not find pending surveillance with id " + survId);
		}
		CertifiedProductEntity ownerCp = surv.getCertifiedProduct();
		if(ownerCp == null) {
			throw new EntityNotFoundException("Could not find certified product associated with pending surveillance.");
		}
		boolean userHasAcbPermissions = false;
		for(CertificationBodyDTO acb : userAcbs) {
			if(acb.getId() != null && 
					ownerCp.getCertificationBodyId() != null && 
					acb.getId().longValue() == ownerCp.getCertificationBodyId().longValue()) {
				userHasAcbPermissions = true;
			}
		}
		
		if(!userHasAcbPermissions) {
			throw new AccessDeniedException("Permission denied on ACB " + ownerCp.getCertificationBodyId() + " for user " + Util.getCurrentUser().getSubjectName());
		}
		
		Surveillance toDelete = new Surveillance();
		toDelete.setId(survId);
		
		try {
			survDao.deletePendingSurveillance(toDelete);
		} catch(Exception ex) {
			logger.error("Error marking pending surveillance with id " + survId + " as deleted.", ex);
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public void validate(Surveillance surveillance) {
		validator.validate(surveillance);
	}
	
	@Override
	public void sendSuspiciousActivityEmail(Surveillance questionableSurv) {
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MMM-dd");
		
		String subject = "CHPL Questionable Activity";
		String htmlMessage = "<p>Questionable activity was detected on " + questionableSurv.getCertifiedProduct().getChplProductNumber() + ". "
				+ "An action was taken related to surveillance " + fmt.format(questionableSurv.getStartDate()) + ".<p>"
				+ "<p>To view the details of this activity go to: " + 
				env.getProperty("chplUrlBegin") + "/#/admin/reports</p>";
		
		String emailAddr = env.getProperty("questionableActivityEmail");
		String[] emailAddrs = emailAddr.split(";");
		try {
			sendMailService.sendEmail(emailAddrs, subject, htmlMessage);
		} catch(MessagingException me) {
			logger.error("Could not send questionable activity email", me);
		}
	}
	
	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_ONC_STAFF')")
	public File getProtectedDownloadFile(String filenameToDownload) throws IOException {
		return getFileFromDownloadFolder(filenameToDownload);
	}
	
	@Override
	public File getDownloadFile(String filenameToDownload) throws IOException {
		return getFileFromDownloadFolder(filenameToDownload);
	}
	
	private File getFileFromDownloadFolder(String filenameToDownload) throws IOException {
		String downloadFileLocation = env.getProperty("downloadFolderPath");
		
		File downloadFile = new File(downloadFileLocation + File.separator + filenameToDownload);
		if(!downloadFile.exists() || !downloadFile.canRead()) {
			throw new IOException("Cannot read download file at " + downloadFileLocation + ". File does not exist or cannot be read.");
		} 
		return downloadFile;
	}
	
	private Surveillance convertToDomain(PendingSurveillanceEntity pr) {
		Surveillance surv = new Surveillance();
		surv.setId(pr.getId());
		surv.setSurveillanceIdToReplace(pr.getSurvFriendlyIdToReplace());
		surv.setStartDate(pr.getStartDate());
		surv.setEndDate(pr.getEndDate());
		surv.setRandomizedSitesUsed(pr.getNumRandomizedSites());
		if(pr.getCertifiedProduct() != null) {
			CertifiedProductEntity cpEntity = pr.getCertifiedProduct();
			try {
				CertifiedProductDetailsDTO cpDto = cpDao.getDetailsById(cpEntity.getId());
				surv.setCertifiedProduct(new CertifiedProduct(cpDto));
			} catch(EntityRetrievalException ex) {
				logger.error("Could not find details for certified product " + cpEntity.getId());
			}
		} else {
			CertifiedProduct cp = new CertifiedProduct();
			cp.setId(pr.getCertifiedProductId());
			cp.setChplProductNumber(pr.getCertifiedProductUniqueId());
			surv.setCertifiedProduct(cp);
		}
		
		SurveillanceType survType = new SurveillanceType();
		survType.setName(pr.getSurveillanceType());
		surv.setType(survType);
		
		if(pr.getSurveilledRequirements() != null) {
			for(PendingSurveillanceRequirementEntity preq : pr.getSurveilledRequirements()) {
				SurveillanceRequirement req = new SurveillanceRequirement();
				req.setId(preq.getId());
				req.setRequirement(preq.getSurveilledRequirement());
				SurveillanceResultType result = new SurveillanceResultType();
				result.setName(preq.getResult());
				req.setResult(result);
				SurveillanceRequirementType reqType = new SurveillanceRequirementType();
				reqType.setName(preq.getRequirementType());
				req.setType(reqType);
				
				if(preq.getNonconformities() != null) {
					for(PendingSurveillanceNonconformityEntity pnc : preq.getNonconformities()) {
						SurveillanceNonconformity nc = new SurveillanceNonconformity();
						nc.setCapApprovalDate(pnc.getCapApproval());
						nc.setCapEndDate(pnc.getCapEndDate());
						nc.setCapMustCompleteDate(pnc.getCapMustCompleteDate());
						nc.setCapStartDate(pnc.getCapStart());
						nc.setDateOfDetermination(pnc.getDateOfDetermination());
						nc.setDeveloperExplanation(pnc.getDeveloperExplanation());
						nc.setFindings(pnc.getFindings());
						nc.setId(pnc.getId());
						nc.setNonconformityType(pnc.getType());
						nc.setResolution(pnc.getResolution());
						nc.setSitesPassed(pnc.getSitesPassed());
						nc.setSummary(pnc.getSummary());
						nc.setTotalSites(pnc.getTotalSites());
						SurveillanceNonconformityStatus status = new SurveillanceNonconformityStatus();
						status.setName(pnc.getStatus());
						nc.setStatus(status);
						req.getNonconformities().add(nc);
					}
				}
				surv.getRequirements().add(req);
			}
		}
		return surv;
	}
	
	private SurveillanceNonconformityDocument convertToDomain(SurveillanceNonconformityDocumentationEntity entity, boolean getContents) {
		SurveillanceNonconformityDocument doc = new SurveillanceNonconformityDocument();
		doc.setId(entity.getId());
		doc.setFileType(entity.getFileType());
		doc.setFileName(entity.getFileName());
		if(getContents) {
			doc.setFileContents(entity.getFileData());
		}
		return doc;
	}
	
	private Surveillance convertToDomain(SurveillanceEntity entity) {
		Surveillance surv = new Surveillance();
		surv.setId(entity.getId());
		surv.setFriendlyId(entity.getFriendlyId());
		surv.setStartDate(entity.getStartDate());
		surv.setEndDate(entity.getEndDate());
		surv.setRandomizedSitesUsed(entity.getNumRandomizedSites());
		
		if(entity.getCertifiedProduct() != null) {
			CertifiedProductEntity cpEntity = entity.getCertifiedProduct();
			try {
				CertifiedProductDetailsDTO cpDto = cpDao.getDetailsById(cpEntity.getId());
				surv.setCertifiedProduct(new CertifiedProduct(cpDto));
			} catch(EntityRetrievalException ex) {
				logger.error("Could not find details for certified product " + cpEntity.getId());
			}
		} else {
			CertifiedProduct cp = new CertifiedProduct();
			cp.setId(entity.getCertifiedProductId());
			surv.setCertifiedProduct(cp);
		}
		
		if(entity.getSurveillanceType() != null) {
			SurveillanceType survType = new SurveillanceType();
			survType.setId(entity.getSurveillanceType().getId());
			survType.setName(entity.getSurveillanceType().getName());
			surv.setType(survType);
		} else {
			SurveillanceType survType = new SurveillanceType();
			survType.setId(entity.getSurveillanceTypeId());
			surv.setType(survType);
		}
		
		if(entity.getSurveilledRequirements() != null) {
			for(SurveillanceRequirementEntity reqEntity : entity.getSurveilledRequirements()) {
				SurveillanceRequirement req = new SurveillanceRequirement();
				req.setId(reqEntity.getId());
				if(reqEntity.getCertificationCriterionEntity() != null) {
					req.setRequirement(reqEntity.getCertificationCriterionEntity().getNumber());
				} else {
					req.setRequirement(reqEntity.getSurveilledRequirement());
				}

				if(reqEntity.getSurveillanceResultTypeEntity() != null) {
					SurveillanceResultType result = new SurveillanceResultType();
					result.setId(reqEntity.getSurveillanceResultTypeEntity().getId());
					result.setName(reqEntity.getSurveillanceResultTypeEntity().getName());
					req.setResult(result);
				} else {
					SurveillanceResultType result = new SurveillanceResultType();
					result.setId(reqEntity.getSurveillanceResultTypeId());
					req.setResult(result);
				}
				
				if(reqEntity.getSurveillanceRequirementType() != null) {
					SurveillanceRequirementType result = new SurveillanceRequirementType();
					result.setId(reqEntity.getSurveillanceRequirementType().getId());
					result.setName(reqEntity.getSurveillanceRequirementType().getName());
					req.setType(result);
				} else {
					SurveillanceRequirementType result = new SurveillanceRequirementType();
					result.setId(reqEntity.getSurveillanceRequirementTypeId());
					req.setType(result);
				}
				
				if(reqEntity.getNonconformities() != null) {
					for(SurveillanceNonconformityEntity ncEntity : reqEntity.getNonconformities()) {
						SurveillanceNonconformity nc = new SurveillanceNonconformity();
						nc.setCapApprovalDate(ncEntity.getCapApproval());
						nc.setCapEndDate(ncEntity.getCapEndDate());
						nc.setCapMustCompleteDate(ncEntity.getCapMustCompleteDate());
						nc.setCapStartDate(ncEntity.getCapStart());
						nc.setDateOfDetermination(ncEntity.getDateOfDetermination());
						nc.setDeveloperExplanation(ncEntity.getDeveloperExplanation());
						nc.setFindings(ncEntity.getFindings());
						nc.setId(ncEntity.getId());
						nc.setNonconformityType(ncEntity.getType());
						nc.setResolution(ncEntity.getResolution());
						nc.setSitesPassed(ncEntity.getSitesPassed());
						nc.setSummary(ncEntity.getSummary());
						nc.setTotalSites(ncEntity.getTotalSites());
						if(ncEntity.getNonconformityStatus() != null) {
							SurveillanceNonconformityStatus status = new SurveillanceNonconformityStatus();
							status.setId(ncEntity.getNonconformityStatus().getId());
							status.setName(ncEntity.getNonconformityStatus().getName());
							nc.setStatus(status);
						} else {
							SurveillanceNonconformityStatus status = new SurveillanceNonconformityStatus();
							status.setId(ncEntity.getNonconformityStatusId());
							nc.setStatus(status);
						}
						req.getNonconformities().add(nc);
						
						if(ncEntity.getDocuments() != null && ncEntity.getDocuments().size() > 0) {
							for(SurveillanceNonconformityDocumentationEntity docEntity : ncEntity.getDocuments()) {
								SurveillanceNonconformityDocument doc = convertToDomain(docEntity, false);
								nc.getDocuments().add(doc);
							}
						}
					}
				}
				surv.getRequirements().add(req);
			}
		}
		return surv;
	}
}
