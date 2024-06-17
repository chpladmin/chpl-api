package gov.healthit.chpl.user.cognito;

import lombok.Builder;
import lombok.Data;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ChallengeNameType;

@Data
@Builder
public class CognitoAuthenticationChallenge {
    private ChallengeNameType challenge;
    private String sessionId;
}
