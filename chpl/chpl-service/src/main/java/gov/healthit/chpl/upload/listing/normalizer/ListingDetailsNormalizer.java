package gov.healthit.chpl.upload.listing.normalizer;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;

@Component
public class ListingDetailsNormalizer {
    private CertificationBodyNormalizer acbNormalizer;
    private TestingLabNormalizer atlNormalizer;
    private RwtNormalizer rwtNormalizer;
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
    public ListingDetailsNormalizer(CertificationBodyNormalizer acbNormalizer,
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
        this.acbNormalizer = acbNormalizer;
        this.atlNormalizer = atlNormalizer;
        this.rwtNormalizer = new RwtNormalizer();
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

    public void normalize(CertifiedProductSearchDetails listing, List<CertificationResultLevelNormalizer> additionalNormalizers) {
        if (!listing.getErrorMessages().isEmpty()) {
            listing.clearAllErrorMessages();
        }
        if (CollectionUtils.isNotEmpty(listing.getWarningMessages().castToCollection())) {
            listing.clearAllWarningMessages();
        }

        setEmptyStringFieldsToNull(listing);
        this.acbNormalizer.normalize(listing);
        this.atlNormalizer.normalize(listing);
        this.rwtNormalizer.normalize(listing);
        this.developerNormalizer.normalize(listing);
        this.productVersionNormalizer.normalize(listing);
        this.icsNormalizer.normalize(listing);
        this.accessibilityStandardNormalizer.normalize(listing);
        this.qmsNormalizer.normalize(listing);
        this.targetedUserNormalizer.normalize(listing);
        if (additionalNormalizers != null && additionalNormalizers.size() > 0) {
            this.certResultNormalizer.normalize(listing, additionalNormalizers);
        } else {
            this.certResultNormalizer.normalize(listing);
        }
        this.cqmNormalizer.normalize(listing);
        this.measureNormalizer.normalize(listing);
        this.sedNormalizer.normalize(listing);

    }

    public void normalize(CertifiedProductSearchDetails listing) {
        normalize(listing, null);
    }

    private void setEmptyStringFieldsToNull(CertifiedProductSearchDetails listing) {
        if (StringUtils.isEmpty(listing.getSvapNoticeUrl())) {
            listing.setSvapNoticeUrl(null);
        }
        if (StringUtils.isEmpty(listing.getAcbCertificationId())) {
            listing.setAcbCertificationId(null);
        }
        if (StringUtils.isEmpty(listing.getMandatoryDisclosures())) {
            listing.setMandatoryDisclosures(null);
        }
        if (StringUtils.isEmpty(listing.getReportFileLocation())) {
            listing.setReportFileLocation(null);
        }
        if (StringUtils.isEmpty(listing.getRwtPlansUrl())) {
            listing.setRwtPlansUrl(null);
        }
        if (StringUtils.isEmpty(listing.getRwtResultsUrl())) {
            listing.setRwtResultsUrl(null);
        }
        if (StringUtils.isEmpty(listing.getSedReportFileLocation())) {
            listing.setSedReportFileLocation(null);
        }
        if (StringUtils.isEmpty(listing.getSedIntendedUserDescription())) {
            listing.setSedIntendedUserDescription(null);
        }
    }
}
