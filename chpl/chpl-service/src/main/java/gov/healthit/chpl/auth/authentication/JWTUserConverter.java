package gov.healthit.chpl.auth.authentication;

import java.util.List;
import java.util.Map;

import org.jose4j.jwt.consumer.InvalidJwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gov.healthit.chpl.auth.jwt.JWTConsumer;
import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.dao.auth.UserDAO;
import gov.healthit.chpl.exception.JWTValidationException;
import gov.healthit.chpl.exception.MultipleUserAccountsException;
import gov.healthit.chpl.exception.UserRetrievalException;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class JWTUserConverter {

    private static final int USER_ID = 0;
    private static final int FULL_NAME = 2;
    private static final int IMPERSONATING_USER_ID = 3;
    private static final int IMPERSONATING_USER_EMAIL = 4;
    private static final int FIELDS_WHEN_NOT_IMPERSONATING = 3;

    private JWTConsumer jwtConsumer;
    private UserDAO userDAO;

    @Autowired
    public JWTUserConverter(JWTConsumer jwtConsumer, UserDAO userDAO) {
        this.jwtConsumer = jwtConsumer;
        this.userDAO = userDAO;
    }

    public User getAuthenticatedUser(String jwt) throws JWTValidationException, MultipleUserAccountsException {

        JWTAuthenticatedUser user = new JWTAuthenticatedUser();
        user.setAuthenticated(true);

        Map<String, Object> validatedClaims;
        try {
            validatedClaims = jwtConsumer.consume(jwt);
        } catch (InvalidJwtException e) {
            throw new JWTValidationException(e.getMessage());
        }

        if (validatedClaims == null) {
            throw new JWTValidationException("Invalid authentication token.");
        } else {
            String subject = (String) validatedClaims.remove("sub");
            user.setSubjectName(subject);

            String role = (String) validatedClaims.get("Authority");
            GrantedPermission permission = new GrantedPermission(role);
            user.addPermission(permission);

            @SuppressWarnings("unchecked") List<String> identityInfo = (List<String>) validatedClaims.get("Identity");
            Long userId = Long.valueOf(identityInfo.get(USER_ID));
            String fullName = identityInfo.get(FULL_NAME);
            user.setId(userId);
            user.setFullName(fullName);
            if (identityInfo.size() > FIELDS_WHEN_NOT_IMPERSONATING) {
                String impersonatingEmail = identityInfo.get(IMPERSONATING_USER_EMAIL);
                try {
                    user.setImpersonatingUser(userDAO.getByNameOrEmail(impersonatingEmail));
                } catch (UserRetrievalException e) {
                    LOGGER.error(e.getMessage());
                }
            }
        }
        return user;
    }

    public User getImpersonatingUser(String jwt) throws JWTValidationException {

        JWTAuthenticatedUser user = new JWTAuthenticatedUser();
        user.setAuthenticated(true);

        Map<String, Object> validatedClaims;
        try {
            validatedClaims = jwtConsumer.consume(jwt);
        } catch (InvalidJwtException e) {
            throw new JWTValidationException(e.getMessage());
        }

        if (validatedClaims == null) {
            throw new JWTValidationException("Invalid authentication token.");
        } else {
            String role = (String) validatedClaims.get("Authority");
            GrantedPermission permission = new GrantedPermission(role);
            user.addPermission(permission);

            @SuppressWarnings("unchecked") List<String> identityInfo = (List<String>) validatedClaims.get("Identity");
            Long userId = Long.valueOf(identityInfo.get(IMPERSONATING_USER_ID));
            String subjectName = identityInfo.get(IMPERSONATING_USER_EMAIL);

            user.setId(userId);
            user.setSubjectName(subjectName);
        }
        return user;
    }
}
