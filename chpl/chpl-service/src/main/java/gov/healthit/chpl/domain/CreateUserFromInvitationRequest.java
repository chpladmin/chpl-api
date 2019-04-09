package gov.healthit.chpl.domain;

import java.io.Serializable;

import gov.healthit.chpl.domain.auth.CreateUserRequest;

public class CreateUserFromInvitationRequest implements Serializable {
    private static final long serialVersionUID = 5216297040793549351L;
    private String hash;
    private CreateUserRequest user;

    public CreateUserRequest getUser() {
        return user;
    }

    public void setUser(final CreateUserRequest user) {
        this.user = user;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(final String hash) {
        this.hash = hash;
    }
}
