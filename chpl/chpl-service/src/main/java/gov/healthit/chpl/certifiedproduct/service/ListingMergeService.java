package gov.healthit.chpl.certifiedproduct.service;

import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.codeset.CertificationResultCodeSet;
import gov.healthit.chpl.conformanceMethod.domain.CertificationResultConformanceMethod;
import gov.healthit.chpl.domain.CQMResultCertification;
import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultAdditionalSoftware;
import gov.healthit.chpl.domain.CertificationResultTestData;
import gov.healthit.chpl.domain.CertificationResultTestProcedure;
import gov.healthit.chpl.domain.CertificationResultTestStandard;
import gov.healthit.chpl.domain.CertifiedProductAccessibilityStandard;
import gov.healthit.chpl.domain.CertifiedProductQmsStandard;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductSed;
import gov.healthit.chpl.domain.CertifiedProductTargetedUser;
import gov.healthit.chpl.domain.ListingMeasure;
import gov.healthit.chpl.domain.TestParticipant;
import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.functionalitytested.CertificationResultFunctionalityTested;
import gov.healthit.chpl.optionalStandard.domain.CertificationResultOptionalStandard;
import gov.healthit.chpl.standard.CertificationResultStandard;
import gov.healthit.chpl.svap.domain.CertificationResultSvap;
import gov.healthit.chpl.testtool.CertificationResultTestTool;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ChplProductNumberUtil.ChplProductNumberParts;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class ListingMergeService {

    private ChplProductNumberUtil chplProductNumberUtil;

    @Autowired
    public ListingMergeService(ChplProductNumberUtil chplProductNumberUtil) {
        this.chplProductNumberUtil = chplProductNumberUtil;
    }

    /**
     * Merges the passed-in listing details object with the data from the listing in CHPL.
     * The fields that are copied from CHPL into the passed-in listing are things that the user is
     * not allowed to update via a file. Things like the certification status history, developer/product/version,
     * of the listing. Also we will copy in the IDs of all the fields that we can match with certainty -
     * for example the cert result IDs will be pulled from the database, the cqm result IDs, etc.
     */
    @Transactional
    public void mergeWithListingFromChpl(CertifiedProductSearchDetails updatedListing, CertifiedProductSearchDetails currentListing)
            throws ValidationException {

        //The uploaded listing's CHPL Product Number must match what the current listing's CHPL product number
        //will be after applying the ICS and Additional Software codes from uploaded data. These two fields are
        //specifically applied because they are derived from listing data that is updateable via a file).
        String currentListingChplProductNumberAfterUpdates = applyUpdatesToChplProductNumber(updatedListing, currentListing);
        if (!updatedListing.getChplProductNumber().equals(currentListingChplProductNumberAfterUpdates)) {
            throw new ValidationException("The CHPL Product Number in the uploaded file " + updatedListing.getChplProductNumber()
                        + " does not match the expected CHPL Product Number of the listing after updates are applied: "
                        + currentListingChplProductNumberAfterUpdates + ". "
                        + "Product code, Version code, and Certified Date code may not be changed via upload file.");
        }

        //Set all the things that cannot be changed via upload with the values
        //they currently have from the database.
        updatedListing.setId(currentListing.getId());
        updatedListing.setCertificationEvents(currentListing.getCertificationEvents());
        updatedListing.setDeveloper(currentListing.getDeveloper());
        updatedListing.setProduct(currentListing.getProduct());
        updatedListing.setVersion(currentListing.getVersion());
        updatedListing.setPromotingInteroperabilityUserHistory(currentListing.getPromotingInteroperabilityUserHistory());
        updatedListing.setChplProductNumberHistory(currentListing.getChplProductNumberHistory());
        updatedListing.setCertificationDate(currentListing.getCertificationDate());
        updatedListing.setCertifyingBody(currentListing.getCertifyingBody());
        updatedListing.setDecertificationDay(currentListing.getDecertificationDay());
        updatedListing.setDirectReviews(currentListing.getDirectReviews());
        updatedListing.setDirectReviewsAvailable(currentListing.isDirectReviewsAvailable());
        updatedListing.setEdition(currentListing.getEdition());
        updatedListing.setSurveillance(currentListing.getSurveillance());
        updatedListing.setTestingLabs(currentListing.getTestingLabs());

        setIdsForQmsStandards(updatedListing, currentListing.getQmsStandards());
        setIdsForAccessibilityStandards(updatedListing, currentListing.getAccessibilityStandards());
        setIdsForTargetedUsers(updatedListing, currentListing.getTargetedUsers());
        setIdsForMeasures(updatedListing, currentListing.getMeasures());
        setIdsForSed(updatedListing.getSed(), currentListing.getSed());
        if (!CollectionUtils.isEmpty(updatedListing.getCertificationResults())) {
            updatedListing.getCertificationResults().stream()
                .forEach(certResultFromFile -> setIdsForCertificationResults(certResultFromFile, currentListing));
        }

        if (!CollectionUtils.isEmpty(updatedListing.getCqmResults())) {
            updatedListing.getCqmResults().stream()
                .forEach(cqmResultFromFile -> setIdsForCqmResults(cqmResultFromFile, currentListing));
        }
    }

    private String applyUpdatesToChplProductNumber(CertifiedProductSearchDetails updatedListing, CertifiedProductSearchDetails currentListing) {
        ChplProductNumberParts currChplProductNumberParts
            = chplProductNumberUtil.parseChplProductNumber(currentListing.getChplProductNumber());
        currChplProductNumberParts.setIcsCode(chplProductNumberUtil.deriveIcsCodeFromListing(updatedListing));
        currChplProductNumberParts.setAdditionalSoftwareCode(chplProductNumberUtil.deriveAdditionalSoftwareCodeFromListing(updatedListing));
        return chplProductNumberUtil.getChplProductNumber(currChplProductNumberParts);
    }

    private void setIdsForQmsStandards(CertifiedProductSearchDetails updatedListing, List<CertifiedProductQmsStandard> currQmsStandards) {
        if (CollectionUtils.isEmpty(currQmsStandards)) {
            return;
        }

        currQmsStandards.stream()
            .forEach(currQms -> setIdInUpdatedQmsStandards(updatedListing.getQmsStandards(), currQms));
    }

    private void setIdInUpdatedQmsStandards(List<CertifiedProductQmsStandard> updatedQmsStandards, CertifiedProductQmsStandard currQmsStandard) {
        if (CollectionUtils.isEmpty(updatedQmsStandards)) {
            return;
        }

        CertifiedProductQmsStandard updatedQmsStandard = updatedQmsStandards.stream()
            .filter(updatedQms -> updatedQms.getQmsStandardId().equals(currQmsStandard.getQmsStandardId())
                    && updatedQms.getQmsModification().equals(currQmsStandard.getQmsModification())
                    && updatedQms.getApplicableCriteria().equals(currQmsStandard.getApplicableCriteria()))
            .findAny()
            .orElse(null);
        if (updatedQmsStandard != null) {
            updatedQmsStandard.setId(currQmsStandard.getId());
        }
    }

    private void setIdsForAccessibilityStandards(CertifiedProductSearchDetails updatedListing, List<CertifiedProductAccessibilityStandard> currAccessibilityStandards) {
        if (CollectionUtils.isEmpty(currAccessibilityStandards)) {
            return;
        }

        currAccessibilityStandards.stream()
            .forEach(currAccStd -> setIdInUpdatedAccessibilityStandards(updatedListing.getAccessibilityStandards(), currAccStd));
    }

    private void setIdInUpdatedAccessibilityStandards(List<CertifiedProductAccessibilityStandard> updatedAccessibilityStandards,
            CertifiedProductAccessibilityStandard currAccessibilityStandard) {
        if (CollectionUtils.isEmpty(updatedAccessibilityStandards)) {
            return;
        }

        CertifiedProductAccessibilityStandard updatedAccessibilityStandard = updatedAccessibilityStandards.stream()
            .filter(updatedAccStd -> updatedAccStd.getAccessibilityStandardId().equals(currAccessibilityStandard.getAccessibilityStandardId()))
            .findAny()
            .orElse(null);
        if (updatedAccessibilityStandard != null) {
            updatedAccessibilityStandard.setId(currAccessibilityStandard.getId());
        }
    }

    private void setIdsForTargetedUsers(CertifiedProductSearchDetails updatedListing,
            List<CertifiedProductTargetedUser> currTargetedUsers) {
        if (CollectionUtils.isEmpty(currTargetedUsers)) {
            return;
        }

        currTargetedUsers.stream()
            .forEach(currTargetedUser -> setIdInUpdatedTargetedUsers(updatedListing.getTargetedUsers(), currTargetedUser));
    }

    private void setIdInUpdatedTargetedUsers(List<CertifiedProductTargetedUser> updatedTargetedUsers,
            CertifiedProductTargetedUser currTargetedUser) {
        if (CollectionUtils.isEmpty(updatedTargetedUsers)) {
            return;
        }

        CertifiedProductTargetedUser updatedTargetedUser = updatedTargetedUsers.stream()
            .filter(updatedTu -> updatedTu.getTargetedUserId().equals(currTargetedUser.getTargetedUserId()))
            .findAny()
            .orElse(null);
        if (updatedTargetedUser != null) {
            updatedTargetedUser.setId(currTargetedUser.getId());
        }
    }

    private void setIdsForMeasures(CertifiedProductSearchDetails updatedListing, List<ListingMeasure> currMeasures) {
        if (CollectionUtils.isEmpty(currMeasures)) {
            return;
        }

        currMeasures.stream()
            .forEach(currMeasure -> setIdInUpdatedMeasures(updatedListing.getMeasures(), currMeasure));
    }

    private void setIdInUpdatedMeasures(List<ListingMeasure> updatedMeasures, ListingMeasure currMeasure) {
        if (CollectionUtils.isEmpty(updatedMeasures)) {
            return;
        }

        ListingMeasure matchingUpdatedMeasure = updatedMeasures.stream()
            .filter(updatedMeasure -> updatedMeasure.matches(currMeasure))
            .findAny()
            .orElse(null);
        if (matchingUpdatedMeasure != null) {
            matchingUpdatedMeasure.setId(currMeasure.getId());
        }
    }

    private void setIdsForSed(CertifiedProductSed updatedListingSed, CertifiedProductSed currListingSed) {
        //Note: We don't have a field for the internal database mapping ID between cert result -> UCD Process in
        //the listing details object. So there is nothing to fill in for UCD Process IDs.

        if (!CollectionUtils.isEmpty(updatedListingSed.getTestTasks())) {
            updatedListingSed.getTestTasks().stream()
                .forEach(updatedTestTask -> setIdForTestTask(updatedTestTask, currListingSed.getTestTasks()));
        }

    }

    private void setIdForTestTask(TestTask updatedTestTask, List<TestTask> currTestTasks) {
        if (CollectionUtils.isEmpty(currTestTasks)) {
            return;
        }

        TestTask matchedCurrTestTask = currTestTasks.stream()
                .filter(currTestTask -> currTestTask.matches(updatedTestTask))
                .findAny()
                .orElse(null);
            if (matchedCurrTestTask != null) {
                //TODO: test tasks are not matching and i think it's because of the participant uniqueID comparison
                updatedTestTask.setId(matchedCurrTestTask.getId());
                if (!CollectionUtils.isEmpty(updatedTestTask.getTestParticipants())) {
                    updatedTestTask.getTestParticipants().stream()
                        .forEach(updatedTestParticipant -> setIdForTestParticipant(updatedTestParticipant, matchedCurrTestTask.getTestParticipants()));
                }
            }
    }

    private void setIdForTestParticipant(TestParticipant updatedTestParticipant, Set<TestParticipant> currTestParticipants) {
        if (CollectionUtils.isEmpty(currTestParticipants)) {
            return;
        }

        TestParticipant matchedCurrTestParticipant = currTestParticipants.stream()
                .filter(currTestParticipant -> currTestParticipant.matches(updatedTestParticipant))
                .findAny()
                .orElse(null);
            if (matchedCurrTestParticipant != null) {
                updatedTestParticipant.setId(matchedCurrTestParticipant.getId());
            }
    }

    private void setIdsForCertificationResults(CertificationResult updatedCertResult, CertifiedProductSearchDetails currListing) {
        CertificationResult matchedCurrCertResult = currListing.getCertificationResults().stream()
            .filter(certResult -> certResult.getCriterion().getId().equals(updatedCertResult.getCriterion().getId()))
            .findAny().orElse(null);
        if (matchedCurrCertResult != null) {
            updatedCertResult.setId(matchedCurrCertResult.getId());
            if (!CollectionUtils.isEmpty(updatedCertResult.getAdditionalSoftware())) {
                updatedCertResult.getAdditionalSoftware().stream()
                    .forEach(crAs -> setIdForAdditionalSoftwareMapping(crAs, matchedCurrCertResult.getAdditionalSoftware()));
            }
            if (!CollectionUtils.isEmpty(updatedCertResult.getCodeSets())) {
                updatedCertResult.getCodeSets().stream()
                    .forEach(crCodeSet -> setIdForCodeSetMapping(crCodeSet, matchedCurrCertResult.getCodeSets()));
            }
            if (!CollectionUtils.isEmpty(updatedCertResult.getConformanceMethods())) {
                updatedCertResult.getConformanceMethods().stream()
                    .forEach(crConfMethod -> setIdForConformanceMethodMapping(crConfMethod, matchedCurrCertResult.getConformanceMethods()));
            }
            if (!CollectionUtils.isEmpty(updatedCertResult.getFunctionalitiesTested())) {
                updatedCertResult.getFunctionalitiesTested().stream()
                    .forEach(crFuncTested -> setIdForFunctionalityTestedMapping(crFuncTested, matchedCurrCertResult.getFunctionalitiesTested()));
            }
            if (!CollectionUtils.isEmpty(updatedCertResult.getOptionalStandards())) {
                updatedCertResult.getOptionalStandards().stream()
                    .forEach(crOptStd -> setIdForOptionalStandardMapping(crOptStd, matchedCurrCertResult.getOptionalStandards()));
            }
            if (!CollectionUtils.isEmpty(updatedCertResult.getStandards())) {
                updatedCertResult.getStandards().stream()
                    .forEach(crStd -> setIdForStandardMapping(crStd, matchedCurrCertResult.getStandards()));
            }
            if (!CollectionUtils.isEmpty(updatedCertResult.getSvaps())) {
                updatedCertResult.getSvaps().stream()
                    .forEach(crSvap -> setIdForSvapMapping(crSvap, matchedCurrCertResult.getSvaps()));
            }
            if (!CollectionUtils.isEmpty(updatedCertResult.getTestDataUsed())) {
                updatedCertResult.getTestDataUsed().stream()
                    .forEach(crTestData -> setIdForTestDataMapping(crTestData, matchedCurrCertResult.getTestDataUsed()));
            }
            if (!CollectionUtils.isEmpty(updatedCertResult.getTestProcedures())) {
                updatedCertResult.getTestProcedures().stream()
                    .forEach(crTestProc -> setIdForTestProcedureMapping(crTestProc, matchedCurrCertResult.getTestProcedures()));
            }
            if (!CollectionUtils.isEmpty(updatedCertResult.getTestStandards())) {
                updatedCertResult.getTestStandards().stream()
                    .forEach(crTestStd -> setIdForTestStandardMapping(crTestStd, matchedCurrCertResult.getTestStandards()));
            }
            if (!CollectionUtils.isEmpty(updatedCertResult.getTestToolsUsed())) {
                updatedCertResult.getTestToolsUsed().stream()
                    .forEach(crTestTool -> setIdForTestToolMapping(crTestTool, matchedCurrCertResult.getTestToolsUsed()));
            }
        }
    }

    private void setIdForAdditionalSoftwareMapping(CertificationResultAdditionalSoftware updatedCertAdditionalSoftware,
            List<CertificationResultAdditionalSoftware> currCertAdditionalSoftware) {
        if (CollectionUtils.isEmpty(currCertAdditionalSoftware)) {
            return;
        }

        CertificationResultAdditionalSoftware matchedCurrAdditionalSoftware = currCertAdditionalSoftware.stream()
                .filter(currCertAs -> currCertAs.matches(updatedCertAdditionalSoftware))
                .findAny()
                .orElse(null);
            if (matchedCurrAdditionalSoftware != null) {
                updatedCertAdditionalSoftware.setId(matchedCurrAdditionalSoftware.getId());
            }
    }

    private void setIdForCodeSetMapping(CertificationResultCodeSet updatedCertCodeSet, List<CertificationResultCodeSet> currCertCodeSets) {
        if (CollectionUtils.isEmpty(currCertCodeSets)) {
            return;
        }

        CertificationResultCodeSet matchedCurrCodeSet = currCertCodeSets.stream()
                .filter(currCertCs -> currCertCs.getCodeSet().getId().equals(updatedCertCodeSet.getCodeSet().getId()))
                .findAny()
                .orElse(null);
            if (matchedCurrCodeSet != null) {
                updatedCertCodeSet.setId(matchedCurrCodeSet.getId());
            }
    }

    private void setIdForConformanceMethodMapping(CertificationResultConformanceMethod updatedCertConformanceMethod,
            List<CertificationResultConformanceMethod> currCertConformanceMethods) {
        if (CollectionUtils.isEmpty(currCertConformanceMethods)) {
            return;
        }

        CertificationResultConformanceMethod matchedCurrConformanceMethod = currCertConformanceMethods.stream()
                .filter(currCertCm -> currCertCm.matches(updatedCertConformanceMethod))
                .findAny()
                .orElse(null);
            if (matchedCurrConformanceMethod != null) {
                updatedCertConformanceMethod.setId(matchedCurrConformanceMethod.getId());
            }
    }

    private void setIdForFunctionalityTestedMapping(CertificationResultFunctionalityTested updatedCertFuncTested,
            List<CertificationResultFunctionalityTested> currCertFuncsTested) {
        if (CollectionUtils.isEmpty(currCertFuncsTested)) {
            return;
        }

        CertificationResultFunctionalityTested matchedCurrFunctionalityTested = currCertFuncsTested.stream()
                .filter(currCertFt -> currCertFt.matches(updatedCertFuncTested))
                .findAny()
                .orElse(null);
            if (matchedCurrFunctionalityTested != null) {
                updatedCertFuncTested.setId(matchedCurrFunctionalityTested.getId());
            }
    }

    private void setIdForOptionalStandardMapping(CertificationResultOptionalStandard updatedCertOptionalStandard,
            List<CertificationResultOptionalStandard> currCertOptionalStandards) {
        if (CollectionUtils.isEmpty(currCertOptionalStandards)) {
            return;
        }

        CertificationResultOptionalStandard matchedCurrOptionalStandard = currCertOptionalStandards.stream()
                .filter(currCertOs -> currCertOs.matches(updatedCertOptionalStandard))
                .findAny()
                .orElse(null);
            if (matchedCurrOptionalStandard != null) {
                updatedCertOptionalStandard.setId(matchedCurrOptionalStandard.getId());
            }
    }

    private void setIdForStandardMapping(CertificationResultStandard updatedCertStandard,
            List<CertificationResultStandard> currCertStandards) {
        if (CollectionUtils.isEmpty(currCertStandards)) {
            return;
        }

        CertificationResultStandard matchedCurrStandard = currCertStandards.stream()
                .filter(currCertStd -> currCertStd.matches(updatedCertStandard))
                .findAny()
                .orElse(null);
            if (matchedCurrStandard != null) {
                updatedCertStandard.setId(matchedCurrStandard.getId());
            }
    }

    private void setIdForSvapMapping(CertificationResultSvap updatedCertSvap, List<CertificationResultSvap> currCertSvaps) {
        if (CollectionUtils.isEmpty(currCertSvaps)) {
            return;
        }

        CertificationResultSvap matchedCurrSvap = currCertSvaps.stream()
                .filter(currCertSvap -> currCertSvap.matches(updatedCertSvap))
                .findAny()
                .orElse(null);
            if (matchedCurrSvap != null) {
                updatedCertSvap.setId(matchedCurrSvap.getId());
            }
    }

    private void setIdForTestDataMapping(CertificationResultTestData updatedCertTestData, List<CertificationResultTestData> currCertTestData) {
        if (CollectionUtils.isEmpty(currCertTestData)) {
            return;
        }

        CertificationResultTestData matchedCurrTestData = currCertTestData.stream()
                .filter(currCertSvap -> currCertSvap.matches(updatedCertTestData))
                .findAny()
                .orElse(null);
            if (matchedCurrTestData != null) {
                updatedCertTestData.setId(matchedCurrTestData.getId());
            }
    }

    private void setIdForTestProcedureMapping(CertificationResultTestProcedure updatedCertTestProcedure,
            List<CertificationResultTestProcedure> currCertTestProcedures) {
        if (CollectionUtils.isEmpty(currCertTestProcedures)) {
            return;
        }

        CertificationResultTestProcedure matchedCurrTestProcedure = currCertTestProcedures.stream()
                .filter(currCertTp -> currCertTp.matches(updatedCertTestProcedure))
                .findAny()
                .orElse(null);
            if (matchedCurrTestProcedure != null) {
                updatedCertTestProcedure.setId(matchedCurrTestProcedure.getId());
            }
    }

    private void setIdForTestStandardMapping(CertificationResultTestStandard updatedCertTestStandard,
            List<CertificationResultTestStandard> currCertTestStandards) {
        if (CollectionUtils.isEmpty(currCertTestStandards)) {
            return;
        }

        CertificationResultTestStandard matchedCurrTestStandard = currCertTestStandards.stream()
                .filter(currCertTs -> currCertTs.matches(updatedCertTestStandard))
                .findAny()
                .orElse(null);
            if (matchedCurrTestStandard != null) {
                updatedCertTestStandard.setId(matchedCurrTestStandard.getId());
            }
    }

    private void setIdForTestToolMapping(CertificationResultTestTool updatedCertTestTool,
            List<CertificationResultTestTool> currCertTestTools) {
        if (CollectionUtils.isEmpty(currCertTestTools)) {
            return;
        }

        CertificationResultTestTool matchedCurrTestTool = currCertTestTools.stream()
                .filter(currCertTt -> currCertTt.matches(updatedCertTestTool))
                .findAny()
                .orElse(null);
            if (matchedCurrTestTool != null) {
                updatedCertTestTool.setId(matchedCurrTestTool.getId());
            }
    }

    private void setIdsForCqmResults(CQMResultDetails updatedCqmResult, CertifiedProductSearchDetails currListing) {
        if (CollectionUtils.isEmpty(currListing.getCqmResults())) {
            return;
        }

        CQMResultDetails matchedCurrCqmResult = currListing.getCqmResults().stream()
            .filter(cqmResult -> cqmResult.getCmsId().equals(updatedCqmResult.getCmsId()))
            .findAny()
            .orElse(null);
        if (matchedCurrCqmResult != null) {
            updatedCqmResult.setId(matchedCurrCqmResult.getId());
            if (!CollectionUtils.isEmpty(updatedCqmResult.getCriteria())) {
                updatedCqmResult.getCriteria().stream()
                    .forEach(updatedCqmCert -> setIdForCqmCertMapping(updatedCqmCert, matchedCurrCqmResult.getCriteria()));
            }
        }
    }

    private void setIdForCqmCertMapping(CQMResultCertification updatedCqmCert, List<CQMResultCertification> currCqmCerts) {
        if (CollectionUtils.isEmpty(currCqmCerts)) {
            return;
        }

        CQMResultCertification matchedCurrCqmCert = currCqmCerts.stream()
            .filter(currCqmCert -> currCqmCert.getCriterion().getId().equals(updatedCqmCert.getCriterion().getId()))
            .findAny()
            .orElse(null);
        if (matchedCurrCqmCert != null) {
            updatedCqmCert.setId(matchedCurrCqmCert.getId());
        }
    }
}
