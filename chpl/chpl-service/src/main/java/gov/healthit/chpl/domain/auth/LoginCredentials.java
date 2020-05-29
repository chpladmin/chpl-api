package gov.healthit.chpl.domain.auth;

public class LoginCredentials {
    private String userName;
    private String password;

    public LoginCredentials() {
    }

    public LoginCredentials(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
