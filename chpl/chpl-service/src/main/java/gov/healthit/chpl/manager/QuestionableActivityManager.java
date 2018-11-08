package gov.healthit.chpl.manager;

import java.util.Date;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.dto.ProductVersionDTO;

/**
 * Defines the functionality regarding Questionable Activity.
 * @author TYoung
 *
 */
/**
 * @author TYoung
 *
 */
public interface QuestionableActivityManager {
    /**
     * Determines if Questionable Activity should be written based on a Developer change.
     * @param origDeveloper - DeveloperDTO
     * @param newDeveloper - DeveloperDTO
     * @param activityDate - Date
     * @param activityUser - Long
     */
    void checkDeveloperQuestionableActivity(DeveloperDTO origDeveloper, DeveloperDTO newDeveloper,
            Date activityDate, Long activityUser);

    /**
     * Determines if Questionable Activity should be written based on a Product change.
     * @param origProduct - ProductDTO
     * @param newProduct - ProductDTO
     * @param activityDate - Date
     * @param activityUser - Long
     */
    void checkProductQuestionableActivity(ProductDTO origProduct, ProductDTO newProduct,
            Date activityDate, Long activityUser);

    /**
     * Determines if Questionable Activity should be written based on a ProductVersion change.
     * @param origVersion - ProductVersionDTO
     * @param newVersion - ProductVersionDTO
     * @param activityDate - Date
     * @param activityUser - Long
     */
    void checkVersionQuestionableActivity(ProductVersionDTO origVersion, ProductVersionDTO newVersion,
            Date activityDate, Long activityUser);

    /**
     * Determines if Questionable Activity should be written based on a CertifiedProductSearchDetails change.
     * @param origListing - CertifiedProductSearchDetails
     * @param newListing - CertifiedProductSearchDetails
     * @param activityDate - Date
     * @param activityUser - Long
     * @param activityReason - String
     */
    void checkListingQuestionableActivity(CertifiedProductSearchDetails origListing,
            CertifiedProductSearchDetails newListing, Date activityDate, Long activityUser,
            String activityReason);

    /**
     * Determines if Questionable Activity should be written based on a CertificationResult change.
     * @param origCertResult - CertificationResult
     * @param newCertResult - CertificationResult
     * @param activityDate - Date
     * @param activityUser - Long
     * @param activityReason - String
     */
    void checkCertificationResultQuestionableActivity(CertificationResult origCertResult,
            CertificationResult newCertResult, Date activityDate, Long activityUser,
            String activityReason);
}
