package gov.healthit.chpl.manager.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.SurveillanceDAO;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.domain.SurveillanceNonconformity;
import gov.healthit.chpl.domain.SurveillanceNonconformityStatus;
import gov.healthit.chpl.domain.SurveillanceRequirement;
import gov.healthit.chpl.domain.SurveillanceRequirementType;
import gov.healthit.chpl.domain.SurveillanceResultType;
import gov.healthit.chpl.domain.SurveillanceType;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.entity.CertifiedProductEntity;
import gov.healthit.chpl.entity.PendingSurveillanceEntity;
import gov.healthit.chpl.entity.PendingSurveillanceNonconformityEntity;
import gov.healthit.chpl.entity.PendingSurveillanceRequirementEntity;
import gov.healthit.chpl.entity.SurveillanceResultTypeEntity;
import gov.healthit.chpl.manager.SurveillanceManager;
import gov.healthit.chpl.validation.surveillance.SurveillanceValidator;

@Service
public class SurveillanceManagerImpl implements SurveillanceManager {
	private static final Logger logger = LogManager.getLogger(SurveillanceManagerImpl.class);
	
	@Autowired SurveillanceDAO survDao;
	@Autowired SurveillanceValidator validator;
	
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
	
	private Surveillance convertToDomain(PendingSurveillanceEntity pr) {
		Surveillance surv = new Surveillance();
		surv.setId(pr.getId());
		surv.setSurveillanceIdToReplace(pr.getSurvIdToReplace());
		surv.setStartDate(pr.getStartDate());
		surv.setEndDate(pr.getEndDate());
		surv.setRandomizedSitesUsed(pr.getNumRandomizedSites());
		CertifiedProduct cp = new CertifiedProduct();
		cp.setId(pr.getCertifiedProductId());
		cp.setChplProductNumber(pr.getCertifiedProductUniqueId());
		surv.setCertifiedProduct(cp);
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
}
