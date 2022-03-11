package gov.healthit.chpl.domain.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordRequest {

    private String token;
    private String newPassword;
    private String userName; //Remove with OCD-3421
    private String email;
}
