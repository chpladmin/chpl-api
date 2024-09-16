package gov.healthit.chpl.domain.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CognitoUpdatePasswordRequest {
    private String email;
    private String password;
    private String confirmPassword;
}
