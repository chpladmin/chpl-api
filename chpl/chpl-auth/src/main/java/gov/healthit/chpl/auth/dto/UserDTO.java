package gov.healthit.chpl.auth.dto;

import gov.healthit.chpl.auth.entity.UserEntity;

import java.util.Collection;
import java.util.Date;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UserDTO implements UserDetails {

	
	private static final long serialVersionUID = 1L;
	private Long id;
	private String subjectName;
	private String firstName;
	private String lastName;
	private String email;
	private String phoneNumber;
	private String title;
	private Date signatureDate;
	private Date complianceSignatureDate;
	
	private int failedLoginCount;
	private boolean accountExpired;
	private boolean accountLocked;
	private boolean credentialsExpired;
	private boolean accountEnabled;
	
	
	public UserDTO(){}
	
	public UserDTO(UserEntity entity) {
		if(entity != null) {
			this.id = entity.getId();
			this.subjectName = entity.getSubjectName();
			this.firstName = entity.getFirstName();
			this.lastName = entity.getLastName();
			this.email = entity.getContact().getEmail();
			this.phoneNumber = entity.getContact().getPhoneNumber();
			this.title = entity.getContact().getTitle();
			this.signatureDate = entity.getContact().getSignatureDate();
			this.complianceSignatureDate = entity.getComplianceSignature();
			this.failedLoginCount = entity.getFailedLoginCount();
			this.accountExpired = !entity.isAccountNonExpired();
			this.accountLocked = !entity.isAccountNonLocked();
			this.accountEnabled = entity.isEnabled();
		}
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
	
	public Collection<? extends GrantedAuthority> getAuthorities() {
		// We return null rather than returning authorities here because we
		// don't actually want the DTO to have granted permissions (those
		// come from the JWT token.)
		return null;
	}
	
	public String getName() {
		return subjectName;
	}
	
	@Override
	public String getUsername() {
		return subjectName;
	}
	
	@Override
	public boolean isAccountNonExpired() {
		return !accountExpired;
	}
	
	@Override
	public boolean isAccountNonLocked() {
		return !accountLocked;
	}
	
	@Override
	public boolean isCredentialsNonExpired() {
		return !credentialsExpired;
	}
	
	@Override
	public boolean isEnabled() {
		return accountEnabled;
	}

	@Override
	public String getPassword() {
		return null;
	}

	public boolean isAccountExpired() {
		return accountExpired;
	}

	public void setAccountExpired(boolean accountExpired) {
		this.accountExpired = accountExpired;
	}

	public boolean isAccountLocked() {
		return accountLocked;
	}

	public void setAccountLocked(boolean accountLocked) {
		this.accountLocked = accountLocked;
	}

	public void setCredentialsExpired(boolean credentialsExpired) {
		this.credentialsExpired = credentialsExpired;
	}

	public boolean isAccountEnabled() {
		return accountEnabled;
	}

	public void setAccountEnabled(boolean accountEnabled) {
		this.accountEnabled = accountEnabled;
	}

	public Date getSignatureDate() {
		return signatureDate;
	}

	public void setSignatureDate(Date signatureDate) {
		this.signatureDate = signatureDate;
	}

	public Date getComplianceSignatureDate() {
		return complianceSignatureDate;
	}

	public void setComplianceSignatureDate(Date complianceSignatureDate) {
		this.complianceSignatureDate = complianceSignatureDate;
	}

	public int getFailedLoginCount() {
		return failedLoginCount;
	}

	public void setFailedLoginCount(int failedLoginCount) {
		this.failedLoginCount = failedLoginCount;
	}

	public boolean isCredentialsExpired() {
		return credentialsExpired;
	}
		
}
