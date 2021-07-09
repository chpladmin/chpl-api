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
        checkBCriteriaHaveRequiredDependencies(listing, attestedCriteria);
        checkCCriteriaHaveRequiredDependencies(listing, attestedCriteria);
        checkG10RequiredDependencies(listing, attestedCriteria);
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
}
