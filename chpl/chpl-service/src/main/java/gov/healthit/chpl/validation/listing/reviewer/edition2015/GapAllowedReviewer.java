package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.service.CertificationCriterionService.Criteria2015;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;

@Component("gapAllowedReviewer")
public class GapAllowedReviewer implements Reviewer {

    private String curesEffectiveRuleDate;
    private Long curesEffectiveRuleDateTimestamp;
    private CertificationCriterionService certificationCriterionService;
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public GapAllowedReviewer(CertificationCriterionService certificationCriterionService, ErrorMessageUtil errorMEssageUtil,
            @Value("${cures.ruleEffectiveDate}") String curesEffectiveRuleDate) {
        this.certificationCriterionService = certificationCriterionService;
        this.errorMessageUtil = errorMEssageUtil;
        this.curesEffectiveRuleDate = curesEffectiveRuleDate;
    }

    @PostConstruct
    public void setup() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        Calendar cal = Calendar.getInstance();
        cal.setTime(sdf.parse(curesEffectiveRuleDate));
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        curesEffectiveRuleDateTimestamp = cal.getTimeInMillis();
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        if (isCertificationDateAfterCuresEffictiveRuleDate(listing)) {
            Optional<CertificationResult> f3Result = getF3Criterion(listing);
            if (f3Result.isPresent() && f3Result.get().isSuccess() && f3Result.get().isGap()) {
                listing.addBusinessErrorMessage(errorMessageUtil.getMessage("listing.criteria.f3CannotHaveGap"));
            }
        }
    }

    private boolean isCertificationDateAfterCuresEffictiveRuleDate(CertifiedProductSearchDetails listing) {
        return listing.getCertificationDate() != null && listing.getCertificationDate() > curesEffectiveRuleDateTimestamp;
    }

    private Optional<CertificationResult> getF3Criterion(CertifiedProductSearchDetails listing) {
        return listing.getCertificationResults().stream()
                .filter(crit -> crit.getCriterion() != null && crit.getCriterion().getId() != null
                        && crit.getCriterion().getId().equals(certificationCriterionService.get(Criteria2015.F_3).getId()))
                .findAny();
    }
}
