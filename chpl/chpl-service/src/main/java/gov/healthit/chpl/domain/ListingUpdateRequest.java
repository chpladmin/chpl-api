package gov.healthit.chpl.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ListingUpdateRequest implements ExplainableAction {
    private CertifiedProductSearchDetails listing;
    private String reason;
    private boolean acknowledgeWarnings;
}
