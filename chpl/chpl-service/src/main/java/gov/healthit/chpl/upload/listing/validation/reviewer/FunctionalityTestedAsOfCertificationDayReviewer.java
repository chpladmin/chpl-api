package gov.healthit.chpl.upload.listing.validation.reviewer;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.functionalitytested.FunctionalityTestedDAO;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;

@Component
public class FunctionalityTestedAsOfCertificationDayReviewer extends FunctionalityTestedReviewer {

    @Autowired
    public FunctionalityTestedAsOfCertificationDayReviewer(CertificationResultRules certResultRules,
            ValidationUtils validationUtils,
            FunctionalityTestedDAO functionalityTestedDao, ErrorMessageUtil msgUtil) {
        super(certResultRules, validationUtils, functionalityTestedDao, msgUtil);
    }

    public LocalDate getFunctionalityTestedCheckDate(CertifiedProductSearchDetails listing) {
        return listing.getCertificationDay() == null ? LocalDate.MIN : listing.getCertificationDay();
    }

    public boolean allowsExtension() {
        return false;
    }
}
