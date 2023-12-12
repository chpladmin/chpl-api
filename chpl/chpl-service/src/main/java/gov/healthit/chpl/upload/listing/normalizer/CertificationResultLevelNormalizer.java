package gov.healthit.chpl.upload.listing.normalizer;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;

public interface CertificationResultLevelNormalizer {
    void normalize(CertifiedProductSearchDetails listing);
}
