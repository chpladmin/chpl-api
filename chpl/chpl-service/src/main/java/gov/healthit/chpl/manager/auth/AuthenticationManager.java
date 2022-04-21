package gov.healthit.chpl.manager.auth;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.auth.ChplAccountEmailNotConfirmedException;
import gov.healthit.chpl.auth.ChplAccountStatusException;
import gov.healthit.chpl.auth.jwt.JWTAuthor;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.dao.auth.UserDAO;
import gov.healthit.chpl.domain.auth.LoginCredentials;
import gov.healthit.chpl.domain.auth.UserInvitation;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.exception.JWTCreationException;
import gov.healthit.chpl.exception.MultipleUserAccountsException;
import gov.healthit.chpl.exception.UserManagementException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.manager.InvitationManager;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class AuthenticationManager {
    private JWTAuthor jwtAuthor;
    private UserManager userManager;
    private UserDAO userDAO;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private UserDetailsChecker userDetailsChecker;
    private ErrorMessageUtil msgUtil;
    private InvitationManager invitationManager;
    private Long confirmationWindowInDays;

    @Autowired
    public AuthenticationManager(JWTAuthor jwtAuthor, UserManager userManager, UserDAO userDAO,
            BCryptPasswordEncoder bCryptPasswordEncoder,
            @Qualifier("chplAccountStatusChecker") UserDetailsChecker userDetailsChecker,
            ErrorMessageUtil msgUtil, InvitationManager invitationManager,
            @Value("${resendConfirmationEmailWindowInDays}") Long confirmationWindowInDays) {
        this.jwtAuthor = jwtAuthor;
        this.userManager = userManager;
        this.userDAO = userDAO;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.userDetailsChecker = userDetailsChecker;
        this.msgUtil = msgUtil;
        this.invitationManager = invitationManager;
        this.confirmationWindowInDays = confirmationWindowInDays;
    }

    @Transactional
    public String authenticate(LoginCredentials credentials)
            throws JWTCreationException, UserRetrievalException, MultipleUserAccountsException, ChplAccountEmailNotConfirmedException {
        try {
            UserDTO user = getUser(credentials);
            if (user != null && user.isPasswordResetRequired()) {
                throw new UserRetrievalException(msgUtil.getMessage("auth.changePasswordRequired"));
            } else if (user != null) {
                String jwt = getJWT(credentials);
                logWhenUsername(credentials, user);
                return jwt;
            }
            return null;
        } catch (ChplAccountEmailNotConfirmedException e) {
            if (!isConfirmationPeriodExpired(credentials)) {
                invitationManager.resendConfirmAddressEmailToUser(
                        userManager.getByNameOrEmailUnsecured(credentials.getUserName()).getId());
                throw e;
            } else {
                throw new ChplAccountStatusException(msgUtil.getMessage("auth.loginNotAllowed"));
            }
        }
    }

    private Boolean isConfirmationPeriodExpired(LoginCredentials credentials) {
        try {
            UserDTO user = userDAO.getByNameOrEmail(credentials.getUserName());
            UserInvitation invitation = invitationManager.getByCreatedUserId(user.getId());
            LocalDateTime invitationDate = LocalDateTime.ofInstant(invitation.getCreationDate().toInstant(), ZoneId.systemDefault());
            return Duration.between(invitationDate, LocalDateTime.now()).toDays() > confirmationWindowInDays - 1;
        } catch (Exception e) {
            return true;
        }
    }

    public UserDTO getUser(LoginCredentials credentials)
            throws AccountStatusException, UserRetrievalException,
            MultipleUserAccountsException, ChplAccountEmailNotConfirmedException {

        UserDTO user = getUserByNameOrEmail(credentials.getUserName());
        if (user != null) {
            if (user.getId() < 0) {
                throw new ChplAccountStatusException(msgUtil.getMessage("auth.loginNotAllowed"));
            }
            if (checkPassword(credentials.getPassword(), userManager.getEncodedPassword(user))) {
                if (user.getSignatureDate() == null) {
                    throw new ChplAccountEmailNotConfirmedException(msgUtil.getMessage("auth.accountNotConfirmed"), credentials.getUserName());
                }

                userDetailsChecker.check(user);
                userManager.updateLastLoggedInDate(user);

                // if login was successful reset failed logins to 0
                if (user.getFailedLoginCount() > 0) {
                    try {
                        user.setFailedLoginCount(0);
                        updateFailedLogins(user);
                    } catch (UserManagementException ex) {
                        LOGGER.error("Error adding failed login", ex);
                    }
                }
                return user;
            } else {
                try {
                    user.setFailedLoginCount(user.getFailedLoginCount() + 1);
                    updateFailedLogins(user);
                } catch (UserManagementException ex) {
                    LOGGER.error("Error adding failed login", ex);
                }
                return null;
            }
        } else {
            throw new ChplAccountStatusException(msgUtil.getMessage("auth.loginNotAllowed"));
        }
    }

    private boolean checkPassword(String rawPassword, String encodedPassword) {
        return bCryptPasswordEncoder.matches(rawPassword, encodedPassword);
    }

    public String getJWT(UserDTO user) throws JWTCreationException {
        String jwt = null;

        Map<String, String> stringClaims = new HashMap<String, String>();
        stringClaims.put("Authority", user.getPermission().getAuthority());

        Map<String, List<String>> listClaims = new HashMap<String, List<String>>();
        List<String> identity = new ArrayList<String>();
        identity.add(user.getId().toString());
        identity.add(user.getEmail());
        identity.add(user.getFullName());
        if (user.getImpersonatedBy() != null) {
            identity.add(user.getImpersonatedBy().getId().toString());
            identity.add(user.getImpersonatedBy().getEmail());
        }
        listClaims.put("Identity", identity);

        jwt = jwtAuthor.createJWT(user, stringClaims, listClaims);
        return jwt;
    }

    public String refreshJWT() throws JWTCreationException, UserRetrievalException, MultipleUserAccountsException {
        JWTAuthenticatedUser user = (JWTAuthenticatedUser) AuthUtil.getCurrentUser();

        if (user != null) {
            UserDTO userDto = getUserByNameOrEmail(user.getEmail());
            if (user.getImpersonatingUser() != null) {
                userDto.setImpersonatedBy(user.getImpersonatingUser());
            }
            return getJWT(userDto);
        } else {
            throw new JWTCreationException("Cannot generate token for Anonymous user.");
        }
    }

    @Transactional
    public String getJWT(LoginCredentials credentials) throws JWTCreationException {
        String jwt = null;
        UserDTO user = null;

        try {
            user = getUser(credentials);
        } catch (AccountStatusException e1) {
            throw new JWTCreationException(e1.getMessage());
        } catch (UserRetrievalException | MultipleUserAccountsException | ChplAccountEmailNotConfirmedException e2) {
            throw new JWTCreationException(e2.getMessage());
        }

        if (user != null) {
            jwt = getJWT(user);
        } else {
            throw new ChplAccountStatusException(msgUtil.getMessage("auth.loginNotAllowed"));
        }

        return jwt;

    }

    private UserDTO getUserByNameOrEmail(String usernameOrEmail)
            throws MultipleUserAccountsException, UserRetrievalException {
        UserDTO user = userDAO.getByNameOrEmail(usernameOrEmail);
        return user;
    }

    private UserDTO getUserById(Long id)
            throws MultipleUserAccountsException, UserRetrievalException {
        UserDTO user = userDAO.getById(id);
        return user;
    }

    private void updateFailedLogins(UserDTO userToUpdate) throws UserRetrievalException, UserManagementException {
        try {
            userManager.updateFailedLoginCount(userToUpdate);
        } catch (Exception ex) {
            throw new UserManagementException(
                    "Error increasing the failed login count for user " + userToUpdate.getEmail(), ex);
        }
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).USER_PERMISSIONS, "
            + "T(gov.healthit.chpl.permissions.domains.SecuredUserDomainPermissions).IMPERSONATE_USER, #id)")
    public String impersonateUser(Long id)
            throws UserRetrievalException, JWTCreationException, UserManagementException, MultipleUserAccountsException {
        JWTAuthenticatedUser user = (JWTAuthenticatedUser) AuthUtil.getCurrentUser();
        if (user.getImpersonatingUser() != null) {
            throw new UserManagementException(msgUtil.getMessage("user.impersonate.alreadyImpersonating"));
        }
        UserDTO impersonatingUser = getUserById(user.getId());
        UserDTO impersonatedUser = getUserById(id);

        impersonatedUser.setImpersonatedBy(impersonatingUser);
        return getJWT(impersonatedUser);
    }

    public String unimpersonateUser(User user) throws JWTCreationException, UserRetrievalException,
    MultipleUserAccountsException {
        return getJWT(getUserByNameOrEmail(user.getSubjectName()));
    }

    private void logWhenUsername(LoginCredentials creds, UserDTO user) {
        if (!creds.getUserName().equals(user.getEmail())) {
            LOGGER.warn(String.format("The user with email %s logged in using the username: %s ", user.getEmail(), creds.getUserName()));
        }
    }
}
