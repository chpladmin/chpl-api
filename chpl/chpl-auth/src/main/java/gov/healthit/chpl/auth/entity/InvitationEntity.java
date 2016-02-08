package gov.healthit.chpl.auth.entity;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name="invited_user")
public class InvitationEntity {
	
	@Id 
	@Basic( optional = false )
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column( name = "invited_user_id", nullable = false  )
	private Long id;

	@Column(name="email", unique=true)
	private String emailAddress;
	
	@Column(name = "certification_body_id")
	private Long acbId;
	
	@Column(name = "testing_lab_id")
	private Long testingLabId;
	
	@Column(name="invite_token", unique=true)
	private String inviteToken;
	
	@Column(name="confirm_token", unique=true)
	private String confirmToken;
	
	@Column(name="created_user_id", unique=true)
	private Long createdUserId;
	
	@Basic( optional = true )
	@OneToMany(fetch = FetchType.LAZY, mappedBy="invitedUser")
	private Set<InvitationPermissionEntity> permissions = new HashSet<InvitationPermissionEntity>();
	
	@Column(name="creation_date")
	private Date creationDate;
	
	@Column(name="last_modified_user")
	private Long lastModifiedUser;
	
	@Column(name="last_modified_date")
	private Date lastModifiedDate;
	
	@Column(name="deleted")
	private Boolean deleted;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getInviteToken() {
		return inviteToken;
	}

	public void setInviteToken(String token) {
		this.inviteToken = token;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Long getLastModifiedUser() {
		return lastModifiedUser;
	}

	public void setLastModifiedUser(Long lastModifiedUser) {
		this.lastModifiedUser = lastModifiedUser;
	}

	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}

	public void setLastModifiedDate(Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

	public Boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}

	public Long getAcbId() {
		return acbId;
	}

	public void setAcbId(Long acbId) {
		this.acbId = acbId;
	}

	public Set<InvitationPermissionEntity> getPermissions() {
		return permissions;
	}

	public void setPermissions(Set<InvitationPermissionEntity> permissions) {
		this.permissions = permissions;
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
