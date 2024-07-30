package gov.healthit.chpl.domain;

import lombok.Data;

@Data
public class CognitoRefreshTokenRequest {
    private String refreshToken;
    private String cognitoId;
    private String email;
}
