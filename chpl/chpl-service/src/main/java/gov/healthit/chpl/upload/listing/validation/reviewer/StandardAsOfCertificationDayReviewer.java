package gov.healthit.chpl.upload.listing.validation.reviewer;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.standard.BaselineStandardService;
import gov.healthit.chpl.standard.StandardDAO;
import gov.healthit.chpl.standard.StandardGroupService;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;
import gov.healthit.chpl.validation.listing.reviewer.StandardReviewer;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component("standardAsOfCertificationDayReviewer")
public class StandardAsOfCertificationDayReviewer extends StandardReviewer {

    @Autowired
    public StandardAsOfCertificationDayReviewer(CertificationResultRules certResultRules, ValidationUtils validationUtils,
            StandardDAO standardDao, BaselineStandardService baselineStandardService,
            StandardGroupService standardGroupService, ErrorMessageUtil msgUtil) {
        super(certResultRules, validationUtils, standardDao, baselineStandardService, standardGroupService, msgUtil);
    }

    public LocalDate getStandardsCheckDate(CertifiedProductSearchDetails listing) {
        return listing.getCertificationDay();
    }

}
