package gov.healthit.chpl.upload.listing.validation.reviewer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.util.ValidationUtils;
import gov.healthit.chpl.validation.listing.reviewer.PermissionBasedReviewer;

@Component("listingUploadTestTaskReviewer")
public class TestTaskReviewer extends PermissionBasedReviewer {
    private static final int MINIMUM_TEST_PARTICIPANT_COUNT = 10;

    private CertificationResultRules certResultRules;
    private ValidationUtils validationUtils;
    private List<CertificationCriterion> testTaskCriteria = new ArrayList<CertificationCriterion>();

    @Autowired
    public TestTaskReviewer(CertificationCriterionService criterionService,
            ValidationUtils validationUtils,
            CertificationResultRules certResultRules,
            @Value("${sedCriteria}") String testTaskCriteria,
            ErrorMessageUtil errorMessageUtil, ResourcePermissions resourcePermissions) {
        super(errorMessageUtil, resourcePermissions);
        this.certResultRules = certResultRules;
        this.validationUtils = validationUtils;

        this.testTaskCriteria = Arrays.asList(testTaskCriteria.split(",")).stream()
                .map(id -> criterionService.get(Long.parseLong(id)))
                .collect(Collectors.toList());
    }

    public void review(CertifiedProductSearchDetails listing) {
        if (listing.getSed() == null) {
            return;
        }
        reviewAllTestTaskCriteriaAreAllowed(listing);
        reviewCertResultsHaveTestTasksIfRequired(listing);
        reviewTestTaskFields(listing);
    }

    private void reviewAllTestTaskCriteriaAreAllowed(CertifiedProductSearchDetails listing) {
        if (listing.getSed() != null && !CollectionUtils.isEmpty(listing.getSed().getTestTasks())) {
            listing.getSed().getTestTasks().stream()
                .filter(testTask -> !CollectionUtils.isEmpty(testTask.getCriteria()))
                .flatMap(testTask -> testTask.getCriteria().stream())
                .filter(testTaskCriterion -> !certResultRules.hasCertOption(testTaskCriterion.getNumber(), CertificationResultRules.TEST_TASK))
                .forEach(notAllowedTestTaskCriterion ->
                    listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.testTasksNotApplicable", Util.formatCriteriaNumber(notAllowedTestTaskCriterion))));

            listing.getSed().getTestTasks().stream()
                .filter(testTask -> !CollectionUtils.isEmpty(testTask.getCriteria()))
                .flatMap(testTask -> testTask.getCriteria().stream())
                .filter(ucdCriterion -> !doesListingAttestToCriterion(listing, ucdCriterion))
                .forEach(notAllowedTestTaskCriterion ->
                    listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.testTasksNotApplicable", Util.formatCriteriaNumber(notAllowedTestTaskCriterion))));
        }
    }

    private void reviewCertResultsHaveTestTasksIfRequired(CertifiedProductSearchDetails listing) {
        List<CertificationCriterion> attestedCriteria = validationUtils.getAttestedCriteria(listing);

        testTaskCriteria.stream()
            .filter(criterion -> validationUtils.hasCriterion(criterion, attestedCriteria))
            .map(attestedTestTaskCriterion -> getCertificationResultForCriterion(listing, attestedTestTaskCriterion))
            .filter(certResult -> certResult != null)
            .forEach(certResult -> reviewCertResultHasTestTasksIfRequired(listing, certResult));
    }

    private void reviewCertResultHasTestTasksIfRequired(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (certResult.isSed()) {
            if (listing.getSed() == null || CollectionUtils.isEmpty(listing.getSed().getTestTasks())) {
                addCriterionErrorOrWarningByPermission(listing, certResult, "listing.criteria.missingTestTask",
                        Util.formatCriteriaNumber(certResult.getCriterion()));
            } else if (!doesTestTaskListContainCriterion(listing, certResult.getCriterion())) {
                addCriterionErrorOrWarningByPermission(listing, certResult, "listing.criteria.missingTestTask",
                        Util.formatCriteriaNumber(certResult.getCriterion()));
            }
        }
    }

    public void reviewTestTaskFields(CertifiedProductSearchDetails listing) {
        List<CertificationCriterion> attestedCriteria = validationUtils.getAttestedCriteria(listing);

        testTaskCriteria.stream()
            .filter(criterion -> validationUtils.hasCriterion(criterion, attestedCriteria))
            .map(attestedTestTaskCriterion -> getCertificationResultForCriterion(listing, attestedTestTaskCriterion))
            .filter(certResult -> certResult != null)
            .forEach(certResult -> reviewTestTaskFields(listing, certResult));
    }

    private CertificationResult getCertificationResultForCriterion(CertifiedProductSearchDetails listing, CertificationCriterion criterionToReview) {
        Optional<CertificationResult> certResultToReviewOpt = listing.getCertificationResults().stream()
                .filter(certResult -> certResult.getCriterion().getId().equals(criterionToReview.getId()))
                .findAny();
        if (certResultToReviewOpt.isPresent()) {
            return certResultToReviewOpt.get();
        }
        return null;
    }

    private void reviewTestTaskFields(CertifiedProductSearchDetails listing, CertificationResult certResult) {
//        if (StringUtils.isEmpty(task.getDescription())) {
//            listing.getErrorMessages()
//            .add(msgUtil.getMessage("listing.sed.badTestDescription", description));
//        }
        //TODO: check for stuff like 10 participants, description provided, etc
    }

    private boolean doesTestTaskListContainCriterion(CertifiedProductSearchDetails listing, CertificationCriterion criterion) {
        return listing.getSed().getTestTasks().stream()
            .flatMap(testTask -> testTask.getCriteria().stream())
            .filter(testTaskCriterion -> testTaskCriterion.getId().equals(criterion.getId()))
            .count() > 0;
    }

    private boolean doesListingAttestToCriterion(CertifiedProductSearchDetails listing, CertificationCriterion criterion) {
        return listing.getCertificationResults().stream()
            .filter(certResult -> certResult.getCriterion() != null && BooleanUtils.isTrue(certResult.isSuccess())
                && certResult.getCriterion().getId().equals(criterion.getId()))
            .count() > 0;
    }
}
