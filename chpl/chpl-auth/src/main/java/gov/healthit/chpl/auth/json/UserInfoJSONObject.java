package gov.healthit.chpl.auth.json;

import gov.healthit.chpl.auth.dto.UserDTO;


public class UserInfoJSONObject {
	
	private static final long serialVersionUID = 1L;
	private Long userId;
	private String subjectName;
	private String firstName = null;
	private String lastName = null;
	private String email = null;
	private String phoneNumber = null;
	private String title = null;
	private boolean accountLocked = false;
	private boolean accountEnabled = true;
	
	
	public UserInfoJSONObject(){}
	
	public UserInfoJSONObject(UserDTO dto){
		this.userId = dto.getId();
		this.subjectName = dto.getSubjectName();
		this.firstName = dto.getFirstName();
		this.lastName = dto.getLastName();
		this.email = dto.getEmail();
		this.phoneNumber = dto.getPhoneNumber();
		this.title = dto.getTitle();
		this.accountLocked = dto.isAccountLocked();
		this.accountEnabled = dto.isAccountEnabled();
		
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


	public boolean isAccountLocked() {
		return accountLocked;
	}


	public void setAccountLocked(boolean accountLocked) {
		this.accountLocked = accountLocked;
	}


	public boolean isAccountEnabled() {
		return accountEnabled;
	}


	public void setAccountEnabled(boolean accountEnabled) {
		this.accountEnabled = accountEnabled;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}
	
}
