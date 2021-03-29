package gov.healthit.chpl.validation.listing;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.validation.listing.reviewer.CertificationDateReviewer;
import gov.healthit.chpl.validation.listing.reviewer.CertificationStatusReviewer;
import gov.healthit.chpl.validation.listing.reviewer.ChplNumberComparisonReviewer;
import gov.healthit.chpl.validation.listing.reviewer.ComparisonReviewer;
import gov.healthit.chpl.validation.listing.reviewer.DeveloperBanComparisonReviewer;
import gov.healthit.chpl.validation.listing.reviewer.DeveloperStatusReviewer;
import gov.healthit.chpl.validation.listing.reviewer.DuplicateDataReviewer;
import gov.healthit.chpl.validation.listing.reviewer.FieldLengthReviewer;
import gov.healthit.chpl.validation.listing.reviewer.ListingStatusAndUserRoleReviewer;
import gov.healthit.chpl.validation.listing.reviewer.RealWorldTestingReviewer;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;
import gov.healthit.chpl.validation.listing.reviewer.SvapReviewer;
import gov.healthit.chpl.validation.listing.reviewer.TestStandardReviewer;
import gov.healthit.chpl.validation.listing.reviewer.TestToolReviewer;
import gov.healthit.chpl.validation.listing.reviewer.UnattestedCriteriaWithDataReviewer;
import gov.healthit.chpl.validation.listing.reviewer.UnsupportedCharacterReviewer;
import gov.healthit.chpl.validation.listing.reviewer.UrlReviewer;
import gov.healthit.chpl.validation.listing.reviewer.ValidDataReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2014.RequiredData2014Reviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2014.TestFunctionality2014Reviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2014.TestTool2014Reviewer;

/**
 * Validation interface for any 2014 listing with CHPL number beginning with CHP-.
 *
 * @author kekey
 *
 */
@Component
public class Edition2014LegacyListingValidator extends Validator {
    @Autowired
    @Qualifier("developerStatusReviewer")
    private DeveloperStatusReviewer devStatusReviewer;

    @Autowired
    @Qualifier("unsupportedCharacterReviewer")
    private UnsupportedCharacterReviewer unsupportedCharacterReviewer;

    @Autowired
    @Qualifier("fieldLengthReviewer")
    private FieldLengthReviewer fieldLengthReviewer;

    @Autowired
    @Qualifier("requiredData2014Reviewer")
    private RequiredData2014Reviewer requiredFieldReviewer;

    @Autowired
    @Qualifier("validDataReviewer")
    private ValidDataReviewer validDataReviewer;

    @Autowired
    @Qualifier("certificationStatusReviewer")
    private CertificationStatusReviewer certStatusReviewer;

    @Autowired
    @Qualifier("certificationDateReviewer")
    private CertificationDateReviewer certDateReviewer;

    @Autowired
    @Qualifier("unattestedCriteriaWithDataReviewer")
    private UnattestedCriteriaWithDataReviewer unattestedCriteriaWithDataReviewer;

    @Autowired
    @Qualifier("testStandardReviewer")
    private TestStandardReviewer tsReviewer;

    @Autowired
    @Qualifier("testToolReviewer")
    private TestToolReviewer ttReviewer;

    @Autowired
    @Qualifier("testTool2014Reviewer")
    private TestTool2014Reviewer tt2014Reviewer;

    @Autowired
    @Qualifier("testFunctionality2014Reviewer")
    private TestFunctionality2014Reviewer tfReviewer;

    @Autowired
    @Qualifier("urlReviewer")
    private UrlReviewer urlReviewer;

    @Autowired
    @Qualifier("chplNumberComparisonReviewer")
    private ChplNumberComparisonReviewer chplNumberComparisonReviewer;

    @Autowired
    @Qualifier("developerBanComparisonReviewer")
    private DeveloperBanComparisonReviewer devBanComparisonReviewer;

    @Autowired
    @Qualifier("listingStatusAndUserRoleReviewer")
    private ListingStatusAndUserRoleReviewer listingStatusAndUserRoleReviewer;

    @Autowired
    @Qualifier("duplicateDataReviewer")
    private DuplicateDataReviewer duplicateDataReviewer;

    @Autowired
    @Qualifier("realWorldTestingReviewer")
    private RealWorldTestingReviewer realWorldTestingReviewer;

    @Autowired
    @Qualifier("svapReviewer")
    private SvapReviewer svapReviewer;

    private List<Reviewer> reviewers;
    private List<ComparisonReviewer> comparisonReviewers;

    @Override
    public List<Reviewer> getReviewers() {
        if (reviewers == null) {
            reviewers = new ArrayList<Reviewer>();
            reviewers.add(devStatusReviewer);
            reviewers.add(unsupportedCharacterReviewer);
            reviewers.add(fieldLengthReviewer);
            reviewers.add(requiredFieldReviewer);
            reviewers.add(validDataReviewer);
            reviewers.add(certStatusReviewer);
            reviewers.add(certDateReviewer);
            reviewers.add(unattestedCriteriaWithDataReviewer);
            reviewers.add(tsReviewer);
            reviewers.add(ttReviewer);
            reviewers.add(tt2014Reviewer);
            reviewers.add(tfReviewer);
            reviewers.add(urlReviewer);
            reviewers.add(duplicateDataReviewer);
        }
        return reviewers;
    }

    @Override
    public List<ComparisonReviewer> getComparisonReviewers() {
        if (comparisonReviewers == null) {
            comparisonReviewers = new ArrayList<ComparisonReviewer>();
            comparisonReviewers.add(chplNumberComparisonReviewer);
            comparisonReviewers.add(devBanComparisonReviewer);
            comparisonReviewers.add(listingStatusAndUserRoleReviewer);
            comparisonReviewers.add(realWorldTestingReviewer);
            comparisonReviewers.add(svapReviewer);
        }
        return comparisonReviewers;
    }
}
