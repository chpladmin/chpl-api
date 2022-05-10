package gov.healthit.chpl.validation.listing.reviewer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.conformanceMethod.dao.ConformanceMethodDAO;
import gov.healthit.chpl.conformanceMethod.domain.ConformanceMethod;
import gov.healthit.chpl.conformanceMethod.domain.ConformanceMethodCriteriaMap;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultConformanceMethod;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.service.CertificationCriterionService.Criteria2015;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.util.ValidationUtils;
import lombok.extern.log4j.Log4j2;

@Component("conformanceMethodReviewer")
@Log4j2
public class ConformanceMethodReviewer extends PermissionBasedReviewer {
    private static final String CM_MUST_NOT_HAVE_OTHER_DATA = "Attestation";
    private static final String CM_F3_MUST_HAVE_GAP = "Attestation";
    private static final String CM_F3_CANNOT_HAVE_GAP = "ONC Test Procedure";

    private List<ConformanceMethodCriteriaMap> conformanceMethodCriteriaMap = new ArrayList<ConformanceMethodCriteriaMap>();
    private ValidationUtils validationUtils;
    private CertificationResultRules certResultRules;
    private CertificationCriterion f3;
    private FF4j ff4j;

    @Autowired
    public ConformanceMethodReviewer(ConformanceMethodDAO conformanceMethodDao, ErrorMessageUtil msgUtil,
            ValidationUtils validationUtils, CertificationResultRules certResultRules,
            CertificationCriterionService criteriaService,
            ResourcePermissions resourcePermissions, FF4j ff4j) {
        super(msgUtil, resourcePermissions);
        this.msgUtil = msgUtil;
        this.validationUtils = validationUtils;
        this.certResultRules = certResultRules;
        this.resourcePermissions = resourcePermissions;
        f3 = criteriaService.get(Criteria2015.F_3);
        this.ff4j = ff4j;

        try {
            this.conformanceMethodCriteriaMap = conformanceMethodDao.getAllConformanceMethodCriteriaMap();
        } catch (EntityRetrievalException ex) {
            LOGGER.error("Could not initialize conformance method criteria map.", ex);
        }
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
        removeConformanceMethodsInvalidForCriterion(listing, certResult);
        reviewConformanceMethodsRequired(listing, certResult);
        if (!CollectionUtils.isEmpty(certResult.getConformanceMethods())) {
            certResult.getConformanceMethods().stream()
                .forEach(conformanceMethod -> reviewConformanceMethodFields(listing, certResult, conformanceMethod));
            if (certResult.getCriterion().getId().equals(f3.getId())) {
                reviewF3ConformanceMethodsForGapRequirement(listing, certResult);
            }
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

    private void removeConformanceMethodsInvalidForCriterion(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (CollectionUtils.isEmpty(certResult.getConformanceMethods())) {
            return;
        }
        Iterator<CertificationResultConformanceMethod> conformanceMethodIter = certResult.getConformanceMethods().iterator();
        while (conformanceMethodIter.hasNext()) {
            CertificationResultConformanceMethod conformanceMethod = conformanceMethodIter.next();
            if (!isConformanceMethodAllowed(certResult, conformanceMethod)) {
                conformanceMethodIter.remove();
                listing.getWarningMessages().add(msgUtil.getMessage("listing.criteria.conformanceMethod.invalidCriteria",
                        conformanceMethod.getConformanceMethod().getName(),
                        Util.formatCriteriaNumber(certResult.getCriterion())));
            }
        }
    }

    private boolean isConformanceMethodAllowed(CertificationResult certResult, CertificationResultConformanceMethod conformanceMethod) {
        Optional<ConformanceMethod> allowedConformanceMethod = getConformanceMethodsForCriterion(certResult.getCriterion()).stream()
            .filter(cm -> cm.getId().equals(conformanceMethod.getConformanceMethod().getId()))
            .findAny();
        return allowedConformanceMethod.isPresent();
    }

    private List<ConformanceMethod> getConformanceMethodsForCriterion(CertificationCriterion criterion) {
        return conformanceMethodCriteriaMap.stream()
                .filter(mapping -> mapping.getCriterion().getId().equals(criterion.getId()))
                .map(mapping -> mapping.getConformanceMethod())
                .toList();
    }

    private void reviewConformanceMethodsRequired(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (ff4j.check(FeatureList.CONFORMANCE_METHOD)
                && certResultRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.CONFORMANCE_METHOD)
                && CollectionUtils.isEmpty(certResult.getConformanceMethods())) {
            if (CollectionUtils.isEmpty(certResult.getConformanceMethods())) {
                addCriterionErrorOrWarningByPermission(listing, certResult, "listing.criteria.conformanceMethod.missingConformanceMethod",
                        Util.formatCriteriaNumber(certResult.getCriterion()));
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
        reviewConformanceMethodVersionRequirements(listing, certResult, conformanceMethod);
    }

    private void reviewConformanceMethodVersionRequirements(CertifiedProductSearchDetails listing, CertificationResult certResult,
        CertificationResultConformanceMethod conformanceMethod) {
        if (isMissingVersionDataWhenItIsRequired(conformanceMethod)) {
            addCriterionErrorOrWarningByPermission(listing, certResult,
                    "listing.criteria.conformanceMethod.missingConformanceMethodVersion",
                    Util.formatCriteriaNumber(certResult.getCriterion()),
                    conformanceMethod.getConformanceMethod().getName());
        }
        if (hasVersionDataWhenItIsNotAllowed(conformanceMethod)) {
            if (mayOnlyHaveConformanceMethodWithoutVersion(certResult, conformanceMethod)) {
                listing.getWarningMessages().add(msgUtil.getMessage(
                        "listing.criteria.conformanceMethod.unallowedConformanceMethodVersionRemoved",
                        Util.formatCriteriaNumber(certResult.getCriterion()),
                        conformanceMethod.getConformanceMethod().getName(),
                        conformanceMethod.getConformanceMethodVersion()));
                conformanceMethod.setConformanceMethodVersion(null);
            } else {
                addCriterionErrorOrWarningByPermission(listing, certResult,
                        "listing.criteria.conformanceMethod.unallowedConformanceMethodVersion",
                        Util.formatCriteriaNumber(certResult.getCriterion()),
                        conformanceMethod.getConformanceMethod().getName());
            }
        }
    }

    private boolean mayOnlyHaveConformanceMethodWithoutVersion(CertificationResult certResult,
            CertificationResultConformanceMethod conformanceMethod) {
        List<ConformanceMethod> conformanceMethodsForCriterion = getConformanceMethodsForCriterion(certResult.getCriterion());
        return conformanceMethodsForCriterion != null && conformanceMethodsForCriterion.size() == 1
                && conformanceMethodsForCriterion.get(0).getName().equals(CM_MUST_NOT_HAVE_OTHER_DATA);
    }

    private boolean isMissingVersionDataWhenItIsRequired(CertificationResultConformanceMethod conformanceMethod) {
        return conformanceMethod.getConformanceMethod() != null
                && !StringUtils.isEmpty(conformanceMethod.getConformanceMethod().getName())
                && !conformanceMethod.getConformanceMethod().getName().equalsIgnoreCase(CM_MUST_NOT_HAVE_OTHER_DATA)
                && StringUtils.isEmpty(conformanceMethod.getConformanceMethodVersion());
    }

    private boolean hasVersionDataWhenItIsNotAllowed(CertificationResultConformanceMethod conformanceMethod) {
        return conformanceMethod.getConformanceMethod() != null
                && !StringUtils.isEmpty(conformanceMethod.getConformanceMethod().getName())
                && conformanceMethod.getConformanceMethod().getName().equalsIgnoreCase(CM_MUST_NOT_HAVE_OTHER_DATA)
                && !StringUtils.isEmpty(conformanceMethod.getConformanceMethodVersion());
    }

    private void reviewF3ConformanceMethodsForGapRequirement(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (!CollectionUtils.isEmpty(certResult.getConformanceMethods())) {
            certResult.getConformanceMethods().stream()
                .forEach(conformanceMethod -> reviewF3(listing, certResult, conformanceMethod));
        }
    }

    private void reviewF3(CertifiedProductSearchDetails listing,
            CertificationResult certResult, CertificationResultConformanceMethod conformanceMethod) {
        reviewF3ConformanceMethodForGapRequirement(listing, certResult, conformanceMethod);
        removeF3TestDataAndTestToolsIfNotApplicable(listing, certResult, conformanceMethod);
    }

    private void reviewF3ConformanceMethodForGapRequirement(CertifiedProductSearchDetails listing,
            CertificationResult certResult, CertificationResultConformanceMethod conformanceMethod) {
        if (BooleanUtils.isFalse(certResult.isGap()) && conformanceMethod.getConformanceMethod() != null
                && StringUtils.equals(conformanceMethod.getConformanceMethod().getName(), CM_F3_MUST_HAVE_GAP)) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.conformanceMethod.f3GapMismatch",
                    Util.formatCriteriaNumber(certResult.getCriterion()),
                    conformanceMethod.getConformanceMethod().getName(),
                    "false"));
        } else if (BooleanUtils.isTrue(certResult.isGap()) && conformanceMethod.getConformanceMethod() != null
                && StringUtils.equals(conformanceMethod.getConformanceMethod().getName(), CM_F3_CANNOT_HAVE_GAP)) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.conformanceMethod.f3GapMismatch",
                    Util.formatCriteriaNumber(certResult.getCriterion()),
                    conformanceMethod.getConformanceMethod().getName(),
                    "true"));
        }
    }

    private void removeF3TestDataAndTestToolsIfNotApplicable(CertifiedProductSearchDetails listing,
            CertificationResult certResult, CertificationResultConformanceMethod conformanceMethod) {
        if (BooleanUtils.isTrue(certResult.isGap()) && conformanceMethod.getConformanceMethod() != null
                && StringUtils.equals(conformanceMethod.getConformanceMethod().getName(), CM_F3_MUST_HAVE_GAP)) {
            if (!CollectionUtils.isEmpty(certResult.getTestToolsUsed())) {
                certResult.getTestToolsUsed().clear();
                listing.getWarningMessages().add(msgUtil.getMessage("listing.criteria.conformanceMethod.f3RemovedTestTools",
                        Util.formatCriteriaNumber(certResult.getCriterion()),
                        conformanceMethod.getConformanceMethod().getName()));
            }

            if (!CollectionUtils.isEmpty(certResult.getTestDataUsed())) {
                certResult.getTestDataUsed().clear();
                listing.getWarningMessages().add(msgUtil.getMessage("listing.criteria.conformanceMethod.f3RemovedTestData",
                        Util.formatCriteriaNumber(certResult.getCriterion()),
                        conformanceMethod.getConformanceMethod().getName()));
            }
        }
    }
}
