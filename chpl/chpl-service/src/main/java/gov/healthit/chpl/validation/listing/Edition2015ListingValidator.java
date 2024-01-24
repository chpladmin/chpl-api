package gov.healthit.chpl.validation.listing;

import java.util.ArrayList;
import java.util.List;

import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.upload.listing.validation.reviewer.AccessibilityStandardReviewer;
import gov.healthit.chpl.upload.listing.validation.reviewer.QmsStandardReviewer;
import gov.healthit.chpl.upload.listing.validation.reviewer.TestToolReviewer;
import gov.healthit.chpl.upload.listing.validation.reviewer.UcdProcessReviewer;
import gov.healthit.chpl.validation.listing.reviewer.CertificationDateReviewer;
import gov.healthit.chpl.validation.listing.reviewer.CertificationStatusReviewer;
import gov.healthit.chpl.validation.listing.reviewer.ChplNumberComparisonReviewer;
import gov.healthit.chpl.validation.listing.reviewer.ChplNumberReviewer;
import gov.healthit.chpl.validation.listing.reviewer.ComparisonReviewer;
import gov.healthit.chpl.validation.listing.reviewer.ConformanceMethodReviewer;
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
import gov.healthit.chpl.validation.listing.reviewer.StandardRemovalReviewer;
import gov.healthit.chpl.validation.listing.reviewer.SvapReviewer;
import gov.healthit.chpl.validation.listing.reviewer.TestProcedureReviewer;
import gov.healthit.chpl.validation.listing.reviewer.TestStandardRemovalReviewer;
import gov.healthit.chpl.validation.listing.reviewer.TestStandardReviewer;
import gov.healthit.chpl.validation.listing.reviewer.TestingLabComparisonReviewer;
import gov.healthit.chpl.validation.listing.reviewer.TestingLabReviewer;
import gov.healthit.chpl.validation.listing.reviewer.UnavailableCriteriaReviewer;
import gov.healthit.chpl.validation.listing.reviewer.UnsupportedCharacterReviewer;
import gov.healthit.chpl.validation.listing.reviewer.UrlReviewer;
import gov.healthit.chpl.validation.listing.reviewer.ValidDataReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.AttestedCriteriaCqmReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.CqmAttestedCriteriaReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.FunctionalityTestedAllowedByCriteriaReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.FunctionalityTestedAllowedByRoleReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.GapAllowedReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.InvalidCriteriaCombinationReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.MeasureComparisonReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.MeasureValidityReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.OldCriteriaWithoutIcsReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.PrivacyAndSecurityCriteriaReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.PrivacyAndSecurityCriteriaReviewerPreErdPhase2;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.RequiredAndRelatedCriteriaErdPhase2GracePeriodReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.RequiredAndRelatedCriteriaReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.RequiredData2015Reviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.SedG32015Reviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.StandardAllowedByCriteriaReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.UnavailableCriteriaComparisonReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.UnavailableCriteriaTestTaskComparisonReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.UnavailableCriteriaUcdComparisonReviewer;
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
    @Qualifier("requiredAndRelatedCriteriaErdPhase2GracePeriodReviewer")
    private RequiredAndRelatedCriteriaErdPhase2GracePeriodReviewer requiredAndRelatedCriteriaErdPhase2GracePeriodReviewer;

    @Autowired
    @Qualifier("requiredAndRelatedCriteriaReviewer")
    private RequiredAndRelatedCriteriaReviewer requiredAndRelatedCriteriaReviewer;

    @Autowired
    @Qualifier("listingUnavailableCriteriaReviewer")
    private UnavailableCriteriaReviewer unavailableCriteriaReviewer;

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
    @Qualifier("testStandardRemovalReviewer")
    private TestStandardRemovalReviewer tsrReviewer;

    @Autowired
    @Qualifier("testStandardReviewer")
    private TestStandardReviewer tsReviewer;

    @Autowired
    @Qualifier("testProcedureReviewer")
    private TestProcedureReviewer tpReviewer;

    @Autowired
    @Qualifier("listingUploadTestToolReviewer")
    private TestToolReviewer ttReviewer;

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
    @Qualifier("functionalityTestedAllowedByCriteriaReviewer")
    private FunctionalityTestedAllowedByCriteriaReviewer functionalityTestedReviewer;

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
    @Qualifier("unavailableCriteriaComparisonReviewer")
    private UnavailableCriteriaComparisonReviewer unavailableCriteriaComparisonReviewer;

    @Autowired
    @Qualifier("unavailableCriteriaTestTaskComparisonReviewer")
    private UnavailableCriteriaTestTaskComparisonReviewer unavailableCriteriaTestTaskComparisonReviewer;

    @Autowired
    @Qualifier("unavailableCriteriaUcdComparisonReviewer")
    private UnavailableCriteriaUcdComparisonReviewer unavailableCriteriaUcdComparisonReviewer;

    @Autowired
    @Qualifier("privacyAndSecurityCriteriaReviewerPreErdPhase2")
    private PrivacyAndSecurityCriteriaReviewerPreErdPhase2 privacyAndSecurityCriteriaReviewerPreErdPhase2;

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
    @Qualifier("functionalityTestedAllowedByRoleReviewer")
    private FunctionalityTestedAllowedByRoleReviewer functionalityTestedAllowedByRoleReviewer;

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
    @Qualifier("conformanceMethodReviewer")
    private ConformanceMethodReviewer conformanceMethodReviewer;

    @Autowired
    @Qualifier("ucdProcessReviewer")
    private UcdProcessReviewer ucdProcessReviewer;

    @Autowired
    private AccessibilityStandardReviewer accessibilityStandardReviewer;

    @Autowired
    private QmsStandardReviewer qmsStandardReviewer;

    @Autowired
    private StandardAllowedByCriteriaReviewer standardAllowedByCriteriaReviewer;

    @Autowired
    @Qualifier("deprecatedFieldReviewer")
    private DeprecatedFieldReviewer deprecatedFieldReviewer;

    @Autowired
    private TestingLabComparisonReviewer testingLabComparisonReviewer;

    @Autowired
    private StandardRemovalReviewer standardRemovalReviewer;

    @Autowired
    private FF4j ff4j;

    @Override
    public synchronized List<Reviewer> getReviewers() {
        List<Reviewer> reviewers = new ArrayList<Reviewer>();
        reviewers.add(chplNumberReviewer);
        reviewers.add(devStatusReviewer);
        reviewers.add(unsupportedCharacterReviewer);
        reviewers.add(fieldLengthReviewer);
        reviewers.add(requiredDataReviewer);
        if (!ff4j.check(FeatureList.ERD_PHASE_2_GRACE_PERIOD_END)) {
            //use this reviewer during the grace period
            reviewers.add(requiredAndRelatedCriteriaErdPhase2GracePeriodReviewer);
        } else {
            //use this reviewer after the grace period ends
            reviewers.add(requiredAndRelatedCriteriaReviewer);
        }
        reviewers.add(unavailableCriteriaReviewer);
        reviewers.add(testingLabReviewer);
        reviewers.add(validDataReviewer);
        reviewers.add(sedG3Reviewer);
        reviewers.add(ucdProcessReviewer);
        reviewers.add(oldCriteriaWithoutIcsReviewer);
        reviewers.add(certStatusReviewer);
        reviewers.add(certDateReviewer);
        reviewers.add(optionalStandardReviewer);
        reviewers.add(conformanceMethodReviewer);
        reviewers.add(tsrReviewer);
        reviewers.add(tsReviewer);
        reviewers.add(tpReviewer);
        reviewers.add(inheritanceReviewer);
        reviewers.add(ttReviewer);
        reviewers.add(urlReviewer);
        reviewers.add(functionalityTestedReviewer);
        reviewers.add(standardAllowedByCriteriaReviewer);
        reviewers.add(invalidCriteriaCombinationReviewer);
        reviewers.add(attestedCriteriaCqmReviewer);
        reviewers.add(cqmAttestedCriteriaReviewer);
        reviewers.add(duplicateDataReviewer);
        reviewers.add(gapAllowedReviewer);
        reviewers.add(measureReviewer);
        reviewers.add(accessibilityStandardReviewer);
        reviewers.add(qmsStandardReviewer);
        reviewers.add(realWorldTestingReviewer);
        //after the grace period ends this reviewer should be included
        if (ff4j.check(FeatureList.ERD_PHASE_2_GRACE_PERIOD_END)) {
            reviewers.add(privacyAndSecurityCriteriaReviewer);
        }
        return reviewers;
    }

    @Override
    public List<ComparisonReviewer> getComparisonReviewers() {
            List<ComparisonReviewer> comparisonReviewers = new ArrayList<ComparisonReviewer>();
        comparisonReviewers.add(chplNumberComparisonReviewer);
        comparisonReviewers.add(devBanComparisonReviewer);
        comparisonReviewers.add(measureComparisonReviewer);
        comparisonReviewers.add(unavailableCriteriaComparisonReviewer);
        comparisonReviewers.add(unavailableCriteriaTestTaskComparisonReviewer);
        comparisonReviewers.add(unavailableCriteriaUcdComparisonReviewer);
        comparisonReviewers.add(functionalityTestedAllowedByRoleReviewer);
        comparisonReviewers.add(listingStatusAndUserRoleReviewer);
        //during the grace period the comparison reviewer should be included
        if (!ff4j.check(FeatureList.ERD_PHASE_2_GRACE_PERIOD_END)) {
            comparisonReviewers.add(privacyAndSecurityCriteriaReviewerPreErdPhase2);
        }
        comparisonReviewers.add(svapReviewer);
        comparisonReviewers.add(inheritanceComparisonReviewer);
        comparisonReviewers.add(deprecatedFieldReviewer);
        comparisonReviewers.add(testingLabComparisonReviewer);
        comparisonReviewers.add(standardRemovalReviewer);
        return comparisonReviewers;
    }
}
