package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.service.CertificationCriterionService.Criteria2015;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.util.ValidationUtils;
import gov.healthit.chpl.validation.listing.reviewer.PermissionBasedReviewer;

@Component("requiredAndRelatedCriteriaReviewer")
public class RequiredAndRelatedCriteriaReviewer extends PermissionBasedReviewer {

    private ErrorMessageUtil msgUtil;
    private CertificationCriterionService criterionService;
    private ValidationUtils validationUtils;

    @Autowired
    public RequiredAndRelatedCriteriaReviewer(CertificationCriterionService criterionService,
            ErrorMessageUtil msgUtil, ValidationUtils validationUtils,
            ResourcePermissions resourcePermissions) {
        super(msgUtil, resourcePermissions);
        this.criterionService = criterionService;
        this.msgUtil = msgUtil;
        this.validationUtils = validationUtils;
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        List<CertificationCriterion> attestedCriteria = validationUtils.getAttestedCriteria(listing);

        checkAlwaysRequiredCriteria(listing, attestedCriteria);
        checkACriteriaHaveRequiredDependencies(listing, attestedCriteria);
        checkA4A9CriteriaHaveRequiredDependencies(listing, attestedCriteria);
        checkBCriteriaHaveRequiredDependencies(listing, attestedCriteria);
        checkCCriteriaHaveRequiredDependencies(listing, attestedCriteria);
        checkE1CriterionHasRequiredDependencies(listing, attestedCriteria);
        checkE3CriterionHasRequiredDependencies(listing, attestedCriteria);
        checkFCriteriaHaveRequiredDependencies(listing, attestedCriteria);
        checkG6RequiredDependencies(listing, attestedCriteria);
        checkG7G9G10RequiredDependencies(listing, attestedCriteria);
        checkHCriteriaHaveRequiredDependencies(listing, attestedCriteria);
        checkH1PlusB1Criteria(listing, attestedCriteria);
    }

    private void checkAlwaysRequiredCriteria(CertifiedProductSearchDetails listing, List<CertificationCriterion> attestedCriteria) {
        CertificationCriterion g4 = criterionService.get(Criteria2015.G_4);
        CertificationCriterion g5 = criterionService.get(Criteria2015.G_5);
        if (!isInList(g4, attestedCriteria)) {
            listing.addBusinessErrorMessage(msgUtil.getMessage("listing.criteriaRequired", Util.formatCriteriaNumber(g4)));
        }
        if (!isInList(g5, attestedCriteria)) {
            listing.addBusinessErrorMessage(msgUtil.getMessage("listing.criteriaRequired", Util.formatCriteriaNumber(g5)));
        }
    }

    private void checkACriteriaHaveRequiredDependencies(CertifiedProductSearchDetails listing, List<CertificationCriterion> attestedCriteria) {
        final List<CertificationCriterion> aCriteria = Stream.of(
                criterionService.get(Criteria2015.A_1),
                criterionService.get(Criteria2015.A_2),
                criterionService.get(Criteria2015.A_3),
                criterionService.get(Criteria2015.A_5),
                criterionService.get(Criteria2015.A_12),
                criterionService.get(Criteria2015.A_14),
                criterionService.get(Criteria2015.A_15))
                .collect(Collectors.toList());

        List<CertificationCriterion> requiredByACriteria = Stream.of(
                criterionService.get(Criteria2015.D_1),
                criterionService.get(Criteria2015.D_2_CURES),
                criterionService.get(Criteria2015.D_3_CURES),
                criterionService.get(Criteria2015.D_4),
                criterionService.get(Criteria2015.D_5),
                criterionService.get(Criteria2015.D_6),
                criterionService.get(Criteria2015.D_7))
                .collect(Collectors.toList());

        boolean hasAnyNonRemovedACriteria = attestedCriteria.stream()
                .filter(attestedCriterion -> BooleanUtils.isFalse(attestedCriterion.getRemoved()))
                .filter(attestedCriterion -> isInList(attestedCriterion, aCriteria))
                .findAny().isPresent();

        if (hasAnyNonRemovedACriteria) {
            requiredByACriteria.stream()
                    .filter(requiredCriterion -> BooleanUtils.isFalse(requiredCriterion.getRemoved()))
                    .filter(requiredCriterion -> !isInList(requiredCriterion, attestedCriteria))
                    .forEach(missingRequiredCriterion -> listing.addBusinessErrorMessage(
                            msgUtil.getMessage("listing.criteria.complementaryCriteriaRequired", "170.315 (a)(*)", Util.formatCriteriaNumber(missingRequiredCriterion))));
        }
    }

    private void checkA4A9CriteriaHaveRequiredDependencies(CertifiedProductSearchDetails listing, List<CertificationCriterion> attestedCriteria) {
        final List<CertificationCriterion> aCriteriaWithExceptions = Stream.of(
                criterionService.get(Criteria2015.A_4),
                criterionService.get(Criteria2015.A_9))
                .collect(Collectors.toList());
        List<CertificationCriterion> requiredByACriteria = Stream.of(
                criterionService.get(Criteria2015.D_1),
                criterionService.get(Criteria2015.D_2_CURES),
                criterionService.get(Criteria2015.D_3_CURES),
                criterionService.get(Criteria2015.D_5),
                criterionService.get(Criteria2015.D_6),
                criterionService.get(Criteria2015.D_7))
                .collect(Collectors.toList());

        boolean hasAnyACriteriaWithExceptions = attestedCriteria.stream()
                .filter(attestedCriterion -> BooleanUtils.isFalse(attestedCriterion.getRemoved()))
                .filter(attestedCriterion -> isInList(attestedCriterion, aCriteriaWithExceptions))
                .findAny().isPresent();

        if (hasAnyACriteriaWithExceptions) {
            requiredByACriteria.stream()
                    .filter(requiredCriterion -> BooleanUtils.isFalse(requiredCriterion.getRemoved()))
                    .filter(requiredCriterion -> !isInList(requiredCriterion, attestedCriteria))
                    .forEach(missingRequiredCriterion -> listing.addBusinessErrorMessage(
                            msgUtil.getMessage("listing.criteria.complementaryCriteriaRequired", "170.315 (a)(*)", Util.formatCriteriaNumber(missingRequiredCriterion))));
        }
    }

    private void checkBCriteriaHaveRequiredDependencies(CertifiedProductSearchDetails listing, List<CertificationCriterion> attestedCriteria) {
        List<CertificationCriterion> bCriteria = Stream.of(
                criterionService.get(Criteria2015.B_1_CURES),
                criterionService.get(Criteria2015.B_2_CURES),
                criterionService.get(Criteria2015.B_3_CURES),
                criterionService.get(Criteria2015.B_6),
                criterionService.get(Criteria2015.B_7_CURES),
                criterionService.get(Criteria2015.B_8_CURES),
                criterionService.get(Criteria2015.B_9_CURES))
                .collect(Collectors.toList());
        List<CertificationCriterion> requiredByBCriteria = Stream.of(
                criterionService.get(Criteria2015.D_1),
                criterionService.get(Criteria2015.D_2_CURES),
                criterionService.get(Criteria2015.D_3_CURES),
                criterionService.get(Criteria2015.D_5),
                criterionService.get(Criteria2015.D_6),
                criterionService.get(Criteria2015.D_7),
                criterionService.get(Criteria2015.D_8))
                .collect(Collectors.toList());

        boolean hasAnyNonRemovedBCriteria = attestedCriteria.stream()
                .filter(attestedCriterion -> BooleanUtils.isFalse(attestedCriterion.getRemoved()))
                .filter(attestedCriterion -> isInList(attestedCriterion, bCriteria))
                .findAny().isPresent();

        if (hasAnyNonRemovedBCriteria) {
            requiredByBCriteria.stream()
                    .filter(requiredCriterion -> BooleanUtils.isFalse(requiredCriterion.getRemoved()))
                    .filter(requiredCriterion -> !isInList(requiredCriterion, attestedCriteria))
                    .forEach(missingRequiredCriterion -> listing.addBusinessErrorMessage(
                            msgUtil.getMessage("listing.criteria.complementaryCriteriaRequired", "170.315 (b)(*)", Util.formatCriteriaNumber(missingRequiredCriterion))));
        }
    }

    private void checkCCriteriaHaveRequiredDependencies(CertifiedProductSearchDetails listing, List<CertificationCriterion> attestedCriteria) {
        List<CertificationCriterion> cCriteria = Stream.of(
                criterionService.get(Criteria2015.C_1),
                criterionService.get(Criteria2015.C_2),
                criterionService.get(Criteria2015.C_3_CURES),
                criterionService.get(Criteria2015.C_4))
                .collect(Collectors.toList());

        List<CertificationCriterion> requiredByCCriteria = Stream.of(
                criterionService.get(Criteria2015.D_1),
                criterionService.get(Criteria2015.D_2_CURES),
                criterionService.get(Criteria2015.D_3_CURES),
                criterionService.get(Criteria2015.D_5))
                .collect(Collectors.toList());

        boolean hasAnyNonRemovedCCriteria = attestedCriteria.stream()
                .filter(attestedCriterion -> BooleanUtils.isFalse(attestedCriterion.getRemoved()))
                .filter(attestedCriterion -> isInList(attestedCriterion, cCriteria))
                .findAny().isPresent();

        if (hasAnyNonRemovedCCriteria) {
            requiredByCCriteria.stream()
                    .filter(requiredCriterion -> BooleanUtils.isFalse(requiredCriterion.getRemoved()))
                    .filter(requiredCriterion -> !isInList(requiredCriterion, attestedCriteria))
                    .forEach(missingRequiredCriterion -> listing.addBusinessErrorMessage(
                            msgUtil.getMessage("listing.criteria.complementaryCriteriaRequired", "170.315 (c)(*)", Util.formatCriteriaNumber(missingRequiredCriterion))));
        }
    }

    private void checkE1CriterionHasRequiredDependencies(CertifiedProductSearchDetails listing, List<CertificationCriterion> attestedCriteria) {
        List<CertificationCriterion> requiredByE1Criteria = Stream.of(
                criterionService.get(Criteria2015.D_1),
                criterionService.get(Criteria2015.D_2_CURES),
                criterionService.get(Criteria2015.D_3_CURES),
                criterionService.get(Criteria2015.D_5),
                criterionService.get(Criteria2015.D_7),
                criterionService.get(Criteria2015.D_9))
                .collect(Collectors.toList());
        CertificationCriterion e1Cures = criterionService.get(Criteria2015.E_1_CURES);

        boolean hasE1Cures = attestedCriteria.stream()
                .filter(attestedCriterion -> BooleanUtils.isFalse(attestedCriterion.getRemoved()))
                .filter(attestedCriterion -> attestedCriterion.getId().equals(e1Cures.getId()))
                .findAny().isPresent();

        if (hasE1Cures) {
            requiredByE1Criteria.stream()
                    .filter(requiredCriterion -> BooleanUtils.isFalse(requiredCriterion.getRemoved()))
                    .filter(requiredCriterion -> !isInList(requiredCriterion, attestedCriteria))
                    .forEach(missingRequiredCriterion -> listing.addBusinessErrorMessage(
                            msgUtil.getMessage("listing.criteria.complementaryCriteriaRequired", Util.formatCriteriaNumber(e1Cures), Util.formatCriteriaNumber(missingRequiredCriterion))));
        }
    }

    private void checkE3CriterionHasRequiredDependencies(CertifiedProductSearchDetails listing, List<CertificationCriterion> attestedCriteria) {
        List<CertificationCriterion> requiredByE3Criterion = Stream.of(
                criterionService.get(Criteria2015.D_1),
                criterionService.get(Criteria2015.D_2_CURES),
                criterionService.get(Criteria2015.D_3_CURES),
                criterionService.get(Criteria2015.D_5),
                criterionService.get(Criteria2015.D_9))
                .collect(Collectors.toList());
        CertificationCriterion e3 = criterionService.get(Criteria2015.E_3);

        boolean hasE3Criteria = attestedCriteria.stream()
                .filter(attestedCriterion -> BooleanUtils.isFalse(attestedCriterion.getRemoved()))
                .filter(attestedCriterion -> attestedCriterion.getId().equals(e3.getId()))
                .findAny().isPresent();

        if (hasE3Criteria) {
            requiredByE3Criterion.stream()
                    .filter(requiredCriterion -> BooleanUtils.isFalse(requiredCriterion.getRemoved()))
                    .filter(requiredCriterion -> !isInList(requiredCriterion, attestedCriteria))
                    .forEach(missingRequiredCriterion -> listing.addBusinessErrorMessage(
                            msgUtil.getMessage("listing.criteria.complementaryCriteriaRequired", Util.formatCriteriaNumber(e3), Util.formatCriteriaNumber(missingRequiredCriterion))));
        }
    }

    private void checkFCriteriaHaveRequiredDependencies(CertifiedProductSearchDetails listing, List<CertificationCriterion> attestedCriteria) {
        List<CertificationCriterion> fCriteria = Stream.of(
                criterionService.get(Criteria2015.F_1),
                criterionService.get(Criteria2015.F_2),
                criterionService.get(Criteria2015.F_3),
                criterionService.get(Criteria2015.F_4),
                criterionService.get(Criteria2015.F_5_CURES),
                criterionService.get(Criteria2015.F_6),
                criterionService.get(Criteria2015.F_7))
                .collect(Collectors.toList());

        List<CertificationCriterion> requiredByFCriteria = Stream.of(
                criterionService.get(Criteria2015.D_1),
                criterionService.get(Criteria2015.D_2_CURES),
                criterionService.get(Criteria2015.D_3_CURES),
                criterionService.get(Criteria2015.D_7))
                .collect(Collectors.toList());

        boolean hasAnyNonRemovedFCriteria = attestedCriteria.stream()
                .filter(attestedCriterion -> BooleanUtils.isFalse(attestedCriterion.getRemoved()))
                .filter(attestedCriterion -> isInList(attestedCriterion, fCriteria))
                .findAny().isPresent();

        if (hasAnyNonRemovedFCriteria) {
            requiredByFCriteria.stream()
                    .filter(requiredCriterion -> BooleanUtils.isFalse(requiredCriterion.getRemoved()))
                    .filter(requiredCriterion -> !isInList(requiredCriterion, attestedCriteria))
                    .forEach(missingRequiredCriterion -> listing.addBusinessErrorMessage(
                            msgUtil.getMessage("listing.criteria.complementaryCriteriaRequired", "170.315 (f)(*)", Util.formatCriteriaNumber(missingRequiredCriterion))));
        }
    }

    private void checkG6RequiredDependencies(CertifiedProductSearchDetails listing, List<CertificationCriterion> attestedCriteria) {
        CertificationCriterion g6 = criterionService.get(Criteria2015.G_6_CURES);
        List<CertificationCriterion> criteriaRequiringG6 = Stream.of(
                criterionService.get(Criteria2015.B_1_OLD),
                criterionService.get(Criteria2015.B_1_CURES),
                criterionService.get(Criteria2015.B_2_OLD),
                criterionService.get(Criteria2015.B_2_CURES),
                criterionService.get(Criteria2015.B_4),
                criterionService.get(Criteria2015.B_6),
                criterionService.get(Criteria2015.B_9_OLD),
                criterionService.get(Criteria2015.B_9_CURES),
                criterionService.get(Criteria2015.E_1_OLD),
                criterionService.get(Criteria2015.E_1_CURES),
                criterionService.get(Criteria2015.G_9_OLD),
                criterionService.get(Criteria2015.G_9_CURES))
                .collect(Collectors.toList());

        List<CertificationCriterion> presentAttestedG6Criteria = attestedCriteria.stream()
                .filter(cert -> cert.getRemoved() == null || cert.getRemoved().equals(Boolean.FALSE))
                .filter(cert -> isInList(cert, criteriaRequiringG6))
                .collect(Collectors.<CertificationCriterion> toList());
        List<CertificationCriterion> removedAttestedG6Criteria = attestedCriteria.stream()
                .filter(cert -> cert.getRemoved() != null && cert.getRemoved().equals(Boolean.TRUE))
                .filter(cert -> isInList(cert, criteriaRequiringG6))
                .collect(Collectors.<CertificationCriterion> toList());
        boolean hasG6 = isInList(g6, attestedCriteria);

        if (presentAttestedG6Criteria != null && presentAttestedG6Criteria.size() > 0 && !hasG6) {
            listing.addBusinessErrorMessage(msgUtil.getMessage("listing.criteriaRequired", Util.formatCriteriaNumber(g6)));
        }
        if (removedAttestedG6Criteria != null && removedAttestedG6Criteria.size() > 0
                && (presentAttestedG6Criteria == null || presentAttestedG6Criteria.size() == 0)
                && !hasG6) {
            addListingWarningByPermission(listing, msgUtil.getMessage("listing.criteriaRequired", Util.formatCriteriaNumber(g6)));
        }
    }

    private void checkG7G9G10RequiredDependencies(CertifiedProductSearchDetails listing, List<CertificationCriterion> attestedCriteria) {
        List<CertificationCriterion> g7g9g10Criteria = Stream.of(
                criterionService.get(Criteria2015.G_7),
                criterionService.get(Criteria2015.G_9_CURES),
                criterionService.get(Criteria2015.G_10))
                .collect(Collectors.toList());
        List<CertificationCriterion> d2OrD10Criteria = Stream.of(
                criterionService.get(Criteria2015.D_2_CURES),
                criterionService.get(Criteria2015.D_10_CURES))
                .collect(Collectors.toList());
        List<CertificationCriterion> d1AndD9Criteria = Stream.of(
                criterionService.get(Criteria2015.D_1),
                criterionService.get(Criteria2015.D_9))
                .collect(Collectors.toList());

        boolean hasAnyNonRemovedG7G9G10Criteria = attestedCriteria.stream()
                .filter(attestedCriterion -> BooleanUtils.isFalse(attestedCriterion.getRemoved()))
                .filter(attestedCriterion -> isInList(attestedCriterion, g7g9g10Criteria))
                .findAny().isPresent();

        if (hasAnyNonRemovedG7G9G10Criteria) {
            String g7G9G10 = g7g9g10Criteria.stream()
                    .map(criterion -> Util.formatCriteriaNumber(criterion))
                    .collect(Collectors.joining(" or "));
            d1AndD9Criteria.stream()
                    .filter(requiredCriterion -> BooleanUtils.isFalse(requiredCriterion.getRemoved()))
                    .filter(requiredCriterion -> !isInList(requiredCriterion, attestedCriteria))
                    .forEach(missingRequiredCriterion -> listing.addBusinessErrorMessage(
                            msgUtil.getMessage("listing.criteria.complementaryCriteriaRequired", g7G9G10, Util.formatCriteriaNumber(missingRequiredCriterion))));
            if (!isAnyInList(d2OrD10Criteria, attestedCriteria)) {
                String d2D10 = d2OrD10Criteria.stream()
                        .map(criterion -> Util.formatCriteriaNumber(criterion))
                        .collect(Collectors.joining(" or "));
                listing.addBusinessErrorMessage(
                        msgUtil.getMessage("listing.criteria.complementaryCriteriaRequired", g7G9G10, d2D10));
            }
        }
    }

    private void checkHCriteriaHaveRequiredDependencies(CertifiedProductSearchDetails listing, List<CertificationCriterion> attestedCriteria) {
        List<CertificationCriterion> hCriteria = Stream.of(
                criterionService.get(Criteria2015.H_1),
                criterionService.get(Criteria2015.H_2))
                .collect(Collectors.toList());
        List<CertificationCriterion> requiredByHCriteria = Stream.of(
                criterionService.get(Criteria2015.D_1),
                criterionService.get(Criteria2015.D_2_CURES),
                criterionService.get(Criteria2015.D_3_CURES))
                .collect(Collectors.toList());

        boolean hasAnyNonRemovedHCriteria = attestedCriteria.stream()
                .filter(attestedCriterion -> BooleanUtils.isFalse(attestedCriterion.getRemoved()))
                .filter(attestedCriterion -> isInList(attestedCriterion, hCriteria))
                .findAny().isPresent();

        if (hasAnyNonRemovedHCriteria) {
            requiredByHCriteria.stream()
                    .filter(requiredCriterion -> BooleanUtils.isFalse(requiredCriterion.getRemoved()))
                    .filter(requiredCriterion -> !isInList(requiredCriterion, attestedCriteria))
                    .forEach(missingRequiredCriterion -> listing.addBusinessErrorMessage(
                            msgUtil.getMessage("listing.criteria.complementaryCriteriaRequired", "170.315 (h)(*)", Util.formatCriteriaNumber(missingRequiredCriterion))));
        }
    }

    private void checkH1PlusB1Criteria(CertifiedProductSearchDetails listing, List<CertificationCriterion> attestedCriteria) {
        CertificationCriterion h1 = criterionService.get(Criteria2015.H_1);
        CertificationCriterion b1Cures = criterionService.get(Criteria2015.B_1_CURES);

        boolean attestsH1 = isInList(h1, attestedCriteria);
        boolean attestsB1 = isInList(b1Cures, attestedCriteria);

        if (attestsH1 && !attestsB1) {
            listing.addBusinessErrorMessage(
                    msgUtil.getMessage("listing.criteria.complementaryCriteriaRequired", Util.formatCriteriaNumber(h1), Util.formatCriteriaNumber(b1Cures)));
        }
    }

    private boolean isInList(CertificationCriterion criterionToLookFor, List<CertificationCriterion> criteriaList) {
        return criteriaList.stream()
                .filter(criterionFromList -> criterionFromList.getId().equals(criterionToLookFor.getId()))
                .findFirst().isPresent();
    }

    private boolean isAnyInList(List<CertificationCriterion> criteriaToLookFor, List<CertificationCriterion> criteriaList) {
        List<Long> criteriaIdsToLookFor = criteriaToLookFor.stream()
                .map(criterion -> criterion.getId())
                .collect(Collectors.toList());
        return criteriaList.stream()
                .filter(criterionFromList -> criteriaIdsToLookFor.contains(criterionFromList.getId()))
                .findFirst().isPresent();
    }
}
