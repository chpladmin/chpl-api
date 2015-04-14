package gov.healthit.chpl.auth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserImpl implements User {

	private String subjectName;
	private Map<String, List<String>> claims = 
			new HashMap<String, List<String>>();
	
	
	public UserImpl(){};
	
	public UserImpl(String subjectName, Map<String, List<String>> claims){
		this.subjectName = subjectName;
		this.claims = claims;
	}
	
	public String getSubjectName() {
		return subjectName;
	}

	public void setSubjectName(String subject) {
		this.subjectName = subject;
	}

	public Map<String, List<String>> getClaims() {
		return this.claims;
	}

	public void setClaims(Map<String, List<String>> claims) {
		this.claims = claims;
	}

	public void addClaim(String claimName, String claimValue){
		
		if (claims.get(claimName) != null){
			
			claims.get(claimName).removeAll(Arrays.asList(claimValue));
			claims.get(claimName).add(claimValue);
		
		} else {
			
			ArrayList<String> newValArray = new ArrayList<String>();
			newValArray.add(claimValue);
			claims.put(claimName, newValArray);
		}
		
	}

	public void removeClaim(String claimName, String claimValue) {
		
		if (claims.get(claimName) != null){
			claims.get(claimName).removeAll(Arrays.asList(claimValue));
		}
		
	}	
	
}
