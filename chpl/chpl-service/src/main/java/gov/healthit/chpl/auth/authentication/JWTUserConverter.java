package gov.healthit.chpl.auth.authentication;

import gov.healthit.chpl.auth.user.AuthenticatedUser;
import gov.healthit.chpl.exception.JWTValidationException;
import gov.healthit.chpl.exception.MultipleUserAccountsException;

public interface JWTUserConverter {
    AuthenticatedUser getAuthenticatedUser(String jwt) throws JWTValidationException, MultipleUserAccountsException;
    AuthenticatedUser getImpersonatingUser(String jwt) throws JWTValidationException;
}
