package gov.healthit.chpl.auth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class UserImpl implements User {

	private String subjectName;
	private Map<String, List<Claim>> claims = 
			new HashMap<String, List<Claim>>();
	
	
	public UserImpl(){};
	
	public UserImpl(String subjectName, Map<String, List<Claim>> claims){
		this.subjectName = subjectName;
		this.claims = claims;
	}
	
	public String getSubjectName() {
		return subjectName;
	}

	public void setSubjectName(String subject) {
		this.subjectName = subject;
	}

	public Map<String, List<Claim>> getClaims() {
		return this.claims;
	}

	public void setClaims(Map<String, List<Claim>> claims) {
		this.claims = claims;
	}

	public void addClaim(String claimName, String claimValue){
		
		if (claims.get(claimName) != null){
			
			claims.get(claimName).removeAll(Arrays.asList(claimValue));
			Claim auth = new Claim(claimValue);
			claims.get(claimName).add(auth);
		
		} else {
			
			ArrayList<Claim> newValArray = new ArrayList<Claim>();
			Claim auth = new Claim(claimValue);
			newValArray.add(auth);
			claims.put(claimName, newValArray);
		}
		
	}

	public void removeClaim(String claimName, String claimValue) {
		
		if (claims.get(claimName) != null){
			claims.get(claimName).removeAll(Arrays.asList(claimValue));
		}
		
	}
	
}
