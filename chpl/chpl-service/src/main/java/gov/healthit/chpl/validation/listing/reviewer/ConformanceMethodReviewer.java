package gov.healthit.chpl.validation.listing.reviewer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.conformanceMethod.dao.ConformanceMethodDAO;
import gov.healthit.chpl.conformanceMethod.domain.CertificationResultConformanceMethod;
import gov.healthit.chpl.conformanceMethod.domain.ConformanceMethod;
import gov.healthit.chpl.conformanceMethod.domain.ConformanceMethodCriteriaMap;
import gov.healthit.chpl.dao.CertificationResultDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.service.CertificationCriterionService.Criteria2015;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.DateUtil;
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
    private CertificationResultDAO certResultDao;
    private ValidationUtils validationUtils;
    private CertificationResultRules certResultRules;
    private CertificationCriterion f3;

    @Autowired
    public ConformanceMethodReviewer(ConformanceMethodDAO conformanceMethodDao, CertificationResultDAO certResultDao,
            ErrorMessageUtil msgUtil,
            ValidationUtils validationUtils, CertificationResultRules certResultRules,
            CertificationCriterionService criteriaService,
            ResourcePermissions resourcePermissions) {
        super(msgUtil, resourcePermissions);
        this.msgUtil = msgUtil;
        this.certResultDao = certResultDao;
        this.validationUtils = validationUtils;
        this.certResultRules = certResultRules;
        this.resourcePermissions = resourcePermissions;
        f3 = criteriaService.get(Criteria2015.F_3);

        try {
            this.conformanceMethodCriteriaMap = conformanceMethodDao.getAllConformanceMethodCriteriaMap();
        } catch (EntityRetrievalException ex) {
            LOGGER.error("Could not initialize conformance method criteria map.", ex);
        }
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        listing.getCertificationResults().stream()
                .filter(certResult -> validationUtils.isEligibleForErrors(certResult))
                .forEach(certResult -> reviewCertificationResult(listing, certResult));
        listing.getCertificationResults().stream()
                .forEach(certResult -> removeConformanceMethodsIfNotApplicable(certResult));
    }

    private void reviewCertificationResult(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        reviewCriteriaCanHaveConformanceMethods(listing, certResult);
        fillInDefaultConformanceMethods(listing, certResult);
        removeOrReplaceConformanceMethodsInvalidForCriterion(listing, certResult);
        reviewConformanceMethodsRequired(listing, certResult);
        if (!CollectionUtils.isEmpty(certResult.getConformanceMethods())) {
            certResult.getConformanceMethods().stream()
                    .filter(conformanceMethod -> conformanceMethod.getConformanceMethod() != null)
                    .filter(conformanceMethod -> conformanceMethod.getConformanceMethod().getRemoved())
                    .forEach(removedConformanceMethod -> reviewRemovedConformanceMethodForIcsRequirement(listing, certResult, removedConformanceMethod));
            certResult.getConformanceMethods().stream()
                    .forEach(conformanceMethod -> reviewConformanceMethodFields(listing, certResult, conformanceMethod));
            if (certResult.getCriterion().getId().equals(f3.getId())) {
                reviewF3ConformanceMethodsForGapRequirement(listing, certResult);
            }
        }
    }

    private void reviewCriteriaCanHaveConformanceMethods(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (!certResultRules.hasCertOption(certResult.getCriterion().getId(), CertificationResultRules.CONFORMANCE_METHOD)) {
            if (!CollectionUtils.isEmpty(certResult.getConformanceMethods())) {
                listing.addWarningMessage(msgUtil.getMessage(
                        "listing.criteria.conformanceMethodNotApplicable", Util.formatCriteriaNumber(certResult.getCriterion())));
            }
        }
    }

    private void fillInDefaultConformanceMethods(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (certResult.getConformanceMethods() == null) {
            certResult.setConformanceMethods(new ArrayList<CertificationResultConformanceMethod>());
        }

        // The current upload template doesn't have a column for conformance method name for some criteria
        // even though it is required for all criteria. So for any of the criteria that don't have a column
        // for name, you might get into this "if" block - there could not be any conformance methods parsed
        // inside of the Handler code in the absence of a column in the file.
        if (CollectionUtils.isEmpty(certResult.getConformanceMethods())
                && getDefaultConformanceMethodForCriteria(certResult.getCriterion()) != null) {
            certResult.getConformanceMethods().add(CertificationResultConformanceMethod.builder().build());
        }

        certResult.getConformanceMethods().stream()
                .filter(conformanceMethod -> isConformanceMethodNameMissing(conformanceMethod))
                .forEach(conformanceMethod -> fillInDefaultConformanceMethod(listing, certResult, conformanceMethod));
    }

    private boolean isConformanceMethodNameMissing(CertificationResultConformanceMethod conformanceMethod) {
        return conformanceMethod.getConformanceMethod() == null
                || StringUtils.isEmpty(conformanceMethod.getConformanceMethod().getName());
    }

    private void fillInDefaultConformanceMethod(CertifiedProductSearchDetails listing, CertificationResult certResult,
            CertificationResultConformanceMethod conformanceMethod) {
        ConformanceMethod defaultConformanceMethod = getDefaultConformanceMethodForCriteria(certResult.getCriterion());
        if (defaultConformanceMethod != null) {
            conformanceMethod.setConformanceMethod(defaultConformanceMethod);
            // The upload file doesn't have fields for conformance methods for all the criteria that need them.
            // We will add a default CM for the cert result here if there is only one possible choice for
            // conformance method but we have to tell the user that we did it.
            // This code can't go in the reviewer otherwise during Test Procedure -> Conformance Method conversion
            // a default CM gets added to the listing and may make the converter think that the listing already has a CM.
            if (BooleanUtils.isFalse(certResult.getCriterion().getRemoved())) {
                listing.addWarningMessage(msgUtil.getMessage("listing.criteria.conformanceMethod.addedDefaultForCriterion",
                        Util.formatCriteriaNumber(certResult.getCriterion()),
                        defaultConformanceMethod.getName()));
            }
        }
    }

    private void removeOrReplaceConformanceMethodsInvalidForCriterion(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (CollectionUtils.isEmpty(certResult.getConformanceMethods())) {
            return;
        }
        Map<String, CertificationResultConformanceMethod> conformanceMethodsToReplace = new LinkedHashMap<String, CertificationResultConformanceMethod>();
        Iterator<CertificationResultConformanceMethod> conformanceMethodIter = certResult.getConformanceMethods().iterator();
        while (conformanceMethodIter.hasNext()) {
            CertificationResultConformanceMethod conformanceMethod = conformanceMethodIter.next();
            if (conformanceMethod.getConformanceMethod() != null && !isConformanceMethodAllowed(certResult, conformanceMethod)) {
                ConformanceMethod defaultConformanceMethodForCriterion = getDefaultConformanceMethodForCriteria(certResult.getCriterion());
                if (defaultConformanceMethodForCriterion != null) {
                    CertificationResultConformanceMethod toAdd = CertificationResultConformanceMethod.builder()
                            .conformanceMethod(defaultConformanceMethodForCriterion)
                            .conformanceMethodVersion(conformanceMethod.getConformanceMethodVersion())
                            .build();
                    conformanceMethodsToReplace.put(conformanceMethod.getConformanceMethod().getName(), toAdd);
                    conformanceMethodIter.remove();
                } else {
                    conformanceMethodIter.remove();
                    listing.addWarningMessage(msgUtil.getMessage("listing.criteria.conformanceMethod.invalidCriteriaRemoved",
                            conformanceMethod.getConformanceMethod().getName(),
                            Util.formatCriteriaNumber(certResult.getCriterion())));
                }
            }
        }

        conformanceMethodsToReplace.keySet().stream()
                .forEach(replacedConformanceMethodName -> {
                    CertificationResultConformanceMethod cmToAdd = conformanceMethodsToReplace.get(replacedConformanceMethodName);
                    certResult.getConformanceMethods().add(cmToAdd);
                    listing.addWarningMessage(msgUtil.getMessage("listing.criteria.conformanceMethod.invalidCriteriaReplaced",
                            replacedConformanceMethodName,
                            Util.formatCriteriaNumber(certResult.getCriterion()),
                            cmToAdd.getConformanceMethod().getName()));
                });
    }

    private ConformanceMethod getDefaultConformanceMethodForCriteria(CertificationCriterion criterion) {
        List<ConformanceMethod> allowedConformanceMethodsForCriterion = getConformanceMethodsForCriterion(criterion);
        if (!CollectionUtils.isEmpty(allowedConformanceMethodsForCriterion)
                && allowedConformanceMethodsForCriterion.size() == 1) {
            return allowedConformanceMethodsForCriterion.get(0);
        }
        return null;
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
        if (certResultRules.hasCertOption(certResult.getCriterion().getId(), CertificationResultRules.CONFORMANCE_METHOD)
                && CollectionUtils.isEmpty(certResult.getConformanceMethods())) {
            if (CollectionUtils.isEmpty(certResult.getConformanceMethods())) {
                addBusinessCriterionError(listing, certResult, "listing.criteria.conformanceMethod.missingConformanceMethod",
                        Util.formatCriteriaNumber(certResult.getCriterion()));
            }
        }
    }

    private void removeConformanceMethodsIfNotApplicable(CertificationResult certResult) {
        if (!certResultRules.hasCertOption(certResult.getCriterion().getId(), CertificationResultRules.CONFORMANCE_METHOD)) {
            certResult.setConformanceMethods(null);
        }
    }

    private void reviewConformanceMethodFields(CertifiedProductSearchDetails listing, CertificationResult certResult,
            CertificationResultConformanceMethod conformanceMethod) {
        reviewConformanceMethodNotNullAndHasId(listing, certResult, conformanceMethod);
        reviewConformanceMethodVersionRequirements(listing, certResult, conformanceMethod);
    }

    private void reviewConformanceMethodNotNullAndHasId(CertifiedProductSearchDetails listing, CertificationResult certResult,
            CertificationResultConformanceMethod conformanceMethod) {
        if (conformanceMethod.getConformanceMethod() == null || conformanceMethod.getConformanceMethod().getId() == null) {
            listing.addBusinessErrorMessage(msgUtil.getMessage("listing.criteria.conformanceMethod.missingConformanceMethod",
                    Util.formatCriteriaNumber(certResult.getCriterion())));
        }
    }

    private void reviewConformanceMethodVersionRequirements(CertifiedProductSearchDetails listing, CertificationResult certResult,
            CertificationResultConformanceMethod conformanceMethod) {
        if (isMissingVersionDataWhenItIsRequired(conformanceMethod)) {
            addBusinessCriterionError(listing, certResult,
                    "listing.criteria.conformanceMethod.missingConformanceMethodVersion",
                    Util.formatCriteriaNumber(certResult.getCriterion()),
                    conformanceMethod.getConformanceMethod().getName());
        }
        if (hasVersionDataWhenItIsNotAllowed(conformanceMethod)) {
            if (mayOnlyHaveConformanceMethodWithoutVersion(certResult, conformanceMethod)) {
                listing.addWarningMessage(msgUtil.getMessage(
                        "listing.criteria.conformanceMethod.unallowedConformanceMethodVersionRemoved",
                        Util.formatCriteriaNumber(certResult.getCriterion()),
                        conformanceMethod.getConformanceMethod().getName(),
                        conformanceMethod.getConformanceMethodVersion()));
                conformanceMethod.setConformanceMethodVersion(null);
            } else {
                addBusinessCriterionError(listing, certResult,
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

    private void reviewRemovedConformanceMethodForIcsRequirement(CertifiedProductSearchDetails listing, CertificationResult certResult,
            CertificationResultConformanceMethod conformanceMethod) {
        if (conformanceMethod.getConformanceMethod().getRemovalDate() != null
                && conformanceMethod.getConformanceMethod().getRemovalDate().isBefore(DateUtil.toLocalDate(listing.getCertificationDate()))) {
            // check listing for ICS
            if (listing.getIcs() != null && !CollectionUtils.isEmpty(listing.getIcs().getParents())) {
                Optional<CertifiedProduct> parentWithConformanceMethodOnCriterion = listing.getIcs().getParents().stream()
                        .filter(icsParent -> doesParentHaveRemovedConformanceMethodForCriterion(icsParent.getId(), certResult.getCriterion(),
                                conformanceMethod.getConformanceMethod()))
                        .findAny();
                if (parentWithConformanceMethodOnCriterion.isEmpty()) {
                    listing.addBusinessErrorMessage(msgUtil.getMessage("listing.conformanceMethod.criteria.conformanceMethodRemovedWithoutIcs",
                            Util.formatCriteriaNumber(certResult.getCriterion()),
                            conformanceMethod.getConformanceMethod().getName()));
                }
            } else {
                listing.addBusinessErrorMessage(msgUtil.getMessage("listing.conformanceMethod.criteria.conformanceMethodRemovedWithoutIcs",
                        Util.formatCriteriaNumber(certResult.getCriterion()),
                        conformanceMethod.getConformanceMethod().getName()));
            }
        } // else the certification date is before the removal date so that's not an issue
    }

    private boolean doesParentHaveRemovedConformanceMethodForCriterion(Long parentListingId,
            CertificationCriterion criterion, ConformanceMethod conformanceMethod) {
        List<CertificationResultConformanceMethod> conformanceMethodsForParentCertResult = certResultDao.getConformanceMethodsByListingAndCriterionId(parentListingId, criterion.getId());
        if (conformanceMethodsForParentCertResult == null) {
            return false;
        }
        return conformanceMethodsForParentCertResult.stream()
                .filter(parentCmForCertResult -> parentCmForCertResult.getConformanceMethod().getId().equals(conformanceMethod.getId()))
                .findAny().isPresent();
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
            listing.addBusinessErrorMessage(msgUtil.getMessage("listing.criteria.conformanceMethod.f3GapMismatch",
                    Util.formatCriteriaNumber(certResult.getCriterion()),
                    conformanceMethod.getConformanceMethod().getName(),
                    "false"));
        } else if (BooleanUtils.isTrue(certResult.isGap()) && conformanceMethod.getConformanceMethod() != null
                && StringUtils.equals(conformanceMethod.getConformanceMethod().getName(), CM_F3_CANNOT_HAVE_GAP)) {
            listing.addBusinessErrorMessage(msgUtil.getMessage("listing.criteria.conformanceMethod.f3GapMismatch",
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
                listing.addWarningMessage(msgUtil.getMessage("listing.criteria.conformanceMethod.f3RemovedTestTools",
                        Util.formatCriteriaNumber(certResult.getCriterion()),
                        conformanceMethod.getConformanceMethod().getName()));
            }

            if (!CollectionUtils.isEmpty(certResult.getTestDataUsed())) {
                certResult.getTestDataUsed().clear();
                listing.addWarningMessage(msgUtil.getMessage("listing.criteria.conformanceMethod.f3RemovedTestData",
                        Util.formatCriteriaNumber(certResult.getCriterion()),
                        conformanceMethod.getConformanceMethod().getName()));
            }
        }
    }
}
