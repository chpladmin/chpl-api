package gov.healthit.chpl.auth.user;

public class AuthorizeToken {
    
    private boolean authorized;

    public boolean isAuthorized() {
        return authorized;
    }

    public void setAuthorized(boolean authorized) {
        this.authorized = authorized;
    }
}
