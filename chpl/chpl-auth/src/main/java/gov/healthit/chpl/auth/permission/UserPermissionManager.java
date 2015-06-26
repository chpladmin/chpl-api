package gov.healthit.chpl.auth.permission;


public interface UserPermissionManager {	
	
	public void create(UserPermission permission);
	
	public void update(UserPermission permission);
	
	public void delete(UserPermission permission);

}
