package gov.healthit.chpl.upload.listing.normalizer;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.standard.BaselineStandardService;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component
public class BaselineStandardAsOfCertificationDayNormalizer extends BaselineStandardNormalizer {
    @Autowired
    public BaselineStandardAsOfCertificationDayNormalizer(BaselineStandardService baselineStandardService,
            ErrorMessageUtil msgUtil) {
        super(baselineStandardService, msgUtil);
    }

    public LocalDate getStandardsCheckDateRangeStart(CertifiedProductSearchDetails listing) {
        return listing.getCertificationDay() == null ? LocalDate.MIN : listing.getCertificationDay();
    }

    public LocalDate getStandardsCheckDateRangeEnd(CertifiedProductSearchDetails listing) {
        return listing.getCertificationDay() == null ? LocalDate.MIN : listing.getCertificationDay();
    }
}