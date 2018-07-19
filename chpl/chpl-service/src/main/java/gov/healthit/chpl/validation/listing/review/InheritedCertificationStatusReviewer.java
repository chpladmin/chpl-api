package gov.healthit.chpl.validation.listing.review;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component
public class InheritedCertificationStatusReviewer implements Reviewer {
    @Autowired private ErrorMessageUtil msgUtil;
    

    public void review(CertifiedProductSearchDetails listing) {
        if (!validateIcsCodeCharacters(listing.getChplProductNumber())) {
            listing.getErrorMessages().add(
                    msgUtil.getMessage("listing.badIcsCodeChars", CertifiedProductDTO.ICS_CODE_LENGTH));
        } else {
            String uniqueId = listing.getChplProductNumber();
            String[] uniqueIdParts = uniqueId.split("\\.");
            Integer icsCodeInteger = Integer.valueOf(uniqueIdParts[CertifiedProductDTO.ICS_CODE_INDEX]);
            if (icsCodeInteger != null && icsCodeInteger.intValue() == 0) {
                if (listing.getIcs() != null && listing.getIcs().getParents() != null
                        && listing.getIcs().getParents().size() > 0) {
                    listing.getErrorMessages().add(msgUtil.getMessage("listing.ics00"));
                }

                if (listing.getIcs() != null && listing.getIcs().getInherits() != null
                        && listing.getIcs().getInherits().equals(Boolean.TRUE)) {
                    listing.getErrorMessages().add(msgUtil.getMessage("listing.icsCodeFalseValueTrue"));
                }
            } else if (listing.getIcs() == null || listing.getIcs().getInherits() == null
                    || listing.getIcs().getInherits().equals(Boolean.FALSE) && icsCodeInteger != null
                    && icsCodeInteger.intValue() > 0) {
                listing.getErrorMessages().add(msgUtil.getMessage("listing.icsCodeTrueValueFalse"));
            }
        }
    }
    
    private boolean validateIcsCodeCharacters(final String chplProductNumber) {
        String[] uniqueIdParts = chplProductNumber.split("\\.");
        if (uniqueIdParts.length == CertifiedProductDTO.CHPL_PRODUCT_ID_PARTS) {
            // validate that these pieces match up with data
            String icsCode = uniqueIdParts[CertifiedProductDTO.ICS_CODE_INDEX];
            if (StringUtils.isEmpty(icsCode)
                    || !icsCode.matches("^[0-9]{" + CertifiedProductDTO.ICS_CODE_LENGTH + "}$")) {
                return false;
            }
        }
        return true;
    }
}
