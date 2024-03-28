package gov.healthit.chpl.user.cognito;

import java.util.UUID;

import gov.healthit.chpl.domain.auth.LoginCredentials;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CognitoCredentials extends LoginCredentials {
    private UUID cognitoId;
}
