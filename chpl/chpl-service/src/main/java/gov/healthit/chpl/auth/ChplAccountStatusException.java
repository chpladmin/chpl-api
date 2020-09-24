package gov.healthit.chpl.auth;

import org.springframework.security.authentication.AccountStatusException;

public class ChplAccountStatusException extends AccountStatusException {
    private static final long serialVersionUID = -8666003152297827392L;

    public ChplAccountStatusException(String msg) {
        super(msg);
    }

}
