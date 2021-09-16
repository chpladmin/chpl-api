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
import gov.healthit.chpl.validation.listing.reviewer.DeprecatedFieldReviewer;
import gov.healthit.chpl.validation.listing.reviewer.DeveloperBanComparisonReviewer;
import gov.healthit.chpl.validation.listing.reviewer.DeveloperStatusReviewer;
import gov.healthit.chpl.validation.listing.reviewer.DuplicateDataReviewer;
import gov.healthit.chpl.validation.listing.reviewer.FieldLengthReviewer;
import gov.healthit.chpl.validation.listing.reviewer.InheritanceComparisonReviewer;
import gov.healthit.chpl.validation.listing.reviewer.InheritanceReviewer;
import gov.healthit.chpl.validation.listing.reviewer.ListingStatusAndUserRoleReviewer;
import gov.healthit.chpl.validation.listing.reviewer.OptionalStandardReviewer;
import gov.healthit.chpl.validation.listing.reviewer.RealWorldTestingReviewer;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;
import gov.healthit.chpl.validation.listing.reviewer.SvapReviewer;
import gov.healthit.chpl.validation.listing.reviewer.TestProcedureReviewer;
import gov.healthit.chpl.validation.listing.reviewer.TestStandardRemovalReviewer;
import gov.healthit.chpl.validation.listing.reviewer.TestStandardReviewer;
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
import gov.healthit.chpl.validation.listing.reviewer.edition2015.MeasureComparisonReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.MeasureValidityReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.OldCriteriaWithoutIcsReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.PrivacyAndSecurityCriteriaReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.RemovedCriteriaComparisonReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.RemovedCriteriaTestTaskComparisonReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.RemovedCriteriaUcdComparisonReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.RequiredAndRelatedCriteriaReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.RequiredData2015Reviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.SedG32015Reviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.TestFunctionalityAllowedByCriteriaReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.TestFunctionalityAllowedByRoleReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.TestTool2015Reviewer;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
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
    @Qualifier("requiredAndRelatedCriteriaReviewer")
    private RequiredAndRelatedCriteriaReviewer requiredAndRelatedCriteriaReviewer;

    @Autowired
    @Qualifier("edition20142015testingLabReviewer")
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
    @Qualifier("testStandardRemovalReviewer")
    private TestStandardRemovalReviewer tsrReviewer;

    @Autowired
    @Qualifier("testStandardReviewer")
    private TestStandardReviewer tsReviewer;

    @Autowired
    @Qualifier("testProcedureReviewer")
    private TestProcedureReviewer tpReviewer;

    @Autowired
    @Qualifier("testToolReviewer")
    private TestToolReviewer ttReviewer;

    @Autowired
    @Qualifier("testTool2015Reviewer")
    private TestTool2015Reviewer tt2015Reviewer;

    @Autowired
    @Qualifier("inheritanceReviewer")
    private InheritanceReviewer inheritanceReviewer;

    @Autowired
    @Qualifier("inheritanceComparisonReviewer")
    private InheritanceComparisonReviewer inheritanceComparisonReviewer;

    @Autowired
    @Qualifier("urlReviewer")
    private UrlReviewer urlReviewer;

    @Autowired
    @Qualifier("testFunctionalityAllowedByCriteriaReviewer")
    private TestFunctionalityAllowedByCriteriaReviewer testFunctionalityReviewer;

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
    @Qualifier("measureComparisonReviewer")
    private MeasureComparisonReviewer measureComparisonReviewer;

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

    @Autowired
    @Qualifier("measureValidityReviewer")
    private MeasureValidityReviewer measureReviewer;

    @Autowired
    @Qualifier("optionalStandardReviewer")
    private OptionalStandardReviewer optionalStandardReviewer;

    @Autowired
    @Qualifier("deprecatedFieldReviewer")
    private DeprecatedFieldReviewer deprecatedFieldReviewer;

    private List<Reviewer> reviewers;
    private List<ComparisonReviewer> comparisonReviewers;

    @Override
    public List<Reviewer> getReviewers() {
        if (reviewers == null) {
            reviewers = new ArrayList<Reviewer>();
            if (chplNumberReviewer == null) {
                LOGGER.error("The chplNumberReviewer is null");
            } else {
                reviewers.add(chplNumberReviewer);
            }
            if (devStatusReviewer == null) {
                LOGGER.error("The devStatusReviewer is null");
            } else {
                reviewers.add(devStatusReviewer);
            }
            if (unsupportedCharacterReviewer == null) {
                LOGGER.error("The unsupportedCharacterReviewer is null");
            } else {
                reviewers.add(unsupportedCharacterReviewer);
            }
            if (fieldLengthReviewer == null) {
                LOGGER.error("The fieldLengthReviewer is null");
            } else {
                reviewers.add(fieldLengthReviewer);
            }
            if (requiredDataReviewer == null) {
                LOGGER.error("The requiredDataReviewer is null");
            } else {
                reviewers.add(requiredDataReviewer);
            }
            if (requiredAndRelatedCriteriaReviewer == null) {
                LOGGER.error("The requiredAndRelatedCriteriaReviewer is null");
            } else {
                reviewers.add(requiredAndRelatedCriteriaReviewer);
            }
            if (testingLabReviewer == null) {
                LOGGER.error("The testingLabReviewer is null");
            } else {
                reviewers.add(testingLabReviewer);
            }
            if (validDataReviewer == null) {
                LOGGER.error("The validDataReviewer is null");
            } else {
                reviewers.add(validDataReviewer);
            }
            if (sedG3Reviewer == null) {
                LOGGER.error("The sedG3Reviewer is null");
            } else {
                reviewers.add(sedG3Reviewer);
            }
            if (oldCriteriaWithoutIcsReviewer == null) {
                LOGGER.error("The oldCriteriaWithoutIcsReviewer is null");
            } else {
                reviewers.add(oldCriteriaWithoutIcsReviewer);
            }
            if (certStatusReviewer == null) {
                LOGGER.error("The certStatusReviewer is null");
            } else {
                reviewers.add(certStatusReviewer);
            }
            if (certDateReviewer == null) {
                LOGGER.error("The certDateReviewer is null");
            } else {
                reviewers.add(certDateReviewer);
            }
            if (unattestedCriteriaWithDataReviewer == null) {
                LOGGER.error("The unattestedCriteriaWithDataReviewer is null");
            } else {
                reviewers.add(unattestedCriteriaWithDataReviewer);
            }
            if (optionalStandardReviewer == null) {
                LOGGER.error("The optionalStandardReviewer is null");
            } else {
                reviewers.add(optionalStandardReviewer);
            }
            if (tsrReviewer == null) {
                LOGGER.error("The tsrReviewer is null");
            } else {
                reviewers.add(tsrReviewer);
            }
            if (tsReviewer == null) {
                LOGGER.error("The tsReviewer is null");
            } else {
                reviewers.add(tsReviewer);
            }
            if (tpReviewer == null) {
                LOGGER.error("The tpReviewer is null");
            } else {
                reviewers.add(tpReviewer);
            }

            if (inheritanceReviewer == null) {
                LOGGER.error("The inheritanceReviewer is null");
            } else {
                reviewers.add(inheritanceReviewer);
            }
            if (ttReviewer == null) {
                LOGGER.error("The ttReviewer is null");
            } else {
                reviewers.add(ttReviewer);
            }
            if (tt2015Reviewer == null) {
                LOGGER.error("The tt2015Reviewer is null");
            } else {
                reviewers.add(tt2015Reviewer);
            }
            if (urlReviewer == null) {
                LOGGER.error("The urlReviewer is null");
            } else {
                reviewers.add(urlReviewer);
            }
            if (testFunctionalityReviewer == null) {
                LOGGER.error("The testFunctionalityReviewer is null");
            } else {
                reviewers.add(testFunctionalityReviewer);
            }
            if (invalidCriteriaCombinationReviewer == null) {
                LOGGER.error("The invalidCriteriaCombinationReviewer is null");
            } else {
                reviewers.add(invalidCriteriaCombinationReviewer);
            }
            if (attestedCriteriaCqmReviewer == null) {
                LOGGER.error("The attestedCriteriaCqmReviewer is null");
            } else {
                reviewers.add(attestedCriteriaCqmReviewer);
            }
            if (cqmAttestedCriteriaReviewer == null) {
                LOGGER.error("The cqmAttestedCriteriaReviewer is null");
            } else {
                reviewers.add(cqmAttestedCriteriaReviewer);
            }
            if (duplicateDataReviewer == null) {
                LOGGER.error("The duplicateDataReviewer is null");
            } else {
                reviewers.add(duplicateDataReviewer);
            }
            if (gapAllowedReviewer == null) {
                LOGGER.error("The gapAllowedReviewer is null");
            } else {
                reviewers.add(gapAllowedReviewer);
            }
            if (measureReviewer == null) {
                LOGGER.error("The measureReviewer is null");
            } else {
                reviewers.add(measureReviewer);
            }
        }
        return reviewers;
    }

    @Override
    public List<ComparisonReviewer> getComparisonReviewers() {
        if (comparisonReviewers == null) {
            comparisonReviewers = new ArrayList<ComparisonReviewer>();
            comparisonReviewers.add(chplNumberComparisonReviewer);
            comparisonReviewers.add(devBanComparisonReviewer);
            comparisonReviewers.add(measureComparisonReviewer);
            comparisonReviewers.add(criteriaComparisonReviewer);
            comparisonReviewers.add(testTaskCriteriaComparisonReviewer);
            comparisonReviewers.add(ucdCriteriaComparisonReviewer);
            comparisonReviewers.add(testFunctionalityAllowedByRoleReviewer);
            comparisonReviewers.add(listingStatusAndUserRoleReviewer);
            comparisonReviewers.add(privacyAndSecurityCriteriaReviewer);
            comparisonReviewers.add(realWorldTestingReviewer);
            comparisonReviewers.add(svapReviewer);
            comparisonReviewers.add(inheritanceComparisonReviewer);
            comparisonReviewers.add(deprecatedFieldReviewer);
        }
        return comparisonReviewers;
    }
}
