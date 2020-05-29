package gov.healthit.chpl.domain.auth;

import java.io.Serializable;

public class AuthorizeCredentials extends LoginCredentials implements Serializable {
    private static final long serialVersionUID = 5419635752541177318L;
    private String hash;

    public String getHash() {
        return hash;
    }

    public void setHash(final String hash) {
        this.hash = hash;
    }
}
