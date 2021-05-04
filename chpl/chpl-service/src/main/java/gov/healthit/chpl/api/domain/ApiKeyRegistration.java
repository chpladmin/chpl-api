package gov.healthit.chpl.api.domain;

import java.io.Serializable;

public class ApiKeyRegistration implements Serializable {
    private static final long serialVersionUID = 1101884894293322964L;
    private String email;
    private String name;

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

}
