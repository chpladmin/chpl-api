package gov.healthit.chpl.domain.auth;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UpdateExpiredPasswordRequest extends UpdatePasswordRequest {

    private String email;

    public LoginCredentials getLoginCredentials() {
        return new LoginCredentials(this.email, this.getOldPassword());
    }
}
