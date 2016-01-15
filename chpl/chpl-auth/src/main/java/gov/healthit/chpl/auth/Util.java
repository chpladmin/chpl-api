package gov.healthit.chpl.auth;

import java.util.Collection;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.auth.user.User;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

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
					   
	public static User getUnprivilegedUser(Long id){
		
		JWTAuthenticatedUser unprivileged = new JWTAuthenticatedUser() {
			
			@Override
			public Long getId() {
				return id == null ? -3L : id;
			}
			
			@Override
			public Collection<? extends GrantedAuthority> getAuthorities() {
				return null;
			}

			@Override
			public Object getCredentials(){
				return null;
			}

			@Override
			public Object getDetails() {
				return null;
			}

			@Override
			public Object getPrincipal(){
				return getName();
			}
			
			@Override 
			public String getSubjectName() {
				return this.getName();
			}
			
			@Override
			public boolean isAuthenticated(){
				return true;
			}

			@Override
			public void setAuthenticated(boolean arg0) throws IllegalArgumentException {}
			
			@Override
			public String getName(){
				return "unprivileged";
			}
			
		};
		return unprivileged;
	}
	
	
	public static String fromInt(Integer toStr){
		return toStr.toString();
	}
	
}
