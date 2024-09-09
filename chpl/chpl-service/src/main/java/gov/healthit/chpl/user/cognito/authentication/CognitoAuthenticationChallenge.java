package gov.healthit.chpl.user.cognito.authentication;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Builder;
import lombok.Data;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ChallengeNameType;

@Data
@Builder
public class CognitoAuthenticationChallenge {
    @JsonIgnore
    private ChallengeNameType challenge;

    private String sessionId;
}
