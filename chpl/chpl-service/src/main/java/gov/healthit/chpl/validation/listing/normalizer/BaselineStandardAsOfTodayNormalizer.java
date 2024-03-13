package gov.healthit.chpl.validation.listing.normalizer;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.standard.BaselineStandardService;
import gov.healthit.chpl.upload.listing.normalizer.BaselineStandardNormalizer;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component
public class BaselineStandardAsOfTodayNormalizer extends BaselineStandardNormalizer {
    @Autowired
    public BaselineStandardAsOfTodayNormalizer(BaselineStandardService baselineStandardService,
            ErrorMessageUtil msgUtil) {
        super(baselineStandardService, msgUtil);
    }

    public LocalDate getStandardsCheckDate(CertifiedProductSearchDetails listing) {
        return LocalDate.now();
    }
}