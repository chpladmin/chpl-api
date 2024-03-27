package gov.healthit.chpl.upload.listing.validation.reviewer;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.codeset.CodeSetDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.CodeSetReviewer;

@Component
public class CodeSetAsOfCertificationDayReviewer extends CodeSetReviewer {

    @Autowired
    public CodeSetAsOfCertificationDayReviewer(CertificationResultRules certResultRules,
            CodeSetDAO codeSetDao,
            ValidationUtils validationUtils,
            ErrorMessageUtil msgUtil) {
        super(certResultRules, codeSetDao, validationUtils, msgUtil);
    }

    public LocalDate getCodeSetCheckDate(CertifiedProductSearchDetails listing) {
        return listing.getCertificationDay();
    }
}
