package gov.healthit.chpl.validation.surveillance.reviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.domain.auth.Authority;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component
public class AuthorityReviewer implements Reviewer {
    private ErrorMessageUtil msgUtil;

    @Autowired
    public AuthorityReviewer(ErrorMessageUtil msgUtil) {
        this.msgUtil = msgUtil;
    }

    public void review(Surveillance surv) {
        if (!StringUtils.isEmpty(surv.getAuthority())) {
            if (!surv.getAuthority().equalsIgnoreCase(Authority.ROLE_ONC)
                    && !surv.getAuthority().equalsIgnoreCase(Authority.ROLE_ACB)) {
                surv.getErrorMessages().add(msgUtil.getMessage("surveillance.authorityRequired",
                        Authority.ROLE_ONC, Authority.ROLE_ACB));
            }
        }
    }
}
