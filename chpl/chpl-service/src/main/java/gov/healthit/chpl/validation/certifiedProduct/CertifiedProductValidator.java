package gov.healthit.chpl.validation.certifiedProduct;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;

public interface CertifiedProductValidator {
    static final long AMBULATORY_CQM_TYPE_ID = 1;
    static final long INPATIENT_CQM_TYPE_ID = 2;
    static final String URL_PATTERN = "^https?://([\\da-z\\.-]+)\\.([a-z\\.]{2,6})(:[0-9]+)?([\\/\\w \\.\\-\\,=&%#]*)*(\\?([\\/\\w \\.\\-\\,=&%#]*)*)?";
    
    public boolean validateUniqueId(String uniqueId);

    public boolean validateProductCodeCharacters(String uniqueId);

    public boolean validateVersionCodeCharacters(String uniqueId);

    public boolean validateIcsCodeCharacters(String chplProductNumber);

    public boolean validateAdditionalSoftwareCodeCharacters(String chplProductNumber);

    public boolean validateCertifiedDateCodeCharacters(String chplProductNumber);

    public void validate(PendingCertifiedProductDTO product);

    public void validate(CertifiedProductSearchDetails product);
    
    public int getMaxLength(String field);
    
    public String getErrorMessage(String errorField);
    
    public String getErrorMessage(String errorField, String input);
}
