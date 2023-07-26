package gov.healthit.chpl.upload.listing.normalizer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.optionalStandard.dao.OptionalStandardDAO;
import gov.healthit.chpl.optionalStandard.domain.CertificationResultOptionalStandard;
import gov.healthit.chpl.optionalStandard.domain.OptionalStandard;
import gov.healthit.chpl.optionalStandard.domain.OptionalStandardCriteriaMap;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class OptionalStandardNormalizer {
    private OptionalStandardDAO optionalStandardDao;
    private List<OptionalStandardCriteriaMap> optionalStandardCriteriaMap = new ArrayList<OptionalStandardCriteriaMap>();


    @Autowired
    public OptionalStandardNormalizer(OptionalStandardDAO optionalStandardDao) {
        this.optionalStandardDao = optionalStandardDao;

        try {
            this.optionalStandardCriteriaMap = optionalStandardDao.getAllOptionalStandardCriteriaMap();
        } catch (EntityRetrievalException ex) {
            LOGGER.error("Could not initialize optional standard criteria map for flexible upload.", ex);
        }
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
            .filter(certResult -> (certResult.isSuccess() == null || BooleanUtils.isFalse(certResult.isSuccess())
                    && certResult.getOptionalStandards() != null && certResult.getOptionalStandards().size() > 0))
            .forEach(unattestedCertResult -> unattestedCertResult.getOptionalStandards().clear());

        listing.getCertificationResults().stream()
            .filter(certResult -> (certResult.isSuccess() == null || BooleanUtils.isFalse(certResult.isSuccess()))
                    && certResult.getTestStandards() != null && certResult.getTestStandards().size() > 0)
            .forEach(unattestedCertResult -> unattestedCertResult.getTestStandards().clear());
    }

    private void fillInOptionalStandardsData(CertificationResult certResult) {
        populateAllowedOptionalStandards(certResult);
        populateOptionalStandardsFields(certResult.getOptionalStandards());
    }

    private void populateAllowedOptionalStandards(CertificationResult certResult) {
        if (certResult != null && certResult.getCriterion() != null
                && certResult.getCriterion().getId() != null) {
            List<OptionalStandard> allowedOptionalStandards = optionalStandardCriteriaMap.stream()
                .filter(osm -> osm.getCriterion().getId().equals(certResult.getCriterion().getId()))
                .map(osm -> osm.getOptionalStandard())
                .collect(Collectors.toList());
            certResult.setAllowedOptionalStandards(allowedOptionalStandards);
        }
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
