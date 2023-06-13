package gov.healthit.chpl.subscription.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionSubject {
    private Long id;
    private SubscriptionObjectType type;
    private String subject;
}
