package gov.healthit.chpl.validation.pendingListing.reviewer.edition2015;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.service.CertificationCriterionService.Criteria2015;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.util.ValidationUtils;
import gov.healthit.chpl.validation.pendingListing.reviewer.PermissionBasedReviewer;

@Component("pendingRequiredAndRelatedCriteriaReviewer")
public class RequiredAndRelatedCriteriaReviewer  extends PermissionBasedReviewer {
    private static final String B_CRITERIA_NUMBERS_START = "170.315 (b)";
    private static final String B_10 = "170.315 (b)(10)";

    private ErrorMessageUtil msgUtil;
    private CertificationCriterionService criterionService;
    private ValidationUtils validationUtils;

    private CertificationCriterion g4;
    private CertificationCriterion g5;
    private CertificationCriterion g10;
    private CertificationCriterion d1;
    private CertificationCriterion d9;
    private List<CertificationCriterion> d2Ord10 = new ArrayList<CertificationCriterion>();

    private List<CertificationCriterion> requiredByBCriteria = new ArrayList<CertificationCriterion>();

    @Autowired
    public RequiredAndRelatedCriteriaReviewer(CertificationCriterionService criterionService,
            ErrorMessageUtil msgUtil, ValidationUtils validationUtils,
            ResourcePermissions resourcePermissions) {
        super(msgUtil, resourcePermissions);
        this.criterionService = criterionService;
        this.msgUtil = msgUtil;
        this.validationUtils = validationUtils;
    }

    @PostConstruct
    public void postConstruct() {
        g4 = criterionService.get(Criteria2015.G_4);
        g5 = criterionService.get(Criteria2015.G_5);

        requiredByBCriteria.add(criterionService.get(Criteria2015.D_1));
        requiredByBCriteria.add(criterionService.get(Criteria2015.D_2_OLD));
        requiredByBCriteria.add(criterionService.get(Criteria2015.D_2_CURES));
        requiredByBCriteria.add(criterionService.get(Criteria2015.D_3_OLD));
        requiredByBCriteria.add(criterionService.get(Criteria2015.D_3_CURES));
        requiredByBCriteria.add(criterionService.get(Criteria2015.D_5));
        requiredByBCriteria.add(criterionService.get(Criteria2015.D_6));
        requiredByBCriteria.add(criterionService.get(Criteria2015.D_7));
        requiredByBCriteria.add(criterionService.get(Criteria2015.D_8));

        g10 = criterionService.get(Criteria2015.G_10);
        d1 = criterionService.get(Criteria2015.D_1);
        d9 = criterionService.get(Criteria2015.D_9);
        d2Ord10.add(criterionService.get(Criteria2015.D_2_OLD));
        d2Ord10.add(criterionService.get(Criteria2015.D_2_CURES));
        d2Ord10.add(criterionService.get(Criteria2015.D_10_OLD));
        d2Ord10.add(criterionService.get(Criteria2015.D_10_CURES));
    }

    @Override
    public void review(PendingCertifiedProductDTO listing) {
        List<CertificationCriterion> attestedCriteria = validationUtils.getAttestedCriteria(listing);

        checkAlwaysRequiredCriteria(listing, attestedCriteria);
        checkBCriteriaHaveRequiredDependencies(listing, attestedCriteria);
        checkG10RequiredDependencies(listing, attestedCriteria);
    }

    private void checkAlwaysRequiredCriteria(PendingCertifiedProductDTO listing,
            List<CertificationCriterion> attestedCriteria) {
        if (!validationUtils.hasCriterion(g4, attestedCriteria)) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteriaRequired", Util.formatCriteriaNumber(g4)));
        }
        if (!validationUtils.hasCriterion(g5, attestedCriteria)) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteriaRequired", Util.formatCriteriaNumber(g5)));
        }
    }

    private void checkBCriteriaHaveRequiredDependencies(PendingCertifiedProductDTO listing,
            List<CertificationCriterion> attestedCriteria) {
        List<String> excludedCertNumbers = Stream.of(B_10).collect(Collectors.toList());
        List<String> requiredByBCriteriaCertNumbers
            = requiredByBCriteria.stream().map(criterion -> criterion.getNumber()).distinct().collect(Collectors.toList());

        List<String> errors = validationUtils.checkClassSubsetOfCriteriaForMissingComplementaryCriteriaNumberErrors(B_CRITERIA_NUMBERS_START,
                attestedCriteria,
                requiredByBCriteriaCertNumbers,
                excludedCertNumbers);
        listing.getErrorMessages().addAll(errors);

        List<String> warnings = validationUtils.checkClassSubsetOfCriteriaForMissingComplementaryCriteriaNumberWarnings(
                B_CRITERIA_NUMBERS_START,
                attestedCriteria,
                requiredByBCriteriaCertNumbers,
                excludedCertNumbers);
        addListingWarningsByPermission(listing, warnings);
    }

    private void checkG10RequiredDependencies(PendingCertifiedProductDTO listing,
            List<CertificationCriterion> attestedCriteria) {
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
