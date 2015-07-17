package gov.healthit.chpl.auth.user;
import gov.healthit.chpl.auth.permission.UserPermission;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;

public class UserDTO {

	
	private static final long serialVersionUID = 1L;
	private Long id;
	private String subjectName;
	private String firstName;
	private String lastName;
	private String email;
	private String phoneNumber;
	private String title;
	private Set<UserPermission> permissions = new HashSet<UserPermission>();
	private boolean accountExpired;
	private boolean accountLocked;
	private boolean credentialsExpired;
	private boolean accountEnabled;
	
	
	public UserDTO(){}
	
	public UserDTO(UserEntity entity){
		
		this.id = entity.getId();
		this.subjectName = entity.getSubjectName();
		this.firstName = entity.getFirstName();
		this.lastName = entity.getLastName();
		this.email = entity.getContact().getEmail();
		this.phoneNumber = entity.getContact().getPhoneNumber();
		this.title = entity.getContact().getEmail();
		this.permissions = entity.getPermissions();
		this.accountExpired = !entity.isAccountNonExpired();
		this.accountLocked = !entity.isAccountNonLocked();
		this.accountEnabled = entity.isEnabled();
		
	}
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getSubjectName() {
		return subjectName;
	}
	
	public void setSubjectName(String subject) {
		this.subjectName = subject;
	}
	
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public Set<UserPermission> getPermissions() {
		return this.permissions;
	}
	
	public void addPermission(UserPermission permission){
		this.permissions.add(permission);
	}
	
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return this.getPermissions();
	}

	public String getName() {
		return subjectName;
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

}
