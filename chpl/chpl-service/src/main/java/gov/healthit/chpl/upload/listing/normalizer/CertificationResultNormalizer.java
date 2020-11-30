package gov.healthit.chpl.upload.listing.normalizer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;

@Component
public class CertificationResultNormalizer {
    private CertificationCriterionNormalizer criterionNormalizer;
    private AdditionalSoftwareNormalizer additionalSoftwareNormalizer;
    private TestDataNormalizer testDataNormalizer;
    private TestFunctionalityNormalizer testFunctionalityNormalizer;
    private TestProcedureNormalizer testProcedureNormalizer;
    private TestStandardNormalizer testStandardNormalizer;
    private TestToolNormalizer testToolNormalizer;

    @Autowired
    public CertificationResultNormalizer(CertificationCriterionNormalizer criterionNormalizer,
        AdditionalSoftwareNormalizer additionalSoftwareNormalizer,
        TestDataNormalizer testDataNormalizer,
        TestFunctionalityNormalizer testFunctionalityNormalizer,
        TestProcedureNormalizer testProcedureNormalizer,
        TestStandardNormalizer testStandardNormalizer,
        TestToolNormalizer testToolNormalizer) {
        this.criterionNormalizer = criterionNormalizer;
        this.additionalSoftwareNormalizer = additionalSoftwareNormalizer;
        this.testDataNormalizer = testDataNormalizer;
        this.testFunctionalityNormalizer = testFunctionalityNormalizer;
        this.testProcedureNormalizer = testProcedureNormalizer;
        this.testStandardNormalizer = testStandardNormalizer;
        this.testToolNormalizer = testToolNormalizer;
    }

    public void normalize(CertifiedProductSearchDetails listing) {
        this.criterionNormalizer.normalize(listing);
        this.additionalSoftwareNormalizer.normalize(listing);
        this.testDataNormalizer.normalize(listing);
        this.testFunctionalityNormalizer.normalize(listing);
        this.testProcedureNormalizer.normalize(listing);
        this.testStandardNormalizer.normalize(listing);
        this.testToolNormalizer.normalize(listing);
    }
}
