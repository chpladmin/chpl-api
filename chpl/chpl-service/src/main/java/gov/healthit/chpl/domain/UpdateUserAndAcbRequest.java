package gov.healthit.chpl.domain;


public class UpdateUserAndAcbRequest {
	private Long acbId;
	private Long userId;
	private ChplPermission authority;
	
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
	public ChplPermission getAuthority() {
		return authority;
	}
	public void setAuthority(ChplPermission authority) {
		this.authority = authority;
	}
	
}
