package gov.healthit.chpl.auth.authorization;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import gov.healthit.chpl.acb.CertificationBody;
import gov.healthit.chpl.auth.Claim;
import gov.healthit.chpl.auth.User;
import gov.healthit.chpl.auth.authentication.Authenticator;


@Service("authorizerService")
public class Authorizer {
	
	
	public static boolean isChris(String name){
		return name.equals("Chris");
	}
	
	public static boolean hasGroup(String group){
		return hasAuthority(group);
	}
	
	public static boolean hasAuthority(String claim){
		
		Authentication currentUser = SecurityContextHolder.getContext().getAuthentication();
		Collection<? extends GrantedAuthority> authorities = currentUser.getAuthorities();
		
		for (GrantedAuthority authority : authorities){
			if (authority.getAuthority().equals(claim)){
				return true;
			}
		}
		return false;
	}
	
	public static boolean userIsGlobalAdmin(){
		
		Authentication currentUser = SecurityContextHolder.getContext().getAuthentication();
		Collection<? extends GrantedAuthority> authorities = currentUser.getAuthorities();
		
		for (GrantedAuthority authority : authorities){
			
			if (authority.getAuthority().equals("ROLE_GLOBAL_ADMIN")){
				return true;
			}
		}
		return false;
	}
	
	/*
	public static boolean hasAuthority(CertificationBody acb){
		
		Authentication currentUser = SecurityContextHolder.getContext().getAuthentication();
		Collection<? extends GrantedAuthority> authorities = currentUser.getAuthorities();
		
		for (String perm : acb.getPermissions()){
			for (GrantedAuthority authority : authorities){
				
				if (authority.getAuthority().equals(perm)){
					return true;
				}
			}
		}
		return false;
	}
	*/
}
