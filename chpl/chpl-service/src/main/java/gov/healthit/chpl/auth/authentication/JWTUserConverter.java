package gov.healthit.chpl.auth.authentication;

import gov.healthit.chpl.auth.jwt.JWTValidationException;
import gov.healthit.chpl.auth.user.User;

public interface JWTUserConverter {

    /**
     * Check JWT and build a user given a JWT.
     * @param jwt Java Web Token to check
     * @return a User
     * @throws JWTValidationException if JWT cannot be validated
     */
    User getAuthenticatedUser(String jwt) throws JWTValidationException;
    User getImpersonatingUser(String jwt) throws JWTValidationException;
}
