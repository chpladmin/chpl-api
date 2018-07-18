package gov.healthit.chpl.validation.pendingListing.review;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;

public interface Reviewer {
    public void review(PendingCertifiedProductDTO listing);
}
