package gov.healthit.chpl.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ListingUpdateRequest implements ExplainableAction {
    private CertifiedProductSearchDetails listing;
    private String reason;
    private boolean acknowledgeWarnings;
    private boolean acknowledgeBusinessErrors;
}
