package gov.healthit.chpl.auth.user;


import gov.healthit.chpl.acb.CertificationBody;

import java.util.List;

import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;

public interface UserManager {
	
	
	public void addPermission(AuthenticatedUser user, Sid recipient, Permission permission);
	
	
	public void deletePermission(AuthenticatedUser user, Sid recipient, Permission permission);
	
	
	public void create(AuthenticatedUser user);
	
	
	public void update(AuthenticatedUser user);
	
	
	public void delete(AuthenticatedUser user);
	
	
	public List<User> getAll();
	

	public User getById(Long id);
	
}
