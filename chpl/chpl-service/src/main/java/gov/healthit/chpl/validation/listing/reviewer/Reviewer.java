package gov.healthit.chpl.validation.listing.reviewer;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;

public interface Reviewer {
    public void review(CertifiedProductSearchDetails listing);
}
