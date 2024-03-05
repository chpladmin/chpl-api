package gov.healthit.chpl.validation.listing.reviewer;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.standard.StandardDAO;
import gov.healthit.chpl.standard.StandardGroupService;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component("standardAsOfTodayReviewer")
public class StandardAsOfTodayReviewer extends StandardReviewer {

    @Autowired
    public StandardAsOfTodayReviewer(CertificationResultRules certResultRules, ValidationUtils validationUtils,
            StandardDAO standardDao, StandardGroupService standardGroupService, ErrorMessageUtil msgUtil) {
        super(certResultRules, validationUtils, standardDao, standardGroupService, msgUtil);
    }

    public LocalDate getStandardsCheckDate(CertifiedProductSearchDetails listing) {
        return LocalDate.now();
    }

}
