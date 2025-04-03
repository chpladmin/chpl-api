package gov.healthit.chpl.auth.authentication;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.exception.JWTValidationException;
import gov.healthit.chpl.exception.MultipleUserAccountsException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.user.cognito.CognitoApiWrapper;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class JWTUserConverterFacade implements JWTUserConverter {
    private CognitoJwtUserConverter cognitoJwtUserConverter;
    private CognitoApiWrapper cognitoApiWrapper;

    public JWTUserConverterFacade(CognitoApiWrapper cognitoApiWrapper,
            @Value("${cognito.region}") String region,
            @Value("${cognito.userPoolId}") String userPoolId,
            @Value("${cognito.tokenizezRsaKeyUrl}") String tokenizeRsaKeyUrl) {
        cognitoJwtUserConverter = new CognitoJwtUserConverter(region, userPoolId, tokenizeRsaKeyUrl);
        this.cognitoApiWrapper = cognitoApiWrapper;
    }

    @Override
    public JWTAuthenticatedUser getAuthenticatedUser(String jwt) throws JWTValidationException, MultipleUserAccountsException {
        JWTAuthenticatedUser user = cognitoJwtUserConverter.getAuthenticatedUser(jwt);
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
        } else {
            LOGGER.warn("Could not get authenticated user from jwt");
        }
        return user;
    }
}
