package gov.healthit.chpl.auth.manager.impl;


import gov.healthit.chpl.auth.dao.UserContactDAO;
import gov.healthit.chpl.auth.dao.UserDAO;
import gov.healthit.chpl.auth.dao.UserPermissionDAO;
import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.dto.UserPermissionDTO;
import gov.healthit.chpl.auth.entity.UserEntity;
import gov.healthit.chpl.auth.manager.SecuredUserManager;
import gov.healthit.chpl.auth.permission.UserPermissionRetrievalException;
import gov.healthit.chpl.auth.user.UserCreationException;
import gov.healthit.chpl.auth.user.UserManagementException;
import gov.healthit.chpl.auth.user.UserRetrievalException;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
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
public class SecuredUserManagerImpl implements SecuredUserManager {

	@Autowired
	private UserDAO userDAO;
	
	@Autowired
	private UserContactDAO userContactDAO;
	
	@Autowired
	UserPermissionDAO userPermissionDAO;
	
	@Autowired
	private MutableAclService mutableAclService;
	
	
	@Override
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_INVITED_USER_CREATOR') or "
			+ "hasRole('ROLE_ACB_ADMIN') or hasRole('ROLE_USER_CREATOR')")
	public UserDTO create(UserDTO user, String encodedPassword) throws UserCreationException, UserRetrievalException {
				
		user = userDAO.create(user, encodedPassword);
		
		// Grant the user administrative permission over itself.
		addAclPermission(user, new PrincipalSid(user.getSubjectName()),
				BasePermission.ADMINISTRATION);
		
		return user;
	}
	
	
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_ACB_ADMIN') or hasPermission(#user, admin)")
	public UserDTO update(UserDTO user) throws UserRetrievalException {
		return userDAO.update(user);
	}
	
	
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasPermission(#user, admin)")
	public void updateContactInfo(UserEntity user){
		userContactDAO.update(user.getContact());
	}
	
	
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@Transactional
	public void delete(UserDTO user) throws UserRetrievalException, UserPermissionRetrievalException, UserManagementException {
		//find the granted permissions for this user and remove them
		Set<UserPermissionDTO> permissions = getGrantedPermissionsForUser(user);
		for(UserPermissionDTO permission : permissions) {
			if(permission.getAuthority().equals("ROLE_ADMIN")) {
				removeAdmin(user.getSubjectName());
			} else {
				removeRole(user, permission.getAuthority());
			}
		}
		
		//remove all ACLs for the user for all users and acbs
		ObjectIdentity oid = new ObjectIdentityImpl(UserDTO.class, user.getId());
		mutableAclService.deleteAcl(oid, false);
		
		//now delete the user
		userDAO.delete(user.getId());
	}
	
	
	@PostFilter("hasRole('ROLE_ADMIN') or hasPermission(filterObject, 'read') or hasPermission(filterObject, admin)")
	public List<UserDTO> getAll(){
		return userDAO.findAll();
	}
	
	
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_ACB_ADMIN') or hasPermission(#id, 'gov.healthit.chpl.auth.dto.UserDTO', admin)")
	public UserDTO getById(Long id) throws UserRetrievalException{
		return userDAO.getById(id);
	}
	
	
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_ACB_ADMIN') or hasPermission(#user, admin)")
	public void addAclPermission(UserDTO user, Sid recipient, Permission permission){
		
		MutableAcl acl;
		ObjectIdentity oid = new ObjectIdentityImpl(UserDTO.class, user.getId());

		try {
			acl = (MutableAcl) mutableAclService.readAclById(oid);
		}
		catch (NotFoundException nfe) {
			acl = mutableAclService.createAcl(oid);
		}

		acl.insertAce(acl.getEntries().size(), permission, recipient, true);
		mutableAclService.updateAcl(acl);
		
	}
	
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasPermission(#user, admin)")
	public void deleteAclPermission(UserDTO user, Sid recipient, Permission permission){
		
		ObjectIdentity oid = new ObjectIdentityImpl(UserDTO.class, user.getId());
		MutableAcl acl = (MutableAcl) mutableAclService.readAclById(oid);
		
		List<AccessControlEntry> entries = acl.getEntries();

		for (int i = 0; i < entries.size(); i++) {
			if (entries.get(i).getSid().equals(recipient)
					&& entries.get(i).getPermission().equals(permission)) {
				acl.deleteAce(i);
			}
		}
		mutableAclService.updateAcl(acl);	
	}
	
	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_ACB_ADMIN') or "
			+ "hasRole('ROLE_INVITED_USER_CREATOR')")
	public void grantRole(String userName, String role) throws UserRetrievalException, UserManagementException, UserPermissionRetrievalException {
		if (role.equals("ROLE_ADMIN") || role.equals("ROLE_ACL_ADMIN") || 
				role.equals("ROLE_ADMINISTRATOR") || role.equals("ROLE_USER_AUTHENTICATOR")) {
			throw new UserManagementException("This role cannot be granted using the grant role functionality");
		}
		
		userDAO.addPermission(userName, role);
		
	}
	
	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_INVITED_USER_CREATOR')")
	public void grantAdmin(String userName) throws UserPermissionRetrievalException, UserRetrievalException, UserManagementException {
		userDAO.addPermission(userName, "ROLE_ADMIN");
	}
	
	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_ACB_ADMIN') or hasPermission(#user, admin)")
	public void removeRole(UserDTO user, String role) throws UserManagementException, UserRetrievalException, UserPermissionRetrievalException {
		removeRole(user.getSubjectName(), role);
	}
	
	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_ACB_ADMIN') or hasPermission(#user, admin)")
	public void removeRole(String userName, String role) throws UserManagementException, UserRetrievalException, UserPermissionRetrievalException {
		if (role.equals("ROLE_ADMIN") || role.equals("ROLE_ACL_ADMIN") || 
				role.equals("ROLE_ADMINISTRATOR") || role.equals("ROLE_USER_AUTHENTICATOR")) {
			throw new UserManagementException("This role cannot be removed using the remove role functionality");
		}
		
		userDAO.removePermission(userName, role);
	}
	
	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public void removeAdmin(String userName) throws UserPermissionRetrievalException, UserRetrievalException, UserManagementException {
		
		userDAO.removePermission(userName, "ROLE_ADMIN");
		
	}
	
	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasPermission(#user, admin)")
	public void updatePassword(UserDTO user, String encodedPassword) throws UserRetrievalException{
		userDAO.updatePassword(user.getSubjectName(), encodedPassword);
	}
	
	@Override
	@PreAuthorize("hasRole('ROLE_USER_AUTHENTICATOR')")
	public void updateFailedLoginCount(UserDTO user) throws UserRetrievalException {
		userDAO.updateFailedLoginCount(user.getSubjectName(), user.getFailedLoginCount());
	}
	
	@Override
	@PreAuthorize("hasRole('ROLE_USER_AUTHENTICATOR')")
	public void updateAccountLockedStatus(UserDTO user) throws UserRetrievalException {
		userDAO.updateAccountLockedStatus(user.getSubjectName(), user.isAccountLocked());
	}
	
	@Override
	@PreAuthorize("hasRole('ROLE_USER_AUTHENTICATOR') or hasRole('ROLE_ADMIN') or hasRole('ROLE_ACB_ADMIN') or hasPermission(#user, 'read') or hasPermission(#user, admin)")
	public Set<UserPermissionDTO> getGrantedPermissionsForUser(UserDTO user){
		return this.userPermissionDAO.findPermissionsForUser(user.getId());
	}


	@Override
	@PostAuthorize("hasRole('ROLE_INVITED_USER_CREATOR') or hasRole('ROLE_USER_AUTHENTICATOR') or "
			+ "hasRole('ROLE_ADMIN') or hasRole('ROLE_ACB_ADMIN') or hasPermission(returnObject, 'read') or hasPermission(returnObject, admin)")
	public UserDTO getBySubjectName(String userName) throws UserRetrievalException {
		return userDAO.getByName(userName);
	}
	
	
}
