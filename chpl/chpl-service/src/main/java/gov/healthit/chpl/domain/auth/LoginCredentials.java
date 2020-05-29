package gov.healthit.chpl.domain.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
public class LoginCredentials {

    private String userName;
    private String password;
}
