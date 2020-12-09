package gov.healthit.chpl.manager.auth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.auth.ChplAccountStatusException;
import gov.healthit.chpl.auth.jwt.JWTAuthor;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.dao.auth.UserDAO;
import gov.healthit.chpl.domain.auth.LoginCredentials;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.exception.JWTCreationException;
import gov.healthit.chpl.exception.MultipleUserAccountsException;
import gov.healthit.chpl.exception.UserManagementException;
import gov.healthit.chpl.exception.UserRetrievalException;
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

    @Autowired
    public AuthenticationManager(JWTAuthor jwtAuthor, UserManager userManager, UserDAO userDAO,
            BCryptPasswordEncoder bCryptPasswordEncoder,
            @Qualifier("chplAccountStatusChecker") UserDetailsChecker userDetailsChecker,
            ErrorMessageUtil msgUtil) {
        this.jwtAuthor = jwtAuthor;
        this.userManager = userManager;
        this.userDAO = userDAO;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.userDetailsChecker = userDetailsChecker;
        this.msgUtil = msgUtil;
    }

    public String authenticate(LoginCredentials credentials)
            throws JWTCreationException, UserRetrievalException, MultipleUserAccountsException {

        String jwt = getJWT(credentials);
        UserDTO user = getUser(credentials);
        if (user != null && user.isPasswordResetRequired()) {
            throw new UserRetrievalException(msgUtil.getMessage("auth.changePasswordRequired"));
        }
        return jwt;
    }

    public UserDTO getUser(LoginCredentials credentials)
            throws AccountStatusException, UserRetrievalException, MultipleUserAccountsException {
        UserDTO user = getUserByNameOrEmail(credentials.getUserName());
        if (user != null) {
            if (user.getSignatureDate() == null) {
                throw new ChplAccountStatusException(msgUtil.getMessage("auth.accountNotConfirmed", user.getEmail()));
            }
            if (user.getId() < 0) {
                throw new ChplAccountStatusException(msgUtil.getMessage("auth.loginNotAllowed"));
            }
            if (checkPassword(credentials.getPassword(), userManager.getEncodedPassword(user))) {
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
                throw new ChplAccountStatusException(msgUtil.getMessage("auth.loginNotAllowed"));
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
        } catch (UserRetrievalException | MultipleUserAccountsException e2) {
            throw new JWTCreationException(e2.getMessage());
        }

        if (user != null) {
            jwt = getJWT(user);
        }

        return jwt;

    }

    private UserDTO getUserByName(String userName) throws UserRetrievalException {
        UserDTO user = userDAO.getByName(userName);
        return user;
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

    @Deprecated
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).USER_PERMISSIONS, "
            + "T(gov.healthit.chpl.permissions.domains.SecuredUserDomainPermissions).IMPERSONATE_USER, #username)")
    public String impersonateUserByUsername(String username)
            throws UserRetrievalException, JWTCreationException, UserManagementException {
        JWTAuthenticatedUser user = (JWTAuthenticatedUser) AuthUtil.getCurrentUser();
        if (user.getImpersonatingUser() != null) {
            throw new UserManagementException(msgUtil.getMessage("user.impersonate.alreadyImpersonating"));
        }
        UserDTO impersonatingUser = getUserByName(user.getSubjectName());
        UserDTO impersonatedUser = getUserByName(username);

        impersonatedUser.setImpersonatedBy(impersonatingUser);
        return getJWT(impersonatedUser);
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
}
