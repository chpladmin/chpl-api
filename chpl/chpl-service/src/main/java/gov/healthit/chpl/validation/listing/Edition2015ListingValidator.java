package gov.healthit.chpl.validation.listing;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.validation.listing.reviewer.CertificationDateReviewer;
import gov.healthit.chpl.validation.listing.reviewer.CertificationStatusReviewer;
import gov.healthit.chpl.validation.listing.reviewer.ChplNumberComparisonReviewer;
import gov.healthit.chpl.validation.listing.reviewer.ChplNumberReviewer;
import gov.healthit.chpl.validation.listing.reviewer.ComparisonReviewer;
import gov.healthit.chpl.validation.listing.reviewer.DeveloperBanComparisonReviewer;
import gov.healthit.chpl.validation.listing.reviewer.DeveloperStatusReviewer;
import gov.healthit.chpl.validation.listing.reviewer.DuplicateDataReviewer;
import gov.healthit.chpl.validation.listing.reviewer.FieldLengthReviewer;
import gov.healthit.chpl.validation.listing.reviewer.InheritedCertificationStatusReviewer;
import gov.healthit.chpl.validation.listing.reviewer.ListingStatusAndUserRoleReviewer;
import gov.healthit.chpl.validation.listing.reviewer.RealWorldTestingReviewer;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;
import gov.healthit.chpl.validation.listing.reviewer.SvapReviewer;
import gov.healthit.chpl.validation.listing.reviewer.TestToolReviewer;
import gov.healthit.chpl.validation.listing.reviewer.TestingLabReviewer;
import gov.healthit.chpl.validation.listing.reviewer.UnattestedCriteriaWithDataReviewer;
import gov.healthit.chpl.validation.listing.reviewer.UnsupportedCharacterReviewer;
import gov.healthit.chpl.validation.listing.reviewer.UrlReviewer;
import gov.healthit.chpl.validation.listing.reviewer.ValidDataReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.AttestedCriteriaCqmReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.CqmAttestedCriteriaReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.GapAllowedReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.InvalidCriteriaCombinationReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.MacraMeasureComparisonReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.OldCriteriaWithoutIcsReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.PrivacyAndSecurityCriteriaReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.RemovedCriteriaComparisonReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.RemovedCriteriaTestTaskComparisonReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.RemovedCriteriaUcdComparisonReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.RequiredData2015Reviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.SedG32015Reviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.TestFunctionality2015Reviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.TestFunctionalityAllowedByRoleReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.TestTool2015Reviewer;

@Component
public class Edition2015ListingValidator extends Validator {
    @Autowired
    @Qualifier("chplNumberReviewer")
    private ChplNumberReviewer chplNumberReviewer;

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
    @Qualifier("requiredData2015Reviewer")
    private RequiredData2015Reviewer requiredDataReviewer;

    @Autowired
    @Qualifier("testingLabReviewer")
    private TestingLabReviewer testingLabReviewer;

    @Autowired
    @Qualifier("validDataReviewer")
    private ValidDataReviewer validDataReviewer;

    @Autowired
    @Qualifier("sedG32015Reviewer")
    private SedG32015Reviewer sedG3Reviewer;

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
    @Qualifier("testToolReviewer")
    private TestToolReviewer ttReviewer;

    @Autowired
    @Qualifier("testTool2015Reviewer")
    private TestTool2015Reviewer tt2015Reviewer;

    @Autowired
    @Qualifier("icsReviewer")
    private InheritedCertificationStatusReviewer icsReviewer;

    @Autowired
    @Qualifier("urlReviewer")
    private UrlReviewer urlReviewer;

    @Autowired
    @Qualifier("testFunctionality2015Reviewer")
    private TestFunctionality2015Reviewer testFunctionalityReviewer;

    @Autowired
    @Qualifier("duplicateDataReviewer")
    private DuplicateDataReviewer duplicateDataReviewer;

    @Autowired
    @Qualifier("developerBanComparisonReviewer")
    private DeveloperBanComparisonReviewer devBanComparisonReviewer;

    @Autowired
    @Qualifier("chplNumberComparisonReviewer")
    private ChplNumberComparisonReviewer chplNumberComparisonReviewer;

    @Autowired
    @Qualifier("macraMeasureComparisonReviewer")
    private MacraMeasureComparisonReviewer macraComparisonReviewer;

    @Autowired
    @Qualifier("oldCriteriaWithoutIcsReviewer")
    private OldCriteriaWithoutIcsReviewer oldCriteriaWithoutIcsReviewer;

    @Autowired
    @Qualifier("removedCriteriaComparisonReviewer")
    private RemovedCriteriaComparisonReviewer criteriaComparisonReviewer;

    @Autowired
    @Qualifier("removedCriteriaTestTaskComparisonReviewer")
    private RemovedCriteriaTestTaskComparisonReviewer testTaskCriteriaComparisonReviewer;

    @Autowired
    @Qualifier("removedCriteriaUcdComparisonReviewer")
    private RemovedCriteriaUcdComparisonReviewer ucdCriteriaComparisonReviewer;

    @Autowired
    @Qualifier("privacyAndSecurityCriteriaReviewer")
    private PrivacyAndSecurityCriteriaReviewer privacyAndSecurityCriteriaReviewer;

    @Autowired
    @Qualifier("invalidCriteriaCombinationReviewer")
    private InvalidCriteriaCombinationReviewer invalidCriteriaCombinationReviewer;

    @Autowired
    @Qualifier("cqmAttestedCriteriaReviewer")
    private CqmAttestedCriteriaReviewer cqmAttestedCriteriaReviewer;

    @Autowired
    @Qualifier("attestedCriteriaCqmReviewer")
    private AttestedCriteriaCqmReviewer attestedCriteriaCqmReviewer;

    @Autowired
    @Qualifier("testFunctionalityAllowedByRoleReviewer")
    private TestFunctionalityAllowedByRoleReviewer testFunctionalityAllowedByRoleReviewer;

    @Autowired
    @Qualifier("listingStatusAndUserRoleReviewer")
    private ListingStatusAndUserRoleReviewer listingStatusAndUserRoleReviewer;

    @Autowired
    @Qualifier("svapReviewer")
    private SvapReviewer svapReviewer;

    @Autowired
    @Qualifier("realWorldTestingReviewer")
    private RealWorldTestingReviewer realWorldTestingReviewer;

    @Autowired
    @Qualifier("gapAllowedReviewer")
    private GapAllowedReviewer gapAllowedReviewer;

    private List<Reviewer> reviewers;
    private List<ComparisonReviewer> comparisonReviewers;

    @Override
    public List<Reviewer> getReviewers() {
        if (reviewers == null) {
            reviewers = new ArrayList<Reviewer>();
            reviewers.add(chplNumberReviewer);
            reviewers.add(devStatusReviewer);
            reviewers.add(unsupportedCharacterReviewer);
            reviewers.add(fieldLengthReviewer);
            reviewers.add(requiredDataReviewer);
            reviewers.add(testingLabReviewer);
            reviewers.add(validDataReviewer);
            reviewers.add(sedG3Reviewer);
            reviewers.add(oldCriteriaWithoutIcsReviewer);
            reviewers.add(certStatusReviewer);
            reviewers.add(certDateReviewer);
            reviewers.add(unattestedCriteriaWithDataReviewer);
            reviewers.add(icsReviewer);
            reviewers.add(ttReviewer);
            reviewers.add(tt2015Reviewer);
            reviewers.add(urlReviewer);
            reviewers.add(testFunctionalityReviewer);
            reviewers.add(invalidCriteriaCombinationReviewer);
            reviewers.add(attestedCriteriaCqmReviewer);
            reviewers.add(cqmAttestedCriteriaReviewer);
            reviewers.add(duplicateDataReviewer);
            reviewers.add(svapReviewer);
            reviewers.add(gapAllowedReviewer);
        }
        return reviewers;
    }

    @Override
    public List<ComparisonReviewer> getComparisonReviewers() {
        if (comparisonReviewers == null) {
            comparisonReviewers = new ArrayList<ComparisonReviewer>();
            comparisonReviewers.add(chplNumberComparisonReviewer);
            comparisonReviewers.add(devBanComparisonReviewer);
            comparisonReviewers.add(macraComparisonReviewer);
            comparisonReviewers.add(criteriaComparisonReviewer);
            comparisonReviewers.add(testTaskCriteriaComparisonReviewer);
            comparisonReviewers.add(ucdCriteriaComparisonReviewer);
            comparisonReviewers.add(testFunctionalityAllowedByRoleReviewer);
            comparisonReviewers.add(listingStatusAndUserRoleReviewer);
            comparisonReviewers.add(privacyAndSecurityCriteriaReviewer);
            comparisonReviewers.add(realWorldTestingReviewer);
        }
        return comparisonReviewers;
    }
}
