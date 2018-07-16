package gov.healthit.chpl.validation.certifiedProduct;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;

/**
 * Perform validations common to all types of Certified Products.
 * @author alarned
 *
 */
public interface CertifiedProductValidator {
    /** ID for CQM of type "Ambulatory". */
    long AMBULATORY_CQM_TYPE_ID = 1;
    /** ID for CQM of type "Inpatient". */
    long INPATIENT_CQM_TYPE_ID = 2;
    /* TODO: is this the right URL Pattern? Aren't URLs hard */
    /** Patter for URL to use in validation. */
    String URL_PATTERN =
            "^https?://([\\da-z\\.-]+)\\.([a-z\\.]{2,6})(:[0-9]+)?([\\/\\w \\.\\-\\,=&%#]*)*(\\?([\\/\\w \\.\\-\\,=&%#]*)*)?";

    /**
     * Ensure that the ID passed in is unique in the CHPL.
     * @param uniqueId ID to validate
     * @return true iff ID is unique
     */
    boolean validateUniqueId(String uniqueId);

    /**
     * Verify that the Product Code has the correct format.
     * @param uniqueId the passed in ID
     * @return true iff Code has correct format
     */
    boolean validateProductCodeCharacters(String uniqueId);

    /**
     * Verify that the Version Code has the correct format.
     * @param uniqueId the passed in ID
     * @return true iff Code has correct format
     */
    boolean validateVersionCodeCharacters(String uniqueId);

    /**
     * Verify that the ICS Code has the correct format.
     * @param chplProductNumber the passed in ID
     * @return true iff Code has correct format
     */
    boolean validateIcsCodeCharacters(String chplProductNumber);

    /**
     * Verify that the Additional Software Code has the correct format.
     * @param chplProductNumber the passed in ID
     * @return true iff Code has correct format
     */
    boolean validateAdditionalSoftwareCodeCharacters(String chplProductNumber);

    /**
     * Verify that the Certification Date Code has the correct format.
     * @param chplProductNumber the passed in ID
     * @return true iff Code has correct format
     */
    boolean validateCertifiedDateCodeCharacters(String chplProductNumber);

    /**
     * Analyze a Pending Certified Product to ensure it is valid according to CHPL business logic.
     * @param product passed in product
     */
    void validate(PendingCertifiedProductDTO product);

    /**
     * Analyze a Certified Product to ensure it is valid according to CHPL business logic.
     * @param product passed in product
     */
    void validate(CertifiedProductSearchDetails product);

    /**
     * TODO: Get rid of this in the interface; it should be a private method in the implementation
     */
    int getMaxLength(String field);

    /**
     * Retrieve the locale specific message referenced by the message code.
     * @param messageCode message code
     * @return locale specific string
     */
    String getMessage(String messageCode);

    /**
     * Retrieve the local specific message referenced by the message code, interpolated with input data.
     * @param messageCode message code
     * @param input string to interpolate
     * @return locale specific string
     */
    String getMessage(String messageCode, String input);
}
