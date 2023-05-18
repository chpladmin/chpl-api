package gov.healthit.chpl.upload.listing.normalizer;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;

@Component
public class ListingDetailsNormalizer {
    private CertificationEditionNormalizer editionNormalizer;
    private CertificationBodyNormalizer acbNormalizer;
    private TestingLabNormalizer atlNormalizer;
    private DeveloperNormalizer developerNormalizer;
    private ProductAndVersionNormalizer productVersionNormalizer;
    private IcsNormalizer icsNormalizer;
    private AccessibilityStandardNormalizer accessibilityStandardNormalizer;
    private QmsStandardNormalizer qmsNormalizer;
    private TargetedUserNormalizer targetedUserNormalizer;
    private CertificationResultNormalizer certResultNormalizer;
    private CqmNormalizer cqmNormalizer;
    private MeasureNormalizer measureNormalizer;
    private SedNormalizer sedNormalizer;

    @SuppressWarnings("checkstyle:parameternumber")
    @Autowired
    public ListingDetailsNormalizer(CertificationEditionNormalizer editionNormalizer,
            CertificationBodyNormalizer acbNormalizer,
            TestingLabNormalizer atlNormalizer,
            DeveloperNormalizer developerNormalizer,
            ProductAndVersionNormalizer productVersionNormalizer,
            IcsNormalizer icsNormalizer,
            AccessibilityStandardNormalizer accessibilityStandardNormalizer,
            QmsStandardNormalizer qmsNormalizer,
            TargetedUserNormalizer targetedUserNormalizer,
            CertificationResultNormalizer certResultNormalizer,
            CqmNormalizer cqmNormalizer,
            MeasureNormalizer measureNormalizer,
            SedNormalizer sedNormalizer) {
        this.editionNormalizer = editionNormalizer;
        this.acbNormalizer = acbNormalizer;
        this.atlNormalizer = atlNormalizer;
        this.developerNormalizer = developerNormalizer;
        this.productVersionNormalizer = productVersionNormalizer;
        this.icsNormalizer = icsNormalizer;
        this.accessibilityStandardNormalizer = accessibilityStandardNormalizer;
        this.qmsNormalizer = qmsNormalizer;
        this.targetedUserNormalizer = targetedUserNormalizer;
        this.certResultNormalizer = certResultNormalizer;
        this.cqmNormalizer = cqmNormalizer;
        this.measureNormalizer = measureNormalizer;
        this.sedNormalizer = sedNormalizer;
    }

    public void normalize(CertifiedProductSearchDetails listing) {
        if (!listing.getErrorMessages().isEmpty()) {
            listing.clearAllErrorMessages();
        }
        if (CollectionUtils.isNotEmpty(listing.getWarningMessages().castToCollection())) {
            listing.clearAllWarningMessages();;
        }

        this.editionNormalizer.normalize(listing);
        this.acbNormalizer.normalize(listing);
        this.atlNormalizer.normalize(listing);
        this.developerNormalizer.normalize(listing);
        this.productVersionNormalizer.normalize(listing);
        this.icsNormalizer.normalize(listing);
        this.accessibilityStandardNormalizer.normalize(listing);
        this.qmsNormalizer.normalize(listing);
        this.targetedUserNormalizer.normalize(listing);
        this.certResultNormalizer.normalize(listing);
        this.cqmNormalizer.normalize(listing);
        this.measureNormalizer.normalize(listing);
        this.sedNormalizer.normalize(listing);
    }

}
