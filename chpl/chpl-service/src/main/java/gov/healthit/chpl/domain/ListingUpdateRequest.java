package gov.healthit.chpl.domain;

import lombok.Data;

@Data
public class ListingUpdateRequest implements ExplainableAction {
    private CertifiedProductSearchDetails listing;
    private String reason;
    private boolean acknowledgeWarnings;
}
