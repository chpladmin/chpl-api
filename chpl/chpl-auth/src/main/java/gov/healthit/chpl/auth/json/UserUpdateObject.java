package gov.healthit.chpl.auth.json;


public class UserUpdateObject {
	
	private static final long serialVersionUID = 1L;
	private String subjectName;
	private String firstName = null;
	private String lastName = null;
	private String email = null;
	private String phoneNumber = null;
	private String title = null;
	private boolean accountLocked = false;
	private boolean accountEnabled = true;
	
	
	public UserUpdateObject(){}


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
	
}
