package gov.healthit.chpl.user.cognito;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.CognitoSecretHash;
import gov.healthit.chpl.PasswordUtil;
import gov.healthit.chpl.caching.CognitoUserCacheRefresh;
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
import gov.healthit.chpl.sharedstore.user.RemoveUserBy;
import gov.healthit.chpl.sharedstore.user.SharedUserStoreProvider;
import gov.healthit.chpl.sharedstore.user.UserStoreRemove;
import gov.healthit.chpl.user.cognito.authentication.CognitoAuthenticationChallenge;
import gov.healthit.chpl.user.cognito.authentication.CognitoAuthenticationChallengeException;
import lombok.extern.log4j.Log4j2;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminAddUserToGroupRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminDeleteUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminDisableUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminEnableUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminListGroupsForUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminListGroupsForUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminRespondToAuthChallengeRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminRespondToAuthChallengeResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminSetUserPasswordRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminUpdateUserAttributesRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminUserGlobalSignOutRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthenticationResultType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ChallengeNameType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.GroupType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUsersInGroupRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUsersInGroupResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.MessageActionType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserType;

@Log4j2
@Component
public class CognitoApiWrapper {
    private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";
    public static final String ORGANIZATIONS_ATTRIBUTE_NAME = "custom:organizations";
    public static final String ROLES_ATTRIBUTE_NAME = "custom:roles";
    public static final String FORCE_PASSWORD_RESET_ATTRIBUTE_NAME = "custom:forcePasswordReset";

    private String clientId;
    private String userPoolId;
    private String userPoolClientSecret;
    private String environmentGroupName;
    private CognitoIdentityProviderClient cognitoClient;
    private CertificationBodyDAO certificationBodyDAO;
    private DeveloperDAO developerDAO;
    private CertificationBodyDAO acbDao;
    private SharedUserStoreProvider sharedUserStoreProvider;

    @Autowired
    public CognitoApiWrapper(@Value("${cognito.accessKey}") String accessKey,
            @Value("${cognito.secretKey}") String secretKey,
            @Value("${cognito.region}") String region,
            @Value("${cognito.clientId}") String clientId,
            @Value("${cognito.userPoolId}") String userPoolId,
            @Value("${cognito.userPoolClientSecret}") String userPoolClientSecret,
            @Value("${cognito.environment.groupName}") String environmentGroupName,
            CertificationBodyDAO certificationBodyDAO,
            DeveloperDAO developerDAO,
            CertificationBodyDAO acbDao,
            SharedUserStoreProvider sharedUserStoreProvider) {

        cognitoClient = createCognitoClient(accessKey, secretKey, region);
        this.clientId = clientId;
        this.userPoolId = userPoolId;
        this.environmentGroupName = environmentGroupName;
        this.userPoolClientSecret = userPoolClientSecret;
        this.certificationBodyDAO = certificationBodyDAO;
        this.developerDAO = developerDAO;
        this.acbDao = acbDao;
        this.sharedUserStoreProvider = sharedUserStoreProvider;
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
            LOGGER.error("Authentication error for user {}: {}", credentials.getUserName(), e.getMessage(), e);
            return null;
        }
    }

    public AuthenticationResultType refreshToken(String refreshToken, UUID cognitoId) {
        Map<String, String> authParams = new LinkedHashMap<String, String>();
        authParams.put("REFRESH_TOKEN", refreshToken);
        authParams.put("SECRET_HASH", calculateSecretHash(cognitoId.toString()));

        AdminInitiateAuthRequest authRequest = AdminInitiateAuthRequest.builder()
                .authFlow(AuthFlowType.REFRESH_TOKEN_AUTH)
                .userPoolId(userPoolId)
                .clientId(clientId)
                .authParameters(authParams)
                .build();

        try {
            AdminInitiateAuthResponse authResult = cognitoClient.adminInitiateAuth(authRequest);
            return authResult.authenticationResult();
        } catch (Exception e) {
            LOGGER.error("Error refreshing token", e);
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
        return sharedUserStoreProvider.get(cognitoId.toString(), () -> {
            try {
                return getUserInfoNoCache(cognitoId);
            } catch (UserRetrievalException e) {
                LOGGER.error(e);
                return null;
            }
        });
    }

    public User getUserInfoNoCache(UUID cognitoId) throws UserRetrievalException {
        return getUserInfoNoCache(cognitoId.toString());
    }

    //username can be email or cognito guid
    public User getUserInfoNoCache(String username) throws UserRetrievalException {
        AdminGetUserRequest request = AdminGetUserRequest.builder()
                .userPoolId(userPoolId)
                .username(username)
                .build();

        AdminGetUserResponse response = cognitoClient.adminGetUser(request);
        if (response == null || response.sdkHttpResponse() == null || !response.sdkHttpResponse().isSuccessful()) {
            return null;
        }

        User user = createUserFromGetUserResponse(response);
        List<GroupType> groupsForUser = getGroupsForUser(user.getEmail());
        if (!doesGroupMatchCurrentEnvironment(groupsForUser)) {
            return null;
        }
        return user;
    }

    public CognitoCredentials createUser(CreateUserRequest userRequest, String roleName) throws UserCreationException {
        try {
            String tempPassword = PasswordUtil.generatePassword();

            AdminCreateUserRequest request = AdminCreateUserRequest.builder()
                    .userPoolId(userPoolId)
                    .username(userRequest.getEmail())
                    .userAttributes(
                            AttributeType.builder().name("name").value(userRequest.getFullName()).build(),
                            AttributeType.builder().name("email").value(userRequest.getEmail()).build(),
                            AttributeType.builder().name(ROLES_ATTRIBUTE_NAME).value(roleName != null ? roleName : "").build(),
                            AttributeType.builder().name(ORGANIZATIONS_ATTRIBUTE_NAME).value(
                                    userRequest.getOrganizationId() != null ? userRequest.getOrganizationId().toString() : "").build())
                    .temporaryPassword(tempPassword)
                    .messageAction(MessageActionType.SUPPRESS)
                    .build();

            AdminCreateUserResponse response = cognitoClient.adminCreateUser(request);
            //get and cache the newly created user
            getUserInfo(UUID.fromString(response.user().username()));

            return CognitoCredentials.builder()
                    .cognitoId(UUID.fromString(response.user().username()))
                    .userName(userRequest.getEmail())
                    .password(tempPassword)
                    .build();
        } catch (Exception e) {
            throw new UserCreationException(String.format("Error creating user with email %s in store.", userRequest.getEmail()), e);
        }
    }

    @CognitoUserCacheRefresh
    @UserStoreRemove(removeBy = RemoveUserBy.USER_ID, id = "#existingUser.cognitoId.toString()")
    public CognitoCredentials reenableUser(User existingUser) throws UserCreationException {
        try {
            enableUser(existingUser);
            updateUser(existingUser);

            String tempPassword = PasswordUtil.generatePassword();
            setUserPassword(existingUser.getEmail(), tempPassword, false);

            return CognitoCredentials.builder()
                    .cognitoId(existingUser.getCognitoId())
                    .userName(existingUser.getEmail())
                    .password(tempPassword)
                    .build();
        } catch (Exception e) {
            throw new UserCreationException(String.format("Error re-enabling user with email %s in store.", existingUser.getEmail()), e);
        }
    }

    @CognitoUserCacheRefresh
    @UserStoreRemove(removeBy = RemoveUserBy.USER_ID, id = "#result.cognitoId.toString()")
    public User setUserPassword(String username, String password, Boolean permanent) {
        AdminSetUserPasswordRequest request = AdminSetUserPasswordRequest.builder()
                .username(username)
                .password(password)
                .permanent(permanent)
                .userPoolId(userPoolId)
                .build();

        cognitoClient.adminSetUserPassword(request);

        if (permanent) {
            try {
                User user = getUserInfoNoCache(username);
                user.setPasswordResetRequired(false);
                updateUser(user);
            } catch (UserRetrievalException e) {
                LOGGER.error("Could not retrieve user: {}", username, e);
            }
        }

        User user = null;
        try {
            user = getUserInfoNoCache(username);
        } catch (UserRetrievalException e) {
            LOGGER.error("Could not retrieve user: {}", username, e);
        }
        return user;
    }

    @CognitoUserCacheRefresh
    @UserStoreRemove(removeBy = RemoveUserBy.USER_ID, id = "#result.cognitoId.toString()")
    public User addUserToGroup(String username, String groupName) {
        AdminAddUserToGroupRequest request = AdminAddUserToGroupRequest.builder()
                .userPoolId(userPoolId)
                .groupName(groupName)
                .username(username)
                .build();

        cognitoClient.adminAddUserToGroup(request);

        User user = null;
        try {
            user = getUserInfoNoCache(username);
        } catch (UserRetrievalException e) {
            LOGGER.error("Could not retrieve user: {}", username, e);
        }
        return user;
    }

    @CognitoUserCacheRefresh
    @UserStoreRemove(removeBy = RemoveUserBy.USER_ID, id = "#cognitoId.toString()")
    public User deleteUser(UUID cognitoId) {
        try {
            User user = getUserInfo(cognitoId);
            AdminDeleteUserRequest request = AdminDeleteUserRequest.builder()
                    .userPoolId(userPoolId)
                    .username(cognitoId.toString())
                    .build();
            cognitoClient.adminDeleteUser(request);
            return user;
        } catch (Exception e) {
            return null;
        }
    }

    public List<User> getAllUsers() {
        return sharedUserStoreProvider.getAll();
    }

    //The main method that is going to be used to populate the shared user store
    //The shared user store in any environment will contain all of the users in their most current
    //state that have access to that environment.
    public List<User> getAllUsersNoCache() {
        List<Developer> allDevIdsAndNames = developerDAO.findAllIdsAndNames();
        List<CertificationBody> allAcbs = acbDao.findAll();

        ListUsersInGroupRequest request = ListUsersInGroupRequest.builder()
                .userPoolId(userPoolId)
                .groupName(environmentGroupName)
                .build();

        List<User> users = new ArrayList<User>();

        ListUsersInGroupResponse response = cognitoClient.listUsersInGroup(request);
        users.addAll(response.users().stream()
                .map(userType -> createUserFromUserType(userType, allDevIdsAndNames, allAcbs))
                .toList());

        while (response.nextToken() != null) {
            request = ListUsersInGroupRequest.builder()
                .userPoolId(userPoolId)
                .groupName(environmentGroupName)
                .nextToken(response.nextToken())
                .build();

            response = cognitoClient.listUsersInGroup(request);

            users.addAll(response.users().stream()
                    .map(userType -> createUserFromUserType(userType, allDevIdsAndNames, allAcbs))
                    .toList());
        }

        return users.stream()
                .filter(currUser -> currUser.getAccountEnabled())
                .collect(Collectors.toList());
    }

    public void invalidateTokensForUser(String email) {
        AdminUserGlobalSignOutRequest request = AdminUserGlobalSignOutRequest.builder()
                .userPoolId(userPoolId)
                .username(email)
                .build();
        cognitoClient.adminUserGlobalSignOut(request);
    }

    @CognitoUserCacheRefresh
    @UserStoreRemove(removeBy = RemoveUserBy.USER_ID, id = "#user.cognitoId.toString()")
    public void updateUser(User user) throws UserRetrievalException {
        List<AttributeType> attributes = new ArrayList<AttributeType>();
        attributes.add(AttributeType.builder().name("name").value(user.getFullName()).build());
        attributes.add(AttributeType.builder().name("email_verified").value("true").build());
        attributes.add(AttributeType.builder().name(FORCE_PASSWORD_RESET_ATTRIBUTE_NAME).value(user.getPasswordResetRequired() ? "1" : "0").build());

        AdminUpdateUserAttributesRequest request = AdminUpdateUserAttributesRequest.builder()
                .userPoolId(userPoolId)
                .username(user.getCognitoId().toString())
                .userAttributes(attributes)
                .build();

        cognitoClient.adminUpdateUserAttributes(request);
    }

    @CognitoUserCacheRefresh
    @UserStoreRemove(removeBy = RemoveUserBy.USER_ID, id = "#user.cognitoId.toString()")
    public void addOrgToUser(User user, Long orgId) throws UserRetrievalException {
        List<AttributeType> attributes = new ArrayList<AttributeType>();
        Set<Long> orgIds = CollectionUtils.isEmpty(user.getOrganizations())
                ? new HashSet<Long>()
                : user.getOrganizations().stream()
                        .map(org -> org.getId())
                        .collect(Collectors.toSet());
        orgIds.add(orgId);

        attributes.add(AttributeType.builder().name(ORGANIZATIONS_ATTRIBUTE_NAME).value(
                orgIds.stream()
                        .map(o -> o.toString())
                        .collect(Collectors.joining(",")))
                .build());

        AdminUpdateUserAttributesRequest request = AdminUpdateUserAttributesRequest.builder()
                .userPoolId(userPoolId)
                .username(user.getCognitoId().toString())
                .userAttributes(attributes)
                .build();

        cognitoClient.adminUpdateUserAttributes(request);
    }

    @CognitoUserCacheRefresh
    @UserStoreRemove(removeBy = RemoveUserBy.USER_ID, id = "#user.cognitoId.toString()")
    public void removeOrgsFromUser(User user, List<Long> orgIdsToRemove) throws UserRetrievalException {
        Set<Long> orgIds = CollectionUtils.isEmpty(user.getOrganizations())
                ? new HashSet<Long>()
                : user.getOrganizations().stream()
                        .map(org -> org.getId())
                        .collect(Collectors.toSet());
        orgIdsToRemove.stream()
            .forEach(orgIdToRemove -> orgIds.remove(orgIdToRemove));

        List<AttributeType> attributes = new ArrayList<AttributeType>();
        attributes.add(AttributeType.builder().name(ORGANIZATIONS_ATTRIBUTE_NAME).value(
                orgIds.stream()
                        .map(o -> o.toString())
                        .collect(Collectors.joining(",")))
                .build());

        AdminUpdateUserAttributesRequest request = AdminUpdateUserAttributesRequest.builder()
                .userPoolId(userPoolId)
                .username(user.getCognitoId().toString())
                .userAttributes(attributes)
                .build();

        cognitoClient.adminUpdateUserAttributes(request);
    }

    @CognitoUserCacheRefresh
    @UserStoreRemove(removeBy = RemoveUserBy.USER_ID, id = "#user.cognitoId.toString()")
    public void enableUser(User user) {
        //If a user is getting enabled, it's because they were at one time disabled
        //and our workflow is that they can only become re-enabled by receiving a new invitation.
        //As part of the new invitation process, we want to force the user to reset their password as well.
        AdminEnableUserRequest enableUserRequest = AdminEnableUserRequest.builder()
                .userPoolId(userPoolId)
                .username(user.getCognitoId().toString())
                .build();
        cognitoClient.adminEnableUser(enableUserRequest);
    }

    @CognitoUserCacheRefresh
    @UserStoreRemove(removeBy = RemoveUserBy.USER_ID, id = "#user.cognitoId.toString()")
    public void disableUser(User user) {
        AdminDisableUserRequest request = AdminDisableUserRequest.builder()
                .userPoolId(userPoolId)
                .username(user.getCognitoId().toString())
                .build();
        cognitoClient.adminDisableUser(request);
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
                .filter(acb -> acb != null)
                .map(acb -> new Organization(acb.getId(), acb.getName()))
                .toList();
    }

    private CertificationBody getCertificationBody(Long certificationBodyId) {
        try {
            return certificationBodyDAO.getById(certificationBodyId);
        } catch (EntityRetrievalException e) {
            LOGGER.error("A user exists with reference to ACB organization {} which doees not exist.", certificationBodyId, e);
            return null;
        }
    }

    private List<Organization> getDeveloperOrganizations(String role, List<Long> orgIds) {
        return orgIds.stream()
                .map(developerId -> getDeveloper(developerId))
                .filter(dev -> dev != null)
                .map(dev -> new Organization(dev.getId(), dev.getName()))
                .toList();
    }

    private Developer getDeveloper(Long developerId) {
        try {
            return developerDAO.getSimpleDeveloperById(developerId, false);
        } catch (EntityRetrievalException e) {
            LOGGER.error("A user exists with reference to developer organization {} which doees not exist.", developerId);
            return null;
        }
    }

    private User createUserFromUserType(UserType userType, List<Developer> developers, List<CertificationBody> acbs) {
        User user = new User();
        user.setCognitoId(UUID.fromString(userType.username()));
        user.setSubjectName(getUserAttribute(userType.attributes(), "email").value());
        user.setFullName(getUserAttribute(userType.attributes(), "name").value());
        user.setEmail(getUserAttribute(userType.attributes(), "email").value());
        user.setAccountEnabled(userType.enabled());
        user.setStatus(userType.userStatusAsString());
        user.setPasswordResetRequired(getForcePasswordReset(userType.attributes()));
        user.setRole(getRole(userType.attributes()));

        AttributeType orgIdsAttribute = getUserAttribute(userType.attributes(), ORGANIZATIONS_ATTRIBUTE_NAME);
        if (orgIdsAttribute != null && StringUtils.isNotEmpty(orgIdsAttribute.value())) {
            List<Long> organizationIds = Stream.of(orgIdsAttribute.value().split(",")).map(Long::valueOf).toList();
            if (user.getRole().equalsIgnoreCase(CognitoGroups.CHPL_ACB)) {
                user.setOrganizations(acbs.stream()
                        .filter(acb -> organizationIds.contains(acb.getId()))
                        .map(acb -> new Organization(acb.getId(), acb.getName()))
                        .collect(Collectors.toList()));
            } else if (user.getRole().equalsIgnoreCase(CognitoGroups.CHPL_DEVELOPER)) {
                user.setOrganizations(developers.stream()
                        .filter(dev -> organizationIds.contains(dev.getId()))
                        .map(dev -> new Organization(dev.getId(), dev.getName()))
                        .collect(Collectors.toList()));
            }
        }
        return user;
    }

    private User createUserFromGetUserResponse(AdminGetUserResponse response) {
        User user = new User();
        user.setCognitoId(UUID.fromString(getUserAttribute(response.userAttributes(), "sub").value()));
        user.setSubjectName(getUserAttribute(response.userAttributes(), "email").value());
        user.setFullName(getUserAttribute(response.userAttributes(), "name").value());
        user.setEmail(getUserAttribute(response.userAttributes(), "email").value());
        user.setAccountEnabled(response.enabled());
        user.setStatus(response.userStatusAsString());
        user.setPasswordResetRequired(getForcePasswordReset(response.userAttributes()));
        user.setRole(getRole(response.userAttributes()));
        AttributeType orgIdsAttribute = getUserAttribute(response.userAttributes(), ORGANIZATIONS_ATTRIBUTE_NAME);
        if (orgIdsAttribute != null && StringUtils.isNotEmpty(orgIdsAttribute.value())) {
            user.setOrganizations(getOrganizations(user.getRole(), Stream.of(orgIdsAttribute.value().split(","))
                .map(Long::valueOf)
                .toList()));
        }
        return user;
    }

    private Boolean getForcePasswordReset(List<AttributeType> attributes) {
        String forcePasswordReset = getUserAttribute(attributes, FORCE_PASSWORD_RESET_ATTRIBUTE_NAME).value();
        if (!StringUtils.isEmpty(forcePasswordReset)) {
            return forcePasswordReset.equals("1");
        }
        return false;
    }

    private String getRole(List<AttributeType> attributes) {
        String role = null;
        String delimitedRoleNames = getUserAttribute(attributes, ROLES_ATTRIBUTE_NAME).value();
        if (delimitedRoleNames != null && StringUtils.isNotEmpty(delimitedRoleNames)) {
            role = Stream.of(delimitedRoleNames.split(",")).toList().get(0);
        }
        return role;
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
