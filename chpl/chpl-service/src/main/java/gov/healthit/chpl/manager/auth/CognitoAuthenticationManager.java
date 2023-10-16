package gov.healthit.chpl.manager.auth;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.CognitoSecretHash;
import gov.healthit.chpl.domain.auth.LoginCredentials;
import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.exception.UserRetrievalException;
import lombok.extern.log4j.Log4j2;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthenticationResultType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserStatusType;

@Log4j2
@Component
public class CognitoAuthenticationManager {

    private String clientId;
    private String userPoolId;
    private CognitoIdentityProviderClient cognitoClient;

    @Autowired
    public CognitoAuthenticationManager(@Value("${cognito.accessKey}") String accessKey, @Value("${cognito.secretKey}") String secretKey,
            @Value("${cognito.region}") String region, @Value("${cognito.clientId}") String clientId, @Value("${cognito.userPoolId}") String userPoolId) {

        cognitoClient = createCognitoClient(accessKey, secretKey, region);
        this.clientId = clientId;
        this.userPoolId = userPoolId;
    }

    public String authenticate(LoginCredentials credentials) {
        String secretHash = CognitoSecretHash.calculateSecretHash(clientId, "7ap2or3qt6f8nfdgqt3haljaob8eeq7ui0hj2gb4sj8q1h2js49", credentials.getUserName());

        Map<String, String> authParams = new LinkedHashMap<String, String>();
        authParams.put("USERNAME", credentials.getUserName());
        authParams.put("PASSWORD", credentials.getPassword());
        authParams.put("SECRET_HASH", secretHash);

        AdminInitiateAuthRequest authRequest = AdminInitiateAuthRequest.builder()
                .authFlow(AuthFlowType.ADMIN_USER_PASSWORD_AUTH)
                .userPoolId(userPoolId)
                .clientId(clientId)
                .authParameters(authParams)
                .build();

        try {
            AdminInitiateAuthResponse authResult = cognitoClient.adminInitiateAuth(authRequest);

            AuthenticationResultType resultType = authResult.authenticationResult();

            LOGGER.info("Access Token: {}", resultType.accessToken());
            LOGGER.info("ID Token: {}", resultType.idToken());
            LOGGER.info("Refresh Token: {}", resultType.refreshToken());

            return resultType.idToken();
        } catch (Exception e) {
            LOGGER.error("Authentication error: {}", e.getMessage(), e);
            return null;
        }
    }

    //@Transactional
    //@PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SECURED_USER, "
    //        + "T(gov.healthit.chpl.permissions.domains.SecuredUserDomainPermissions).GET_BY_USER_NAME)")
    //@PostAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SECURED_USER, "
    //        + "T(gov.healthit.chpl.permissions.domains.SecuredUserDomainPermissions).GET_BY_USER_NAME, returnObject)")
    public User getUserInfo(String userName) throws UserRetrievalException {
        AdminGetUserRequest userRequest = AdminGetUserRequest.builder()
                .userPoolId(userPoolId)
                .username(userName)
                .build();
        AdminGetUserResponse userResponse = cognitoClient.adminGetUser(userRequest);

        userResponse.userAttributes().stream()
                .forEach(att -> LOGGER.info("User Attribute {} | {}", att.name(), att.value()));

        User user = new User();
        user.setUserId(Long.valueOf(getUserAttribute(userResponse.userAttributes(), "custom:chpl_user_id").value()));
        user.setSubjectName(getUserAttribute(userResponse.userAttributes(), "email").value());
        user.setFriendlyName(getUserAttribute(userResponse.userAttributes(), "name").value());
        user.setFullName(getUserAttribute(userResponse.userAttributes(), "name").value());
        user.setEmail(getUserAttribute(userResponse.userAttributes(), "email").value());
        user.setAccountLocked(false);
        user.setAccountEnabled(userResponse.enabled());
        user.setCredentialsExpired(false);
        user.setPasswordResetRequired(userResponse.userStatus().equals(UserStatusType.RESET_REQUIRED));
        user.setLastLoggedInDate(new Date());
        user.setRole("ROLE_ADMIN");
        //user.organizations(null);

        LOGGER.info("User: {}", user.toString());

        return user;
    }

    private AttributeType getUserAttribute(List<AttributeType> attributes, String name) {
        return attributes.stream()
            .filter(att -> att.name().equals(name))
            .findAny()
            .get();
    }

    private CognitoIdentityProviderClient createCognitoClient(String accessKey, String secretKey, String region) {
        AwsCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);

        return CognitoIdentityProviderClient.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
    }
}
