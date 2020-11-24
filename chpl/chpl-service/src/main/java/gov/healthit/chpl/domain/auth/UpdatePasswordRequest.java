package gov.healthit.chpl.domain.auth;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdatePasswordRequest {

    private String oldPassword;
    private String newPassword;
}
