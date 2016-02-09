package gov.healthit.chpl.auth.json;

import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.auth.dto.InvitationDTO;
import gov.healthit.chpl.auth.dto.InvitationPermissionDTO;

public class UserInvitation {
	
	private String emailAddress;
	private Long acbId;
	private Long testingLabId;
	private List<String> permissions;
	private String hash;
	
	public UserInvitation() {
		this.permissions = new ArrayList<String>();
	}
	
	public UserInvitation(InvitationDTO dto) {
		this.emailAddress = dto.getEmail();
		this.acbId = dto.getAcbId();
		this.testingLabId = dto.getTestingLabId();
		this.hash = dto.getInviteToken();
		this.permissions = new ArrayList<String>();
		for(InvitationPermissionDTO permission : dto.getPermissions()) {
			this.permissions.add(permission.getPermissionName());
		}
	}
	
	public String getEmailAddress() {
		return emailAddress;
	}
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}
	public Long getAcbId() {
		return acbId;
	}
	public void setAcbId(Long acbId) {
		this.acbId = acbId;
	}
	public List<String> getPermissions() {
		return permissions;
	}
	public void setPermissions(List<String> permissions) {
		this.permissions = permissions;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public Long getTestingLabId() {
		return testingLabId;
	}

	public void setTestingLabId(Long testingLabId) {
		this.testingLabId = testingLabId;
	}
	
}
