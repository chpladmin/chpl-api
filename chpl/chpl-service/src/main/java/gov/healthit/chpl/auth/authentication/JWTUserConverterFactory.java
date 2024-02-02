package gov.healthit.chpl.auth.authentication;

import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.auth.jwt.JWTConsumer;
import gov.healthit.chpl.dao.auth.UserDAO;

@Component
public class JWTUserConverterFactory {
    private ChplJWTUserConverter chplJwtUserConverter;
    private CognitoJwtUserConverter cognitoJwtUserConverter;

    private FF4j ff4j;

    public JWTUserConverterFactory(JWTConsumer jwtConsumer, UserDAO userDAO, @Value("${cognito.region}") String region,
            @Value("${cognito.userPoolId}") String userPoolId, @Value("${cognito.clientId}") String clientId,
            @Value("${cognito.tokenizezRsaKeyUrl}") String tokenizeRsaKeyUrl, FF4j ff4j) {
        chplJwtUserConverter = new ChplJWTUserConverter(jwtConsumer, userDAO);
        cognitoJwtUserConverter = new CognitoJwtUserConverter(region, userPoolId, clientId, tokenizeRsaKeyUrl);
        this.ff4j = ff4j;
    }

    public JWTUserConverter get() {
        if (ff4j.check(FeatureList.SSO)) {
            return cognitoJwtUserConverter;
        } else {
            return chplJwtUserConverter;
        }
    }

}
