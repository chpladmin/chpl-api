package gov.healthit.chpl.auth.user;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.auth.json.UserCreationObject;
import gov.healthit.chpl.auth.json.UserInfoObject;
import gov.healthit.chpl.auth.permission.UserPermissionDTO;
import gov.healthit.chpl.auth.permission.UserPermissionRetrievalException;
import gov.healthit.chpl.auth.permission.dao.UserPermissionDAO;
import gov.healthit.chpl.auth.user.dao.UserContactDAO;
import gov.healthit.chpl.auth.user.dao.UserDAO;

import java.util.List;
import java.util.Set;

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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserManagerImpl implements UserManager {

	@Autowired
	private UserDAO userDAO;
	
	@Autowired
	private UserContactDAO userContactDAO;
	
	@Autowired
	UserPermissionDAO userPermissionDAO;
	
	@Autowired
	private MutableAclService mutableAclService;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	
	@Override
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER_CREATOR')")
	public void create(UserCreationObject userInfo) throws UserCreationException, UserRetrievalException {
		
		String encodedPassword = encodePassword(userInfo.getPassword());
		userDAO.create(userInfo, encodedPassword);
		
		UserDTO user = userDAO.getByName(userInfo.getSubjectName());
		
		// Grant the current principal administrative permission to the user
		addAclPermission(user, new PrincipalSid(Util.getUsername()),
				BasePermission.ADMINISTRATION);
		
		addAclPermission(user, new PrincipalSid(Util.getUsername()),
				BasePermission.DELETE);
		
		// Grant the user administrative permission over itself.
		addAclPermission(user, new PrincipalSid(user.getSubjectName()),
				BasePermission.ADMINISTRATION);
	}
	
	@Override
	@Transactional
	public void update(UserInfoObject userInfo) throws UserRetrievalException{
		
		UserDTO userDTO = getByUserName(userInfo.getSubjectName());
		
		if (userInfo.getFirstName() != null){
			userDTO.setFirstName(userInfo.getFirstName());
		}
		
		if (userInfo.getLastName() != null){
			userDTO.setLastName(userInfo.getLastName());
		}
		
		if (userInfo.getEmail() != null){
			userDTO.setEmail(userInfo.getEmail());
		}
		
		if (userInfo.getPhoneNumber() != null){
			userDTO.setPhoneNumber(userInfo.getPhoneNumber());
		}
		
		if (userInfo.getTitle() != null){
			userDTO.setTitle(userInfo.getTitle());
		}
		
		userDTO.setAccountLocked(userInfo.isAccountLocked());
		userDTO.setAccountEnabled(userInfo.isAccountEnabled());
		update(userDTO);
	}
	
	
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasPermission(#user, admin)")
	private void update(UserDTO user) throws UserRetrievalException {	
		userDAO.update(user);
	}
	
	
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN) or hasPermission(#user, admin)")
	private void updateContactInfo(UserEntity user){
		userContactDAO.update(user.getContact());
	}
	
	
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public void delete(UserDTO user){
		
		userDAO.delete(user.getId());
		// Delete the ACL information as well
		ObjectIdentity oid = new ObjectIdentityImpl(UserDTO.class, user.getId());
		mutableAclService.deleteAcl(oid, false);
	}
	
	@Override
	@Transactional
	public void delete(String userName) throws UserRetrievalException{
		
		UserDTO user = getByUserName(userName);
		if (user == null){
			throw new UserRetrievalException("User not found");
		} else {
			delete(user);
		}
	}
	
	
	@Transactional
	@PostFilter("hasRole('ROLE_ADMIN') or hasPermission(filterObject, 'read') or hasPermission(filterObject, admin)")
	public List<UserDTO> getAll(){
		return userDAO.findAll();
	}
	
	
	private UserDTO getByUserName(String uname) throws UserRetrievalException {
		return userDAO.getByName(uname);
	}
	
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasPermission(#id, 'gov.healthit.chpl.auth.user.User', admin)")
	public UserDTO getById(Long id) throws UserRetrievalException{
		return userDAO.getById(id);
	}

	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasPermission(#user, admin)")
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
	
	@Transactional
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
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasPermission(#user, admin)")
	public void grantRole(String userName, String role) throws UserRetrievalException, UserManagementException, UserPermissionRetrievalException {
		
		
		if ((role == "ROLE_ADMIN") || (role == "ROLE_ACL_ADMIN") || (role =="ROLE_ADMINISTRATOR")){
			throw new UserManagementException("This role cannot be granted using the grant role functionality");
		}
		
		userDAO.addPermission(userName, role);
		
	}
	
	@Override
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public void grantAdmin(String userName) throws UserPermissionRetrievalException, UserRetrievalException, UserManagementException {
		
		userDAO.addPermission(userName, "ROLE_ADMIN");
		
	}
	
	@Override
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasPermission(#user, admin)")
	public void removeRole(UserDTO user, String role) throws UserRetrievalException, UserPermissionRetrievalException {
		userDAO.removePermission(user.getSubjectName(), role);
	}
	
	@Override
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasPermission(#user, admin)")
	public void removeRole(String userName, String role) throws UserRetrievalException, UserPermissionRetrievalException {
		userDAO.removePermission(userName, role);
	}
	
	
	@Override
	@Transactional
	public void updateUserPassword(String userName, String password) throws UserRetrievalException {
		
		String encodedPassword = encodePassword(password);
		UserDTO userToUpdate = this.getByName(userName);
		updatePassword(userToUpdate, encodedPassword);
		
	}
	
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasPermission(#user, admin)")
	private void updatePassword(UserDTO user, String encodedPassword) throws UserRetrievalException{
		userDAO.updatePassword(user.getSubjectName(), encodedPassword);
	}
	
	
	@Override
	public String encodePassword(String password){
		String encodedPassword = bCryptPasswordEncoder.encode(password);
		return encodedPassword;
	}

	@Override
	public String getEncodedPassword(UserDTO user) throws UserRetrievalException {
		return userDAO.getEncodedPassword(user);
	}
	
	
	@Override
	//TODO: add security to this method?
	public Set<UserPermissionDTO> getGrantedPermissionsForUser(UserDTO user){
		return this.userPermissionDAO.findPermissionsForUser(user.getId());
	}


	@Override
	//TODO: add security to this method.
	public UserDTO getByName(String userName) throws UserRetrievalException {
		return userDAO.getByName(userName);
	}
	
	
}
