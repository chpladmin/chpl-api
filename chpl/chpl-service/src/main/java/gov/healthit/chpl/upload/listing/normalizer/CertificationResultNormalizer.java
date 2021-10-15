package gov.healthit.chpl.upload.listing.normalizer;

import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;

@Component
public class CertificationResultNormalizer {
    private CertificationCriterionNormalizer criterionNormalizer;
    private AdditionalSoftwareNormalizer additionalSoftwareNormalizer;
    private TestDataNormalizer testDataNormalizer;
    private TestFunctionalityNormalizer testFunctionalityNormalizer;
    private TestProcedureNormalizer testProcedureNormalizer;
    private OptionalStandardNormalizer optionalStandardNormalizer;
    private TestToolNormalizer testToolNormalizer;

    @Autowired
    public CertificationResultNormalizer(CertificationCriterionNormalizer criterionNormalizer,
        AdditionalSoftwareNormalizer additionalSoftwareNormalizer,
        TestDataNormalizer testDataNormalizer,
        TestFunctionalityNormalizer testFunctionalityNormalizer,
        TestProcedureNormalizer testProcedureNormalizer,
        OptionalStandardNormalizer optionalStandardNormalizer,
        TestToolNormalizer testToolNormalizer) {
        this.criterionNormalizer = criterionNormalizer;
        this.additionalSoftwareNormalizer = additionalSoftwareNormalizer;
        this.testDataNormalizer = testDataNormalizer;
        this.testFunctionalityNormalizer = testFunctionalityNormalizer;
        this.testProcedureNormalizer = testProcedureNormalizer;
        this.optionalStandardNormalizer = optionalStandardNormalizer;
        this.testToolNormalizer = testToolNormalizer;
    }

    public void normalize(CertifiedProductSearchDetails listing) {
        this.criterionNormalizer.normalize(listing);
        this.additionalSoftwareNormalizer.normalize(listing);
        this.testDataNormalizer.normalize(listing);
        this.testFunctionalityNormalizer.normalize(listing);
        this.testProcedureNormalizer.normalize(listing);
        this.optionalStandardNormalizer.normalize(listing);
        this.testToolNormalizer.normalize(listing);

        removeCertificationResultsWithNullCriterion(listing);
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
}
