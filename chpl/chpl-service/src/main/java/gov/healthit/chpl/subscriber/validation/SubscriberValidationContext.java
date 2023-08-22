package gov.healthit.chpl.subscriber.validation;

import java.util.UUID;

import gov.healthit.chpl.subscription.dao.SubscriberDao;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriberValidationContext {
    private UUID subscriberId;
    private Long roleId;
    private SubscriberDao subscriberDao;
    private ErrorMessageUtil errorMessageUtil;
}
