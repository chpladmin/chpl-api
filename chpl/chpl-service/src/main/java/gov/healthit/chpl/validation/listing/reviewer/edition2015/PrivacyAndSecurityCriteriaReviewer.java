package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.SpecialProperties;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component("privacyAndSecurityCriteriaReviewer")
public class PrivacyAndSecurityCriteriaReviewer implements Reviewer {

    private ErrorMessageUtil errorMessageUtil;
    private SpecialProperties specialProperties;
    private ValidationUtils validationUtils;

    private List<CertificationCriterion> privacyAndSecurityCriteria = new ArrayList<CertificationCriterion>();
    private List<CertificationCriterion> privacyAndSecurityRequiredCriteria = new ArrayList<CertificationCriterion>();

    @Autowired
    public PrivacyAndSecurityCriteriaReviewer(CertificationCriterionService criterionService,
            ErrorMessageUtil errorMessageUtil, SpecialProperties specialProperties, ValidationUtils validationUtils,
            @Value("${privacyAndSecurityCriteria}") String privacyAndSecurityCriteria,
            @Value("${privacyAndSecurityRequiredCriteria}") String privacyAndSecurityRequiredCriteria) {
        this.errorMessageUtil = errorMessageUtil;
        this.specialProperties = specialProperties;
        this.validationUtils = validationUtils;

        this.privacyAndSecurityCriteria = Arrays.asList(privacyAndSecurityCriteria.split(",")).stream()
                .map(id -> criterionService.get(Long.parseLong(id)))
                .filter(criteria -> BooleanUtils.isFalse(criteria.getRemoved()))
                .collect(Collectors.toList());

        this.privacyAndSecurityRequiredCriteria = Arrays
                .asList(privacyAndSecurityRequiredCriteria.split(",")).stream()
                .map(id -> criterionService.get(Long.parseLong(id)))
                .collect(Collectors.toList());
    }

    public void review(CertifiedProductSearchDetails listing) {
        if (listing.getCertificationDate() != null
                && isDateAfterCuresEffectiveRuleDate(listing.getCertificationDate())) {
            List<CertificationCriterion> attestedToCriteria = listing.getCertificationResults().stream()
                    .filter(certResult -> BooleanUtils.isTrue(certResult.isSuccess()))
                    .map(certResult -> certResult.getCriterion())
                    .collect(Collectors.toList());

            listing.getErrorMessages().addAll(
                    validationUtils.checkSubordinateCriteriaAllRequired(
                            privacyAndSecurityCriteria,
                            privacyAndSecurityRequiredCriteria,
                            attestedToCriteria, errorMessageUtil));
        }
    }

    private boolean isDateAfterCuresEffectiveRuleDate(Long dateMillisToCheck) {
        Date dateToCheck = new Date(dateMillisToCheck);
        return dateToCheck.after(specialProperties.getEffectiveRuleDate())
                || dateToCheck.equals(specialProperties.getEffectiveRuleDate());
    }
}
