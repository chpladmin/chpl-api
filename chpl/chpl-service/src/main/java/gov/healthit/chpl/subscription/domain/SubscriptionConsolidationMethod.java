package gov.healthit.chpl.subscription.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionConsolidationMethod {
    public static final String CONSOLIDATION_METHOD_DAILY = "Daily";

    private Long id;
    private String name;
}
