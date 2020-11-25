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
    private TestStandardNormalizer testStandardNormalizer;
    private TestToolNormalizer testToolNormalizer;
    private UcdProcessNormalizer ucdProcessNormalizer;

    @Autowired
    public CertificationResultNormalizer(CertificationCriterionNormalizer criterionNormalizer,
        AdditionalSoftwareNormalizer additionalSoftwareNormalizer,
        TestDataNormalizer testDataNormalizer,
        TestFunctionalityNormalizer testFunctionalityNormalizer,
        TestStandardNormalizer testStandardNormalizer,
        TestToolNormalizer testToolNormalizer,
        UcdProcessNormalizer ucdProcessNormalizer) {
        this.criterionNormalizer = criterionNormalizer;
        this.additionalSoftwareNormalizer = additionalSoftwareNormalizer;
        this.testDataNormalizer = testDataNormalizer;
        this.testFunctionalityNormalizer = testFunctionalityNormalizer;
        this.testStandardNormalizer = testStandardNormalizer;
        this.testToolNormalizer = testToolNormalizer;
        this.ucdProcessNormalizer = ucdProcessNormalizer;
    }

    public void normalize(CertifiedProductSearchDetails listing) {
        this.criterionNormalizer.normalize(listing);
        this.additionalSoftwareNormalizer.normalize(listing);
        this.testDataNormalizer.normalize(listing);
        this.testFunctionalityNormalizer.normalize(listing);
        this.testStandardNormalizer.normalize(listing);
        this.testToolNormalizer.normalize(listing);
        this.ucdProcessNormalizer.normalize(listing);
    }
}
