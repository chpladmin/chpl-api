package gov.healthit.chpl.subscription;

import java.util.Collections;
import java.util.Set;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertifiedProductSearchDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dao.ProductDAO;
import gov.healthit.chpl.subscription.domain.SubscriptionRequest;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component
public class SubscriptionRequestValidator {
    private CertifiedProductSearchDAO listingDao;
    private DeveloperDAO developerDao;
    private ProductDAO productDao;
    private ErrorMessageUtil msgUtil;

    public SubscriptionRequestValidator(ErrorMessageUtil msgUtil) {
        this.msgUtil = msgUtil;
    }

    public Set<String> getErrorMessages(SubscriptionRequest subscriptionRequest) {
        return Collections.EMPTY_SET;
    }
}
