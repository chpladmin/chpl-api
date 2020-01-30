package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;

@Component
public class RequiredCriteriaValidator implements Reviewer {

    @Value("${criterion.170_315_d_12}")
    private Integer criteriaD12Id;

    @Value("${criterion.170_315_d_13}")
    private Integer criteriaD13Id;

    @Value("#{new java.text.SimpleDateFormat(\"MM/dd/yyyy\").parse(\"${cures.ruleEffectiveDate}\")}")
    private Date ruleEffectiveDate;

    private ErrorMessageUtil msgUtil;

    @Autowired
    public RequiredCriteriaValidator(ErrorMessageUtil msgUtil) {
        this.msgUtil = msgUtil;
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        // These criterion are only required after effective rule date
        if ((new Date(listing.getCertificationDate())).after(ruleEffectiveDate)
                || (new Date(listing.getCertificationDate())).equals(ruleEffectiveDate)) {
            checkRequiredCriterionExist(listing, criteriaD12Id);
            checkRequiredCriterionExist(listing, criteriaD13Id);
        }

    }

    private void checkRequiredCriterionExist(CertifiedProductSearchDetails listing, Integer criterionId) {
        Optional<CertificationResult> certResult = findCertificationResult(listing, criterionId);
        if (certResult.isPresent() && !certResult.get().isSuccess()) {
            listing.getErrorMessages().add(
                    msgUtil.getMessage("listing.criteria.required", certResult.get().getCriterion().getNumber()));

        }
    }

    private Optional<CertificationResult> findCertificationResult(CertifiedProductSearchDetails listing, Integer criterionId) {
        return listing.getCertificationResults().stream()
                .filter(cr -> cr.getCriterion().getId().equals(Long.valueOf(criterionId)))
                .findFirst();
    }
}
