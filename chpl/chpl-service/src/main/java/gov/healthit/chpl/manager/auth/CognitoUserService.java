package gov.healthit.chpl.manager.auth;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.CognitoSecretHash;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.Organization;
import gov.healthit.chpl.domain.auth.CognitoGroups;
import gov.healthit.chpl.domain.auth.CreateUserRequest;
import gov.healthit.chpl.domain.auth.LoginCredentials;
import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.UserRetrievalException;
import lombok.extern.log4j.Log4j2;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminAddUserToGroupRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminAddUserToGroupResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminListGroupsForUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminListGroupsForUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthenticationResultType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUsersRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUsersResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.MessageActionType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserStatusType;

@Log4j2
@Component
public class CognitoUserService {

    private String clientId;
    private String userPoolId;
    private String userPoolClientSecret;
    private CognitoIdentityProviderClient cognitoClient;
    private CertificationBodyDAO certificationBodyDAO;
    private DeveloperDAO developerDAO;

    @Autowired
    public CognitoUserService(@Value("${cognito.accessKey}") String accessKey, @Value("${cognito.secretKey}") String secretKey,
            @Value("${cognito.region}") String region, @Value("${cognito.clientId}") String clientId, @Value("${cognito.userPoolId}") String userPoolId,
            @Value("${cognito.userPoolClientSecret}") String userPoolClientSecret, CertificationBodyDAO certificationBodyDAO, DeveloperDAO developerDAO) {

        cognitoClient = createCognitoClient(accessKey, secretKey, region);
        this.clientId = clientId;
        this.userPoolId = userPoolId;
        this.userPoolClientSecret = userPoolClientSecret;
        this.certificationBodyDAO = certificationBodyDAO;
        this.developerDAO = developerDAO;
    }

    public String authenticate(LoginCredentials credentials) {
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
            return resultType.idToken();
        } catch (Exception e) {
            //This is cluttering the logs when the SSO flag is on, and the user logs in using CHPL creds
            //We might want to uncomment it when we move to only using Cognito creds
            //LOGGER.error("Authentication error: {}", e.getMessage(), e);
            return null;
        }
    }

    public User getUserInfo(UUID cognitoId) throws UserRetrievalException {
        ListUsersResponse response = cognitoClient.listUsers(ListUsersRequest.builder()
                .userPoolId(userPoolId)
                .filter("sub = \"" + cognitoId.toString() + "\"")
                .limit(1)
                .build());

        User user = new User();
        user.setCognitoId(cognitoId);
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

        AttributeType orgIdsAttribute = getUserAttribute(response.users().get(0).attributes(), "custom:organizations");
        if (orgIdsAttribute != null && StringUtils.isNotEmpty(orgIdsAttribute.value())) {
            user.setOrganizations(getOrganizations(user.getRole(), Stream.of(orgIdsAttribute.value().split(","))
                .map(Long::valueOf)
                .toList()));
        }

        return user;
    }

    public AdminCreateUserResponse createUser(CreateUserRequest userRequest) {
        AdminCreateUserRequest request = AdminCreateUserRequest.builder()
                .userPoolId(userPoolId)
                .username(userRequest.getEmail())
                .userAttributes(
                        AttributeType.builder().name("name").value(userRequest.getFriendlyName()).build(),
                        AttributeType.builder().name("email").value(userRequest.getEmail()).build())
                .temporaryPassword("Temp-Password-1!")
                .messageAction(MessageActionType.SUPPRESS)
                .build();


        return cognitoClient.adminCreateUser(request);
    }

    public AdminAddUserToGroupResponse addUserToAdminGroup(String email) {
        AdminAddUserToGroupRequest request = AdminAddUserToGroupRequest.builder()
                .userPoolId(userPoolId)
                .groupName("chpl-admin")
                .username(email)
                .build();

        return cognitoClient.adminAddUserToGroup(request);
    }

    private CognitoIdentityProviderClient createCognitoClient(String accessKey, String secretKey, String region) {
        AwsCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);

        return CognitoIdentityProviderClient.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
    }

    private AttributeType getUserAttribute(List<AttributeType> attributes, String name) {
        return attributes.stream()
            .filter(att -> att.name().equals(name))
            .findAny()
            .orElse(null);

    }

    private List<Organization> getOrganizations(String role, List<Long> orgIds) {
        if (role.equalsIgnoreCase(CognitoGroups.CHPL_ACB)) {
            return getCerificationBodyOrganizations(role, orgIds);
        } else if (role.equalsIgnoreCase(CognitoGroups.CHPL_DEVELOPER)) {
            return getDeveloperOrganizations(role, orgIds);
        }
        return null;
    }

    private List<Organization> getCerificationBodyOrganizations(String role, List<Long> orgIds) {
        return orgIds.stream()
                .map(acbId -> getCertificationBody(acbId))
                .map(acb -> new Organization(acb.getId(), acb.getName()))
                .toList();
    }

    private CertificationBody getCertificationBody(Long certificationBodyId) {
        try {
            return certificationBodyDAO.getById(certificationBodyId);
        } catch (EntityRetrievalException e) {
            LOGGER.error("Could not retrieve ACB: {}", certificationBodyId, e);
            return null;
        }
    }

    private List<Organization> getDeveloperOrganizations(String role, List<Long> orgIds) {
        return orgIds.stream()
                .map(developerId -> getDeveloper(developerId))
                .map(dev -> new Organization(dev.getId(), dev.getName()))
                .toList();
    }

    private Developer getDeveloper(Long developerId) {
        try {
            return developerDAO.getById(developerId);
        } catch (EntityRetrievalException e) {
            LOGGER.error("Could not retrieve Developer: {}", developerId, e);
            return null;
        }
    }

}
