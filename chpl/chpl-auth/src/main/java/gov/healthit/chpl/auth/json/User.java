package gov.healthit.chpl.auth.json;

import gov.healthit.chpl.auth.dto.UserDTO;

public class User {
	private Long userId;
	private String subjectName;
	private String firstName;
	private String lastName;
	private String email;
	private String phoneNumber;
	private String title;
	private Boolean accountLocked;
	private Boolean accountEnabled;
	
	public User() {}
	public User(UserDTO dto) {
		this.setUserId(dto.getId());
		this.setSubjectName(dto.getSubjectName());
		this.setFirstName(dto.getFirstName());
		this.setLastName(dto.getLastName());
		this.setEmail(dto.getEmail());
		this.setPhoneNumber(dto.getPhoneNumber());
		this.setTitle(dto.getTitle());
		this.setAccountLocked(dto.isAccountLocked());
		this.setAccountEnabled(dto.isAccountEnabled());
	}
	
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public String getSubjectName() {
		return subjectName;
	}
	public void setSubjectName(String subjectName) {
		this.subjectName = subjectName;
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
	public Boolean getAccountLocked() {
		return accountLocked;
	}
	public void setAccountLocked(Boolean accountLocked) {
		this.accountLocked = accountLocked;
	}
	public Boolean getAccountEnabled() {
		return accountEnabled;
	}
	public void setAccountEnabled(Boolean accountEnabled) {
		this.accountEnabled = accountEnabled;
	}
}
