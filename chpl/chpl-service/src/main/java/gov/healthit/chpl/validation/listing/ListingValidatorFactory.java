package gov.healthit.chpl.validation.listing;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class ListingValidatorFactory {

    private AllowedListingValidator allowedValidator;
    private Edition2015ListingValidator edition2015Validator;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public ListingValidatorFactory(AllowedListingValidator allowedValidator, Edition2015ListingValidator edition2015Validator,
            ErrorMessageUtil msgUtil) {
        this.allowedValidator = allowedValidator;
        this.edition2015Validator = edition2015Validator;
        this.msgUtil = msgUtil;
    }

    public Validator getValidator(final CertifiedProductSearchDetails listing) {
        String edition = listing.getEdition() != null ? listing.getEdition().getName() : null;
        if (StringUtils.isEmpty(listing.getChplProductNumber())) {
            String errMsg = msgUtil.getMessage("listing.validator.editionOrChplNumberNotFound", listing.getId().toString());
            listing.addBusinessErrorMessage(errMsg);
            LOGGER.error(errMsg);
            return null;
        }

        if (edition == null || edition.equals(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear())) {
            return edition2015Validator;
        } else if (edition.equals(CertificationEditionConcept.CERTIFICATION_EDITION_2011.getYear())
                || edition.equals(CertificationEditionConcept.CERTIFICATION_EDITION_2014.getYear())) {
            return allowedValidator;
        } else {
            String errMsg = msgUtil.getMessage("listing.validator.certificationEditionNotFound", edition);
            listing.addBusinessErrorMessage(errMsg);
            LOGGER.error(errMsg);
        }
        return null;
    }
}
