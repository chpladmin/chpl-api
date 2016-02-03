package gov.healthit.chpl.auth.user;

public class UpdatePasswordRequest {

	private String oldPassword;
	private String newPassword;
	
	public UpdatePasswordRequest(){}

	public String getOldPassword() {
		return oldPassword;
	}

	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}
	
}
