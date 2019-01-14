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
import gov.healthit.chpl.validation.pendingListing.reviewer.Reviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.TestToolReviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.UnattestedCriteriaWithDataReviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.UnsupportedCharacterReviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.UrlReviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.ValidDataReviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.edition2014.DuplicateData2014Reviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.edition2014.RequiredData2014Reviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.edition2014.SedG32014Reviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.edition2014.TestFunctionality2014Reviewer;

/**
 * Validation interface for listings in the pending stage of upload to the CHPL.
 * @author kekey
 *
 */
@Component
public class Edition2014PendingListingValidator extends PendingValidator {
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
    @Qualifier("pendingRequiredData2014Reviewer")
    private RequiredData2014Reviewer requiredDataReviewer;

    @Autowired
    @Qualifier("pendingSedG32014Reviewer")
    private SedG32014Reviewer sedG3Reviewer;

    @Autowired
    @Qualifier("pendingTestToolReviewer")
    private TestToolReviewer ttReviewer;

    @Autowired
    @Qualifier("pendingTestFunctionality2014Reviewer")
    private TestFunctionality2014Reviewer tfReviewer;

    @Autowired
    @Qualifier("pendingUrlReviewer")
    private UrlReviewer urlReviewer;

    @Autowired
    @Qualifier("pendingDuplicateData2014Reviewer")
    private DuplicateData2014Reviewer duplicateData2014Reviewer;

    private List<Reviewer> reviewers;

    @Override
    public List<Reviewer> getReviewers() {
        if (reviewers == null) {
            reviewers = new ArrayList<Reviewer>();
            reviewers.add(duplicateData2014Reviewer);
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
            reviewers.add(tfReviewer);
            reviewers.add(urlReviewer);
        }
        return reviewers;
    }
}
