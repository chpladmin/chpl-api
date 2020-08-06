package gov.healthit.chpl.domain;

import lombok.Data;

@Data
public class ConfirmCertifiedProductRequest {
    private PendingCertifiedProductDetails pendingListing;
    private boolean warningAcknowledgement;
}
