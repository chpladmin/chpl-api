package gov.healthit.chpl.validation.certifiedProduct;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;

public interface CertifiedProductValidator {
	static final long AMBULATORY_CQM_TYPE_ID = 1;
	static final long INPATIENT_CQM_TYPE_ID = 2;
	static final String URL_PATTERN = "^https?://([\\da-z\\.-]+)\\.([a-z\\.]{2,6})(:[0-9]+)?([\\/\\w \\.\\-\\,=&%#]*)*(\\?([\\/\\w \\.\\-\\,=&%#]*)*)?";

	static final int CHPL_PRODUCT_ID_PARTS = 9;
	static final int EDITION_CODE_INDEX = 0;
	static final int ATL_CODE_INDEX = 1;
	static final int ACB_CODE_INDEX = 2;
	static final int DEVELOPER_CODE_INDEX = 3;
	static final int PRODUCT_CODE_INDEX = 4;
	static final int VERSION_CODE_INDEX = 5;
	static final int ICS_CODE_INDEX = 6;
	static final int ADDITIONAL_SOFTWARE_CODE_INDEX = 7;
	static final int CERTIFIED_DATE_CODE_INDEX = 8;
	
	public boolean validateUniqueId(String uniqueId);
	public boolean validateProductCodeCharacters(String uniqueId);
	public void validate(PendingCertifiedProductDTO product);
	public void validate(CertifiedProductSearchDetails product);
}
