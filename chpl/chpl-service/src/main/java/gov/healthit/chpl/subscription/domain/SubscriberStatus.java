package gov.healthit.chpl.subscription.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriberStatus {
    public static final String SUBSCRIBER_STATUS_PENDING = "Pending";

    private Long id;
    private String name;
}
