package gov.healthit.chpl.auth.authentication;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gov.healthit.chpl.auth.jwt.JWTConsumer;
import gov.healthit.chpl.auth.jwt.JWTValidationException;
import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.auth.user.User;

@Service
public class JWTUserConverterImpl implements JWTUserConverter {

    @Autowired
    private JWTConsumer jwtConsumer;

    public JWTUserConverterImpl() {}

    public User getAuthenticatedUser(final String jwt) throws JWTValidationException {

        JWTAuthenticatedUser user = new JWTAuthenticatedUser();
        user.setAuthenticated(true);

        Map<String, Object> validatedClaims = jwtConsumer.consume(jwt);

        if (validatedClaims == null) {
            throw new JWTValidationException("Invalid authentication token.");
        } else {

            /*
             * Handle the standard claim types. These won't be lists of Strings,
             * which we'll be expecting from the claims we are creating ourselves
             */
            Object issuer = validatedClaims.remove("iss");
            Object audience = validatedClaims.remove("aud");
            Object issuedAt = validatedClaims.remove("iat");
            Object notBefore = validatedClaims.remove("nbf");
            Object expires = validatedClaims.remove("exp");
            Object jti = validatedClaims.remove("jti");
            Object typ = validatedClaims.remove("typ");

            String subject = (String) validatedClaims.remove("sub");

            user.setSubjectName(subject);

            List<String> authorities = (List<String>) validatedClaims.get("Authorities");
            List<String> identityInfo = (List<String>) validatedClaims.get("Identity");

            for (String claim: authorities) {
                GrantedPermission permission = new GrantedPermission(claim);
                user.addPermission(permission);
            }

            Long userId = Long.valueOf(identityInfo.get(0));
            String fullName = identityInfo.get(2);

            user.setId(userId);
            user.setFullName(fullName);
        }
        return user;
    }
}
