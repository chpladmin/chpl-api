package gov.healthit.chpl.certifiedproduct.service;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.domain.CQMResultCertification;
import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductAccessibilityStandard;
import gov.healthit.chpl.domain.CertifiedProductQmsStandard;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductTargetedUser;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ChplProductNumberUtil.ChplProductNumberParts;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class ListingMergeService {

    private CertifiedProductDetailsManager cpdManager;
    private ChplProductNumberUtil chplProductNumberUtil;

    @Autowired
    public ListingMergeService(CertifiedProductDetailsManager cpdManager,
            ChplProductNumberUtil chplProductNumberUtil) {
        this.cpdManager = cpdManager;
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
        updatedListing.setEdition(currentListing.getEdition());
        updatedListing.setSurveillance(currentListing.getSurveillance());
        updatedListing.setTestingLabs(currentListing.getTestingLabs());

        setIdsForQmsStandards(updatedListing, currentListing.getQmsStandards());
        setIdsForAccessibilityStandards(updatedListing, currentListing.getAccessibilityStandards());
        setIdsForTargetedUsers(updatedListing, currentListing.getTargetedUsers());
        updatedListing.getCertificationResults().stream()
            .forEach(certResultFromFile -> setIdsForCertificationResults(certResultFromFile, currentListing));
        updatedListing.getCqmResults().stream()
            .forEach(cqmResultFromFile -> setIdsForCqmResults(cqmResultFromFile, currentListing));
        //TODO: set IDs for SED, ICS
    }

    private String applyUpdatesToChplProductNumber(CertifiedProductSearchDetails updatedListing, CertifiedProductSearchDetails currentListing) {
        ChplProductNumberParts currChplProductNumberParts
            = chplProductNumberUtil.parseChplProductNumber(currentListing.getChplProductNumber());
        currChplProductNumberParts.setIcsCode(chplProductNumberUtil.deriveIcsCodeFromListing(updatedListing));
        currChplProductNumberParts.setAdditionalSoftwareCode(chplProductNumberUtil.deriveAdditionalSoftwareCodeFromListing(updatedListing));
        return chplProductNumberUtil.getChplProductNumber(currChplProductNumberParts);
    }

    private void setIdsForQmsStandards(CertifiedProductSearchDetails updatedListing, List<CertifiedProductQmsStandard> currQmsStandards) {
        currQmsStandards.stream()
            .forEach(currQms -> setIdInUpdatedQmsStandards(updatedListing.getQmsStandards(), currQms));
    }

    private void setIdInUpdatedQmsStandards(List<CertifiedProductQmsStandard> updatedQmsStandards, CertifiedProductQmsStandard currQmsStandard) {
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
        currAccessibilityStandards.stream()
            .forEach(currAccStd -> setIdInUpdatedAccessibilityStandards(updatedListing.getAccessibilityStandards(), currAccStd));
    }

    private void setIdInUpdatedAccessibilityStandards(List<CertifiedProductAccessibilityStandard> updatedAccessibilityStandards,
            CertifiedProductAccessibilityStandard currAccessibilityStandard) {
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
        currTargetedUsers.stream()
            .forEach(currTargetedUser -> setIdInUpdatedTargetedUsers(updatedListing.getTargetedUsers(), currTargetedUser));
    }

    private void setIdInUpdatedTargetedUsers(List<CertifiedProductTargetedUser> updatedTargetedUsers,
            CertifiedProductTargetedUser currTargetedUser) {
        CertifiedProductTargetedUser updatedTargetedUser = updatedTargetedUsers.stream()
            .filter(updatedTu -> updatedTu.getTargetedUserId().equals(currTargetedUser.getTargetedUserId()))
            .findAny()
            .orElse(null);
        if (updatedTargetedUser != null) {
            updatedTargetedUser.setId(currTargetedUser.getId());
        }
    }

    private void setIdsForCertificationResults(CertificationResult updatedCertResult, CertifiedProductSearchDetails currListing) {
        CertificationResult matchedCurrCertResult = currListing.getCertificationResults().stream()
            .filter(certResult -> certResult.getCriterion().getId().equals(updatedCertResult.getCriterion().getId()))
            .findAny().orElse(null);
        if (matchedCurrCertResult != null) {
            updatedCertResult.setId(matchedCurrCertResult.getId());
            //TODO: set ids for all sub-data
        }
    }

    private void setIdsForCqmResults(CQMResultDetails updatedCqmResult, CertifiedProductSearchDetails currListing) {
        CQMResultDetails matchedCurrCqmResult = currListing.getCqmResults().stream()
            .filter(cqmResult -> cqmResult.getCmsId().equals(updatedCqmResult.getCmsId()))
            .findAny().orElse(null);
        if (matchedCurrCqmResult != null) {
            updatedCqmResult.setId(matchedCurrCqmResult.getId());
            if (!CollectionUtils.isEmpty(updatedCqmResult.getCriteria())) {
                updatedCqmResult.getCriteria().stream()
                    .forEach(updatedCqmCert -> setIdForCqmCertMapping(updatedCqmCert, matchedCurrCqmResult.getCriteria()));
            }
        }
    }

    private void setIdForCqmCertMapping(CQMResultCertification updatedCqmCert, List<CQMResultCertification> currCqmCerts) {
        CQMResultCertification matchedCurrCqmCert = currCqmCerts.stream()
            .filter(currCqmCert -> currCqmCert.getCriterion().getId().equals(updatedCqmCert.getCriterion().getId()))
            .findAny()
            .orElse(null);
        if (matchedCurrCqmCert != null) {
            updatedCqmCert.setId(matchedCurrCqmCert.getId());
        }
    }
}
