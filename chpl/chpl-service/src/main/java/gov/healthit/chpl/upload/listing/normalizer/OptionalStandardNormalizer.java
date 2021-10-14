package gov.healthit.chpl.upload.listing.normalizer;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.optionalStandard.dao.OptionalStandardDAO;
import gov.healthit.chpl.optionalStandard.domain.CertificationResultOptionalStandard;
import gov.healthit.chpl.optionalStandard.domain.OptionalStandard;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class OptionalStandardNormalizer {
    private OptionalStandardDAO optionalStandardDao;

    @Autowired
    public OptionalStandardNormalizer(OptionalStandardDAO optionalStandardDao) {
        this.optionalStandardDao = optionalStandardDao;
    }

    public void normalize(CertifiedProductSearchDetails listing) {
        if (listing.getCertificationResults() != null && listing.getCertificationResults().size() > 0) {
            listing.getCertificationResults().stream()
                .forEach(certResult -> populateOptionalStandardIds(listing, certResult.getOptionalStandards()));
        }
    }

    private void populateOptionalStandardIds(CertifiedProductSearchDetails listing,
            List<CertificationResultOptionalStandard> optionalStandards) {
        if (optionalStandards != null && optionalStandards.size() > 0) {
            optionalStandards.stream()
                .forEach(optionalStandard -> populateOptionalStandardId(listing, optionalStandard));
        }
    }

    private void populateOptionalStandardId(CertifiedProductSearchDetails listing, CertificationResultOptionalStandard optionalStandard) {
        if (!StringUtils.isEmpty(optionalStandard.getCitation())) {
            OptionalStandard os = optionalStandardDao.getByCitation(optionalStandard.getCitation());
            if (os != null) {
                optionalStandard.setOptionalStandardId(os.getId());
                optionalStandard.setDescription(os.getDescription());
            }
        }
    }
}
