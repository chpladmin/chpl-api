package gov.healthit.chpl.auth.authentication;

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
    public JWTAuthenticatedUser getAuthenticatedUser(String jwt) throws JWTValidationException, MultipleUserAccountsException {
        JWTAuthenticatedUser user = null;
        //If SSO is on, try to validate the jwt using the Cognito converter
        if (ff4j.check(FeatureList.SSO)) {
            user = cognitoJwtUserConverter.getAuthenticatedUser(jwt);
        } else {
            user = chplJwtUserConverter.getAuthenticatedUser(jwt);
        }
        return user;
    }

    @Override
    public JWTAuthenticatedUser getImpersonatingUser(String jwt) throws JWTValidationException {
        //Since we only support impersonating if logged in using a CHPL (not Cognito) user, always try to use the
        //ChplJwtUserConverter.  If there is an error, i.e. the user is a Cognito user, return null and handle it
        //in the controller.
        try {
            return chplJwtUserConverter.getImpersonatingUser(jwt);
        } catch (JWTValidationException e) {
            LOGGER.error("Possibly tried to get the impersonating user that is not a CHPL user.", e);
            return null;
        }
    }
}
