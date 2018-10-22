package gov.healthit.chpl.manager;

import java.util.Date;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.dto.ProductVersionDTO;

public interface QuestionableActivityManager {
    void checkDeveloperQuestionableActivity(final DeveloperDTO origDeveloper, final DeveloperDTO newDeveloper,
            final Date activityDate, final Long activityUser);
    
    void checkProductQuestionableActivity(final ProductDTO origProduct, final ProductDTO newProduct,
            final Date activityDate, final Long activityUser);
    
    void checkVersionQuestionableActivity(final ProductVersionDTO origVersion,
            final ProductVersionDTO newVersion, final Date activityDate, final Long activityUser);
    
    void checkListingQuestionableActivity(final CertifiedProductSearchDetails origListing,
            final CertifiedProductSearchDetails newListing, final Date activityDate, final Long activityUser,
            final String activityReason);
    
    void checkCertificationResultQuestionableActivity(final CertificationResult origCertResult,
            final CertificationResult newCertResult, final Date activityDate, final Long activityUser,
            final String activityReason);
}
