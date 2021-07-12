package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
public class RequiredAndRelatedCriteriaReviewer  extends PermissionBasedReviewer {
    private static final String A_CRITERIA_NUMBERS_START = "170.315 (a)";
    private static final String B_CRITERIA_NUMBERS_START = "170.315 (b)";
    private static final String C_CRITERIA_NUMBERS_START = "170.315 (c)";
    private static final String F_CRITERIA_NUMBERS_START = "170.315 (f)";
    private static final String H_CRITERIA_NUMBERS_START = "170.315 (h)";

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

    //TODO: break up the reviewers and tests into multiple classes

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        List<CertificationCriterion> attestedCriteria = validationUtils.getAttestedCriteria(listing);

        checkAlwaysRequiredCriteria(listing, attestedCriteria);
        checkACriteriaHaveRequiredDependencies(listing, attestedCriteria);
        checkBCriteriaHaveRequiredDependencies(listing, attestedCriteria);
        checkCCriteriaHaveRequiredDependencies(listing, attestedCriteria);
        checkE1CriterionHasRequiredDependencies(listing, attestedCriteria);
        checkE2E3CriteriaHaveRequiredDependencies(listing, attestedCriteria);
        checkFCriteriaHaveRequiredDependencies(listing, attestedCriteria);
        checkG6RequiredDependencies(listing, attestedCriteria);
        checkG7G8G9RequiredDependencies(listing, attestedCriteria);
        checkG10RequiredDependencies(listing, attestedCriteria);
        checkHCriteriaHaveRequiredDependencies(listing, attestedCriteria);
        checkH1PlusB1Criteria(listing, attestedCriteria);
    }

    private void checkAlwaysRequiredCriteria(CertifiedProductSearchDetails listing, List<CertificationCriterion> attestedCriteria) {
        CertificationCriterion g4 = criterionService.get(Criteria2015.G_4);
        CertificationCriterion g5 = criterionService.get(Criteria2015.G_5);
        if (!validationUtils.hasCriterion(g4, attestedCriteria)) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteriaRequired", Util.formatCriteriaNumber(g4)));
        }
        if (!validationUtils.hasCriterion(g5, attestedCriteria)) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteriaRequired", Util.formatCriteriaNumber(g5)));
        }
    }

    private void checkACriteriaHaveRequiredDependencies(CertifiedProductSearchDetails listing, List<CertificationCriterion> attestedCriteria) {
        List<CertificationCriterion> requiredByACriteria = Stream.of(
                criterionService.get(Criteria2015.D_1),
                criterionService.get(Criteria2015.D_2_OLD),
                criterionService.get(Criteria2015.D_2_CURES),
                criterionService.get(Criteria2015.D_3_OLD),
                criterionService.get(Criteria2015.D_3_CURES),
                criterionService.get(Criteria2015.D_5),
                criterionService.get(Criteria2015.D_6),
                criterionService.get(Criteria2015.D_7))
                .collect(Collectors.toList());
        List<CertificationCriterion> exceptionsToACriteria = Stream.of(
                criterionService.get(Criteria2015.A_4),
                criterionService.get(Criteria2015.A_9),
                criterionService.get(Criteria2015.A_10),
                criterionService.get(Criteria2015.A_13))
                .collect(Collectors.toList());
        List<CertificationCriterion> exceptionsToRequiredByACriteria = Stream.of(
                criterionService.get(Criteria2015.D_4))
                .collect(Collectors.toList());

        List<String> errors = validationUtils.checkClassOfCriteriaForMissingComplementaryCriteriaErrors(A_CRITERIA_NUMBERS_START,
                attestedCriteria, requiredByACriteria);
        listing.getErrorMessages().addAll(errors);
        List<String> warnings = validationUtils.checkClassOfCriteriaForMissingComplementaryCriteriaWarnings(A_CRITERIA_NUMBERS_START,
                attestedCriteria, requiredByACriteria);
        addListingWarningsByPermission(listing, warnings);

        errors = validationUtils.checkClassSubsetOfCriteriaForMissingComplementaryCriteriaErrors(A_CRITERIA_NUMBERS_START,
                attestedCriteria, exceptionsToRequiredByACriteria, exceptionsToACriteria);
        listing.getErrorMessages().addAll(errors);
        warnings = validationUtils.checkClassSubsetOfCriteriaForMissingComplementaryCriteriaWarnings(A_CRITERIA_NUMBERS_START,
                attestedCriteria, exceptionsToRequiredByACriteria, exceptionsToACriteria);
        addListingWarningsByPermission(listing, warnings);
    }

    private void checkBCriteriaHaveRequiredDependencies(CertifiedProductSearchDetails listing, List<CertificationCriterion> attestedCriteria) {
        List<CertificationCriterion> requiredByBCriteria = Stream.of(
                criterionService.get(Criteria2015.D_1),
                criterionService.get(Criteria2015.D_2_OLD),
                criterionService.get(Criteria2015.D_2_CURES),
                criterionService.get(Criteria2015.D_3_OLD),
                criterionService.get(Criteria2015.D_3_CURES),
                criterionService.get(Criteria2015.D_5),
                criterionService.get(Criteria2015.D_6),
                criterionService.get(Criteria2015.D_7),
                criterionService.get(Criteria2015.D_8))
                .collect(Collectors.toList());
        List<CertificationCriterion> excludedBCriteria = Stream.of(
                criterionService.get(Criteria2015.B_10))
                .collect(Collectors.toList());

        List<String> errors = validationUtils.checkClassSubsetOfCriteriaForMissingComplementaryCriteriaErrors(B_CRITERIA_NUMBERS_START,
                attestedCriteria,
                requiredByBCriteria,
                excludedBCriteria);
        listing.getErrorMessages().addAll(errors);

        List<String> warnings = validationUtils.checkClassSubsetOfCriteriaForMissingComplementaryCriteriaWarnings(
                B_CRITERIA_NUMBERS_START,
                attestedCriteria,
                requiredByBCriteria,
                excludedBCriteria);
        addListingWarningsByPermission(listing, warnings);
    }

    private void checkCCriteriaHaveRequiredDependencies(CertifiedProductSearchDetails listing, List<CertificationCriterion> attestedCriteria) {
        List<CertificationCriterion> requiredByCCriteria = Stream.of(
                criterionService.get(Criteria2015.D_1),
                criterionService.get(Criteria2015.D_2_OLD),
                criterionService.get(Criteria2015.D_2_CURES),
                criterionService.get(Criteria2015.D_3_OLD),
                criterionService.get(Criteria2015.D_3_CURES),
                criterionService.get(Criteria2015.D_5))
                .collect(Collectors.toList());

        List<String> errors = validationUtils.checkClassOfCriteriaForMissingComplementaryCriteriaErrors(C_CRITERIA_NUMBERS_START,
                attestedCriteria,
                requiredByCCriteria);
        listing.getErrorMessages().addAll(errors);

        List<String> warnings = validationUtils.checkClassOfCriteriaForMissingComplementaryCriteriaWarnings(
                C_CRITERIA_NUMBERS_START,
                attestedCriteria,
                requiredByCCriteria);
        addListingWarningsByPermission(listing, warnings);
    }

    private void checkE1CriterionHasRequiredDependencies(CertifiedProductSearchDetails listing, List<CertificationCriterion> attestedCriteria) {
        List<CertificationCriterion> requiredByE1Criteria = Stream.of(
                criterionService.get(Criteria2015.D_1),
                criterionService.get(Criteria2015.D_2_OLD),
                criterionService.get(Criteria2015.D_2_CURES),
                criterionService.get(Criteria2015.D_3_OLD),
                criterionService.get(Criteria2015.D_3_CURES),
                criterionService.get(Criteria2015.D_5),
                criterionService.get(Criteria2015.D_7),
                criterionService.get(Criteria2015.D_9))
                .collect(Collectors.toList());
        CertificationCriterion e1 = criterionService.get(Criteria2015.E_1_OLD);
        CertificationCriterion e1Cures = criterionService.get(Criteria2015.E_1_CURES);

        List<String> errors = validationUtils.checkSpecificCriterionForMissingComplementaryCriteriaErrors(
                e1,
                attestedCriteria,
                requiredByE1Criteria);
        listing.getErrorMessages().addAll(errors);

        errors = validationUtils.checkSpecificCriterionForMissingComplementaryCriteriaErrors(
                e1Cures,
                attestedCriteria,
                requiredByE1Criteria);
        listing.getErrorMessages().addAll(errors);

    }

    private void checkE2E3CriteriaHaveRequiredDependencies(CertifiedProductSearchDetails listing, List<CertificationCriterion> attestedCriteria) {
        checkE2E3AllRequiredDependencyGroup(listing, attestedCriteria);
        checkE2E3AnyD2RequiredDependencyGroup(listing, attestedCriteria);
        checkE2E3AnyD3RequiredDependencyGroup(listing, attestedCriteria);
    }

    private void checkE2E3AllRequiredDependencyGroup(CertifiedProductSearchDetails listing, List<CertificationCriterion> attestedCriteria) {
        List<CertificationCriterion> requiredByE2E3Criteria = Stream.of(
                criterionService.get(Criteria2015.D_1),
                criterionService.get(Criteria2015.D_5),
                criterionService.get(Criteria2015.D_9))
                .collect(Collectors.toList());
        List<CertificationCriterion> e2e3Criteria = Stream.of(
                criterionService.get(Criteria2015.E_2),
                criterionService.get(Criteria2015.E_3))
                .collect(Collectors.toList());

        List<String> errors = validationUtils.checkComplementaryCriteriaAllRequired(
                e2e3Criteria,
                requiredByE2E3Criteria,
                attestedCriteria);
        listing.getErrorMessages().addAll(errors);
    }

    private void checkE2E3AnyD2RequiredDependencyGroup(CertifiedProductSearchDetails listing, List<CertificationCriterion> attestedCriteria) {
        List<CertificationCriterion> requiredByE2E3Criteria = Stream.of(
                criterionService.get(Criteria2015.D_2_OLD),
                criterionService.get(Criteria2015.D_2_CURES))
                .collect(Collectors.toList());
        List<CertificationCriterion> e2e3Criteria = Stream.of(
                criterionService.get(Criteria2015.E_2),
                criterionService.get(Criteria2015.E_3))
                .collect(Collectors.toList());

        List<String> errors = validationUtils.checkComplementaryCriteriaAnyRequired(
                e2e3Criteria,
                requiredByE2E3Criteria,
                attestedCriteria);
        listing.getErrorMessages().addAll(errors);
    }

    private void checkE2E3AnyD3RequiredDependencyGroup(CertifiedProductSearchDetails listing, List<CertificationCriterion> attestedCriteria) {
        List<CertificationCriterion> requiredByE2E3Criteria = Stream.of(
                criterionService.get(Criteria2015.D_3_OLD),
                criterionService.get(Criteria2015.D_3_CURES))
                .collect(Collectors.toList());
        List<CertificationCriterion> e2e3Criteria = Stream.of(
                criterionService.get(Criteria2015.E_2),
                criterionService.get(Criteria2015.E_3))
                .collect(Collectors.toList());

        List<String> errors = validationUtils.checkComplementaryCriteriaAnyRequired(
                e2e3Criteria,
                requiredByE2E3Criteria,
                attestedCriteria);
        listing.getErrorMessages().addAll(errors);
    }

    private void checkFCriteriaHaveRequiredDependencies(CertifiedProductSearchDetails listing, List<CertificationCriterion> attestedCriteria) {
        List<CertificationCriterion> requiredByFCriteria = Stream.of(
                criterionService.get(Criteria2015.D_1),
                criterionService.get(Criteria2015.D_2_OLD),
                criterionService.get(Criteria2015.D_2_CURES),
                criterionService.get(Criteria2015.D_3_OLD),
                criterionService.get(Criteria2015.D_3_CURES),
                criterionService.get(Criteria2015.D_7))
                .collect(Collectors.toList());

        List<String> errors = validationUtils.checkClassOfCriteriaForMissingComplementaryCriteriaErrors(F_CRITERIA_NUMBERS_START,
                attestedCriteria,
                requiredByFCriteria);
        listing.getErrorMessages().addAll(errors);

        List<String> warnings = validationUtils.checkClassOfCriteriaForMissingComplementaryCriteriaWarnings(
                F_CRITERIA_NUMBERS_START,
                attestedCriteria,
                requiredByFCriteria);
        addListingWarningsByPermission(listing, warnings);
    }

    private void checkG6RequiredDependencies(CertifiedProductSearchDetails listing, List<CertificationCriterion> attestedCriteria) {
        List<CertificationCriterion> g6Criteria = Stream.of(
                criterionService.get(Criteria2015.G_6_OLD),
                criterionService.get(Criteria2015.G_6_CURES))
                .collect(Collectors.toList());
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
                .filter(cert -> validationUtils.hasCriterion(cert, criteriaRequiringG6))
                .collect(Collectors.<CertificationCriterion>toList());
        List<CertificationCriterion> removedAttestedG6Criteria = attestedCriteria.stream()
                .filter(cert -> cert.getRemoved() != null && cert.getRemoved().equals(Boolean.TRUE))
                .filter(cert -> validationUtils.hasCriterion(cert, criteriaRequiringG6))
                .collect(Collectors.<CertificationCriterion>toList());
        boolean hasG6 = validationUtils.hasAnyCriteria(g6Criteria, attestedCriteria);

        if (presentAttestedG6Criteria != null && presentAttestedG6Criteria.size() > 0 && !hasG6) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteriaRequired",
                    g6Criteria.stream().map(criterion -> Util.formatCriteriaNumber(criterion)).collect(Collectors.joining(" or "))));
        }
        if (removedAttestedG6Criteria != null && removedAttestedG6Criteria.size() > 0
                && (presentAttestedG6Criteria == null || presentAttestedG6Criteria.size() == 0)
                && !hasG6) {
            addListingWarningByPermission(listing, msgUtil.getMessage("listing.criteriaRequired",
                    g6Criteria.stream().map(criterion -> Util.formatCriteriaNumber(criterion)).collect(Collectors.joining(" or "))));
        }
    }


    private void checkG7G8G9RequiredDependencies(CertifiedProductSearchDetails listing, List<CertificationCriterion> attestedCriteria) {
        CertificationCriterion d1 = criterionService.get(Criteria2015.D_1);
        CertificationCriterion d9 = criterionService.get(Criteria2015.D_9);
        List<CertificationCriterion> d2Ord10 = Stream.of(
                criterionService.get(Criteria2015.D_2_OLD),
                criterionService.get(Criteria2015.D_2_CURES),
                criterionService.get(Criteria2015.D_10_OLD),
                criterionService.get(Criteria2015.D_10_CURES))
            .collect(Collectors.toList());
        List<CertificationCriterion> g7g8g9Criteria = Stream.of(
                criterionService.get(Criteria2015.G_7),
                criterionService.get(Criteria2015.G_8),
                criterionService.get(Criteria2015.G_9_OLD),
                criterionService.get(Criteria2015.G_9_CURES))
                .collect(Collectors.toList());

        List<String> errors = validationUtils.checkComplementaryCriteriaAllRequired(
                g7g8g9Criteria,
                Stream.of(d1, d9).collect(Collectors.toList()),
                attestedCriteria);
        listing.getErrorMessages().addAll(errors);

        errors = validationUtils.checkComplementaryCriteriaAnyRequired(
                g7g8g9Criteria,
                d2Ord10,
                attestedCriteria);
        listing.getErrorMessages().addAll(errors);
    }

    private void checkG10RequiredDependencies(CertifiedProductSearchDetails listing, List<CertificationCriterion> attestedCriteria) {
        CertificationCriterion g10 = criterionService.get(Criteria2015.G_10);
        CertificationCriterion d1 = criterionService.get(Criteria2015.D_1);
        CertificationCriterion d9 = criterionService.get(Criteria2015.D_9);
        List<CertificationCriterion> d2Ord10 = Stream.of(
                criterionService.get(Criteria2015.D_2_OLD),
                criterionService.get(Criteria2015.D_2_CURES),
                criterionService.get(Criteria2015.D_10_OLD),
                criterionService.get(Criteria2015.D_10_CURES))
            .collect(Collectors.toList());

        if (!validationUtils.hasCriterion(g10, attestedCriteria)) {
            return;
        }
        if (!validationUtils.hasCriterion(d1, attestedCriteria)) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.dependentCriteriaRequired",
                    Util.formatCriteriaNumber(g10),
                    Util.formatCriteriaNumber(d1)));
        }
        if (!validationUtils.hasCriterion(d9, attestedCriteria)) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.dependentCriteriaRequired",
                    Util.formatCriteriaNumber(g10),
                    Util.formatCriteriaNumber(d9)));
        }
        if (!validationUtils.hasAnyCriteria(d2Ord10, attestedCriteria)) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.dependentCriteriaRequired",
                    Util.formatCriteriaNumber(g10),
                    d2Ord10.stream().map(criterion -> Util.formatCriteriaNumber(criterion))
                        .collect(Collectors.joining(" or "))));
        }
    }

    private void checkHCriteriaHaveRequiredDependencies(CertifiedProductSearchDetails listing, List<CertificationCriterion> attestedCriteria) {
        List<CertificationCriterion> requiredByHCriteria = Stream.of(
                criterionService.get(Criteria2015.D_1),
                criterionService.get(Criteria2015.D_2_OLD),
                criterionService.get(Criteria2015.D_2_CURES),
                criterionService.get(Criteria2015.D_3_OLD),
                criterionService.get(Criteria2015.D_3_CURES))
        .collect(Collectors.toList());

        List<String> errors = validationUtils.checkClassOfCriteriaForMissingComplementaryCriteriaErrors(
                H_CRITERIA_NUMBERS_START,
                attestedCriteria,
                requiredByHCriteria);
        listing.getErrorMessages().addAll(errors);

        List<String> warnings = validationUtils.checkClassOfCriteriaForMissingComplementaryCriteriaWarnings(
                H_CRITERIA_NUMBERS_START,
                attestedCriteria,
                requiredByHCriteria);
        addListingWarningsByPermission(listing, warnings);
    }

    private void checkH1PlusB1Criteria(CertifiedProductSearchDetails listing, List<CertificationCriterion> attestedCriteria) {
        CertificationCriterion h1 = criterionService.get(Criteria2015.H_1);
        boolean hasH1 = validationUtils.hasCriterion(h1, attestedCriteria);
        if (hasH1) {
            List<CertificationCriterion> b1Criteria = Stream.of(criterionService.get(Criteria2015.B_1_OLD),
                    criterionService.get(Criteria2015.B_1_CURES))
                    .collect(Collectors.toList());
            boolean hasAttestedB1Criterion = b1Criteria.stream()
                .filter(b1Criterion -> validationUtils.hasCriterion(b1Criterion, attestedCriteria))
                .findFirst().isPresent();
            if (!hasAttestedB1Criterion) {
                listing.getErrorMessages().add("Certification criterion "
                        + Util.formatCriteriaNumber(h1)
                        + " was found so "
                        + b1Criteria.stream().map(criterion -> Util.formatCriteriaNumber(criterion)).collect(Collectors.joining(" or "))
                        + " is required but was not found.");
            }
            //TODO: add tests
        }
    }
}
