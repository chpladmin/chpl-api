package gov.healthit.chpl.manager.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ApplicationObjectSupport;
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

import gov.healthit.chpl.auth.dao.UserDAO;
import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.manager.UserManager;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.certifiedProduct.upload.CertifiedProductUploadHandlerFactory;
import gov.healthit.chpl.dao.CQMCriterionDAO;
import gov.healthit.chpl.dao.CertificationStatusDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.PendingCertifiedProductDAO;
import gov.healthit.chpl.domain.CQMCriterion;
import gov.healthit.chpl.dto.CQMCriterionDTO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertificationStatusDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.entity.PendingCertifiedProductEntity;
import gov.healthit.chpl.manager.CertificationBodyManager;
import gov.healthit.chpl.manager.PendingCertifiedProductManager;

@Service
public class PendingCertifiedProductManagerImpl extends ApplicationObjectSupport implements PendingCertifiedProductManager {

	@Autowired CertifiedProductUploadHandlerFactory uploadHandlerFactory;
	@Autowired PendingCertifiedProductDAO pcpDao;
	@Autowired CertificationStatusDAO statusDao;
	@Autowired CertificationBodyManager acbManager;
	@Autowired UserManager userManager;
	@Autowired UserDAO userDAO;
	@Autowired private MutableAclService mutableAclService;
	@Autowired private CQMCriterionDAO cqmCriterionDAO;
	private List<CQMCriterion> cqmCriteria = new ArrayList<CQMCriterion>();
	
	@PostConstruct
	public void setup() {
		loadCQMCriteria();
	}
	
	@Override
	@Transactional(readOnly = true)
	@PostFilter("hasRole('ROLE_ADMIN') or "
			+ "((hasRole('ROLE_ACB_ADMIN') or hasRole('ROLE_ACB_STAFF')) and "
			+ "(hasPermission(filterObject, read) or hasPermission(filterObject, admin)))")
	public List<PendingCertifiedProductDTO> getAll() {
		return pcpDao.findAll();
	}

	@Override
	@Transactional(readOnly = true)
	@PostFilter("hasRole('ROLE_ADMIN') or "
			+ "((hasRole('ROLE_ACB_ADMIN') or hasRole('ROLE_ACB_STAFF')) and "
			+ "(hasPermission(filterObject, read) or hasPermission(filterObject, admin)))")
	public List<PendingCertifiedProductDTO> getPending() {
		CertificationStatusDTO statusDto = statusDao.getByStatusName("Pending");
		return pcpDao.findByStatus(statusDto.getId());
	}
	
	@Override
	@Transactional(readOnly = true)
	@PreAuthorize("hasRole('ROLE_ADMIN') or ((hasRole('ROLE_ACB_ADMIN') or hasRole('ROLE_ACB_STAFF')) and "
			+ "hasPermission(#id, 'gov.healthit.chpl.dto.PendingCertifiedProductDTO', admin))")	
	public PendingCertifiedProductDTO getById(Long id) throws EntityRetrievalException {
		return pcpDao.findById(id);
	}

	@Override
	@Transactional (readOnly = true)
	@PreAuthorize("hasRole('ROLE_ADMIN') or "
			+ "((hasRole('ROLE_ACB_ADMIN') or hasRole('ROLE_ACB_STAFF')) and "
			+ "(hasPermission(#acb, read) or hasPermission(#acb, admin)))")
	public List<PendingCertifiedProductDTO> getByAcb(CertificationBodyDTO acb) {
		return pcpDao.findByAcbId(acb.getId());
	}
	
	@Override
	@PreAuthorize("(hasRole('ROLE_ACB_STAFF') or hasRole('ROLE_ACB_ADMIN')) "
			+ "and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin)")
	public PendingCertifiedProductDTO create(Long acbId, PendingCertifiedProductEntity toCreate) 
		throws EntityRetrievalException, EntityCreationException {
		//insert the record
		PendingCertifiedProductDTO pendingCpDto = pcpDao.create(toCreate);
		//add appropriate ACLs
		//who already has access to this ACB?
		CertificationBodyDTO acb = acbManager.getById(acbId);
		List<UserDTO> usersOnAcb = acbManager.getAllUsersOnAcb(acb);
		//give each of those people access to this PendingCertifiedProduct
		for(UserDTO user : usersOnAcb) {
			addPermission(acb, pendingCpDto, user, BasePermission.ADMINISTRATION);
		}
		return pendingCpDto;
	}
	
	@Override
	@Transactional
	@PreAuthorize("(hasRole('ROLE_ACB_ADMIN') or hasRole('ROLE_ACB_STAFF')) and "
			+ "hasPermission(#pendingProductId, 'gov.healthit.chpl.dto.PendingCertifiedProductDTO', admin)")
	public void reject(Long pendingProductId) throws EntityRetrievalException {
		CertificationStatusDTO newStatus = statusDao.getByStatusName("Withdrawn");
		pcpDao.delete(pendingProductId, newStatus);
	}
	
	@Override
	@Transactional
	@PreAuthorize("(hasRole('ROLE_ACB_ADMIN') or hasRole('ROLE_ACB_STAFF')) and "
			+ "hasPermission(#pendingProductId, 'gov.healthit.chpl.dto.PendingCertifiedProductDTO', admin)")
	public void confirm(Long pendingProductId) throws EntityRetrievalException {
		CertificationStatusDTO newStatus = statusDao.getByStatusName("Active");
		pcpDao.updateStatus(pendingProductId, newStatus);
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
		if (pendingCpDto.getCertificationEdition().startsWith("2011")){
		 	return getAvailableNQFVersions();
		} else if(pendingCpDto.getCertificationEdition().startsWith("2014")){
			return getAvailableCQMVersions();
		}
		return cqmCriteria;
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
			
			if (criterion.getNumber().startsWith("CMS")){
				criteria.add(criterion);
			}
		}
		return criteria;
	}
	
	private List<CQMCriterion> getAvailableNQFVersions(){
		List<CQMCriterion> nqfs = new ArrayList<CQMCriterion>();
		
		for (CQMCriterion criterion : cqmCriteria){
			
			if (criterion.getNumber().startsWith("NQF")){
				nqfs.add(criterion);
			}
		}
		return nqfs;
	}
}
