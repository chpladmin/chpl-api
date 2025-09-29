package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.testdata.CertificationResultTestData;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.validation.listing.reviewer.ComparisonReviewer;

@Component("testDataComparisonReviewer")
public class TestDataComparisonReviewer implements ComparisonReviewer {
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public TestDataComparisonReviewer(ErrorMessageUtil errorMessageUtil) {
        this.errorMessageUtil = errorMessageUtil;
    }

    @Override
    public void review(CertifiedProductSearchDetails existingListing, CertifiedProductSearchDetails updatedListing) {
        //Add warning for any newly added criteria that have test data
        List<CertificationResult> addedCertificationResults = getAddedCertificationResults(existingListing, updatedListing);
        addedCertificationResults.stream()
            .filter(cr -> !CollectionUtils.isEmpty(cr.getTestDataUsed()))
            .forEach(cr -> addWarningForNewCertResultWithTestData(updatedListing, cr));

        //Add warning for any existing criteria that have changed test data
        List<CertificationResult> updatedCertificationResults = getUpdatedCertificationResults(existingListing, updatedListing);
        updatedCertificationResults.stream()
            .forEach(cr -> {
                CertificationResult existingListingCr = getCertResult(existingListing, cr.getCriterion().getId());
                List<CertificationResultTestData> updatedTestData = getUpdatedTestData(existingListingCr, cr);
                if (!CollectionUtils.isEmpty(updatedTestData)) {
                    addWarningForExistingCertResultWithTestData(updatedListing, cr);
                }
            });

    }

    private void addWarningForNewCertResultWithTestData(CertifiedProductSearchDetails listing, CertificationResult cr) {
        listing.addWarningMessage(errorMessageUtil.getMessage("listing.criteria.testDataNotApplicable",
                Util.formatCriteriaNumber(cr.getCriterion())));
        cr.setTestDataUsed(null);
    }

    private void addWarningForExistingCertResultWithTestData(CertifiedProductSearchDetails listing, CertificationResult cr) {
        listing.addWarningMessage(errorMessageUtil.getMessage("listing.criteria.testDataNotUpdated",
                Util.formatCriteriaNumber(cr.getCriterion())));
        cr.setTestDataUsed(null);
    }

    private List<CertificationResult> getAddedCertificationResults(CertifiedProductSearchDetails existingListing, CertifiedProductSearchDetails updatedListing) {
        return updatedListing.getCertificationResults().stream()
            .filter(updatedCertResult -> !isCriterionAttested(existingListing, updatedCertResult.getCriterion().getId()))
            .collect(Collectors.toList());
    }

    private List<CertificationResult> getUpdatedCertificationResults(CertifiedProductSearchDetails existingListing, CertifiedProductSearchDetails updatedListing) {
        return updatedListing.getCertificationResults().stream()
            .filter(updatedCertResult -> isCriterionAttested(existingListing, updatedCertResult.getCriterion().getId()))
            .collect(Collectors.toList());
    }

    private boolean isCriterionAttested(CertifiedProductSearchDetails listing, Long criterionId) {
        return listing.getCertificationResults().stream()
                .filter(certResult -> certResult.getCriterion().getId().equals(criterionId))
                .findAny().isPresent();
    }

    private CertificationResult getCertResult(CertifiedProductSearchDetails listing, Long criterionId) {
        return listing.getCertificationResults().stream()
                .filter(certResult -> certResult.getCriterion().getId().equals(criterionId))
                .findAny().orElse(null);
    }

    private List<CertificationResultTestData> getUpdatedTestData(CertificationResult existingCr, CertificationResult updatedCr) {
        return Stream.concat(getAddedTestData(existingCr.getTestDataUsed(), updatedCr.getTestDataUsed()).stream(),
                getRemovedTestData(existingCr.getTestDataUsed(), updatedCr.getTestDataUsed()).stream())
                .collect(Collectors.toList());
    }

    private List<CertificationResultTestData> getAddedTestData(List<CertificationResultTestData> existingTestData,
            List<CertificationResultTestData> updatedTestData) {
        if (CollectionUtils.isEmpty(updatedTestData)) {
            return new ArrayList<CertificationResultTestData>();
        }

        return updatedTestData.stream()
            .filter(updatedTdItem -> !containsTestData(existingTestData, updatedTdItem))
            .collect(Collectors.toList());
    }

    private List<CertificationResultTestData> getRemovedTestData(List<CertificationResultTestData> existingTestData,
            List<CertificationResultTestData> updatedTestData) {
        if (CollectionUtils.isEmpty(existingTestData)) {
            return new ArrayList<CertificationResultTestData>();
        }

        return existingTestData.stream()
            .filter(existingTdItem -> !containsTestData(updatedTestData, existingTdItem))
            .collect(Collectors.toList());
    }

    private boolean containsTestData(List<CertificationResultTestData> testData, CertificationResultTestData otherTestData) {
        if (CollectionUtils.isEmpty(testData)) {
            return false;
        }

        return testData.stream()
                .filter(td -> td.matches(otherTestData))
                .findAny().isPresent();
    }
}
