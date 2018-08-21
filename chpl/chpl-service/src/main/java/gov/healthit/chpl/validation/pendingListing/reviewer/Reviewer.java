package gov.healthit.chpl.validation.pendingListing.reviewer;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;

public interface Reviewer {
    public void review(PendingCertifiedProductDTO listing);
}
