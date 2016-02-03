package gov.healthit.chpl.auth.entity;

import gov.healthit.chpl.auth.dto.UserPermissionDTO;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;


@Entity
@Table(name="`user`")
@SQLDelete(sql = "UPDATE openchpl.\"user\" SET deleted = true WHERE user_id = ?")
@Where(clause = "NOT deleted")
public class UserEntity {

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name="user_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name="user_name", unique=true)
	private String subjectName;
	
	@Column(name="password")
	private String password = null;
	
	@Column(name="account_expired")
	private boolean accountExpired;
	
	@Column(name="account_locked")
	private boolean accountLocked;
	
	@Column(name="credentials_expired")
	private boolean credentialsExpired;
	
	@Column(name="account_enabled")
	private boolean accountEnabled;
	
	@Column(name="compliance_signature")
	private Date complianceSignature;
	
	@Column(name="failed_login_count")
	private int failedLoginCount;
	
	@Column(name="last_modified_user")
	private Long lastModifiedUser;
	
	@Column(name="last_modified_date")
	private Date lastModifiedDate;
	
	@Column(name="deleted")
	private Boolean deleted;
	
	@OneToMany(mappedBy="user", fetch=FetchType.LAZY)
 	private Set<UserPermissionUserMappingEntity> permissionMappings;
	
	@ManyToOne(optional=false, fetch=FetchType.EAGER)
	@JoinColumn(name="contact_id", unique=true, nullable=false)
	private UserContactEntity contact;
	
	
	public UserEntity(){
		this.subjectName = null;
		this.password = null;
		this.accountExpired = false;
		this.accountLocked = false;
		this.credentialsExpired = false;
		this.accountEnabled = true;
		
		this.contact = new UserContactEntity();
	}
	
	
	public UserEntity(String subjectName) {
		this.subjectName = subjectName;
		this.password = null;
		this.accountExpired = false;
		this.accountLocked = false;
		this.credentialsExpired = false;
		this.accountEnabled = true;
		
		this.contact = new UserContactEntity();
	}
	
	public UserEntity(String subjectName, String encodedPassword) {
		this.subjectName = subjectName;
		this.password = encodedPassword;
		this.accountExpired = false;
		this.accountLocked = false;
		this.credentialsExpired = false;
		this.accountEnabled = true;
		
		this.contact = new UserContactEntity();		
	}
	
	public String getSubjectName() {
		return subjectName;
	}
	
	public void setSubjectName(String subject) {
		this.subjectName = subject;
	}
	
	public String getFirstName(){
		return contact.getFirstName();
	}
	
	public void setFirstName(String firstName){
		contact.setFirstName(firstName);
	}
	
	public String getLastName(){
		return contact.getLastName();
	}
	
	public void setLastName(String lastName){
		contact.setLastName(lastName);
	}
	
	public Set<UserPermissionDTO> getPermissions() {
		
		Set<UserPermissionDTO> permissions = new HashSet<UserPermissionDTO>();
		
		for (UserPermissionUserMappingEntity mapping : permissionMappings){
			
			permissions.add(new UserPermissionDTO(mapping.getPermission()));
		}
		return permissions;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String encodedPassword){
		this.password = encodedPassword;
	}

	public String getUsername() {
		return subjectName;
	}

	public boolean isAccountNonExpired() {
		return !accountExpired;
	}
	
	public boolean isAccountNonLocked() {
		return !accountLocked;
	}

	public boolean isCredentialsNonExpired() {
		return !credentialsExpired;
	}

	public boolean isEnabled() {
		return accountEnabled;
	}

	public Long getId() {
		return id;
	}
	
	public UserContactEntity getContact() {
		return contact;
	}
	
	public void setContact(UserContactEntity contact) {
		this.contact = contact;
	}
	
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	public void setAccountExpired(boolean accountExpired) {
		this.accountExpired = accountExpired;
	}
	
	public void setAccountLocked(boolean accountLocked) {
		this.accountLocked = accountLocked;
	}
	
	public void setCredentialsExpired(boolean credentialsExpired) {
		this.credentialsExpired = credentialsExpired;
	}
	
	public void setAccountEnabled(boolean accountEnabled) {
		this.accountEnabled = accountEnabled;
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


	public Date getComplianceSignature() {
		return complianceSignature;
	}


	public void setComplianceSignature(Date complianceSignature) {
		this.complianceSignature = complianceSignature;
	}


	public int getFailedLoginCount() {
		return failedLoginCount;
	}


	public void setFailedLoginCount(int failedLoginCount) {
		this.failedLoginCount = failedLoginCount;
	}


	public Set<UserPermissionUserMappingEntity> getPermissionMappings() {
		return permissionMappings;
	}


	public void setPermissionMappings(Set<UserPermissionUserMappingEntity> permissionMappings) {
		this.permissionMappings = permissionMappings;
	}


	public boolean isAccountExpired() {
		return accountExpired;
	}


	public boolean isAccountLocked() {
		return accountLocked;
	}


	public boolean isCredentialsExpired() {
		return credentialsExpired;
	}


	public boolean isAccountEnabled() {
		return accountEnabled;
	}
	
	
	
}
