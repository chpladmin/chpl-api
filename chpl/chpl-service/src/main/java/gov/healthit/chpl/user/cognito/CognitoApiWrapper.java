package gov.healthit.chpl.user.cognito;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.CognitoSecretHash;
import gov.healthit.chpl.PasswordGenerator;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.Organization;
import gov.healthit.chpl.domain.auth.CognitoGroups;
import gov.healthit.chpl.domain.auth.CognitoNewPasswordRequiredRequest;
import gov.healthit.chpl.domain.auth.CreateUserRequest;
import gov.healthit.chpl.domain.auth.LoginCredentials;
import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.UserCreationException;
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
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminDeleteUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminListGroupsForUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminListGroupsForUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminRespondToAuthChallengeRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminRespondToAuthChallengeResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminSetUserPasswordRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthenticationResultType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ChallengeNameType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.GroupType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUsersInGroupRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUsersInGroupResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUsersRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUsersResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.MessageActionType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserStatusType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserType;

@Log4j2
@Component
public class CognitoApiWrapper {
    private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";

    private String clientId;
    private String userPoolId;
    private String userPoolClientSecret;
    private String environmentGroupName;
    private CognitoIdentityProviderClient cognitoClient;
    private CertificationBodyDAO certificationBodyDAO;
    private DeveloperDAO developerDAO;

    private Map<UUID, User> userMap = new HashMap<UUID, User>();

    @Autowired
    public CognitoApiWrapper(@Value("${cognito.accessKey}") String accessKey, @Value("${cognito.secretKey}") String secretKey,
            @Value("${cognito.region}") String region, @Value("${cognito.clientId}") String clientId, @Value("${cognito.userPoolId}") String userPoolId,
            @Value("${cognito.userPoolClientSecret}") String userPoolClientSecret, @Value("${cognito.environment.groupName}") String environmentGroupName,
            CertificationBodyDAO certificationBodyDAO, DeveloperDAO developerDAO) {

        cognitoClient = createCognitoClient(accessKey, secretKey, region);
        this.clientId = clientId;
        this.userPoolId = userPoolId;
        this.environmentGroupName = environmentGroupName;
        this.userPoolClientSecret = userPoolClientSecret;
        this.certificationBodyDAO = certificationBodyDAO;
        this.developerDAO = developerDAO;

    }

    public AuthenticationResultType authenticate(LoginCredentials credentials) throws CognitoAuthenticationChallengeException {
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
            if (authResult.challengeName() != null
                    && authResult.challengeName().equals(ChallengeNameType.NEW_PASSWORD_REQUIRED)) {
                throw CognitoAuthenticationChallengeException.builder()
                        .challenge(CognitoAuthenticationChallenge.builder()
                                .sessionId(authResult.session())
                                .challenge(authResult.challengeName())
                                .build())
                        .build();
            }
            return  authResult.authenticationResult();
        } catch (CognitoAuthenticationChallengeException e) {
            throw e;
        } catch (Exception e) {
            //This is cluttering the logs when the SSO flag is on, and the user logs in using CHPL creds
            //We might want to uncomment it when we move to only using Cognito creds
            //LOGGER.error("Authentication error: {}", e.getMessage(), e);
            return null;
        }
    }

    public AuthenticationResultType respondToNewPasswordRequiredChallenge(CognitoNewPasswordRequiredRequest newPassworRequiredRequest) {
        AdminRespondToAuthChallengeRequest request = AdminRespondToAuthChallengeRequest.builder()
                .userPoolId(userPoolId)
                .clientId(clientId)
                .challengeName(ChallengeNameType.NEW_PASSWORD_REQUIRED)
                .challengeResponses(Map.of("NEW_PASSWORD", newPassworRequiredRequest.getPassword(),
                        "USERNAME", newPassworRequiredRequest.getUserName(),
                        "SECRET_HASH", calculateSecretHash(newPassworRequiredRequest.getUserName())))
                .session(newPassworRequiredRequest.getSessionId())
                .build();

        try {
            AdminRespondToAuthChallengeResponse response = cognitoClient.adminRespondToAuthChallenge(request);

            if (response.challengeName() != null) {
                LOGGER.error("Received Challenge {} when responding to NEW_PASSWORD_REQUIRED");
                return null;
            }
            return response.authenticationResult();
        } catch (Exception e) {
            LOGGER.error("Error responding to NEW_PASSWORD_REQUIRED challenge: {}", e.getMessage(), e);
            return null;
        }
    }

    public User getUserInfo(UUID cognitoId) throws UserRetrievalException {
        if (!userMap.containsKey(cognitoId)) {
            User user = getUserInfoFromCognito(cognitoId);
            userMap.put(cognitoId, user);
        }
        return userMap.get(cognitoId);
    }

    private User getUserInfoFromCognito(UUID cognitoId) throws UserRetrievalException {
        ListUsersResponse response = cognitoClient.listUsers(ListUsersRequest.builder()
                .userPoolId(userPoolId)
                .filter("sub = \"" + cognitoId.toString() + "\"")
                .limit(1)
                .build());

        if (response.users().size() > 0) {
            String email = getUserAttribute(response.users().get(0).attributes(), "email").value();
            List<GroupType> userGroups = getGroupsForUser(email);
            if (doesGroupMatchCurrentEnvironment(userGroups)) {
                return createUserFromUserType(response.users().get(0));
            }
        }
        return null;
    }

    public User getUserInfo(String email) throws UserRetrievalException {
        ListUsersResponse response = cognitoClient.listUsers(ListUsersRequest.builder()
                .userPoolId(userPoolId)
                .filter("email = \"" + email + "\"")
                .limit(1)
                .build());

        if (response.users().size() > 0) {
            List<GroupType> userGroups = getGroupsForUser(email);
            if (doesGroupMatchCurrentEnvironment(userGroups)) {
                return createUserFromUserType(response.users().get(0));
            }
        }
        return null;
    }

    public CognitoCredentials createUser(CreateUserRequest userRequest) throws UserCreationException {
        try {
            String tempPassword = PasswordGenerator.generate();

            AdminCreateUserRequest request = AdminCreateUserRequest.builder()
                    .userPoolId(userPoolId)
                    .username(userRequest.getEmail())
                    .userAttributes(
                            AttributeType.builder().name("name").value(userRequest.getFullName()).build(),
                            AttributeType.builder().name("email").value(userRequest.getEmail()).build(),
                            AttributeType.builder().name("phone_number").value("+1" + userRequest.getPhoneNumber().replaceAll("[^0-9.]", "")).build(),
                            AttributeType.builder().name("nickname").value("THIS ATTRIBUTE NEEDS TO BE MADE NOT REQUIRED").build(),
                            AttributeType.builder().name("custom:title").value("THIS ATTRIBUTE NEEDS TO BE REMOVED").build(),
                            AttributeType.builder().name("custom:organizations").value(
                                    userRequest.getOrganizationId() != null ? userRequest.getOrganizationId().toString() : "").build())
                    .temporaryPassword(tempPassword)
                    .messageAction(MessageActionType.SUPPRESS)
                    .build();

            AdminCreateUserResponse response = cognitoClient.adminCreateUser(request);

            return CognitoCredentials.builder()
                    .cognitoId(UUID.fromString(response.user().username()))
                    .userName(userRequest.getEmail())
                    .password(tempPassword)
                    .build();
        } catch (Exception e) {
            throw new UserCreationException(String.format("Error creating user with email %s in store.", userRequest.getEmail()), e);
        }
    }

    public void setUserPassword(String userName, String password) {
        AdminSetUserPasswordRequest request = AdminSetUserPasswordRequest.builder()
                .username(userName)
                .password(password)
                .permanent(true)
                .userPoolId(userPoolId)
                .build();

        cognitoClient.adminSetUserPassword(request);
    }

    public void setTemporaryUserPassword(String userName, String password) {
        AdminSetUserPasswordRequest request = AdminSetUserPasswordRequest.builder()
                .username(userName)
                .password(password)
                .permanent(false)
                .userPoolId(userPoolId)
                .build();

        cognitoClient.adminSetUserPassword(request);
    }


    public AdminAddUserToGroupResponse addUserToGroup(String email, String groupName) {
        AdminAddUserToGroupRequest request = AdminAddUserToGroupRequest.builder()
                .userPoolId(userPoolId)
                .groupName(groupName)
                .username(email)
                .build();

        return cognitoClient.adminAddUserToGroup(request);
    }

    public Boolean deleteUser(UUID cognitoId) {
        try {
            AdminDeleteUserRequest request = AdminDeleteUserRequest.builder()
                    .userPoolId(userPoolId)
                    .username(cognitoId.toString())
                    .build();
            cognitoClient.adminDeleteUser(request);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public List<User> getAllUsers() {
        ListUsersInGroupRequest request = ListUsersInGroupRequest.builder()
                .userPoolId(userPoolId)
                .groupName(environmentGroupName)
                .build();

        List<User> users = new ArrayList<User>();

        ListUsersInGroupResponse response = cognitoClient.listUsersInGroup(request);
        users.addAll(response.users().stream()
                .map(userType -> createUserFromUserType(userType))
                .toList());

        while (response.nextToken() != null) {
            request = ListUsersInGroupRequest.builder()
                .userPoolId(userPoolId)
                .groupName(environmentGroupName)
                .nextToken(response.nextToken())
                .build();

            response = cognitoClient.listUsersInGroup(request);

            users.addAll(response.users().stream()
                    .map(userType -> createUserFromUserType(userType))
                    .toList());

        }
        return users;
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
            .orElse(AttributeType.builder().value("").build());

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

    private User createUserFromUserType(UserType userType) {
        User user = new User();
        user.setCognitoId(UUID.fromString(userType.username()));
        user.setSubjectName(getUserAttribute(userType.attributes(), "email").value());
        user.setFullName(getUserAttribute(userType.attributes(), "name").value());
        user.setEmail(getUserAttribute(userType.attributes(), "email").value());
        user.setPhoneNumber(getUserAttribute(userType.attributes(), "phone_number").value());
        user.setAccountLocked(!userType.enabled());
        user.setAccountEnabled(userType.enabled());
        user.setStatus(userType.userStatusAsString());
        user.setPasswordResetRequired(userType.userStatus().equals(UserStatusType.RESET_REQUIRED));
        user.setRole(getRoleBasedOnFilteredGroups(getGroupsForUser(user.getEmail())));

        AttributeType orgIdsAttribute = getUserAttribute(userType.attributes(), "custom:organizations");
        if (orgIdsAttribute != null && StringUtils.isNotEmpty(orgIdsAttribute.value())) {
            user.setOrganizations(getOrganizations(user.getRole(), Stream.of(orgIdsAttribute.value().split(","))
                .map(Long::valueOf)
                .toList()));
        }
        return user;
    }

    private String getRoleBasedOnFilteredGroups(List<GroupType> groups) {
        return groups.stream()
                .map(groupType -> groupType.groupName())
                .filter(group -> !group.endsWith("-env"))
                .findAny()
                .orElseThrow(() -> new RuntimeException("Could not determine user's role"));
    }

    private List<GroupType> getGroupsForUser(String email) {
        AdminListGroupsForUserRequest groupsRequest = AdminListGroupsForUserRequest.builder()
                .userPoolId(userPoolId)
                .username(email)
                .build();
        AdminListGroupsForUserResponse groupsResponse = cognitoClient.adminListGroupsForUser(groupsRequest);
        return groupsResponse.groups();
    }

    private boolean doesGroupMatchCurrentEnvironment(List<GroupType> groups) {
        return groups.stream()
                .filter(grp -> grp.groupName().equals(environmentGroupName))
                .findAny()
                .isPresent();
    }

    private String calculateSecretHash(String userName) {
        SecretKeySpec signingKey = new SecretKeySpec(
                userPoolClientSecret.getBytes(StandardCharsets.UTF_8),
                HMAC_SHA256_ALGORITHM);
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
            mac.init(signingKey);
            mac.update(userName.getBytes(StandardCharsets.UTF_8));
            byte[] rawHmac = mac.doFinal(clientId.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(rawHmac);
        } catch (Exception e) {
            throw new RuntimeException("Error while calculating ");
        }
    }
}
