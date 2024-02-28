package gov.healthit.chpl.auth.authentication;

import org.apache.commons.lang3.NotImplementedException;
import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.auth.jwt.JWTConsumer;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.dao.auth.UserDAO;
import gov.healthit.chpl.exception.JWTValidationException;
import gov.healthit.chpl.exception.MultipleUserAccountsException;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class JWTUserConverterFacade implements JWTUserConverter {
    private ChplJWTUserConverter chplJwtUserConverter;
    private CognitoJwtUserConverter cognitoJwtUserConverter;

    private FF4j ff4j;

    public JWTUserConverterFacade(JWTConsumer jwtConsumer, UserDAO userDAO, @Value("${cognito.region}") String region,
            @Value("${cognito.userPoolId}") String userPoolId, @Value("${cognito.clientId}") String clientId,
            @Value("${cognito.tokenizezRsaKeyUrl}") String tokenizeRsaKeyUrl, FF4j ff4j) {
        chplJwtUserConverter = new ChplJWTUserConverter(jwtConsumer, userDAO);
        cognitoJwtUserConverter = new CognitoJwtUserConverter(region, userPoolId, clientId, tokenizeRsaKeyUrl);
        this.ff4j = ff4j;
    }

    @Override
    public JWTAuthenticatedUser getAuthenticatedUser(String jwt) {
        try {
            JWTAuthenticatedUser user = null;
            //If SSO is on, try to validate the jwt using the Cognito converter
            if (ff4j.check(FeatureList.SSO)) {
                user = cognitoJwtUserConverter.getAuthenticatedUser(jwt);
            }

            //If SSO is off or jwt cannot be converted using the Cognito converter, use the CHP converter
            if (user == null) {
                user = chplJwtUserConverter.getAuthenticatedUser(jwt);
            }
            return user;
        } catch (JWTValidationException | MultipleUserAccountsException e) {
            LOGGER.error("Error converting JWT -> AuthenticatedUser object", e);
            return null;
        }
    }

    @Override
    public JWTAuthenticatedUser getImpersonatingUser(String jwt) throws JWTValidationException {
        if (ff4j.check(FeatureList.SSO)) {
            throw new NotImplementedException("CognitoJwtUserConverter.getImpersonatingUser() has not been implemented.");
        } else {
            return chplJwtUserConverter.getImpersonatingUser(jwt);
        }
    }
}
