package gov.healthit.chpl.domain.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginCredentials {

    private String userName;
    private String password;
}
