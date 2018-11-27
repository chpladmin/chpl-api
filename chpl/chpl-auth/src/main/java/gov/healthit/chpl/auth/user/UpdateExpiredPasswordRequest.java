package gov.healthit.chpl.auth.user;

import gov.healthit.chpl.auth.authentication.LoginCredentials;

/**
 * Object containing fields required for changing an expired password.
 * @author alarned
 *
 */
public class UpdateExpiredPasswordRequest extends UpdatePasswordRequest {

    private String userName;

    /** Default constructor. */
    public UpdateExpiredPasswordRequest() {
        super();
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(final String userName) {
        this.userName = userName;
    }

    public LoginCredentials getLoginCredentials() {
        return new LoginCredentials(this.userName, this.getOldPassword());
    }
}
