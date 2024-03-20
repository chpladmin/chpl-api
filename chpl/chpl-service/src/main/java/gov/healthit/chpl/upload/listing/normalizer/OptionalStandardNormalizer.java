package gov.healthit.chpl.upload.listing.normalizer;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.optionalStandard.OptionalStandardDAO;
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
            clearDataForUnattestedCriteria(listing);
            listing.getCertificationResults().stream()
                .forEach(certResult -> fillInOptionalStandardsData(certResult));
        }
    }

    private void clearDataForUnattestedCriteria(CertifiedProductSearchDetails listing) {
        listing.getCertificationResults().stream()
            .filter(certResult -> (certResult.getSuccess() == null || BooleanUtils.isFalse(certResult.getSuccess())
                    && certResult.getOptionalStandards() != null && certResult.getOptionalStandards().size() > 0))
            .forEach(unattestedCertResult -> unattestedCertResult.getOptionalStandards().clear());

        listing.getCertificationResults().stream()
            .filter(certResult -> (certResult.getSuccess() == null || BooleanUtils.isFalse(certResult.getSuccess()))
                    && certResult.getTestStandards() != null && certResult.getTestStandards().size() > 0)
            .forEach(unattestedCertResult -> unattestedCertResult.getTestStandards().clear());
    }

    private void fillInOptionalStandardsData(CertificationResult certResult) {
        populateOptionalStandardsFields(certResult.getOptionalStandards());
    }

    private void populateOptionalStandardsFields(List<CertificationResultOptionalStandard> optionalStandards) {
        if (!CollectionUtils.isEmpty(optionalStandards)) {
            optionalStandards.stream()
                .forEach(optionalStandard -> populateOptionalStandardFields(optionalStandard));
        }
    }

    private void populateOptionalStandardFields(CertificationResultOptionalStandard cros) {
        if (!StringUtils.isEmpty(cros.getCitation())) {
            OptionalStandard optionalStandard = optionalStandardDao.getByCitation(cros.getCitation());
            if (optionalStandard != null) {
                cros.setOptionalStandardId(optionalStandard.getId());
                cros.setDescription(optionalStandard.getDescription());
            }
        }
    }
}
