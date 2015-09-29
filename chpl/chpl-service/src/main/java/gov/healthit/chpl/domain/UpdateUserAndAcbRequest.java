package gov.healthit.chpl.domain;


public class UpdateUserAndAcbRequest {
	private Long acbId;
	private Long userId;
	private CertificationBodyPermission authority;
	
	public Long getAcbId() {
		return acbId;
	}
	public void setAcbId(Long acbId) {
		this.acbId = acbId;
	}
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public CertificationBodyPermission getAuthority() {
		return authority;
	}
	public void setAuthority(CertificationBodyPermission authority) {
		this.authority = authority;
	}
	
}
