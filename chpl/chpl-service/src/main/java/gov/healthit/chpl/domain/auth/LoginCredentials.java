package gov.healthit.chpl.domain.auth;

import lombok.Data;

@Data
public class LoginCredentials {

    private String userName;
    private String password;
}
