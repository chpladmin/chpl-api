package gov.healthit.chpl.manager.auth;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.NotImplementedException;
import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.CognitoSecretHash;
import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.domain.auth.LoginCredentials;
import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.exception.UserRetrievalException;
import lombok.extern.log4j.Log4j2;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminListGroupsForUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminListGroupsForUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthenticationResultType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUsersRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUsersResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserStatusType;

@Log4j2
@Component
public class CognitoAuthenticationManager {

    private String clientId;
    private String userPoolId;
    private String userPoolClientSecret;
    private CognitoIdentityProviderClient cognitoClient;
    private FF4j ff4j;
    @Autowired
    public CognitoAuthenticationManager(FF4j ff4j, @Value("${cognito.accessKey}") String accessKey, @Value("${cognito.secretKey}") String secretKey,
            @Value("${cognito.region}") String region, @Value("${cognito.clientId}") String clientId, @Value("${cognito.userPoolId}") String userPoolId,
            @Value("${cognito.userPoolClientSecret}") String userPoolClientSecret) {

        this.ff4j = ff4j;
        cognitoClient = createCognitoClient(accessKey, secretKey, region);
        this.clientId = clientId;
        this.userPoolId = userPoolId;
        this.userPoolClientSecret = userPoolClientSecret;
    }

    public String authenticate(LoginCredentials credentials) {
        if (!ff4j.check(FeatureList.SSO)) {
            throw new NotImplementedException("This feature has not been implemented");
        }
        String secretHash = CognitoSecretHash.calculateSecretHash(clientId, userPoolClientSecret, credentials.getUserName());

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

    //TODO OCD-4203 - this will need to be uncommented when we implement more of the Cognito security server side
    //@PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SECURED_USER, "
    //        + "T(gov.healthit.chpl.permissions.domains.SecuredUserDomainPermissions).GET_BY_USER_NAME)")
    //@PostAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SECURED_USER, "
    //        + "T(gov.healthit.chpl.permissions.domains.SecuredUserDomainPermissions).GET_BY_USER_NAME, returnObject)")
    public User getUserInfo(UUID ssoUserId) throws UserRetrievalException {
        if (!ff4j.check(FeatureList.SSO)) {
            throw new NotImplementedException("This feature has not been implemented");
        }

        ListUsersResponse response = cognitoClient.listUsers(ListUsersRequest.builder()
                .userPoolId(userPoolId)
                .filter("sub = \"" + ssoUserId.toString() + "\"")
                .limit(1)
                .build());

        User user = new User();
        user.setUserSsoId(ssoUserId);
        user.setSubjectName(getUserAttribute(response.users().get(0).attributes(), "email").value());
        user.setFriendlyName(getUserAttribute(response.users().get(0).attributes(), "name").value());
        user.setFullName(getUserAttribute(response.users().get(0).attributes(), "name").value());
        user.setEmail(getUserAttribute(response.users().get(0).attributes(), "email").value());
        user.setAccountLocked(!response.users().get(0).enabled());
        user.setAccountEnabled(response.users().get(0).enabled());
        user.setCredentialsExpired(false);
        user.setPasswordResetRequired(response.users().get(0).userStatus().equals(UserStatusType.RESET_REQUIRED));
        user.setLastLoggedInDate(new Date());

        AdminListGroupsForUserRequest groupsRequest = AdminListGroupsForUserRequest.builder()
                .userPoolId(userPoolId)
                .username(user.getEmail())
                .build();
        AdminListGroupsForUserResponse groupsResponse = cognitoClient.adminListGroupsForUser(groupsRequest);
        user.setRole(groupsResponse.groups().get(0).groupName());

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
