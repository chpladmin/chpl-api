package gov.healthit.chpl.auth.permission;


public interface UserPermissionManager {	
	
	public void create(UserPermissionEntity permission);
	
	public void update(UserPermissionEntity permission);
	
	public void delete(UserPermissionEntity permission);

}
