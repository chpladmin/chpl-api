package gov.healthit.chpl.domain.auth;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ResetPasswordRequest {

    private String token;
    private String newPassword;
    private String userName; //Remove with OCD-3421
    private String email;
}
