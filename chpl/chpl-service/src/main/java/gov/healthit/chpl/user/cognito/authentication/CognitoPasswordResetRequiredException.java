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
public class CognitoPasswordResetRequiredException extends Exception {
    private static final long serialVersionUID = -4209870832388366351L;

    private String message;
}
