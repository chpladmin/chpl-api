package gov.healthit.chpl.upload.listing.validation.reviewer;

import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingMeasure;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.util.ValidationUtils;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;

@Component("listingUploadMeasureReviewer")
public class MeasureReviewer implements Reviewer {
    private static final String MEASUREMENT_TYPE_G1 = "G1";
    private static final String MEASUREMENT_TYPE_G2 = "G2";
    private static final String G1_CRITERIA_NUMBER = "170.315 (g)(1)";
    private static final String G2_CRITERIA_NUMBER = "170.315 (g)(2)";
    private static final String ASSOCIATED_CRITERIA_ERROR_PREFIX = "associated with ";

    private ValidationUtils validationUtils;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public MeasureReviewer(ValidationUtils validationUtils, ErrorMessageUtil msgUtil) {
        this.validationUtils = validationUtils;
        this.msgUtil = msgUtil;
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        //if they have attested to G1 or G2 criterion, require at least one measure of that type
        reviewG1RequiredMeasures(listing);
        reviewG2RequiredMeasures(listing);

        if (CollectionUtils.isEmpty(listing.getMeasures())) {
            return;
        }
        removeMeasuresThatDoNotExist(listing);
        if (!CollectionUtils.isEmpty(listing.getMeasures())) {
            for (ListingMeasure measure : listing.getMeasures()) {
                reviewMeasureTypeExists(listing, measure);
                if (measure != null && measure.getMeasure() != null
                        && measure.getMeasure().getId() != null) {
                    //ICS + Removed check is only done for uploaded listings
                    reviewIcsAndRemovedMeasures(listing, measure);
                    removeMeasureAssociatedCriteriaWithoutIds(listing, measure);
                    reviewMeasureHasAssociatedCriteria(listing, measure);
                    reviewMeasureHasOnlyAllowedCriteria(listing, measure);
                    if (BooleanUtils.isFalse(measure.getMeasure().getRequiresCriteriaSelection())) {
                        reviewMeasureHasAllAllowedCriteria(listing, measure);
                    }
                }
            }
        }
    }

    private void reviewG1RequiredMeasures(CertifiedProductSearchDetails listing) {
        List<CertificationCriterion> attestedCriteria = listing.getCertificationResults().stream()
                .filter(certResult -> BooleanUtils.isTrue(certResult.isSuccess()))
                .map(certResult -> certResult.getCriterion())
                .collect(Collectors.toList());
        if (validationUtils.hasCert(G1_CRITERIA_NUMBER, attestedCriteria)) {
            // must have at least one measure of type G1
            long g1MeasureCount = listing.getMeasures().stream()
                .filter(measure -> ObjectUtils.allNotNull(measure.getMeasureType(), measure.getMeasureType().getName())
                        && measure.getMeasureType().getName().equals(MEASUREMENT_TYPE_G1))
                .count();
            if (g1MeasureCount == 0) {
                listing.getErrorMessages().add(msgUtil.getMessage("listing.missingG1Measures"));
            }
        }
    }

    private void reviewG2RequiredMeasures(CertifiedProductSearchDetails listing) {
        List<CertificationCriterion> attestedCriteria = listing.getCertificationResults().stream()
                .filter(certResult -> BooleanUtils.isTrue(certResult.isSuccess()))
                .map(certResult -> certResult.getCriterion())
                .collect(Collectors.toList());
        if (validationUtils.hasCert(G2_CRITERIA_NUMBER, attestedCriteria)) {
            // must have at least one measure of type G1
            long g2MeasureCount = listing.getMeasures().stream()
                .filter(measure -> ObjectUtils.allNotNull(measure.getMeasureType(), measure.getMeasureType().getName())
                        && measure.getMeasureType().getName().equals(MEASUREMENT_TYPE_G2))
                .count();
            if (g2MeasureCount == 0) {
                listing.getErrorMessages().add(msgUtil.getMessage("listing.missingG2Measures"));
            }
        }
    }

    private void reviewIcsAndRemovedMeasures(CertifiedProductSearchDetails listing, ListingMeasure measure) {
        if (measure.getMeasure() != null
                && doesNotHaveIcs(listing)
                && measureIsRemoved(measure)) {
            listing.getErrorMessages().add(
                    msgUtil.getMessage("listing.removedMeasureNoIcs",
                            measure.getMeasureType().getName(),
                            measure.getMeasure().getName(),
                            measure.getMeasure().getAbbreviation()));
        }
    }

    private boolean doesNotHaveIcs(CertifiedProductSearchDetails listing) {
        return listing.getIcs() == null || listing.getIcs().getInherits() == null
                || listing.getIcs().getInherits().equals(Boolean.FALSE);
    }

    private boolean measureIsRemoved(ListingMeasure measure) {
        return measure.getMeasure() != null && measure.getMeasure().getRemoved() != null
                && measure.getMeasure().getRemoved().equals(Boolean.TRUE);
    }

    private void reviewMeasureHasOnlyAllowedCriteria(
            CertifiedProductSearchDetails listing, ListingMeasure measure) {
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
                .filter(assocCriterion -> assocCriterion.getId() != null)
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
            CertifiedProductSearchDetails listing, ListingMeasure measure) {
        if (measure.getMeasure() == null || CollectionUtils.isEmpty(measure.getAssociatedCriteria())
                || CollectionUtils.isEmpty(measure.getMeasure().getAllowedCriteria())) {
            return;
        }

        Predicate<CertificationCriterion> notInAssociatedCriteria =
                allowedCriterion -> !measure.getAssociatedCriteria().stream()
                .filter(assocCriterion -> assocCriterion.getId() != null)
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
            CertifiedProductSearchDetails listing, ListingMeasure measure) {
        if (measure.getAssociatedCriteria() == null || measure.getAssociatedCriteria().size() == 0) {
            listing.getErrorMessages().add(msgUtil.getMessage(
                    "listing.measure.missingAssociatedCriteria",
                    measure.getMeasureType().getName(),
                    measure.getMeasure().getName(),
                    measure.getMeasure().getAbbreviation()));
        }
    }

    private void removeMeasureAssociatedCriteriaWithoutIds(
            CertifiedProductSearchDetails listing, ListingMeasure measure) {
        measure.getAssociatedCriteria().stream()
            .filter(assocCriterion -> assocCriterion.getId() == null)
            .forEach(assocCriterion -> listing.getWarningMessages().add(msgUtil.getMessage(
                    "listing.measure.invalidAssociatedCriterion",
                    measure.getMeasureType().getName(),
                    measure.getMeasure().getName(),
                    measure.getMeasure().getAbbreviation(),
                    assocCriterion.getNumber())));
        Iterator<CertificationCriterion> assocCriteriaIter = measure.getAssociatedCriteria().iterator();
        while (assocCriteriaIter.hasNext()) {
            CertificationCriterion cc = assocCriteriaIter.next();
            if (cc.getId() == null) {
                assocCriteriaIter.remove();
            }
        }
    }

    private void reviewMeasureTypeExists(CertifiedProductSearchDetails listing, ListingMeasure measure) {
        if (measure.getMeasureType() == null || measure.getMeasureType().getId() == null) {
            if (measure.getMeasureType() != null && !StringUtils.isEmpty(measure.getMeasureType().getName())) {
                listing.getErrorMessages().add(
                        msgUtil.getMessage("listing.invalidMeasureType", measure.getMeasureType().getName()));
            } else {
                listing.getErrorMessages().add(msgUtil.getMessage("listing.missingMeasureType"));
            }
        }
    }

    private void removeMeasuresThatDoNotExist(CertifiedProductSearchDetails listing) {
        if (CollectionUtils.isEmpty(listing.getMeasures())) {
            return;
        }

        Iterator<ListingMeasure> measureIter = listing.getMeasures().iterator();
        while (measureIter.hasNext()) {
            ListingMeasure measure = measureIter.next();
            if (measure != null && measure.getMeasure() != null
                    && measure.getMeasure().getId() == null) {
                measureIter.remove();

                String typeName = measure.getMeasureType() == null ? "The" : measure.getMeasureType().getName();
                String measureName = getDisplayMeasureNameForUnknownMeasure(measure);
                String assocCriteria = null;
                if (!CollectionUtils.isEmpty(measure.getAssociatedCriteria())) {
                    assocCriteria = ASSOCIATED_CRITERIA_ERROR_PREFIX
                            + measure.getAssociatedCriteria().stream().map(crit -> Util.formatCriteriaNumber(crit)).collect(Collectors.joining(", "));
                }
                if (StringUtils.isEmpty(assocCriteria)) {
                    assocCriteria = "";
                }
                listing.getWarningMessages().add(msgUtil.getMessage("listing.measureNotFoundAndRemoved",
                        typeName, measureName, assocCriteria));
            }
        }
    }

    private String getDisplayMeasureNameForUnknownMeasure(ListingMeasure measure) {
        String measureName = null;
        if (measure.getMeasure() != null && measure.getMeasure().getDomain() != null
                && !StringUtils.isEmpty(measure.getMeasure().getDomain().getName())) {
            measureName = measure.getMeasure().getDomain().getName();
        }
        if (measure.getMeasure() != null && !StringUtils.isEmpty(measure.getMeasure().getAbbreviation())) {
            if (measureName != null) {
                measureName += " + " + measure.getMeasure().getAbbreviation();
            } else {
                measureName = measure.getMeasure().getAbbreviation();
            }
        }
        if (measureName == null && measure.getMeasure() != null && !StringUtils.isEmpty(measure.getMeasure().getName())) {
            measureName = measure.getMeasure().getName();
        }
        return !StringUtils.isEmpty(measureName) ? measureName : "";
    }
}
