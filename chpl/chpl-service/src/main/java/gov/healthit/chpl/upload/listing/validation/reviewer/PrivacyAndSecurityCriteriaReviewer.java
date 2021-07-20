package gov.healthit.chpl.upload.listing.validation.reviewer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.SpecialProperties;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;

@Component("listingUploadPrivacyAndSecurityCriteriaReviewer")
public class PrivacyAndSecurityCriteriaReviewer {

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
                    .filter(certResult -> certResult.isSuccess() != null && certResult.isSuccess())
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
