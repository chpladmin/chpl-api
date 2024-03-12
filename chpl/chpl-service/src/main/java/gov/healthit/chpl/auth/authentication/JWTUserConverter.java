package gov.healthit.chpl.auth.authentication;

import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.exception.JWTValidationException;
import gov.healthit.chpl.exception.MultipleUserAccountsException;

public interface JWTUserConverter {
    JWTAuthenticatedUser getAuthenticatedUser(String jwt) throws JWTValidationException, MultipleUserAccountsException;
    JWTAuthenticatedUser getImpersonatingUser(String jwt) throws JWTValidationException;
}
