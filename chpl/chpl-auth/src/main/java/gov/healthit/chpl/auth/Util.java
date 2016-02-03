package gov.healthit.chpl.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.User;

public class Util {
	
	
	public static String getUsername() {
		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		if (auth.getPrincipal() instanceof UserDetails) {
			return ((UserDetails) auth.getPrincipal()).getUsername();
		}
		else {
			return auth.getPrincipal().toString();
		}
	}
	
	public static boolean isUserRoleAdmin(){
		User user = getCurrentUser();
		if(user == null){
			return false;
		}
		for (GrantedPermission perm : user.getPermissions()){
			if (perm.getAuthority().equals("ROLE_ADMIN")){
				return true;
			}
		}
		return false;
	}

	public static User getCurrentUser(){
		
		User user = null;
		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		if (auth instanceof User){
			user = (User) auth;
		}
		return user;	
	}
	
	public static String fromInt(Integer toStr){
		return toStr.toString();
	}
	
}
