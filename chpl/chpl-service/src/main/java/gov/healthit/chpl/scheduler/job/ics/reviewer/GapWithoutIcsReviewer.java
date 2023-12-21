package gov.healthit.chpl.scheduler.job.ics.reviewer;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.SpecialProperties;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2(topic = "icsErrorsReportCreatorJobLogger")
public class GapWithoutIcsReviewer extends IcsErrorsReviewer {

    private SpecialProperties specialProperties;
    private String errorMessage;
    private Date curesRuleEffectiveDate;

    @Autowired
    public GapWithoutIcsReviewer(SpecialProperties specialProperties,
            @Value("${ics.gapListingError}") String errorMessage) {
        this.specialProperties = specialProperties;
        this.errorMessage = errorMessage;

        this.curesRuleEffectiveDate = getCuresRuleEffectiveDate();
    }

    @Override
    public String getIcsError(CertifiedProductSearchDetails listing) {
        if (curesRuleEffectiveDate == null) {
            return null;
        } else if (!hasIcs(listing) && doesGapExistForListing(listing) && isCertificationDateAfterRuleEffectiveDate(listing)) {
            LOGGER.info("\tListing " + listing.getId() + " has GAP but not ICS and is certified after ERD.");
            return errorMessage;
        }
        return null;
    }

    private boolean doesGapExistForListing(CertifiedProductSearchDetails listing) {
        return listing.getCertificationResults().stream()
                .filter(cert -> BooleanUtils.isTrue(cert.getSuccess())
                        && cert.getGap() != null ? cert.getGap() : false)
                .count() > 0;
    }

    private boolean isCertificationDateAfterRuleEffectiveDate(CertifiedProductSearchDetails listing) {
        Date certDate = new Date(listing.getCertificationDate());
        return certDate.equals(curesRuleEffectiveDate) || certDate.after(curesRuleEffectiveDate);
    }

    private Date getCuresRuleEffectiveDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        Date effectiveRuleDate = specialProperties.getEffectiveRuleDate();
        LOGGER.info("cures.ruleEffectiveDate = " + sdf.format(effectiveRuleDate));
        return effectiveRuleDate;
    }
}
