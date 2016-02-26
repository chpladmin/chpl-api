package gov.healthit.chpl.manager.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.auth.dao.UserDAO;
import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.manager.UserManager;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.certifiedProduct.upload.CertifiedProductUploadHandlerFactory;
import gov.healthit.chpl.certifiedProduct.validation.CertifiedProductValidator;
import gov.healthit.chpl.certifiedProduct.validation.CertifiedProductValidatorFactory;
import gov.healthit.chpl.dao.CQMCriterionDAO;
import gov.healthit.chpl.dao.CertificationStatusDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.PendingCertifiedProductDAO;
import gov.healthit.chpl.domain.ActivityConcept;
import gov.healthit.chpl.domain.CQMCriterion;
import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.PendingCertifiedProductDetails;
import gov.healthit.chpl.dto.CQMCriterionDTO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertificationStatusDTO;
import gov.healthit.chpl.dto.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.entity.PendingCertifiedProductEntity;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.CertificationBodyManager;
import gov.healthit.chpl.manager.PendingCertifiedProductManager;
import gov.healthit.chpl.util.CertificationResultRules;

@Service
public class PendingCertifiedProductManagerImpl implements PendingCertifiedProductManager {
	private static final Logger logger = LogManager.getLogger(PendingCertifiedProductManagerImpl.class);

	@Autowired private CertificationResultRules certRules;
	@Autowired CertifiedProductUploadHandlerFactory uploadHandlerFactory;
	@Autowired CertifiedProductValidatorFactory validatorFactory;
	
	@Autowired PendingCertifiedProductDAO pcpDao;
	@Autowired CertificationStatusDAO statusDao;
	@Autowired CertificationBodyManager acbManager;
	@Autowired UserManager userManager;
	@Autowired UserDAO userDAO;
	@Autowired private MutableAclService mutableAclService;
	@Autowired private CQMCriterionDAO cqmCriterionDAO;
	private List<CQMCriterion> cqmCriteria = new ArrayList<CQMCriterion>();
	
	@Autowired
	private ActivityManager activityManager;
	
	@PostConstruct
	public void setup() {
		loadCQMCriteria();
	}

	@Override
	@Transactional(readOnly = true)
	@PostFilter("hasRole('ROLE_ADMIN') or "
			+ "((hasRole('ROLE_ACB_ADMIN') or hasRole('ROLE_ACB_STAFF')) and "
			+ "(hasPermission(filterObject, read) or hasPermission(filterObject, admin)))")
	public List<PendingCertifiedProductDTO> getPending() {
		CertificationStatusDTO statusDto = statusDao.getByStatusName("Pending");
		List<PendingCertifiedProductDTO> products = pcpDao.findByStatus(statusDto.getId());
		updateCertResults(products);
		validate(products);
		
		return products;
	}
	
	@Override
	@Transactional(readOnly = true)
	@PreAuthorize("hasRole('ROLE_ADMIN') or ((hasRole('ROLE_ACB_ADMIN') or hasRole('ROLE_ACB_STAFF')) and "
			+ "hasPermission(#id, 'gov.healthit.chpl.dto.PendingCertifiedProductDTO', admin))")	
	public PendingCertifiedProductDetails getById(Long id) throws EntityRetrievalException {
		PendingCertifiedProductDTO dto = pcpDao.findById(id);
		updateCertResults(dto);
		validate(dto);

		PendingCertifiedProductDetails pcpDetails = new PendingCertifiedProductDetails(dto);
		addAllVersionsToCmsCriterion(pcpDetails);
		
		return pcpDetails;
	}
	
	private void updateCertResults(PendingCertifiedProductDTO dto) {
		List<PendingCertifiedProductDTO> products = new ArrayList<PendingCertifiedProductDTO>();
		products.add(dto);
		updateCertResults(products);
	}
	
	private void updateCertResults(List<PendingCertifiedProductDTO> products) {
		for(PendingCertifiedProductDTO product : products) {
			for(PendingCertificationResultDTO certResult : product.getCertificationCriterion()) {
				if(!certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.GAP)) {
					certResult.setGap(null);
				}
				if(!certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.G1_SUCCESS)) {
					certResult.setG1Success(null);
				}
				if(!certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.G2_SUCCESS)) {
					certResult.setG2Success(null);
				}
				if(!certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.SED)) {
					certResult.setSed(null);
				}
				if(!certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.UCD_FIELDS)) {
					certResult.setUcdProcesses(null);
				}
				if(!certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.ADDITIONAL_SOFTWARE)) {
					certResult.setAdditionalSoftware(null);
				}
				if(!certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.FUNCTIONALITY_TESTED)) {
					certResult.setTestFunctionality(null);
				}
				if(!certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.STANDARDS_TESTED)) {
					certResult.setTestStandards(null);
				}
				if(!certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.TEST_DATA)) {
					certResult.setTestData(null);
				}
				if(!certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.TEST_PROCEDURE_VERSION)) {
					certResult.setTestProcedures(null);
				}
				if(!certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.TEST_TOOLS_USED)) {
					certResult.setTestTools(null);
				}
			}
		}
	}
	
	@Override
	@Transactional (readOnly = true)
	@PreAuthorize("hasRole('ROLE_ADMIN') or "
			+ "((hasRole('ROLE_ACB_ADMIN') or hasRole('ROLE_ACB_STAFF')) and "
			+ "(hasPermission(#acb, read) or hasPermission(#acb, admin)))")
	public List<PendingCertifiedProductDTO> getByAcb(CertificationBodyDTO acb) {
		List<PendingCertifiedProductDTO> products = pcpDao.findByAcbId(acb.getId());
		validate(products);
		
		return products;
	}
	
	@Override
	@Transactional (readOnly = true)
	@PreAuthorize("hasRole('ROLE_ADMIN') or "
			+ "((hasRole('ROLE_ACB_ADMIN') or hasRole('ROLE_ACB_STAFF')) and "
			+ "(hasPermission(#acb, read) or hasPermission(#acb, admin)))")
	public List<PendingCertifiedProductDetails> getDetailsByAcb(CertificationBodyDTO acb) {
		List<PendingCertifiedProductDTO> products = pcpDao.findByAcbId(acb.getId());
		validate(products);
		
		List<PendingCertifiedProductDetails> result = new ArrayList<PendingCertifiedProductDetails>();
		for(PendingCertifiedProductDTO product : products) {
			PendingCertifiedProductDetails pcpDetails = new PendingCertifiedProductDetails(product);
			addAllVersionsToCmsCriterion(pcpDetails);
			result.add(pcpDetails);
		}
		
		return result;
	}
	
	@Override
	@PreAuthorize("(hasRole('ROLE_ACB_STAFF') or hasRole('ROLE_ACB_ADMIN')) "
			+ "and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin)")
	public PendingCertifiedProductDTO createOrReplace(Long acbId, PendingCertifiedProductEntity toCreate) 
		throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
		Long existingId = pcpDao.findIdByOncId(toCreate.getUniqueId());
		if(existingId != null) {
			CertificationStatusDTO newStatus = statusDao.getByStatusName("Withdrawn");
			pcpDao.delete(existingId, newStatus);
		}
		
		//insert the record
		PendingCertifiedProductDTO pendingCpDto = pcpDao.create(toCreate);
		updateCertResults(pendingCpDto);
		//add appropriate ACLs
		//who already has access to this ACB?
		CertificationBodyDTO acb = acbManager.getById(acbId);
		List<UserDTO> usersOnAcb = acbManager.getAllUsersOnAcb(acb);
		//give each of those people access to this PendingCertifiedProduct
		for(UserDTO user : usersOnAcb) {
			addPermission(acb, pendingCpDto, user, BasePermission.ADMINISTRATION);
		}
		validate(pendingCpDto);
		
		String activityMsg = "Certified product "+pendingCpDto.getProductName()+" is pending.";
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_PENDING_CERTIFIED_PRODUCT, pendingCpDto.getId(), activityMsg, null, pendingCpDto);
		
		return pendingCpDto;
	}
	
	@Override
	@Transactional
	@PreAuthorize("(hasRole('ROLE_ACB_ADMIN') or hasRole('ROLE_ACB_STAFF')) and "
			+ "hasPermission(#pendingProductId, 'gov.healthit.chpl.dto.PendingCertifiedProductDTO', admin)")
	public void reject(Long pendingProductId) throws EntityRetrievalException, JsonProcessingException, EntityCreationException {
		
		PendingCertifiedProductDTO pendingCpDto = pcpDao.findById(pendingProductId);
		
		CertificationStatusDTO newStatus = statusDao.getByStatusName("Withdrawn");
		pcpDao.delete(pendingProductId, newStatus);
		
		String activityMsg = "Pending certified product "+pendingCpDto.getProductName()+" has been rejected.";
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_PENDING_CERTIFIED_PRODUCT, pendingCpDto.getId(), activityMsg, pendingCpDto, null);

		
	}
	
	@Override
	@Transactional
	@PreAuthorize("(hasRole('ROLE_ACB_ADMIN') or hasRole('ROLE_ACB_STAFF')) and "
			+ "hasPermission(#pendingProductId, 'gov.healthit.chpl.dto.PendingCertifiedProductDTO', admin)")
	public void confirm(Long pendingProductId) throws EntityRetrievalException, JsonProcessingException, EntityCreationException {
		
		PendingCertifiedProductDTO pendingCpDto = pcpDao.findById(pendingProductId);
		
		CertificationStatusDTO newStatus = statusDao.getByStatusName("Active");
		pcpDao.updateStatus(pendingProductId, newStatus);
		
		String activityMsg = "Pending certified product "+pendingCpDto.getProductName()+" has been confirmed.";
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_PENDING_CERTIFIED_PRODUCT, pendingCpDto.getId(), activityMsg, pendingCpDto, pendingCpDto);
		
	}
	
	@Override
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or  hasRole('ROLE_INVITED_USER_CREATOR') or "
			+ "((hasRole('ROLE_ACB_ADMIN') or hasRole('ROLE_ACB_STAFF')) and hasPermission(#acb, admin))")
	public void addPermission(CertificationBodyDTO acb, PendingCertifiedProductDTO pcpDto, Long userId, Permission permission) throws UserRetrievalException {
		UserDTO user = userDAO.getById(userId);
		addPermission(acb, pcpDto, user, permission);
	}
	
	@Override
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or  hasRole('ROLE_INVITED_USER_CREATOR')  or "
			+ "((hasRole('ROLE_ACB_ADMIN') or hasRole('ROLE_ACB_STAFF')) and hasPermission(#acb, admin))")
	public void addPermission(CertificationBodyDTO acb, PendingCertifiedProductDTO pcpDto, UserDTO user, Permission permission) {
		MutableAcl acl;
		ObjectIdentity oid = new ObjectIdentityImpl(PendingCertifiedProductDTO.class, pcpDto.getId());

		try {
			acl = (MutableAcl) mutableAclService.readAclById(oid);
		}
		catch (NotFoundException nfe) {
			acl = mutableAclService.createAcl(oid);
		}
		
		Sid recipient = new PrincipalSid(user.getSubjectName());
		if(permissionExists(acl, recipient, permission)) {
			logger.debug("User " + recipient + " already has permission on the pending certified product " + pcpDto.getId());
		} else {
			acl.insertAce(acl.getEntries().size(), permission, recipient, true);
			mutableAclService.updateAcl(acl);
			logger.debug("Added permission " + permission + " for Sid " + recipient
					+ " pending certified product " + pcpDto);
		}
	}
	
	@Override
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or  hasRole('ROLE_INVITED_USER_CREATOR') or "
			+ "((hasRole('ROLE_ACB_ADMIN') or hasRole('ROLE_ACB_STAFF')) and hasPermission(#acb, admin))")
	public void addPermissionToAllPendingCertifiedProductsOnAcb(CertificationBodyDTO acb, UserDTO user, Permission permission) {
		List<PendingCertifiedProductDTO> pcps = getByAcb(acb);
		for(PendingCertifiedProductDTO pcp : pcps) {
			addPermission(acb, pcp, user, permission);
		}
	}
	
	@Override
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or "
			+ "((hasRole('ROLE_ACB_ADMIN') or hasRole('ROLE_ACB_STAFF')) and hasPermission(#pcpDto, admin))")
	public void deletePermission(PendingCertifiedProductDTO pcpDto, Sid recipient, Permission permission) {
		ObjectIdentity oid = new ObjectIdentityImpl(PendingCertifiedProductDTO.class, pcpDto.getId());
		MutableAcl acl = (MutableAcl) mutableAclService.readAclById(oid);
		
		List<AccessControlEntry> entries = acl.getEntries();

		//if the current size is only 1 we shouldn't be able to delete the last one right??
		//then nobody would be able to ever add or delete or read from the pcp again
		if(entries != null && entries.size() > 1) {
			for (int i = 0; i < entries.size(); i++) {
				if (entries.get(i).getSid().equals(recipient)
						&& entries.get(i).getPermission().equals(permission)) {
					acl.deleteAce(i);
				}
			}
			mutableAclService.updateAcl(acl);
		}
		logger.debug("Deleted pcp " + pcpDto.getId() + " ACL permission " + permission + " for recipient " + recipient);
	}
	
	@Override
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or "
			+ "((hasRole('ROLE_ACB_ADMIN') or hasRole('ROLE_ACB_STAFF')) and hasPermission(#pcpDto, admin))")
	public void deleteUserPermissionsOnPendingCertifiedProduct(PendingCertifiedProductDTO pcpDto, Sid recipient) {
		ObjectIdentity oid = new ObjectIdentityImpl(PendingCertifiedProductDTO.class, pcpDto.getId());
		MutableAcl acl = (MutableAcl) mutableAclService.readAclById(oid);
		
		//TODO: this seems very dangerous. I think we should somehow prevent from deleting the ADMIN user???
		List<AccessControlEntry> entries = acl.getEntries();

		for (int i = 0; i < entries.size(); i++) {
			if(entries.get(i).getSid().equals(recipient)) {
				acl.deleteAce(i);
				//cannot just loop through deleting because the "entries" 
				//list changes size each time that we delete one
				//so we have to re-fetch the entries and re-set the counter
				entries = acl.getEntries();
				i = 0;
			}
		}

		mutableAclService.updateAcl(acl);
		logger.debug("Deleted all pcp " + pcpDto.getId() + " ACL permissions for recipient " + recipient);
	}
	
	@Override
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or "
			+ "((hasRole('ROLE_ACB_ADMIN') or hasRole('ROLE_ACB_STAFF')) and hasPermission(#acb, admin))")
	public void deleteUserPermissionFromAllPendingCertifiedProductsOnAcb(CertificationBodyDTO acb, Sid user) {
		List<PendingCertifiedProductDTO> pcps = getByAcb(acb);
		if(pcps != null && pcps.size() > 0) {
			for(PendingCertifiedProductDTO pcp : pcps) {
				deleteUserPermissionsOnPendingCertifiedProduct(pcp, user);
			}
		}
	}
	
	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_ACB_ADMIN') or hasRole('ROLE_ACB_STAFF')") 
	public void deletePermissionsForUser(UserDTO userDto) throws UserRetrievalException {
		if(userDto.getSubjectName() == null) {
			userDto = userDAO.getById(userDto.getId());
		}
		
		List<PendingCertifiedProductDTO> dtos = pcpDao.findAll();
		for(PendingCertifiedProductDTO dto : dtos) {
			ObjectIdentity oid = new ObjectIdentityImpl(PendingCertifiedProductDTO.class, dto.getId());
			MutableAcl acl = (MutableAcl) mutableAclService.readAclById(oid);
			
			List<Permission> permissions = new ArrayList<Permission>();
			List<AccessControlEntry> entries = acl.getEntries();
			for (int i = 0; i < entries.size(); i++) {
				AccessControlEntry currEntry = entries.get(i);
				if(currEntry.getSid().equals(userDto.getSubjectName())) {
					permissions.remove(currEntry.getPermission());
				}
			}
		}
	}
	
	@Override
	public List<CQMCriterion> getApplicableCriteria(PendingCertifiedProductDTO pendingCpDto) {
		if(!StringUtils.isEmpty(pendingCpDto.getCertificationEdition())) {
			if (pendingCpDto.getCertificationEdition().startsWith("2011")){
			 	return getAvailableNQFVersions();
			} else if(pendingCpDto.getCertificationEdition().startsWith("2014")){
				return getAvailableCQMVersions();
			}
		}
		return new ArrayList<CQMCriterion>();
	}
	
	private boolean permissionExists(MutableAcl acl, Sid recipient, Permission permission) {
		boolean permissionExists = false;
		List<AccessControlEntry> entries = acl.getEntries();

		for (int i = 0; i < entries.size(); i++) {
			AccessControlEntry currEntry = entries.get(i);
			if(currEntry.getSid().equals(recipient) && 
					currEntry.getPermission().equals(permission)) {
				permissionExists = true;
			}
		}
		return permissionExists;
	}
	
	private void loadCQMCriteria() {		
		List<CQMCriterionDTO> dtos = cqmCriterionDAO.findAll();
		for (CQMCriterionDTO dto: dtos) {
			CQMCriterion criterion = new CQMCriterion();
			criterion.setCmsId(dto.getCmsId());
			criterion.setCqmCriterionTypeId(dto.getCqmCriterionTypeId());
			criterion.setCqmDomain(dto.getCqmDomain());
			criterion.setCqmVersionId(dto.getCqmVersionId());
			criterion.setCqmVersion(dto.getCqmVersion());
			criterion.setCriterionId(dto.getId());
			criterion.setDescription(dto.getDescription());
			criterion.setNqfNumber(dto.getNqfNumber());
			criterion.setNumber(dto.getNumber());
			criterion.setTitle(dto.getTitle());
			cqmCriteria.add(criterion);
		}
	}
	
	private List<CQMCriterion> getAvailableCQMVersions(){
		List<CQMCriterion> criteria = new ArrayList<CQMCriterion>();
		
		for (CQMCriterion criterion : cqmCriteria){
			if(!StringUtils.isEmpty(criterion.getCmsId()) && criterion.getCmsId().startsWith("CMS")) {
				criteria.add(criterion);
			}
		}
		return criteria;
	}
	
	private List<CQMCriterion> getAvailableNQFVersions(){
		List<CQMCriterion> nqfs = new ArrayList<CQMCriterion>();
		
		for (CQMCriterion criterion : cqmCriteria){
			
			if(StringUtils.isEmpty(criterion.getCmsId())) {
				nqfs.add(criterion);
			}
		}
		return nqfs;
	}
	
	private void validate(List<PendingCertifiedProductDTO> products) {
		for(PendingCertifiedProductDTO dto : products) {
			CertifiedProductValidator validator = validatorFactory.getValidator(dto);
			if(validator != null) {
				validator.validate(dto);
			}
		}
	}
	
	private void validate(PendingCertifiedProductDTO... products) {
		for(PendingCertifiedProductDTO dto : products) {
			CertifiedProductValidator validator = validatorFactory.getValidator(dto);
			if(validator != null) {
				validator.validate(dto);
			}
		}
	}
	
	public void addAllVersionsToCmsCriterion(PendingCertifiedProductDetails pcpDetails) {
		//now add allVersions for CMSs
		String certificationEdition = pcpDetails.getCertificationEdition().get("name").toString();
		if (certificationEdition.startsWith("2014")){
			List<CQMCriterion> cqms2014 = getAvailableCQMVersions();
			for(CQMCriterion cqm : cqms2014) {
				boolean cqmExists = false;
				for(CQMResultDetails details : pcpDetails.getCqmResults()) {
					if(cqm.getCmsId().equals(details.getCmsId())) {
						cqmExists = true;
						details.getAllVersions().add(cqm.getCqmVersion());
					}
				}
				if(!cqmExists) {
					CQMResultDetails result = new CQMResultDetails();
					result.setCmsId(cqm.getCmsId());
					result.setNqfNumber(cqm.getNqfNumber());
					result.setNumber(cqm.getNumber());
					result.setTitle(cqm.getTitle());
					result.setSuccess(Boolean.FALSE);
					result.setTypeId(cqm.getCqmCriterionTypeId());
					result.getAllVersions().add(cqm.getCqmVersion());
					pcpDetails.getCqmResults().add(result);
				}
			}
		}
	}
}
