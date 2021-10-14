package gov.healthit.chpl.upload.listing.normalizer;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
    private Map<Long, List<OptionalStandardCriteriaMap>> optionalStandardCriteriaMap = null;

    @Autowired
    public OptionalStandardNormalizer(OptionalStandardDAO optionalStandardDao) {
        try {
            optionalStandardCriteriaMap = optionalStandardDao.getAllOptionalStandardCriteriaMap().stream()
                    .collect(Collectors.groupingBy(scm -> scm.getCriterion().getId()));
        } catch (EntityRetrievalException ex) {
            LOGGER.error("Could not normalize Optional Standards", ex);
            return;
        }
    }

    public void normalize(CertifiedProductSearchDetails listing) {
        if (listing.getCertificationResults() != null && listing.getCertificationResults().size() > 0) {
            listing.getCertificationResults().stream()
                .forEach(certResult -> populateOptionalStandardsFields(certResult.getOptionalStandards(), optionalStandardCriteriaMap.get(certResult.getCriterion().getId())));
        }
    }

    private void populateOptionalStandardsFields(List<CertificationResultOptionalStandard> optionalStandards,
            List<OptionalStandardCriteriaMap> allowedOptionalStandards) {
        if (optionalStandards != null && optionalStandards.size() > 0) {
            optionalStandards.stream()
                .forEach(optionalStandard -> populateOptionalStandardFields(optionalStandard, allowedOptionalStandards));
        }
    }

    private void populateOptionalStandardFields(CertificationResultOptionalStandard cros,
            List<OptionalStandardCriteriaMap> allowedOptionalStandards) {
        if (cros.getOptionalStandardId() != null) {
            Optional<OptionalStandard> optionalStandard = getOptionalStandard(cros.getOptionalStandardId(), allowedOptionalStandards);
            if (optionalStandard.isPresent()) {
                cros.setCitation(optionalStandard.get().getCitation());
                cros.setDescription(optionalStandard.get().getDescription());
            }
        } else if (!StringUtils.isEmpty(cros.getCitation())) {
            Optional<OptionalStandard> optionalStandard = getOptionalStandard(cros.getCitation(), allowedOptionalStandards);
            if (optionalStandard.isPresent()) {
                cros.setOptionalStandardId(optionalStandard.get().getId());
                cros.setDescription(optionalStandard.get().getDescription());
            }
        }
    }

    private Optional<OptionalStandard> getOptionalStandard(Long optionalStandardId, List<OptionalStandardCriteriaMap> allowedOptionalStandards) {
        if (CollectionUtils.isEmpty(allowedOptionalStandards)) {
            return  Optional.empty();
        }

        return allowedOptionalStandards.stream()
                .map(oscm -> oscm.getOptionalStandard())
                .filter(os -> os.getId().equals(optionalStandardId))
                .findAny();
    }

    private Optional<OptionalStandard> getOptionalStandard(String citation, List<OptionalStandardCriteriaMap> allowedOptionalStandards) {
        if (CollectionUtils.isEmpty(allowedOptionalStandards)) {
            return Optional.empty();
        }

        return allowedOptionalStandards.stream()
                .map(oscm -> oscm.getOptionalStandard())
                .filter(os -> os.getCitation().equals(citation))
                .findAny();
    }
}
