package gov.healthit.chpl.auth.authentication;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gov.healthit.chpl.auth.jwt.JWTConsumer;
import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.dao.auth.UserDAO;
import gov.healthit.chpl.exception.JWTValidationException;
import gov.healthit.chpl.exception.UserRetrievalException;

@Service
public class JWTUserConverterImpl implements JWTUserConverter {

    private static final int USER_ID = 0;
    private static final int FULL_NAME = 2;
    private static final int IMPERSONATING_USER_ID = 3;
    private static final int IMPERSONATING_USER_SUBJECT_NAME = 4;
    private static final int FIELDS_WHEN_NOT_IMPERSONATING = 3;

    @Autowired
    private JWTConsumer jwtConsumer;

    @Autowired
    private UserDAO userDAO;
    // private UserManager userManager;

    public JWTUserConverterImpl() {
    }

    public User getAuthenticatedUser(final String jwt) throws JWTValidationException {

        JWTAuthenticatedUser user = new JWTAuthenticatedUser();
        user.setAuthenticated(true);

        Map<String, Object> validatedClaims = jwtConsumer.consume(jwt);

        if (validatedClaims == null) {
            throw new JWTValidationException("Invalid authentication token.");
        } else {
            String subject = (String) validatedClaims.remove("sub");
            user.setSubjectName(subject);

            String role = (String) validatedClaims.get("Role");
            GrantedPermission permission = new GrantedPermission(role);
            user.addPermission(permission);

            List<String> identityInfo = (List<String>) validatedClaims.get("Identity");
            Long userId = Long.valueOf(identityInfo.get(USER_ID));
            String fullName = identityInfo.get(FULL_NAME);
            user.setId(userId);
            user.setFullName(fullName);
            if (identityInfo.size() > FIELDS_WHEN_NOT_IMPERSONATING) {
                String impersonatingSubjectName = identityInfo.get(IMPERSONATING_USER_SUBJECT_NAME);
                try {
                    // user.setImpersonatingUser(userManager.getByName(impersonatingSubjectName));
                    user.setImpersonatingUser(userDAO.getByName(impersonatingSubjectName));
                } catch (UserRetrievalException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return user;
    }

    @Override
    public User getImpersonatingUser(final String jwt) throws JWTValidationException {

        JWTAuthenticatedUser user = new JWTAuthenticatedUser();
        user.setAuthenticated(true);

        Map<String, Object> validatedClaims = jwtConsumer.consume(jwt);

        if (validatedClaims == null) {
            throw new JWTValidationException("Invalid authentication token.");
        } else {
            String role = (String) validatedClaims.get("Role");
            GrantedPermission permission = new GrantedPermission(role);
            user.addPermission(permission);

            List<String> identityInfo = (List<String>) validatedClaims.get("Identity");
            Long userId = Long.valueOf(identityInfo.get(IMPERSONATING_USER_ID));
            String subjectName = identityInfo.get(IMPERSONATING_USER_SUBJECT_NAME);

            user.setId(userId);
            user.setSubjectName(subjectName);
        }
        return user;
    }
}
