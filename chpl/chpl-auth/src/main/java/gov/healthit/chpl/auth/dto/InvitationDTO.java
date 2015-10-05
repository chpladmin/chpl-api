package gov.healthit.chpl.auth.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import gov.healthit.chpl.auth.entity.InvitationEntity;
import gov.healthit.chpl.auth.entity.InvitationPermissionEntity;

public class InvitationDTO {
	private static final long VALID_INVITATION_LENGTH = 3*24*60*60*1000;
	
	private Long id;
	private String email;
	private Long acbId;
	private String token;
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
		this.token = entity.getToken();
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
	
	public boolean isExpired() {
		if(this.creationDate == null) {
			return true;
		}
		
		Date now = new Date();
		if((now.getTime() - this.creationDate.getTime()) > VALID_INVITATION_LENGTH) {
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

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
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
}
