package gov.healthit.chpl.auth.authentication;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.dto.UserPermissionDTO;
import gov.healthit.chpl.auth.jwt.JWTAuthor;
import gov.healthit.chpl.auth.jwt.JWTCreationException;
import gov.healthit.chpl.auth.manager.UserManager;
import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.auth.user.UserManagementException;
import gov.healthit.chpl.auth.user.UserRetrievalException;

@Service
public class UserAuthenticator implements Authenticator {
    private static final Logger LOGGER = LogManager.getLogger(UserAuthenticator.class);

    @Autowired
    private JWTAuthor jwtAuthor;

    @Autowired
    protected UserManager userManager;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private UserDetailsChecker userDetailsChecker;

    @Override
    public UserDTO getUser(final LoginCredentials credentials)
            throws BadCredentialsException, AccountStatusException, UserRetrievalException {
        UserDTO user = getUserByName(credentials.getUserName());

        if (user != null) {
            if (user.getSignatureDate() == null) {
                throw new BadCredentialsException(
                        "Account for user " + user.getSubjectName() + " has not been confirmed.");
            }

            if (checkPassword(credentials.getPassword(), userManager.getEncodedPassword(user))) {

                try {
                    userDetailsChecker.check(user);

                    // if login was successful reset failed logins to 0
                    if (user.getFailedLoginCount() > 0) {
                        try {
                            user.setFailedLoginCount(0);
                            updateFailedLogins(user);
                        } catch (UserManagementException ex) {
                            LOGGER.error("Error adding failed login", ex);
                        }
                    }
                } catch (AccountStatusException ex) {
                    throw ex;
                }
                return user;

            } else {
                try {
                    user.setFailedLoginCount(user.getFailedLoginCount() + 1);
                    updateFailedLogins(user);
                } catch (UserManagementException ex) {
                    LOGGER.error("Error adding failed login", ex);
                }
                throw new BadCredentialsException("Bad username and password combination.");
            }
        } else {
            throw new BadCredentialsException("There is no CHPL user with name " + credentials.getUserName());
        }
    }

    protected boolean checkPassword(final String rawPassword, final String encodedPassword) {
        return bCryptPasswordEncoder.matches(rawPassword, encodedPassword);
    }

    @Override
    public String getJWT(final UserDTO user) throws JWTCreationException {

        String jwt = null;
        Map<String, List<String>> claims = new HashMap<String, List<String>>();
        List<String> claimStrings = new ArrayList<String>();

        Set<UserPermissionDTO> permissions = getUserPermissions(user);

        for (UserPermissionDTO claim : permissions) {
            claimStrings.add(claim.getAuthority());
        }
        claims.put("Authorities", claimStrings);

        List<String> identity = new ArrayList<String>();

        identity.add(user.getId().toString());
        identity.add(user.getUsername());
        identity.add(user.getFullName());

        claims.put("Identity", identity);

        jwt = jwtAuthor.createJWT(user.getSubjectName(), claims);
        return jwt;

    }

    @Override
    public String refreshJWT() throws JWTCreationException {

        User user = Util.getCurrentUser();
        String jwt = null;

        if (user != null) {
            Map<String, List<String>> claims = new HashMap<String, List<String>>();
            List<String> claimStrings = new ArrayList<String>();

            Set<GrantedPermission> permissions = user.getPermissions();

            for (GrantedPermission claim : permissions) {
                claimStrings.add(claim.getAuthority());
            }
            claims.put("Authorities", claimStrings);

            List<String> identity = new ArrayList<String>();

            identity.add(user.getId().toString());
            identity.add(user.getUsername());
            identity.add(user.getFullName());

            claims.put("Identity", identity);

            jwt = jwtAuthor.createJWT(user.getSubjectName(), claims);
        } else {
            throw new JWTCreationException("Cannot generate token for Anonymous user.");
        }
        return jwt;
    }

    @Override
    @Transactional
    public String getJWT(final LoginCredentials credentials) throws JWTCreationException {

        String jwt = null;
        UserDTO user = null;

        try {
            user = getUser(credentials);
        } catch (AccountStatusException e1) {
            throw new JWTCreationException(e1.getMessage());
        } catch (UserRetrievalException e2) {
            throw new JWTCreationException(e2.getMessage());
        }

        if (user != null) {
            jwt = getJWT(user);
        }

        return jwt;

    }

    private Set<UserPermissionDTO> getUserPermissions(final UserDTO user) {

        Authentication authenticator = new Authentication() {

            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                List<GrantedAuthority> auths = new ArrayList<GrantedAuthority>();
                auths.add(new GrantedPermission("ROLE_USER_AUTHENTICATOR"));
                return auths;
            }

            @Override
            public Object getCredentials() {
                return null;
            }

            @Override
            public Object getDetails() {
                return null;
            }

            @Override
            public Object getPrincipal() {
                return null;
            }

            @Override
            public boolean isAuthenticated() {
                return true;
            }

            @Override
            public void setAuthenticated(final boolean arg0) throws IllegalArgumentException {
            }

            @Override
            public String getName() {
                return "AUTHENTICATOR";
            }

        };
        SecurityContextHolder.getContext().setAuthentication(authenticator);
        try {
            Set<UserPermissionDTO> permissions = userManager.getGrantedPermissionsForUser(user);
            return permissions;
        } finally {
            SecurityContextHolder.getContext().setAuthentication(null);
        }

    }

    private UserDTO getUserByName(final String userName) throws UserRetrievalException {

        Authentication authenticator = new Authentication() {
            private static final long serialVersionUID = 6718133333641942231L;

            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                List<GrantedAuthority> auths = new ArrayList<GrantedAuthority>();
                auths.add(new GrantedPermission("ROLE_USER_AUTHENTICATOR"));
                return auths;
            }

            @Override
            public Object getCredentials() {
                return null;
            }

            @Override
            public Object getDetails() {
                return null;
            }

            @Override
            public Object getPrincipal() {
                return null;
            }

            @Override
            public boolean isAuthenticated() {
                return true;
            }

            @Override
            public void setAuthenticated(final boolean arg0) throws IllegalArgumentException {
            }

            @Override
            public String getName() {
                return "AUTHENTICATOR";
            }

        };

        SecurityContextHolder.getContext().setAuthentication(authenticator);
        try {
            UserDTO user = userManager.getByName(userName);
            return user;
        } finally {
            SecurityContextHolder.getContext().setAuthentication(null);
        }

    }

    private void updateFailedLogins(final UserDTO userToUpdate) throws UserRetrievalException, UserManagementException {

        Authentication authenticator = new Authentication() {

            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                List<GrantedAuthority> auths = new ArrayList<GrantedAuthority>();
                auths.add(new GrantedPermission("ROLE_USER_AUTHENTICATOR"));
                return auths;
            }

            @Override
            public Object getCredentials() {
                return null;
            }

            @Override
            public Object getDetails() {
                return null;
            }

            @Override
            public Object getPrincipal() {
                return null;
            }

            @Override
            public boolean isAuthenticated() {
                return true;
            }

            @Override
            public void setAuthenticated(final boolean arg0) throws IllegalArgumentException {
            }

            @Override
            public String getName() {
                return "AUTHENTICATOR";
            }

        };

        SecurityContextHolder.getContext().setAuthentication(authenticator);
        try {
            userManager.updateFailedLoginCount(userToUpdate);
        } catch (Exception ex) {
            throw new UserManagementException(
                    "Error increasing the failed login count for user " + userToUpdate.getSubjectName(), ex);
        } finally {
            SecurityContextHolder.getContext().setAuthentication(null);
        }
    }

    public JWTAuthor getJwtAuthor() {
        return jwtAuthor;
    }

    public void setJwtAuthor(final JWTAuthor jwtAuthor) {
        this.jwtAuthor = jwtAuthor;
    }

}
