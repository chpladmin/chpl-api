package gov.healthit.chpl.auth.user;

import java.util.List;
import java.util.Map;

public interface User {
	
	String getSubjectName();
	void setSubjectName(String subject);
	
	Map<String, List<String> > getClaims();
	void setClaims(Map<String, List<String> > claims);
	
	void addClaim(String claimName, String claimValue);
	void removeClaim(String claimName, String claimValue);
	
}
