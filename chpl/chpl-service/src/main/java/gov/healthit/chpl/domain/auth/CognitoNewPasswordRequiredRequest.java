package gov.healthit.chpl.domain.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CognitoNewPasswordRequiredRequest {
    private String userName;
    private String password;
    private String sessionId;
}
