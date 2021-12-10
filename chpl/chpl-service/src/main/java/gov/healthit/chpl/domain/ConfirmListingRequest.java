package gov.healthit.chpl.domain;

import lombok.Data;

@Data
public class ConfirmListingRequest {
    private CertifiedProductSearchDetails listing;
    private boolean acknowledgeWarnings;
}
