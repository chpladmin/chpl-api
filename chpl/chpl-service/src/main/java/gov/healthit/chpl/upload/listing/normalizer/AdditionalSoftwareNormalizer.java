package gov.healthit.chpl.upload.listing.normalizer;

import java.util.List;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResultAdditionalSoftware;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.CertifiedProductUtil;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class AdditionalSoftwareNormalizer {
    private CertifiedProductUtil certifiedProductUtil;

    @Autowired
    public AdditionalSoftwareNormalizer(CertifiedProductUtil certifiedProductUtil) {
        this.certifiedProductUtil = certifiedProductUtil;
    }

    public void normalize(CertifiedProductSearchDetails listing) {
        if (listing.getCertificationResults() != null && listing.getCertificationResults().size() > 0) {
            clearDataForUnattestedCriteria(listing);
            listing.getCertificationResults().stream()
                .forEach(certResult -> populateAdditionalSoftwareIds(certResult.getAdditionalSoftware()));
        }
    }

    private void clearDataForUnattestedCriteria(CertifiedProductSearchDetails listing) {
        listing.getCertificationResults().stream()
            .filter(certResult -> (certResult.getSuccess() == null || BooleanUtils.isFalse(certResult.getSuccess()))
                    && certResult.getAdditionalSoftware() != null && certResult.getAdditionalSoftware().size() > 0)
            .forEach(unattestedCertResult -> unattestedCertResult.getAdditionalSoftware().clear());
    }

    private void populateAdditionalSoftwareIds(List<CertificationResultAdditionalSoftware> additionalSoftwares) {
        if (additionalSoftwares != null && additionalSoftwares.size() > 0) {
            additionalSoftwares.stream()
                .filter(additionalSoftware -> hasListingAsAdditionalSoftware(additionalSoftware))
                .forEach(additionalSoftware -> populateAdditionalSoftwareId(additionalSoftware));
        }
    }

    private boolean hasListingAsAdditionalSoftware(CertificationResultAdditionalSoftware additionalSoftware) {
        return !StringUtils.isEmpty(additionalSoftware.getCertifiedProductNumber())
                && additionalSoftware.getCertifiedProductId() == null;
    }

    private void populateAdditionalSoftwareId(CertificationResultAdditionalSoftware additionalSoftware) {
        String chplProductNumber = additionalSoftware.getCertifiedProductNumber();
        CertifiedProduct cp = certifiedProductUtil.getListing(chplProductNumber);
        if (cp != null) {
            additionalSoftware.setCertifiedProductId(cp.getId());
        } else {
            LOGGER.error("Could not find listing with chpl product number " + chplProductNumber);
        }
    }
}
