package gov.healthit.chpl.user.cognito.authentication;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CognitoAuthenticationChallengeException extends Exception {
    private static final long serialVersionUID = -8253760446160706496L;

    private CognitoAuthenticationChallenge challenge;
}
