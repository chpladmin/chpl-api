package gov.healthit.chpl.validation.listing.reviewer;

import java.util.Optional;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.conformanceMethod.dao.ConformanceMethodDAO;
import gov.healthit.chpl.conformanceMethod.domain.ConformanceMethod;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultConformanceMethod;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.util.ValidationUtils;

@Component("conformanceMethodReviewer")
public class ConformanceMethodReviewer extends PermissionBasedReviewer {
    private ConformanceMethodDAO conformanceMethodDAO;
    private ValidationUtils validationUtils;
    private CertificationResultRules certResultRules;
    private FF4j ff4j;
    private static final String CM_MUST_NOT_HAVE_OTHER_DATA = "Attestation";

    @Autowired
    public ConformanceMethodReviewer(ConformanceMethodDAO conformanceMethodDAO, ErrorMessageUtil msgUtil,
            ValidationUtils validationUtils, CertificationResultRules certResultRules,
            ResourcePermissions resourcePermissions, FF4j ff4j) {
        super(msgUtil, resourcePermissions);
        this.conformanceMethodDAO = conformanceMethodDAO;
        this.msgUtil = msgUtil;
        this.validationUtils = validationUtils;
        this.certResultRules = certResultRules;
        this.resourcePermissions = resourcePermissions;
        this.ff4j = ff4j;
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        if (listing.getCertificationResults().stream()
                .filter(cr -> cr.isSuccess() && cr.getConformanceMethods() != null && cr.getConformanceMethods().size() > 0)
                .count() > 0
                && !ff4j.check(FeatureList.CONFORMANCE_METHOD)) {
            listing.getErrorMessages().add("Conformance Methods are not implemented yet");
        } else {
            listing.getCertificationResults().stream()
                    .filter(certResult -> validationUtils.isEligibleForErrors(certResult))
                    .forEach(certResult -> reviewCertificationResult(listing, certResult));
            listing.getCertificationResults().stream()
                .forEach(certResult -> removeConformanceMethodsIfNotApplicable(certResult));
        }
    }

    private void reviewCertificationResult(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        reviewCriteriaCanHaveConformanceMethods(listing, certResult);
        if (!CollectionUtils.isEmpty(certResult.getConformanceMethods())) {
            certResult.getConformanceMethods().stream()
                .forEach(conformanceMethod -> reviewConformanceMethodFields(listing, certResult, conformanceMethod));
        }
    }

    private void reviewCriteriaCanHaveConformanceMethods(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (!certResultRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.CONFORMANCE_METHOD)) {
            if (!CollectionUtils.isEmpty(certResult.getConformanceMethods())) {
                listing.getWarningMessages().add(msgUtil.getMessage(
                    "listing.criteria.conformanceMethodNotApplicable", Util.formatCriteriaNumber(certResult.getCriterion())));
            }
        }
    }

    private void removeConformanceMethodsIfNotApplicable(CertificationResult certResult) {
        if (!certResultRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.CONFORMANCE_METHOD)) {
            certResult.setConformanceMethods(null);
        }
    }

    private void reviewConformanceMethodFields(CertifiedProductSearchDetails listing, CertificationResult certResult,
            CertificationResultConformanceMethod conformanceMethod) {
        checkIfConformanceMethodIsAllowed(listing, certResult, conformanceMethod);
        checkIfConformanceMethodVersionRequirements(listing, certResult, conformanceMethod);
    }

    private void checkIfConformanceMethodIsAllowed(CertifiedProductSearchDetails listing, CertificationResult certResult,
            CertificationResultConformanceMethod conformanceMethod) {
        Optional<ConformanceMethod> allowedConformanceMethod = conformanceMethodDAO.getByCriterionId(certResult.getCriterion().getId()).stream()
            .filter(cm -> cm.getId().equals(conformanceMethod.getConformanceMethod().getId()))
            .findAny();
        if (!allowedConformanceMethod.isPresent()) {
            addCriterionErrorOrWarningByPermission(listing, certResult,
                    "listing.criteria.conformanceMethod.invalidCriteria",
                    conformanceMethod.getConformanceMethod().getName(),
                    Util.formatCriteriaNumber(certResult.getCriterion()));
        }
    }

    private void checkIfConformanceMethodVersionRequirements(CertifiedProductSearchDetails listing, CertificationResult certResult,
            CertificationResultConformanceMethod conformanceMethod) {
            if (conformanceMethod.getConformanceMethod() != null
                    && !StringUtils.isEmpty(conformanceMethod.getConformanceMethod().getName())
                    && !conformanceMethod.getConformanceMethod().getName().equalsIgnoreCase(CM_MUST_NOT_HAVE_OTHER_DATA)
                    && StringUtils.isEmpty(conformanceMethod.getConformanceMethodVersion())) {
                addCriterionErrorOrWarningByPermission(listing, certResult,
                        "listing.criteria.conformanceMethod.missingConformanceMethodVersion",
                        Util.formatCriteriaNumber(certResult.getCriterion()),
                        conformanceMethod.getConformanceMethod().getName());
            }
            if (conformanceMethod.getConformanceMethod() != null
                    && !StringUtils.isEmpty(conformanceMethod.getConformanceMethod().getName())
                    && conformanceMethod.getConformanceMethod().getName().equalsIgnoreCase(CM_MUST_NOT_HAVE_OTHER_DATA)
                    && !StringUtils.isEmpty(conformanceMethod.getConformanceMethodVersion())) {
                addCriterionErrorOrWarningByPermission(listing, certResult,
                        "listing.criteria.conformanceMethod.unallowedConformanceMethodVersion",
                        Util.formatCriteriaNumber(certResult.getCriterion()),
                        conformanceMethod.getConformanceMethod().getName());
            }
    }
}
