package gov.healthit.chpl.domain.auth;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CognitoSetForgottenPasswordRequest {
    private UUID forgotPasswordToken;
    private String newPassword;
}
