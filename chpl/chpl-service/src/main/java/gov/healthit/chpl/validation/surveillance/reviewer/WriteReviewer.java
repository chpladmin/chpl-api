package gov.healthit.chpl.validation.surveillance.reviewer;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.surveillance.Surveillance;

public interface WriteReviewer {

    void review(CertifiedProductSearchDetails listing, Surveillance surveillance);
}
