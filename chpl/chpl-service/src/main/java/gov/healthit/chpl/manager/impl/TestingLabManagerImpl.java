package gov.healthit.chpl.manager.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.auth.dao.UserDAO;
import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.dto.UserPermissionDTO;
import gov.healthit.chpl.auth.manager.UserManager;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.TestingLabDAO;
import gov.healthit.chpl.domain.ActivityConcept;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.TestingLabManager;

@Service
public class TestingLabManagerImpl extends ApplicationObjectSupport implements TestingLabManager {

	@Autowired CertificationBodyDAO certificationBodyDao;
	@Autowired
	private TestingLabDAO testingLabDAO;
	
	@Autowired UserManager userManager;
	@Autowired UserDAO userDAO;
	
	@Autowired
	private MutableAclService mutableAclService;

	@Autowired
	private ActivityManager activityManager;
	

	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public TestingLabDTO create(TestingLabDTO atl) throws UserRetrievalException, EntityCreationException, EntityRetrievalException, JsonProcessingException {
		String maxCode = testingLabDAO.getMaxCode();
		int maxCodeValue = Integer.parseInt(maxCode);
		int nextCodeValue = maxCodeValue + 1;
		
		String nextAtlCode = "";
		if(nextCodeValue < 10) {
			nextAtlCode = "0" + nextCodeValue;
		} else if(nextCodeValue > 99) {
			throw new EntityCreationException("Cannot create a 2-digit ATL code since there are more than 99 ATLs in the system.");
		} else {
			nextAtlCode = nextCodeValue + "";
		}
		atl.setTestingLabCode(nextAtlCode);
		
		// Create the atl itself
		TestingLabDTO result = testingLabDAO.create(atl);

		// Grant the current principal administrative permission to the ATL
		addPermission(result, Util.getCurrentUser().getId(),
				BasePermission.ADMINISTRATION);
		
		logger.debug("Created testing lab " + result
					+ " and granted admin permission to recipient " + gov.healthit.chpl.auth.Util.getUsername());
		
		String activityMsg = "Created Testing Lab "+result.getName();
		
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_ATL, result.getId(), activityMsg, null, result);
		
		return result;
	}
	
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasPermission(#atl, admin)")
	public TestingLabDTO update(TestingLabDTO atl) throws EntityRetrievalException, JsonProcessingException, EntityCreationException, UpdateTestingLabException {
		
		TestingLabDTO toUpdate = testingLabDAO.getById(atl.getId());
		TestingLabDTO result = testingLabDAO.update(atl);
		
		if((atl.getName() != null && Util.isUserRoleAdmin()) || atl.getName() == null || atl.getName().equals(toUpdate.getName())){
		
		logger.debug("Updated testingLab " + atl);
		
		String activityMsg = "Updated testing lab " + atl.getName();
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_ATL, result.getId(), activityMsg, toUpdate, result);
		}else{
			 logger.debug("ATL update failed: only admin can update atl name.");
			 throw new UpdateTestingLabException("Only ADMIN can change the name of an ATL.");
		}
		return result;
	}
	
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public void undelete(TestingLabDTO atl) throws JsonProcessingException, EntityCreationException, EntityRetrievalException {
		TestingLabDTO original = testingLabDAO.getById(atl.getId(), true);
		atl.setDeleted(false);
		TestingLabDTO result = testingLabDAO.update(atl);
		
		String activityMsg = "Testing Lab " + original.getName() + " is no longer marked as deleted.";
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_ATL, result.getId(), activityMsg, original, result);	
	}
	
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public void delete(TestingLabDTO atl) throws JsonProcessingException, EntityCreationException,
		EntityRetrievalException, UserRetrievalException {
		//get the users associated with this ATL
		//normally we shouldn't call an internal manager method because permissions will be
		//ignored but we know the user calling this has ROLE_ADMIN already
		List<UserDTO> usersOnAcb = getAllUsersOnAtl(atl);

		//check all the ACBs to see if each user has permission on it
		List<CertificationBodyDTO> allAcbs = certificationBodyDao.findAll(false);
		List<TestingLabDTO> allTestingLabs = testingLabDAO.findAll(false);

		for(UserDTO currUser : usersOnAcb) {
			boolean userHasOtherPermissions = false;
			Set<UserPermissionDTO> permissions = userManager.getGrantedPermissionsForUser(currUser);
			for(UserPermissionDTO currPermission : permissions) {
				if(!currPermission.getAuthority().startsWith("ROLE_ATL")) {
					userHasOtherPermissions = true;
				}
			}

			boolean userHasOtherAccesses = false;
			if(!userHasOtherPermissions) {
				//does the user have access to any ATLs besidees this one?
				for(TestingLabDTO currTestingLab : allTestingLabs) {
					if(currTestingLab.getId().longValue() != atl.getId().longValue()) {
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
				
				if(!userHasOtherAccesses) {
					for(CertificationBodyDTO currAcb : allAcbs) {
						//does the user have access to any ACBs?
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
			}
			
			if(!userHasOtherPermissions && !userHasOtherAccesses) {
				UserDTO prevUser = currUser;
				//if not, then mark their account disabled
				currUser.setAccountEnabled(false);
				UserDTO updatedUser = userManager.update(currUser);
				//log this activity
				activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_USER, currUser.getId(), 
						"Disabled account for " + currUser.getSubjectName() + " because it was only associated with a deleted ATL.", 
						prevUser, updatedUser);
			}
		}
		
		//delete the ATL
		testingLabDAO.delete(atl.getId());
		String activityMsg = "Deleted testing lab " + atl.getName();
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_ATL, atl.getId(), activityMsg, atl, null);
		
	}
	
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasPermission(#atl, admin) or hasPermission(#atl, read)")
	public List<UserDTO> getAllUsersOnAtl(TestingLabDTO atl) {
		ObjectIdentity oid = new ObjectIdentityImpl(TestingLabDTO.class, atl.getId());
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
	
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasPermission(#atl, read) or hasPermission(#atl, admin)") 
	public List<Permission> getPermissionsForUser(TestingLabDTO atl, Sid recipient) {
		ObjectIdentity oid = new ObjectIdentityImpl(TestingLabDTO.class, atl.getId());
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
			+ "(hasRole('ROLE_ATL_ADMIN') and hasPermission(#atl, admin))")
	public void addPermission(TestingLabDTO atl, Long userId, Permission permission) throws UserRetrievalException {
		MutableAcl acl;
		ObjectIdentity oid = new ObjectIdentityImpl(TestingLabDTO.class, atl.getId());

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
			logger.debug("User " + recipient + " already has permission on the testing lab " + atl.getName());
		} else {
			acl.insertAce(acl.getEntries().size(), permission, recipient, true);
			mutableAclService.updateAcl(acl);
			logger.debug("Added permission " + permission + " for Sid " + recipient
					+ " testing lab " + atl.getName());
		}
	}

	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or (hasRole('ROLE_ATL_ADMIN') and hasPermission(#atl, admin))")
	public void deletePermission(TestingLabDTO atl, Sid recipient, Permission permission) {
		ObjectIdentity oid = new ObjectIdentityImpl(TestingLabDTO.class, atl.getId());
		MutableAcl acl = (MutableAcl) mutableAclService.readAclById(oid);
		
		List<AccessControlEntry> entries = acl.getEntries();

		//if the current size is only 1 we shouldn't be able to delete the last one right??
		//then nobody would be able to ever add or delete or read from the atl again
		//in fact the spring code will throw runtime errors if we try to access the ACLs for this ATL.
		if(entries != null && entries.size() > 1) {
			for (int i = 0; i < entries.size(); i++) {
				if (entries.get(i).getSid().equals(recipient)
						&& entries.get(i).getPermission().equals(permission)) {
					acl.deleteAce(i);
				}
			}
			mutableAclService.updateAcl(acl);
		}
		logger.debug("Deleted testing lab " + atl.getName() + " ACL permission " + permission + " for recipient " + recipient);
	}

	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or (hasRole('ROLE_ATL_ADMIN') and hasPermission(#atl, admin))")
	public void deleteAllPermissionsOnAtl(TestingLabDTO atl, Sid recipient) {
		ObjectIdentity oid = new ObjectIdentityImpl(TestingLabDTO.class, atl.getId());
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
		logger.debug("Deleted all testing lab " + atl.getName() + " ACL permissions for recipient " + recipient);
	}
	
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_ATL_ADMIN')") 
	public void deletePermissionsForUser(UserDTO userDto) throws UserRetrievalException {
		if(userDto.getSubjectName() == null) {
			userDto = userDAO.getById(userDto.getId());
		}
		
		List<TestingLabDTO> atls = testingLabDAO.findAll(false);
		for(TestingLabDTO atl : atls) {
			ObjectIdentity oid = new ObjectIdentityImpl(TestingLabDTO.class, atl.getId());
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
	public List<TestingLabDTO> getAll(boolean showDeleted) {
		return testingLabDAO.findAll(showDeleted);
	}
	
	@Transactional(readOnly = true)
	@PostFilter("hasRole('ROLE_ADMIN') or hasPermission(filterObject, 'read') or hasPermission(filterObject, admin)")
	public List<TestingLabDTO> getAllForUser(boolean showDeleted) {
		return testingLabDAO.findAll(showDeleted);
	}

	@Transactional(readOnly = true)
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_INVITED_USER_CREATOR') or "
			+ "hasPermission(#id, 'gov.healthit.chpl.dto.TestingLabDTO', read) or "
			+ "hasPermission(#id, 'gov.healthit.chpl.dto.TestingLabDTO', admin)")
	public TestingLabDTO getById(Long id) throws EntityRetrievalException {
		return testingLabDAO.getById(id);
	}
	
	@Transactional(readOnly = true)
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_INVITED_USER_CREATOR') or "
			+ "hasPermission(#id, 'gov.healthit.chpl.dto.TestingLabDTO', read) or "
			+ "hasPermission(#id, 'gov.healthit.chpl.dto.TestingLabDTO', admin)")
	public TestingLabDTO getById(Long id, boolean includeDeleted) throws EntityRetrievalException {
		return testingLabDAO.getById(id, includeDeleted);
	}
}
