package gov.healthit.chpl.validation.listing.normalizer;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.standard.BaselineStandardService;
import gov.healthit.chpl.upload.listing.normalizer.BaselineStandardNormalizer;
import gov.healthit.chpl.util.CertificationStatusUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
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

    @Override
    public void normalize(CertifiedProductSearchDetails listing) {
        if (!CertificationStatusUtil.isActive(listing)) {
            LOGGER.info("Updating listing " + listing.getId() + " and status is not an Active status, so no baseline standards will be added.");
            return;
        }
        super.normalize(listing);
    }
}