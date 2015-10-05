package gov.healthit.chpl.manager.impl;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.auth.dao.UserDAO;
import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.dto.UserPermissionDTO;
import gov.healthit.chpl.auth.manager.UserManager;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.manager.CertificationBodyManager;
import gov.healthit.chpl.manager.PendingCertifiedProductManager;

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

@Service
public class CertificationBodyManagerImpl extends ApplicationObjectSupport implements CertificationBodyManager {

	@Autowired
	private CertificationBodyDAO certificationBodyDAO;
	
	@Autowired UserManager userManager;
	@Autowired UserDAO userDAO;
	@Autowired PendingCertifiedProductManager pendingCpManager;
	
	@Autowired
	private MutableAclService mutableAclService;


	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public CertificationBodyDTO create(CertificationBodyDTO acb) throws UserRetrievalException, EntityCreationException, EntityRetrievalException {
		// Create the ACB itself
		CertificationBodyDTO result = certificationBodyDAO.create(acb);

		// Grant the current principal administrative permission to the ACB
		addPermission(result, Util.getCurrentUser().getId(),
				BasePermission.ADMINISTRATION);
		
		//all existing users with ROLE_ADMIN now need access to this new ACB
		List<UserDTO> allUsers = userManager.getAll();
		for(UserDTO user : allUsers) {
			Set<UserPermissionDTO> permissions = userManager.getGrantedPermissionsForUser(user);
			boolean isChplAdmin = false;
			for(UserPermissionDTO permission : permissions) {
				if(permission.getAuthority().equals("ROLE_ADMIN")) {
					isChplAdmin = true;
				}
			}
			if(isChplAdmin) {
				addPermission(result, user.getId(), BasePermission.ADMINISTRATION);
			}
		}
		
		logger.debug("Created acb " + result
					+ " and granted admin permission to recipient " + gov.healthit.chpl.auth.Util.getUsername());
		return result;
	}
	
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasPermission(#acb, admin)")
	public CertificationBodyDTO update(CertificationBodyDTO acb) throws EntityRetrievalException {
		CertificationBodyDTO result = certificationBodyDAO.update(acb);
		logger.debug("Updated acb " + acb);
		return result;
	}
	
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasPermission(#acb, 'delete') or hasPermission(#acb, admin)")
	public void delete(CertificationBodyDTO acb) {
		
		certificationBodyDAO.delete(acb.getId());
		// Delete the ACL information as well
		ObjectIdentity oid = new ObjectIdentityImpl(CertificationBodyDTO.class, acb.getId());
		mutableAclService.deleteAcl(oid, false);
		
		if (logger.isDebugEnabled()) {
			logger.debug("Deleted acb " + acb + " including ACL permissions");
		}
		
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
		
		List<CertificationBodyDTO> acbs = certificationBodyDAO.findAll();
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
	
	private boolean permissionExists(CertificationBodyDTO acb, Sid recipient, Permission permission) {
		ObjectIdentity oid = new ObjectIdentityImpl(CertificationBodyDTO.class, acb.getId());
		MutableAcl acl = (MutableAcl) mutableAclService.readAclById(oid);
		return permissionExists(acl, recipient, permission);
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
	@PostFilter("hasRole('ROLE_ADMIN') or hasPermission(filterObject, 'read') or hasPermission(filterObject, admin)")
	public List<CertificationBodyDTO> getAll() {
		
		return certificationBodyDAO.findAll();
		
	}

	@Transactional(readOnly = true)
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_INVITED_USER_CREATOR') or "
			+ "hasPermission(#id, 'gov.healthit.chpl.dto.CertificationBodyDTO', read) or "
			+ "hasPermission(#id, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin)")
	public CertificationBodyDTO getById(Long id) throws EntityRetrievalException {
		if (logger.isDebugEnabled()) {
			logger.debug("Returning acb with id: " + id);
		}

		return certificationBodyDAO.getById(id);
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
