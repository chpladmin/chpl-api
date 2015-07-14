package gov.healthit.chpl.auth.user;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.auth.permission.UserPermissionEntity;
import gov.healthit.chpl.auth.permission.UserPermissionRetrievalException;
import gov.healthit.chpl.auth.permission.dao.UserPermissionDAO;
import gov.healthit.chpl.auth.user.dao.UserDAO;

import java.util.List;

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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserManagerImpl implements UserManager {

	@Autowired
	private UserDAO userDAO;
	
	@Autowired
	private MutableAclService mutableAclService;
	
	@Autowired
	UserPermissionDAO userPermissionDAO;
	
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER_CREATOR')")
	public void create(UserEntity user){
		
		userDAO.create(user);
		// Grant the current principal administrative permission to the user
		addAclPermission(user, new PrincipalSid(Util.getUsername()),
				BasePermission.ADMINISTRATION);
		
	}
	
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasPermission(#user, admin)")
	public void update(UserEntity user) throws UserRetrievalException {
		
		userDAO.update(user);
	}
	
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasPermission(#user, 'delete') or hasPermission(#user, admin)")
	public void delete(UserEntity user){
		
		userDAO.deactivate(user.getId());
		// Delete the ACL information as well
		ObjectIdentity oid = new ObjectIdentityImpl(UserEntity.class, user.getId());
		mutableAclService.deleteAcl(oid, false);
	}
	
	@Transactional(readOnly = true)
	@PostFilter("hasRole('ROLE_ADMIN') or hasPermission(filterObject, 'read') or hasPermission(filterObject, admin)")
	public List<UserEntity> getAll(){
		return userDAO.findAll();
	}
	
	@Transactional(readOnly = true)
	public User getByUserName(String uname) throws UserRetrievalException {
		return userDAO.getByName(uname);
	}
	
	@Transactional(readOnly = true)
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasPermission(#id, 'gov.healthit.chpl.auth.user.User', admin)")
	public User getById(Long id) throws UserRetrievalException{
		return userDAO.getById(id);
	}

	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasPermission(#user, admin)")
	public void addAclPermission(UserEntity user, Sid recipient, Permission permission){
		
		MutableAcl acl;
		ObjectIdentity oid = new ObjectIdentityImpl(UserEntity.class, user.getId());

		try {
			acl = (MutableAcl) mutableAclService.readAclById(oid);
		}
		catch (NotFoundException nfe) {
			acl = mutableAclService.createAcl(oid);
		}

		acl.insertAce(acl.getEntries().size(), permission, recipient, true);
		mutableAclService.updateAcl(acl);
		
	}
	
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasPermission(#user, admin)")
	public void deleteAclPermission(UserEntity user, Sid recipient, Permission permission){
		
		ObjectIdentity oid = new ObjectIdentityImpl(UserEntity.class, user.getId());
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
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasPermission(#user, admin)")
	public void grantRole(String userName, String role) throws UserRetrievalException, UserManagementException, UserPermissionRetrievalException {
		
		
		UserEntity user = (UserEntity) getByUserName(userName);
		
		if ((role == "ROLE_ADMIN") || (role == "ROLE_ACL_ADMIN") || (role =="ROLE_ADMINISTRATOR")){
			throw new UserManagementException("This role cannot be granted using the grant role functionality");
		}
		
		UserPermissionEntity permission = userPermissionDAO.getPermissionFromAuthority(role);
		
		if (permission == null){
			permission = new UserPermissionEntity(role);
			userPermissionDAO.create(permission);
		}
		
		user.addPermission(permission);
		update(user);
		userPermissionDAO.update(permission);
		
	}
	
	@Override
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public void grantAdmin(UserEntity user) throws UserPermissionRetrievalException, UserRetrievalException {
		
		
		UserPermissionEntity adminPermission = userPermissionDAO.getPermissionFromAuthority("ROLE_ADMIN");
		user.addPermission(adminPermission);
		update(user);
		
	}
	
	
	@Override
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasPermission(#user, admin)")
	public void deleteRole(UserEntity user, String role) throws UserRetrievalException {
		user.removePermission(role);
		update(user);
	}
	
}
