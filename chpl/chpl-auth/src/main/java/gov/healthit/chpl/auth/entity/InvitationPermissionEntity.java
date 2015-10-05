package gov.healthit.chpl.auth.entity;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;


@Entity
@Table(name="invited_user_permission")
public class InvitationPermissionEntity {
	
	@Id 
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "invitedUserPermissionInvitedUserPermission_idGenerator")
	@Basic( optional = false )
	@Column( name = "invited_user_permission_id", nullable = false  )
	@SequenceGenerator(name = "invitedUserPermissionInvitedUserPermission_idGenerator", sequenceName = "invited_user_permission_invited_user_permission_id_seq")	
	private Long id;

	@ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="invited_user_id")
	private InvitationEntity invitedUser;
	
	@Column(name = "user_permission_id")
	private Long userPermissionId;
	
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

	public Long getUserPermissionId() {
		return userPermissionId;
	}

	public void setUserPermissionId(Long userPermissionId) {
		this.userPermissionId = userPermissionId;
	}

	public InvitationEntity getInvitedUser() {
		return invitedUser;
	}

	public void setInvitedUser(InvitationEntity invitedUser) {
		this.invitedUser = invitedUser;
	}
}
