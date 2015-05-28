package gov.healthit.chpl.auth.user;

import gov.healthit.chpl.acb.CertificationBody;
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
	
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER_CREATOR')")
	public void create(UserImpl user){
		
		userDAO.create(user);
		// Grant the current principal administrative permission to the user
		addPermission(user, new PrincipalSid(getUsername()),
				BasePermission.ADMINISTRATION);
		
	}
	
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasPermission(#user, admin)")
	public void update(UserImpl user) throws UserRetrievalException {
		
		
		// We need to do a few checks on the user being
		// updated: 
		User orig = getByUserName(user.getUsername());
		
		// In the case where the user has been created by a JWT,
		// the password field will be null.
		// go to the database and get the stored password, set
		// password on the current user object before persisting
		// the updates.
		if (user.getPassword() == null){
			
			user.setPassword(orig.getPassword());
		}
		
		// In order to preserve SIDs in ACL, usernames cannot
		// be changed.
		if (!(user.getSubjectName().equals(orig.getSubjectName()))){
			user.setSubjectName(orig.getSubjectName());
		}
		
		userDAO.update(user);
	}
	
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasPermission(#user, 'delete') or hasPermission(#user, admin)")
	public void delete(UserImpl user){
		
		userDAO.deactivate(user.getId());
		// Delete the ACL information as well
		ObjectIdentity oid = new ObjectIdentityImpl(CertificationBody.class, user.getId());
		mutableAclService.deleteAcl(oid, false);
	}
	
	@Transactional(readOnly = true)
	@PostFilter("hasPermission(filterObject, 'read') or hasPermission(filterObject, admin)")
	public List<User> getAll(){
		return userDAO.findAll();
	}
	
	@Transactional(readOnly = true)
	public User getByUserName(String uname) throws UserRetrievalException {
		return userDAO.getByName(uname);
	}
	
	@Transactional(readOnly = true)
	@PreAuthorize("hasPermission(#id, 'gov.healthit.chpl.auth.user.User', admin)")
	public User getById(Long id) throws UserRetrievalException{
		return userDAO.getById(id);
	}

	@Transactional
	@PreAuthorize("hasPermission(#user, admin)")
	public void addPermission(UserImpl user, Sid recipient, Permission permission){
		
		MutableAcl acl;
		ObjectIdentity oid = new ObjectIdentityImpl(UserImpl.class, user.getId());

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
	@PreAuthorize("hasPermission(#user, admin)")
	public void deletePermission(UserImpl user, Sid recipient, Permission permission){
		
		ObjectIdentity oid = new ObjectIdentityImpl(UserImpl.class, user.getId());
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
	@PreAuthorize("hasPermission(#user, admin)")
	public void addRole(UserImpl user, String role) {
		// TODO Auto-generated method stub
		
	}

	@Override
	@PreAuthorize("hasPermission(#user, admin)")
	public void deleteRole(UserImpl user, String role) {
		// TODO Auto-generated method stub
		
	}
	
	protected String getUsername() {
		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		if (auth.getPrincipal() instanceof UserDetails) {
			return ((UserDetails) auth.getPrincipal()).getUsername();
		}
		else {
			return auth.getPrincipal().toString();
		}
	}
	
}
