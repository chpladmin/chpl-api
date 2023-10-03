package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;

@Component("privacyAndSecurityCriteriaReviewer")
public class PrivacyAndSecurityCriteriaReviewer implements Reviewer {

    private ErrorMessageUtil errorMessageUtil;
    private ValidationUtils validationUtils;

    private List<CertificationCriterion> privacyAndSecurityCriteria = new ArrayList<CertificationCriterion>();
    private List<CertificationCriterion> privacyAndSecurityRequiredCriteria = new ArrayList<CertificationCriterion>();

    @Autowired
    public PrivacyAndSecurityCriteriaReviewer(CertificationCriterionService criterionService,
            ErrorMessageUtil errorMessageUtil, ValidationUtils validationUtils,
            @Value("${privacyAndSecurityCriteria}") String privacyAndSecurityCriteria,
            @Value("${privacyAndSecurityRequiredCriteria}") String privacyAndSecurityRequiredCriteria) {
        this.errorMessageUtil = errorMessageUtil;
        this.validationUtils = validationUtils;

        this.privacyAndSecurityCriteria = Arrays.asList(privacyAndSecurityCriteria.split(",")).stream()
                .map(id -> criterionService.get(Long.parseLong(id)))
                .filter(criteria -> BooleanUtils.isFalse(criteria.isRemoved()))
                .collect(Collectors.toList());

        this.privacyAndSecurityRequiredCriteria = Arrays
                .asList(privacyAndSecurityRequiredCriteria.split(",")).stream()
                .map(id -> criterionService.get(Long.parseLong(id)))
                .collect(Collectors.toList());
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        List<CertificationCriterion> attestedToCriteria = listing.getCertificationResults().stream()
                .filter(certResult -> BooleanUtils.isTrue(certResult.isSuccess()))
                .map(certResult -> certResult.getCriterion())
                .collect(Collectors.toList());

        listing.addAllBusinessErrorMessages(
                validationUtils.checkSubordinateCriteriaAllRequired(
                        privacyAndSecurityCriteria,
                        privacyAndSecurityRequiredCriteria,
                        attestedToCriteria, errorMessageUtil).stream()
                        .collect(Collectors.toSet()));
    }
}
