package gov.healthit.chpl.domain;

import gov.healthit.chpl.auth.json.User;

import java.util.ArrayList;
import java.util.List;

public class CertificationBodyUser {
	
	private User user;
	private Long certificationBodyId;
	private List<CertificationBodyPermission> permissions;
	
	public CertificationBodyUser(){
		this.permissions = new ArrayList<CertificationBodyPermission>();
	}
	
	public CertificationBodyUser(User user, Long certificationBodyId, List<CertificationBodyPermission> permissions){
		this.user = user;
		this.certificationBodyId = certificationBodyId;
		this.permissions = permissions;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Long getCertificationBodyId() {
		return certificationBodyId;
	}

	public void setCertificationBodyId(Long certificationBodyId) {
		this.certificationBodyId = certificationBodyId;
	}

	public List<CertificationBodyPermission> getPermissions() {
		return permissions;
	}

	public void setPermissions(List<CertificationBodyPermission> permissions) {
		this.permissions = permissions;
	}
	
}