package gov.healthit.chpl.upload.listing.normalizer;

import java.util.Comparator;
import java.util.Iterator;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.util.CertificationResultRules;
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
    private CertificationResultRules certResultRules;
    private CertificationCriterionService criterionService;

    @Autowired
    public CertificationResultNormalizer(CertificationCriterionNormalizer criterionNormalizer,
        AdditionalSoftwareNormalizer additionalSoftwareNormalizer,
        TestDataNormalizer testDataNormalizer,
        FunctionalityTestedNormalizer functionalityTestedNormalizer,
        ConformanceMethodNormalizer conformanceMethodNormalizer,
        OptionalStandardNormalizer optionalStandardNormalizer,
        TestToolNormalizer testToolNormalizer,
        SvapNormalizer svapNormalizer,
        CertificationResultRules certResultRules,
        CertificationCriterionService criterionService) {
        this.criterionNormalizer = criterionNormalizer;
        this.additionalSoftwareNormalizer = additionalSoftwareNormalizer;
        this.testDataNormalizer = testDataNormalizer;
        this.functionalityTestedNormalizer = functionalityTestedNormalizer;
        this.conformanceMethodNormalizer = conformanceMethodNormalizer;
        this.optionalStandardNormalizer = optionalStandardNormalizer;
        this.testToolNormalizer = testToolNormalizer;
        this.svapNormalizer = svapNormalizer;
        this.certResultRules = certResultRules;
        this.criterionService = criterionService;
    }

    public void normalize(CertifiedProductSearchDetails listing) {
        this.criterionNormalizer.normalize(listing);
        this.additionalSoftwareNormalizer.normalize(listing);
        this.testDataNormalizer.normalize(listing);
        this.functionalityTestedNormalizer.normalize(listing);
        this.conformanceMethodNormalizer.normalize(listing);
        this.optionalStandardNormalizer.normalize(listing);
        this.testToolNormalizer.normalize(listing);
        this.svapNormalizer.normalize(listing);

        setSedTrueIfApplicableToCriteria(listing);
        removeCertificationResultsWithNullCriterion(listing);
        listing.getCertificationResults().sort(new CertificationResultComparator());
    }

    private void setSedTrueIfApplicableToCriteria(CertifiedProductSearchDetails listing) {
        listing.getCertificationResults().stream()
            .filter(certResult -> certResult.getCriterion() != null
                    && BooleanUtils.isTrue(certResult.isSuccess())
                    && certResultRules.hasCertOption(certResult.getCriterion().getId(), CertificationResultRules.SED))
            .forEach(certResult -> certResult.setSed(true));
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
