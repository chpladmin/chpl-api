package gov.healthit.chpl.auth.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import gov.healthit.chpl.auth.entity.InvitationEntity;
import gov.healthit.chpl.auth.entity.InvitationPermissionEntity;

public class InvitationDTO {	
	private Long id;
	private String email;
	private Long acbId;
	private Long testingLabId;
	private String inviteToken;
	private String confirmToken;
	private Long createdUserId;
	private boolean deleted;
	private Date creationDate;
	private Long lastModifiedUserId;
	private Date lastModifiedDate;
	private List<InvitationPermissionDTO> permissions;
	
	public InvitationDTO() {
		permissions = new ArrayList<InvitationPermissionDTO>();
	}
	
	public InvitationDTO(InvitationEntity entity) {
		this.id = entity.getId();
		this.email = entity.getEmailAddress();
		this.acbId = entity.getAcbId();
		this.testingLabId = entity.getTestingLabId();
		this.inviteToken = entity.getInviteToken();
		this.confirmToken = entity.getConfirmToken();
		this.createdUserId = entity.getCreatedUserId();
		this.deleted = entity.getDeleted();
		this.creationDate = entity.getCreationDate();
		this.lastModifiedUserId = entity.getLastModifiedUser();
		this.lastModifiedDate = entity.getLastModifiedDate();
		
		this.permissions = new ArrayList<InvitationPermissionDTO>();

		if(entity.getPermissions() != null && entity.getPermissions().size() > 0) {
			for(InvitationPermissionEntity permission : entity.getPermissions()) {
				this.permissions.add(new InvitationPermissionDTO(permission));
			}
		}
	}
	
	public boolean isOlderThan(long numDaysInMillis) {
		if(this.creationDate == null || this.lastModifiedDate == null) {
			return true;
		}
		
		Date now = new Date();
		if((now.getTime() - this.lastModifiedDate.getTime()) > numDaysInMillis) {
			return true;
		}
		return false;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getInviteToken() {
		return inviteToken;
	}

	public void setInviteToken(String token) {
		this.inviteToken = token;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Long getAcbId() {
		return acbId;
	}

	public void setAcbId(Long acbId) {
		this.acbId = acbId;
	}

	public List<InvitationPermissionDTO> getPermissions() {
		return permissions;
	}

	public void setPermissions(List<InvitationPermissionDTO> permissions) {
		this.permissions = permissions;
	}

	public Long getLastModifiedUserId() {
		return lastModifiedUserId;
	}

	public void setLastModifiedUserId(Long lastModifiedUserId) {
		this.lastModifiedUserId = lastModifiedUserId;
	}

	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}

	public void setLastModifiedDate(Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

	public String getConfirmToken() {
		return confirmToken;
	}

	public void setConfirmToken(String confirmToken) {
		this.confirmToken = confirmToken;
	}

	public Long getCreatedUserId() {
		return createdUserId;
	}

	public void setCreatedUserId(Long createdUserId) {
		this.createdUserId = createdUserId;
	}

	public Long getTestingLabId() {
		return testingLabId;
	}

	public void setTestingLabId(Long testingLabId) {
		this.testingLabId = testingLabId;
	}
}
