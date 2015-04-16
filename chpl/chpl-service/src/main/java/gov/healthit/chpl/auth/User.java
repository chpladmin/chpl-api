package gov.healthit.chpl.auth;

import java.util.List;
import java.util.Map;


public interface User {
	
	String getSubjectName();
	void setSubjectName(String subject);
	
	Map<String, List<Claim> > getClaims();
	void setClaims(Map<String, List<Claim> > claims);
	
	void addClaim(String claimName, String claimValue);
	void removeClaim(String claimName, String claimValue);
	
}
