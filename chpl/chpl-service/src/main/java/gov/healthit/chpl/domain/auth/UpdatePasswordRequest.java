package gov.healthit.chpl.domain.auth;

/**
 * Object containing fields required for changing a password.
 * @author alarned
 *
 */
public class UpdatePasswordRequest {

    private String oldPassword;
    private String newPassword;

    /** Default constructor. */
    public UpdatePasswordRequest() {
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(final String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(final String newPassword) {
        this.newPassword = newPassword;
    }

}
