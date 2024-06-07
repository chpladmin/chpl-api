package gov.healthit.chpl.user.cognito;

import gov.healthit.chpl.domain.auth.User;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CognitoAuthenticationResponse {
    private String accessToken;
    private String idToken;
    private String refreshToken;
    private User user;
}
