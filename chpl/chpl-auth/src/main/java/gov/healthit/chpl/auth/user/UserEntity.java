package gov.healthit.chpl.auth.user;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.auth.permission.UserPermissionDTO;
import gov.healthit.chpl.auth.permission.UserPermissionUserMappingEntity;

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
	
	@Column(name="last_modified_user")
	private Long lastModifiedUser;
	
	//@OneToMany(mappedBy="pk.user", fetch=FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@OneToMany(mappedBy="pk.user", fetch=FetchType.LAZY)
 	private Set<UserPermissionUserMappingEntity> permissionMappings;
	
	@ManyToOne(optional=false, fetch=FetchType.EAGER)
	//@JoinColumn(name="contact_id", unique=true, nullable=false, updatable=false) //TODO: Why was this non-updatable?
	@JoinColumn(name="contact_id", unique=true, nullable=false)
	private UserContactEntity contact;
	
	
	public UserEntity(){
		this.subjectName = null;
		this.password = null;
		this.accountExpired = false;
		this.accountLocked = false;
		this.credentialsExpired = false;
		this.accountEnabled = true;
		populateLastModifiedUser();
	}
	
	
	public UserEntity(String subjectName) {
		this.subjectName = subjectName;
		this.password = null;
		this.accountExpired = false;
		this.accountLocked = false;
		this.credentialsExpired = false;
		this.accountEnabled = true;
		populateLastModifiedUser();
	}
	
	public UserEntity(String subjectName, String encodedPassword) {
		this.subjectName = subjectName;
		this.password = encodedPassword;
		this.accountExpired = false;
		this.accountLocked = false;
		this.credentialsExpired = false;
		this.accountEnabled = true;
		populateLastModifiedUser();
	}
	
	public String getSubjectName() {
		return subjectName;
	}
	
	public void setSubjectName(String subject) {
		this.subjectName = subject;
		populateLastModifiedUser();
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
		populateLastModifiedUser();
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
	
	private void populateLastModifiedUser(){
		User currentUser = Util.getCurrentUser();
		
		Long userId = new Long(-1);
		
		if (currentUser != null){
			userId = currentUser.getId();
		}
		this.lastModifiedUser = userId;
	}
	
}
