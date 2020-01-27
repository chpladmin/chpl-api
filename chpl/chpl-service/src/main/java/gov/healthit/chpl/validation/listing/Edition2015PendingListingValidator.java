package gov.healthit.chpl.validation.listing;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.validation.pendingListing.reviewer.CertificationDateReviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.ChplNumberReviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.DeveloperStatusReviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.FieldLengthReviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.FuzzyMatchReviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.InheritedCertificationStatusReviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.RemovedCriteriaReviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.Reviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.TestToolReviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.UnattestedCriteriaWithDataReviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.UnsupportedCharacterReviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.UrlReviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.ValidDataReviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.edition2015.DuplicateData2015Reviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.edition2015.InvalidCriteriaCombinationReviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.edition2015.RequiredData2015Reviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.edition2015.SedG32015Reviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.edition2015.TestFunctionality2015Reviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.edition2015.TestTool2015Reviewer;

/**
 * Validation interface for 2015 listings in the pending stage of upload to the CHPL.
 * 
 * @author kekey
 *
 */
@Component
public class Edition2015PendingListingValidator extends PendingValidator {
    @Autowired
    @Qualifier("pendingChplNumberReviewer")
    private ChplNumberReviewer chplNumberReviewer;

    @Autowired
    @Qualifier("pendingDeveloperStatusReviewer")
    private DeveloperStatusReviewer devStatusReviewer;

    @Autowired
    @Qualifier("pendingCertificationDateReviewer")
    private CertificationDateReviewer certDateReviewer;

    @Autowired
    @Qualifier("pendingFuzzyMatchReviewer")
    private FuzzyMatchReviewer fuzzyMatchReviewer;

    @Autowired
    @Qualifier("pendingUnsupportedCharacterReviewer")
    private UnsupportedCharacterReviewer unsupportedCharacterReviewer;

    @Autowired
    @Qualifier("pendingUnattestedCriteriaWithDataReviewer")
    private UnattestedCriteriaWithDataReviewer unattestedCriteriaWithDataReviewer;

    @Autowired
    @Qualifier("pendingValidDataReviewer")
    private ValidDataReviewer validDataReviewer;

    @Autowired
    @Qualifier("pendingFieldLengthReviewer")
    private FieldLengthReviewer fieldLengthReviewer;

    @Autowired
    @Qualifier("pendingSedG32015Reviewer")
    private SedG32015Reviewer sedG3Reviewer;

    @Autowired
    @Qualifier("pendingRequiredData2015Reviewer")
    private RequiredData2015Reviewer requiredDataReviewer;

    @Autowired
    @Qualifier("pendingTestToolReviewer")
    private TestToolReviewer ttReviewer;

    @Autowired
    @Qualifier("pendingTestTool2015Reviewer")
    private TestTool2015Reviewer tt2015Reviewer;

    @Autowired
    @Qualifier("pendingIcsReviewer")
    private InheritedCertificationStatusReviewer icsReviewer;

    @Autowired
    @Qualifier("pendingUrlReviewer")
    private UrlReviewer urlReviewer;

    @Autowired
    @Qualifier("pendingTestFunctionality2015Reviewer")
    private TestFunctionality2015Reviewer testFunctionalityReviewer;

    @Autowired
    @Qualifier("pendingDuplicateData2015Reviewer")
    private DuplicateData2015Reviewer duplicateDataReviewer;

    @Autowired
    @Qualifier("removedCriteriaReviewer")
    private RemovedCriteriaReviewer removedCriteriaReviewer;

    @Autowired
    @Qualifier("pendingInvalidCriteriaCombinationReviewer")
    private InvalidCriteriaCombinationReviewer invalidCriteriaCombinationReviewer;

    private List<Reviewer> reviewers;

    @Override
    public List<Reviewer> getReviewers() {
        if (reviewers == null) {
            reviewers = new ArrayList<Reviewer>();
            reviewers.add(duplicateDataReviewer);
            reviewers.add(chplNumberReviewer);
            reviewers.add(devStatusReviewer);
            reviewers.add(certDateReviewer);
            reviewers.add(fuzzyMatchReviewer);
            reviewers.add(unsupportedCharacterReviewer);
            reviewers.add(unattestedCriteriaWithDataReviewer);
            reviewers.add(validDataReviewer);
            reviewers.add(fieldLengthReviewer);
            reviewers.add(requiredDataReviewer);
            reviewers.add(sedG3Reviewer);
            reviewers.add(ttReviewer);
            reviewers.add(tt2015Reviewer);
            reviewers.add(icsReviewer);
            reviewers.add(urlReviewer);
            reviewers.add(testFunctionalityReviewer);
            reviewers.add(removedCriteriaReviewer);
            reviewers.add(invalidCriteriaCombinationReviewer);
        }
        return reviewers;
    }
}
