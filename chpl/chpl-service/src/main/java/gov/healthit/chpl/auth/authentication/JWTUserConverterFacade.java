package gov.healthit.chpl.auth.authentication;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.auth.jwt.JWTConsumer;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.dao.auth.UserDAO;
import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.exception.JWTValidationException;
import gov.healthit.chpl.exception.MultipleUserAccountsException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.user.cognito.CognitoApiWrapper;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class JWTUserConverterFacade implements JWTUserConverter {
    private ChplJWTUserConverter chplJwtUserConverter;
    private CognitoJwtUserConverter cognitoJwtUserConverter;

    private CognitoApiWrapper cognitoApiWrapper;

    private FF4j ff4j;

    public JWTUserConverterFacade(JWTConsumer jwtConsumer,
            UserDAO userDAO,
            @Value("${cognito.region}") String region,
            @Value("${cognito.userPoolId}") String userPoolId,
            @Value("${cognito.tokenizezRsaKeyUrl}") String tokenizeRsaKeyUrl,
            FF4j ff4j,
            CognitoApiWrapper cognitoApiWrapper) {
        chplJwtUserConverter = new ChplJWTUserConverter(jwtConsumer, userDAO);
        cognitoJwtUserConverter = new CognitoJwtUserConverter(region, userPoolId, tokenizeRsaKeyUrl);
        this.ff4j = ff4j;
        this.cognitoApiWrapper = cognitoApiWrapper;
    }

    @Override
    public JWTAuthenticatedUser getAuthenticatedUser(String jwt) throws JWTValidationException, MultipleUserAccountsException {
        JWTAuthenticatedUser user = null;
        //If SSO is on, try to validate the jwt using the Cognito converter
        if (ff4j.check(FeatureList.SSO)) {
            user = cognitoJwtUserConverter.getAuthenticatedUser(jwt);
            if (user != null) {
                try {
                    //Many values are not available from the Cognito Access Token so we have to set them
                    //manually here from the Cognito user data
                    User cognitoUser = cognitoApiWrapper.getUserInfo(user.getCognitoId());
                    user.setEmail(cognitoUser.getEmail());
                    user.setSubjectName(cognitoUser.getEmail());
                    user.setFullName(cognitoUser.getFullName());
                    if (!StringUtils.isEmpty(cognitoUser.getRole())) {
                        user.setAuthorities(Stream.of(new SimpleGrantedAuthority(cognitoUser.getRole())).collect(Collectors.toSet()));
                    }
                    if (!CollectionUtils.isEmpty(cognitoUser.getOrganizations())) {
                        user.setOrganizationIds(cognitoUser.getOrganizations().stream()
                                .map(org -> org.getId())
                                .toList());
                    }
                } catch (UserRetrievalException e) {
                    throw new JWTValidationException("Could not locate the Cognito user id");
                }
            }
        }

        //If SSO is off or jwt cannot be converted using the Cognito converter, use the CHP converter
        if (user == null) {
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
