package gov.healthit.chpl.manager.impl;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.auth.dao.UserDAO;
import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.dto.UserPermissionDTO;
import gov.healthit.chpl.auth.manager.UserManager;
import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.TestingLabDAO;
import gov.healthit.chpl.domain.ActivityConcept;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.CertificationBodyManager;
import gov.healthit.chpl.manager.PendingCertifiedProductManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Query;

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

import com.fasterxml.jackson.core.JsonProcessingException;

@Service
public class CertificationBodyManagerImpl extends ApplicationObjectSupport implements CertificationBodyManager {

	@Autowired
	private CertificationBodyDAO certificationBodyDAO;
	
	@Autowired private TestingLabDAO testingLabDao;
	@Autowired UserManager userManager;
	@Autowired UserDAO userDAO;
	@Autowired PendingCertifiedProductManager pendingCpManager;
	
	@Autowired
	private MutableAclService mutableAclService;

	@Autowired
	private ActivityManager activityManager;
	

	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public CertificationBodyDTO create(CertificationBodyDTO acb) throws UserRetrievalException, EntityCreationException, EntityRetrievalException, JsonProcessingException {
		//assign a code
		String maxCode = certificationBodyDAO.getMaxCode();
		int maxCodeValue = Integer.parseInt(maxCode);
		int nextCodeValue = maxCodeValue + 1;
		
		String nextAcbCode = "";
		if(nextCodeValue < 10) {
			nextAcbCode = "0" + nextCodeValue;
		} else if(nextCodeValue > 99) {
			throw new EntityCreationException("Cannot create a 2-digit ACB code since there are more than 99 ACBs in the system.");
		} else {
			nextAcbCode = nextCodeValue + "";
		}
		acb.setAcbCode(nextAcbCode);
		
		// Create the ACB itself
		CertificationBodyDTO result = certificationBodyDAO.create(acb);

		// Grant the current principal administrative permission to the ACB
		addPermission(result, Util.getCurrentUser().getId(),
				BasePermission.ADMINISTRATION);
		
		logger.debug("Created acb " + result
					+ " and granted admin permission to recipient " + gov.healthit.chpl.auth.Util.getUsername());
		
		String activityMsg = "Created Certification Body "+result.getName();
		
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_CERTIFICATION_BODY, result.getId(), activityMsg, null, result);
		
		return result;
	}
	
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasPermission(#acb, admin)")
	public CertificationBodyDTO update(CertificationBodyDTO acb) throws EntityRetrievalException, JsonProcessingException, EntityCreationException, UpdateCertifiedBodyException {
		
		CertificationBodyDTO result = null;
		CertificationBodyDTO toUpdate = certificationBodyDAO.getById(acb.getId());
		
		if((acb.getName() != null && Util.isUserRoleAdmin()) || acb.getName() == null || acb.getName().equals(toUpdate.getName())){
			
			result = certificationBodyDAO.update(acb);

			logger.debug("Updated acb " + acb);

			String activityMsg = "Updated acb " + acb.getName();

			activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_CERTIFICATION_BODY, result.getId(), activityMsg, toUpdate, result);
		}else{
			logger.debug("ACB update failed: only admin can update acb name.");
			throw new UpdateCertifiedBodyException("Only ADMIN can change the name of an ACB.");
		}

		return result;
	}
	
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public void undelete(CertificationBodyDTO acb) throws JsonProcessingException, EntityCreationException, EntityRetrievalException {
		CertificationBodyDTO original = certificationBodyDAO.getById(acb.getId(), true);
		acb.setDeleted(false);
		CertificationBodyDTO result = certificationBodyDAO.update(acb);
		
		String activityMsg = "ACB " + original.getName() + " is no longer marked as deleted.";
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_CERTIFICATION_BODY, result.getId(), activityMsg, original, result);	
	}
	
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public void delete(CertificationBodyDTO acb) 
			throws JsonProcessingException, EntityCreationException, EntityRetrievalException,
			UserRetrievalException {
		
		//get the users associated with this ACB
		//normally we shouldn't call an internal manager method because permissions will be
		//ignored but we know the user calling this has ROLE_ADMIN already
		List<UserDTO> usersOnAcb = getAllUsersOnAcb(acb);
		
		//check all the ACBs to see if each user has permission on it
		List<CertificationBodyDTO> allAcbs = certificationBodyDAO.findAll(false);
		List<TestingLabDTO> allTestingLabs = testingLabDao.findAll(false);
		
		for(UserDTO currUser : usersOnAcb) {
			boolean userHasOtherPermissions = false;
			Set<UserPermissionDTO> permissions = userManager.getGrantedPermissionsForUser(currUser);
			for(UserPermissionDTO currPermission : permissions) {
				if(!currPermission.getAuthority().startsWith("ROLE_ACB")) {
					userHasOtherPermissions = true;
				}
			}

			boolean userHasOtherAccesses = false;
			if(!userHasOtherPermissions) {
				for(CertificationBodyDTO currAcb : allAcbs) {
					//does the user have access to anything besides this ACB?
					if(currAcb.getId().longValue() != acb.getId().longValue()) {
						ObjectIdentity oid = new ObjectIdentityImpl(CertificationBodyDTO.class, currAcb.getId());
						MutableAcl acl = (MutableAcl) mutableAclService.readAclById(oid);
						
						List<AccessControlEntry> entries = acl.getEntries();
						for (int i = 0; i < entries.size(); i++) {
							AccessControlEntry currEntry = entries.get(i);
							if(currEntry.getSid().equals(currUser.getSubjectName())) {
								userHasOtherAccesses = true;
							}
						}
					}
				}
				
				if(!userHasOtherAccesses) {
					//does the user have access to any ATLs?
					for(TestingLabDTO currTestingLab : allTestingLabs) {
						ObjectIdentity oid = new ObjectIdentityImpl(TestingLabDTO.class, currTestingLab.getId());
						MutableAcl acl = (MutableAcl) mutableAclService.readAclById(oid);
						
						List<AccessControlEntry> entries = acl.getEntries();
						for (int i = 0; i < entries.size(); i++) {
							AccessControlEntry currEntry = entries.get(i);
							if(currEntry.getSid().equals(currUser.getSubjectName())) {
								userHasOtherAccesses = true;
							}
						}
					}
				}
			}
			
			if(!userHasOtherPermissions && !userHasOtherAccesses) {
				UserDTO prevUser = currUser;
				//if not, then mark their account disabled
				currUser.setAccountEnabled(false);
				UserDTO updatedUser = userManager.update(currUser);
				//log this activity
				activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_USER, currUser.getId(), 
						"Disabled account for " + currUser.getSubjectName() + " because it was only associated with a deleted ACB.", 
						prevUser, updatedUser);
			}
		}
		
		//mark the ACB deleted
		certificationBodyDAO.delete(acb.getId());
		//log ACB delete activity
		String activityMsg = "Deleted acb " + acb.getName();
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_CERTIFICATION_BODY, acb.getId(), activityMsg, acb, null);
	}
	
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasPermission(#acb, admin) or hasPermission(#acb, read)")
	public List<UserDTO> getAllUsersOnAcb(CertificationBodyDTO acb) {
		ObjectIdentity oid = new ObjectIdentityImpl(CertificationBodyDTO.class, acb.getId());
		MutableAcl acl = (MutableAcl) mutableAclService.readAclById(oid);
		
		Set<String> userNames = new HashSet<String>();
		List<AccessControlEntry> entries = acl.getEntries();
		for (int i = 0; i < entries.size(); i++) {
			Sid sid = entries.get(i).getSid();
			if(sid instanceof PrincipalSid) {
				PrincipalSid psid = (PrincipalSid)sid;
				userNames.add(psid.getPrincipal());
			} else {
				userNames.add(sid.toString());
			}
		}

		//pull back the userdto for the sids
		List<UserDTO> users = new ArrayList<UserDTO>();
		if(userNames != null && userNames.size() > 0) {
			List<String> usernameList = new ArrayList<String>(userNames);
			users.addAll(userDAO.findByNames(usernameList));
		}
		return users;
	}
	
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasPermission(#acb, read) or hasPermission(#acb, admin)") 
	public List<Permission> getPermissionsForUser(CertificationBodyDTO acb, Sid recipient) {
		ObjectIdentity oid = new ObjectIdentityImpl(CertificationBodyDTO.class, acb.getId());
		MutableAcl acl = (MutableAcl) mutableAclService.readAclById(oid);
		
		List<Permission> permissions = new ArrayList<Permission>();
		List<AccessControlEntry> entries = acl.getEntries();
		for (int i = 0; i < entries.size(); i++) {
			AccessControlEntry currEntry = entries.get(i);
			if(currEntry.getSid().equals(recipient)) {
				permissions.add(currEntry.getPermission());
			}
		}
		return permissions;
	}
	
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_INVITED_USER_CREATOR') or "
			+ "(hasRole('ROLE_ACB_ADMIN') and hasPermission(#acb, admin))")
	public void addPermission(CertificationBodyDTO acb, Long userId, Permission permission) throws UserRetrievalException {
		MutableAcl acl;
		ObjectIdentity oid = new ObjectIdentityImpl(CertificationBodyDTO.class, acb.getId());

		try {
			acl = (MutableAcl) mutableAclService.readAclById(oid);
		}
		catch (NotFoundException nfe) {
			acl = mutableAclService.createAcl(oid);
		}

		UserDTO user = userDAO.getById(userId);
		if(user == null || user.getSubjectName() == null) {
			throw new UserRetrievalException("Could not find user with id " + userId);
		}
		
		Sid recipient = new PrincipalSid(user.getSubjectName());
		if(permissionExists(acl, recipient, permission)) {
			logger.debug("User " + recipient + " already has permission on the ACB " + acb.getName());
		} else {
			acl.insertAce(acl.getEntries().size(), permission, recipient, true);
			mutableAclService.updateAcl(acl);
			logger.debug("Added permission " + permission + " for Sid " + recipient
					+ " acb " + acb);
			
			//now give them permission on all of the pending certified products for this ACB
			pendingCpManager.addPermissionToAllPendingCertifiedProductsOnAcb(acb, user, permission);
		}
	}

	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or (hasRole('ROLE_ACB_ADMIN') and hasPermission(#acb, admin))")
	public void deletePermission(CertificationBodyDTO acb, Sid recipient, Permission permission) {
		ObjectIdentity oid = new ObjectIdentityImpl(CertificationBodyDTO.class, acb.getId());
		MutableAcl acl = (MutableAcl) mutableAclService.readAclById(oid);
		
		List<AccessControlEntry> entries = acl.getEntries();

		//if the current size is only 1 we shouldn't be able to delete the last one right??
		//then nobody would be able to ever add or delete or read from the acb again
		//in fact the spring code will throw runtime errors if we try to access the ACLs for this ACB.
		if(entries != null && entries.size() > 1) {
			for (int i = 0; i < entries.size(); i++) {
				if (entries.get(i).getSid().equals(recipient)
						&& entries.get(i).getPermission().equals(permission)) {
					acl.deleteAce(i);
				}
			}
			mutableAclService.updateAcl(acl);
		}
		logger.debug("Deleted acb " + acb + " ACL permission " + permission + " for recipient " + recipient);
		
		//now delete permission from all of the pending certified products for this ACB
		pendingCpManager.deleteUserPermissionFromAllPendingCertifiedProductsOnAcb(acb, recipient);
	}

	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or (hasRole('ROLE_ACB_ADMIN') and hasPermission(#acb, admin))")
	public void deleteAllPermissionsOnAcb(CertificationBodyDTO acb, Sid recipient) {
		ObjectIdentity oid = new ObjectIdentityImpl(CertificationBodyDTO.class, acb.getId());
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
		logger.debug("Deleted all acb " + acb + " ACL permissions for recipient " + recipient);
		
		//now delete permission from all of the pending certified products for this ACB
		pendingCpManager.deleteUserPermissionFromAllPendingCertifiedProductsOnAcb(acb, recipient);
	}
	
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_ACB_ADMIN')") 
	public void deletePermissionsForUser(UserDTO userDto) throws UserRetrievalException {
		if(userDto.getSubjectName() == null) {
			userDto = userDAO.getById(userDto.getId());
		}
		
		List<CertificationBodyDTO> acbs = certificationBodyDAO.findAll(false);
		for(CertificationBodyDTO acb : acbs) {
			ObjectIdentity oid = new ObjectIdentityImpl(CertificationBodyDTO.class, acb.getId());
			MutableAcl acl = (MutableAcl) mutableAclService.readAclById(oid);
			
			List<Permission> permissions = new ArrayList<Permission>();
			List<AccessControlEntry> entries = acl.getEntries();
			for (int i = 0; i < entries.size(); i++) {
				AccessControlEntry currEntry = entries.get(i);
				if(currEntry.getSid().equals(userDto.getSubjectName())) {
					permissions.remove(currEntry.getPermission());
				}
			}
			
			//now delete permission from all of the pending certified products for this ACB
			pendingCpManager.deleteUserPermissionFromAllPendingCertifiedProductsOnAcb(acb, new PrincipalSid(userDto.getSubjectName()));
		}
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
	
	@Transactional(readOnly = true)
	public List<CertificationBodyDTO> getAll(boolean showDeleted) {
		return certificationBodyDAO.findAll(showDeleted);
	}
	
	@Transactional(readOnly = true)
	@PostFilter("hasRole('ROLE_ADMIN') or hasPermission(filterObject, 'read') or hasPermission(filterObject, admin)")
	public List<CertificationBodyDTO> getAllForUser(boolean showDeleted) {
		return certificationBodyDAO.findAll(showDeleted);
	}

	@Transactional(readOnly = true)
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_INVITED_USER_CREATOR') or "
			+ "hasPermission(#id, 'gov.healthit.chpl.dto.CertificationBodyDTO', read) or "
			+ "hasPermission(#id, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin)")
	public CertificationBodyDTO getById(Long id) throws EntityRetrievalException {
		return certificationBodyDAO.getById(id);
	}
	
	@Transactional(readOnly = true)
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_INVITED_USER_CREATOR') or "
			+ "hasPermission(#id, 'gov.healthit.chpl.dto.CertificationBodyDTO', read) or "
			+ "hasPermission(#id, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin)")
	public CertificationBodyDTO getById(Long id, boolean includeDeleted) throws EntityRetrievalException {
		return certificationBodyDAO.getById(id, includeDeleted);
	}
	
	public MutableAclService getMutableAclService() {
		return mutableAclService;
	}
	
	public void setCertificationBodyDAO(CertificationBodyDAO acbDAO) {
		this.certificationBodyDAO = acbDAO;
	}

	public void setMutableAclService(MutableAclService mutableAclService) {
		this.mutableAclService = mutableAclService;
	}
	
}
