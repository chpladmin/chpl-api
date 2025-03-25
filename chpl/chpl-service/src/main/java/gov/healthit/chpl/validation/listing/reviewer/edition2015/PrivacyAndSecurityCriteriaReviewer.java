package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.service.CertificationCriterionService.Criteria2015;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;

@Component("privacyAndSecurityCriteriaReviewer")
public class PrivacyAndSecurityCriteriaReviewer implements Reviewer {
    private ErrorMessageUtil errorMessageUtil;
    private ValidationUtils validationUtils;

    private CertificationCriterion b11;
    private List<CertificationCriterion> privacyAndSecurityCriteria = new ArrayList<CertificationCriterion>();
    private List<CertificationCriterion> privacyAndSecurityRequiredCriteria = new ArrayList<CertificationCriterion>();

    @Autowired
    public PrivacyAndSecurityCriteriaReviewer(CertificationCriterionService criteriaService,
            ErrorMessageUtil errorMessageUtil, ValidationUtils validationUtils,
            @Value("${privacyAndSecurityCriteria}") String privacyAndSecurityCriteria,
            @Value("${privacyAndSecurityRequiredCriteria}") String privacyAndSecurityRequiredCriteria) {
        this.errorMessageUtil = errorMessageUtil;
        this.validationUtils = validationUtils;

        this.b11 = criteriaService.get(Criteria2015.B_11);
        this.privacyAndSecurityCriteria = Arrays.asList(privacyAndSecurityCriteria.split(",")).stream()
                .map(id -> criteriaService.get(Long.parseLong(id)))
                .filter(criteria -> BooleanUtils.isFalse(criteria.isRemoved()))
                .collect(Collectors.toList());

        this.privacyAndSecurityRequiredCriteria = Arrays
                .asList(privacyAndSecurityRequiredCriteria.split(",")).stream()
                .map(id -> criteriaService.get(Long.parseLong(id)))
                .collect(Collectors.toList());
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        List<CertificationCriterion> attestedToCriteria = listing.getCertificationResults().stream()
                .filter(certResult -> BooleanUtils.isTrue(certResult.getSuccess()))
                .map(certResult -> certResult.getCriterion())
                .collect(Collectors.toList());

        //if b11 doesn't have a p&S value, it doesn't count towards requiring d12/d13
        //this can most likely be removed after jan 2028 when b11 p&s is no longer optional
        CertificationResult b11CertResult = listing.getCertificationResults().stream()
                .filter(certResult -> certResult.getCriterion().getId().equals(b11.getId()))
                .findAny()
                .orElse(null);
        if (b11CertResult == null || StringUtils.isEmpty(b11CertResult.getPrivacySecurityFramework())) {
            privacyAndSecurityCriteria.remove(b11);
            attestedToCriteria.remove(b11);
        }

        listing.addAllBusinessErrorMessages(
                validationUtils.checkSubordinateCriteriaAllRequired(
                        privacyAndSecurityCriteria,
                        privacyAndSecurityRequiredCriteria,
                        attestedToCriteria, errorMessageUtil).stream()
                        .collect(Collectors.toSet()));
    }
}
