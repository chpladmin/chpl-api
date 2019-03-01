package gov.healthit.chpl.auth.dto;

import gov.healthit.chpl.auth.entity.UserPermissionEntity;
import gov.healthit.chpl.auth.permission.GrantedPermission;


public class UserPermissionDTO  {
	
	private static final long serialVersionUID = 1L;
	
	private String authority;
	private String name;
	private String description;
	
	
	public UserPermissionDTO(){}
	
	public UserPermissionDTO(UserPermissionEntity entity){
		
		this.authority = entity.getAuthority();
		this.name = entity.getName();
		this.description = entity.getDescription();
	}
	
	
	public GrantedPermission getGrantedPermission(){
		return new GrantedPermission(authority);
	}
	
	public String getAuthority() {
		return authority;
	}

	public void setAuthority(String authority) {
		this.authority = authority;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

	public String toString(){
		return authority;
	}
	
}
