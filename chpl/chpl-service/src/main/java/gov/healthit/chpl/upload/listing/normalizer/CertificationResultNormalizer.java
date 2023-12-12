package gov.healthit.chpl.upload.listing.normalizer;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import lombok.NoArgsConstructor;

@Component
public class CertificationResultNormalizer {
    private CertificationCriterionNormalizer criterionNormalizer;
    private AdditionalSoftwareNormalizer additionalSoftwareNormalizer;
    private TestDataNormalizer testDataNormalizer;
    private FunctionalityTestedNormalizer functionalityTestedNormalizer;
    private ConformanceMethodNormalizer conformanceMethodNormalizer;
    private OptionalStandardNormalizer optionalStandardNormalizer;
    private TestToolNormalizer testToolNormalizer;
    private SvapNormalizer svapNormalizer;
    private StandardNormalizer standardNormalizer;
    private CertificationResultRules certResultRules;
    private CertificationCriterionService criterionService;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public CertificationResultNormalizer(CertificationCriterionNormalizer criterionNormalizer,
        AdditionalSoftwareNormalizer additionalSoftwareNormalizer,
        TestDataNormalizer testDataNormalizer,
        FunctionalityTestedNormalizer functionalityTestedNormalizer,
        ConformanceMethodNormalizer conformanceMethodNormalizer,
        OptionalStandardNormalizer optionalStandardNormalizer,
        TestToolNormalizer testToolNormalizer,
        SvapNormalizer svapNormalizer,
        StandardNormalizer standardNormalizer,
        CertificationResultRules certResultRules,
        CertificationCriterionService criterionService,
        ErrorMessageUtil msgUtil) {
        this.criterionNormalizer = criterionNormalizer;
        this.additionalSoftwareNormalizer = additionalSoftwareNormalizer;
        this.testDataNormalizer = testDataNormalizer;
        this.functionalityTestedNormalizer = functionalityTestedNormalizer;
        this.conformanceMethodNormalizer = conformanceMethodNormalizer;
        this.optionalStandardNormalizer = optionalStandardNormalizer;
        this.testToolNormalizer = testToolNormalizer;
        this.svapNormalizer = svapNormalizer;
        this.standardNormalizer = standardNormalizer;
        this.certResultRules = certResultRules;
        this.criterionService = criterionService;
        this.msgUtil = msgUtil;
    }

    public void normalize(CertifiedProductSearchDetails listing, List<CertificationResultLevelNormalizer> additionalNormalizers) {
        removeCertificationResultsWithNullCriterion(listing);
        removeCertificationResultsForDuplicateCriteria(listing);

        this.criterionNormalizer.normalize(listing);
        this.additionalSoftwareNormalizer.normalize(listing);
        this.testDataNormalizer.normalize(listing);
        this.functionalityTestedNormalizer.normalize(listing);
        this.conformanceMethodNormalizer.normalize(listing);
        this.optionalStandardNormalizer.normalize(listing);
        this.testToolNormalizer.normalize(listing);
        this.svapNormalizer.normalize(listing);
        this.standardNormalizer.normalize(listing);

        if (additionalNormalizers != null && additionalNormalizers.size() > 0) {
            additionalNormalizers.forEach(normalizer -> normalizer.normalize(listing));
        }

        setSedTrueIfApplicableToCriteria(listing);
        listing.getCertificationResults().sort(new CertificationResultComparator());
    }


    public void normalize(CertifiedProductSearchDetails listing) {
        normalize(listing, null);
    }

    private void removeCertificationResultsWithNullCriterion(CertifiedProductSearchDetails listing) {
        //this can happen if an upload file has a made-up criterion column like CRITERIA_170_315_B_20__C
        Iterator<CertificationResult> certResultIter = listing.getCertificationResults().iterator();
        while (certResultIter.hasNext()) {
            CertificationResult certResult = certResultIter.next();
            if (certResult.getCriterion() == null || certResult.getCriterion().getId() == null) {
                certResultIter.remove();
            }
        }
    }

    private void removeCertificationResultsForDuplicateCriteria(CertifiedProductSearchDetails listing) {
        //more than 1 column header can map to the same criteria in the case of cures + original
        //so we need this code to ensure we only have one copy of the criteria in the final listing
        Iterator<CertificationResult> certResultIter = listing.getCertificationResults().iterator();
        while (certResultIter.hasNext()) {
            CertificationResult certResult = certResultIter.next();
            if (BooleanUtils.isFalse(certResult.isSuccess())
                    && isCriteriaInAnotherCertResult(listing.getCertificationResults(), certResult)) {
                certResultIter.remove();
            }
        }

        //now if there are still multiple cert results for the same criteria that are attested to (any unattested duplicates
        //were removed above) we will just pick the last one and give a warning
        certResultIter = listing.getCertificationResults().iterator();
        while (certResultIter.hasNext()) {
            CertificationResult certResult = certResultIter.next();
            if (isCriteriaAttestedInAnotherCertResult(listing.getCertificationResults(), certResult)) {
                certResultIter.remove();
                listing.addWarningMessage(msgUtil.getMessage("listing.upload.duplicateAttestedCriteria",
                        Util.formatCriteriaNumber(certResult.getCriterion())));
            }
        }
    }

    private boolean isCriteriaInAnotherCertResult(List<CertificationResult> certResults, CertificationResult unattestedCertResult) {
        return certResults.stream()
            .filter(certResult -> certResult != unattestedCertResult)
            .filter(certResult -> certResult.getCriterion().getId().equals(unattestedCertResult.getCriterion().getId()))
            .findAny().isPresent();
    }

    private boolean isCriteriaAttestedInAnotherCertResult(List<CertificationResult> certResults, CertificationResult unattestedCertResult) {
        return certResults.stream()
            .filter(certResult -> certResult != unattestedCertResult)
            .filter(certResult -> BooleanUtils.isTrue(certResult.isSuccess())
                    && certResult.getCriterion().getId().equals(unattestedCertResult.getCriterion().getId()))
            .findAny().isPresent();
    }

    private void setSedTrueIfApplicableToCriteria(CertifiedProductSearchDetails listing) {
        listing.getCertificationResults().stream()
            .filter(certResult -> certResult.getCriterion() != null
                    && BooleanUtils.isTrue(certResult.isSuccess())
                    && certResultRules.hasCertOption(certResult.getCriterion().getId(), CertificationResultRules.SED))
            .forEach(certResult -> certResult.setSed(true));
    }

    @NoArgsConstructor
    private class CertificationResultComparator implements Comparator<CertificationResult> {
        private boolean descending = false;

        @Override
        public int compare(CertificationResult certResult1, CertificationResult certResult2) {
            if (ObjectUtils.anyNull(certResult1.getCriterion(), certResult2.getCriterion())
                    || StringUtils.isAnyEmpty(certResult1.getCriterion().getNumber(), certResult2.getCriterion().getNumber())) {
                return 0;
            }
            int sortFactor = descending ? -1 : 1;
            return (criterionService.sortCriteria(certResult1.getCriterion(), certResult2.getCriterion())) * sortFactor;
        }
    }
}
