package gov.healthit.chpl.subscriber.validation;

import java.util.UUID;

import gov.healthit.chpl.subscription.dao.SubscriberDao;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
public class SubscriberValidationContext {
    private UUID subscriberId;
    private SubscriberDao subscriberDao;
    private ErrorMessageUtil errorMessageUtil;

    public SubscriberValidationContext(UUID subscriberId,
            SubscriberDao subscriberDao,
        ErrorMessageUtil errorMessageUtil) {
        this.subscriberId = subscriberId;
        this.subscriberDao = subscriberDao;
        this.errorMessageUtil = errorMessageUtil;
    }
}
