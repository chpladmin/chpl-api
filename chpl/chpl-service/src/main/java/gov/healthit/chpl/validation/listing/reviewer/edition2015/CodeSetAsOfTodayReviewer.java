package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.codeset.CodeSetDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;

@Component
public class CodeSetAsOfTodayReviewer extends CodeSetReviewer {

    @Autowired
    public CodeSetAsOfTodayReviewer(CertificationResultRules certResultRules,
            CodeSetDAO codeSetDao,
            ValidationUtils validationUtils,
            ErrorMessageUtil msgUtil) {
        super(certResultRules, codeSetDao, validationUtils, msgUtil);
    }

    public LocalDate getCodeSetCheckDate(CertifiedProductSearchDetails listing) {
        return LocalDate.now();
    }
}
