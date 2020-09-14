package gov.healthit.chpl.validation.pendingListing.reviewer.edition2015;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.service.CertificationCriterionService.Criteria2015;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.pendingListing.reviewer.Reviewer;

@Component("pendingGapAllowedReviewer")
public class GapAllowedReviewer implements Reviewer {

    @Value("${cures.ruleEffectiveDate}")
    private String curesEffectiveRuleDate;

    private Long curesEffectiveRuleDateTimestamp;
    private CertificationCriterionService certificationCriterionService;
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public GapAllowedReviewer(CertificationCriterionService certificationCriterionService, ErrorMessageUtil errorMEssageUtil) {
        this.certificationCriterionService = certificationCriterionService;
        this.errorMessageUtil = errorMEssageUtil;
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
    public void review(PendingCertifiedProductDTO listing) {
        if (isCertificationDateAfterCuresEffictiveRuleDate(listing)) {
            Optional<PendingCertificationResultDTO> f3Result = getF3Criterion(listing);
            if (f3Result.isPresent() && f3Result.get().getMeetsCriteria() && f3Result.get().getGap()) {
                listing.getErrorMessages().add(errorMessageUtil.getMessage("listing.criteria.f_3CannotHaveGap"));
            }
        }
    }

    private boolean isCertificationDateAfterCuresEffictiveRuleDate(PendingCertifiedProductDTO listing) {
        return listing.getCertificationDate().getTime() > curesEffectiveRuleDateTimestamp;
    }

    private Optional<PendingCertificationResultDTO> getF3Criterion(PendingCertifiedProductDTO listing) {
        return listing.getCertificationCriterion().stream()
        .filter(crit -> crit.getCriterion().getNumber().equals(certificationCriterionService.get(Criteria2015.F_3).getNumber()))
        .findAny();
    }

}
