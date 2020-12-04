package gov.healthit.chpl.validation.pendingListing.reviewer.edition2015;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductMeasureDTO;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;
import gov.healthit.chpl.validation.pendingListing.reviewer.Reviewer;

@Component("pendingMeasureValidityReviewer")
public class MeasureValidityReviewer implements Reviewer {
    private static final String MEASUREMENT_TYPE_G1 = "G1";
    private static final String MEASUREMENT_TYPE_G2 = "G2";
    private static final String G1_CRITERIA_NUMBER = "170.315 (g)(1)";
    private static final String G2_CRITERIA_NUMBER = "170.315 (g)(2)";

    private ValidationUtils validationUtils;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public MeasureValidityReviewer(ValidationUtils validationUtils, ErrorMessageUtil msgUtil) {
        this.validationUtils = validationUtils;
        this.msgUtil = msgUtil;
    }

    @Override
    public void review(PendingCertifiedProductDTO listing) {
        //if they have attested to G1 or G2 criterion, require at least one measure of that type
        reviewG1RequiredMeasures(listing);
        reviewG2RequiredMeasures(listing);

        for (PendingCertifiedProductMeasureDTO measure : listing.getMeasures()) {
            if (measure != null && measure.getMeasure() == null) {
                reviewMeasureDidNotExist(listing, measure);
            } else if (measure != null && measure.getMeasure() != null) {
                reviewMeasureHasId(listing, measure);
                reviewIcsAndRemovedMeasures(listing, measure);
                reviewMeasureHasAssociatedCriteria(listing, measure);
                reviewMeasureHasOnlyAllowedCriteria(listing, measure);
                if (measure.getMeasure().getRequiresCriteriaSelection() != null
                        && !measure.getMeasure().getRequiresCriteriaSelection()) {
                    reviewMeasureHasAllAllowedCriteria(listing, measure);
                }
            }
        }
    }

    private void reviewG1RequiredMeasures(PendingCertifiedProductDTO listing) {
        List<CertificationCriterion> attestedCriteria = listing.getCertificationCriterion().stream()
                .filter(certResult -> certResult.getMeetsCriteria())
                .map(certResult -> new CertificationCriterion(certResult.getCriterion()))
                .collect(Collectors.toList());
        if (validationUtils.hasCert(G1_CRITERIA_NUMBER, attestedCriteria)) {
            // must have at least one measure of type G1
            long g1MeasureCount = listing.getMeasures().stream()
                .filter(measure -> measure.getMeasureType() != null
                        && measure.getMeasureType().getName().equals(MEASUREMENT_TYPE_G1))
                .count();
            if (g1MeasureCount == 0) {
                listing.getErrorMessages().add(msgUtil.getMessage("listing.missingG1Measures"));
            }
        }
    }

    private void reviewG2RequiredMeasures(PendingCertifiedProductDTO listing) {
        List<CertificationCriterion> attestedCriteria = listing.getCertificationCriterion().stream()
                .filter(certResult -> certResult.getMeetsCriteria())
                .map(certResult -> new CertificationCriterion(certResult.getCriterion()))
                .collect(Collectors.toList());
        if (validationUtils.hasCert(G2_CRITERIA_NUMBER, attestedCriteria)) {
            // must have at least one measure of type G1
            long g1MeasureCount = listing.getMeasures().stream()
                .filter(measure -> measure.getMeasureType() != null
                        && measure.getMeasureType().getName().equals(MEASUREMENT_TYPE_G2))
                .count();
            if (g1MeasureCount == 0) {
                listing.getErrorMessages().add(msgUtil.getMessage("listing.missingG2Measures"));
            }
        }
    }

    private void reviewIcsAndRemovedMeasures(
            PendingCertifiedProductDTO listing, PendingCertifiedProductMeasureDTO measure) {
        if (measure.getMeasure() != null
                && (listing.getIcs() == null || !listing.getIcs().booleanValue())
                && measure.getMeasure().getRemoved() != null
                && measure.getMeasure().getRemoved().booleanValue()) {
            listing.getErrorMessages().add(
                    msgUtil.getMessage("listing.removedMeasureNoIcs",
                            measure.getMeasureType().getName(),
                            measure.getMeasure().getName(),
                            measure.getMeasure().getAbbreviation()));
        }
    }

    private void reviewMeasureHasOnlyAllowedCriteria(
            PendingCertifiedProductDTO listing, PendingCertifiedProductMeasureDTO measure) {
        if (measure.getMeasure() == null || measure.getMeasure().getId() == null
                || measure.getAssociatedCriteria() == null
                || measure.getAssociatedCriteria().size() == 0
                || measure.getMeasure().getAllowedCriteria() == null
                || measure.getMeasure().getAllowedCriteria().size() == 0) {
            return;
        }

        Predicate<CertificationCriterion> notInAllowedCriteria =
                assocCriterion -> !measure.getMeasure().getAllowedCriteria().stream()
                .anyMatch(allowedCriterion -> allowedCriterion.getId().equals(assocCriterion.getId()));

        List<CertificationCriterion> assocCriteriaNotAllowed =
                measure.getAssociatedCriteria().stream()
                .filter(notInAllowedCriteria)
                .collect(Collectors.toList());

        assocCriteriaNotAllowed.stream().forEach(assocCriterionNotAllowed -> {
            listing.getErrorMessages().add(msgUtil.getMessage(
                    "listing.measure.associatedCriterionNotAllowed",
                    measure.getMeasureType().getName(),
                    measure.getMeasure().getName(),
                    measure.getMeasure().getAbbreviation(),
                    CertificationCriterionService.formatCriteriaNumber(assocCriterionNotAllowed)));
        });
    }

    private void reviewMeasureHasAllAllowedCriteria(
            PendingCertifiedProductDTO listing, PendingCertifiedProductMeasureDTO measure) {
        if (measure.getMeasure() == null || measure.getAssociatedCriteria() == null
                || measure.getMeasure().getAllowedCriteria() == null) {
            return;
        }

        Predicate<CertificationCriterion> notInAssociatedCriteria =
                allowedCriterion -> !measure.getAssociatedCriteria().stream()
                .anyMatch(assocCriterion -> allowedCriterion.getId().equals(assocCriterion.getId()));

        List<CertificationCriterion> missingAllowedCriteria =
                measure.getMeasure().getAllowedCriteria().stream()
                .filter(notInAssociatedCriteria)
                .collect(Collectors.toList());

        missingAllowedCriteria.stream().forEach(missingAllowedCriterion -> {
            listing.getErrorMessages().add(msgUtil.getMessage(
                    "listing.measure.missingRequiredCriterion",
                    measure.getMeasureType().getName(),
                    measure.getMeasure().getName(),
                    measure.getMeasure().getAbbreviation(),
                    CertificationCriterionService.formatCriteriaNumber(missingAllowedCriterion)));
        });
    }

    private void reviewMeasureHasAssociatedCriteria(
            PendingCertifiedProductDTO listing, PendingCertifiedProductMeasureDTO measure) {
        if (measure.getAssociatedCriteria() == null || measure.getAssociatedCriteria().size() == 0) {
            listing.getErrorMessages().add(msgUtil.getMessage(
                    "listing.measure.missingAssociatedCriteria",
                    measure.getMeasureType().getName(),
                    measure.getMeasure().getName(),
                    measure.getMeasure().getAbbreviation()));
        }
    }

    private void reviewMeasureHasId(PendingCertifiedProductDTO listing, PendingCertifiedProductMeasureDTO measure) {
        if (measure == null) {
            return;
        }

        if (measure.getMeasure().getId() == null) {
            String nameForMsg = "";
            if (measure.getUploadedValue() != null) {
                nameForMsg = measure.getUploadedValue();
            } else if (measure.getMeasure().getAbbreviation() != null) {
                    nameForMsg = measure.getMeasure().getAbbreviation();
            } else if (measure.getMeasure().getName() != null) {
                nameForMsg = measure.getMeasure().getName();
            } else if (measure.getMeasure().getRequiredTest() != null) {
                nameForMsg = measure.getMeasure().getRequiredTest();
            }
            listing.getErrorMessages().add(
                    msgUtil.getMessage("listing.invalidMeasure", nameForMsg));
        }

        if (measure.getMeasureType() == null || measure.getMeasureType().getId() == null) {
            String nameForMsg = measure.getMeasureType() == null ? "null"
                    : measure.getMeasureType().getName();
            listing.getErrorMessages().add(
                    msgUtil.getMessage("listing.invalidMeasureType", nameForMsg));
        }
    }

    private void reviewMeasureDidNotExist(PendingCertifiedProductDTO listing, PendingCertifiedProductMeasureDTO measure) {
        if (measure == null) {
            return;
        }

        if (measure.getMeasure() == null) {
            listing.getErrorMessages().add(
                    msgUtil.getMessage("listing.invalidMeasure", measure.getUploadedValue()));
        }
    }
}
