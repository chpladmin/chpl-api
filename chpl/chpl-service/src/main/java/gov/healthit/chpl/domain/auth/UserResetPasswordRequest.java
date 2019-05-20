package gov.healthit.chpl.domain.auth;

public class UserResetPasswordRequest {

    private String userName;
    private String email;

    public String getUserName() {
        return userName;
    }

    public void setUserName(final String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

}
