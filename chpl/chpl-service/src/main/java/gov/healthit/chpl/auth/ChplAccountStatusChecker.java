package gov.healthit.chpl.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.util.ErrorMessageUtil;

@Component("chplAccountStatusChecker")
public class ChplAccountStatusChecker implements UserDetailsChecker {
    private ErrorMessageUtil msgUtil;

    @Autowired
    public ChplAccountStatusChecker(ErrorMessageUtil msgUtil) {
        this.msgUtil = msgUtil;
    }

    public void check(UserDetails user) {
        if (!user.isAccountNonLocked() || !user.isEnabled() || !user.isAccountNonExpired()
                || !user.isCredentialsNonExpired()) {
            throw new ChplAccountStatusException(msgUtil.getMessage("auth.loginNotAllowed"));
        }
    }
}
