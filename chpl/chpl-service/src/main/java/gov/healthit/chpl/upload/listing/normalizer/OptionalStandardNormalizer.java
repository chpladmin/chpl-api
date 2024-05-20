package gov.healthit.chpl.upload.listing.normalizer;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.fuzzyMatching.FuzzyChoicesManager;
import gov.healthit.chpl.fuzzyMatching.FuzzyType;
import gov.healthit.chpl.optionalStandard.OptionalStandardDAO;
import gov.healthit.chpl.optionalStandard.domain.CertificationResultOptionalStandard;
import gov.healthit.chpl.optionalStandard.domain.OptionalStandard;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class OptionalStandardNormalizer {
    private OptionalStandardDAO optionalStandardDao;
    private FuzzyChoicesManager fuzzyChoicesManager;

    @Autowired
    public OptionalStandardNormalizer(OptionalStandardDAO optionalStandardDao,
            FuzzyChoicesManager fuzzyChoicesManager) {
        this.optionalStandardDao = optionalStandardDao;
        this.fuzzyChoicesManager= fuzzyChoicesManager;
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
        populateOptionalStandardsFields(certResult, certResult.getOptionalStandards());
    }

    private void populateOptionalStandardsFields(CertificationResult certResult, List<CertificationResultOptionalStandard> optionalStandards) {
        if (!CollectionUtils.isEmpty(optionalStandards)) {
            optionalStandards.stream()
                .forEach(optionalStandard -> populateOptionalStandardFields(optionalStandard));
            optionalStandards.stream()
                .filter(optionalStandard -> optionalStandard.getOptionalStandard() == null
                    || optionalStandard.getOptionalStandard().getId() == null)
                .forEach(unknownOptionalStandard -> lookForFuzzyMatch(certResult, unknownOptionalStandard));
        }
    }

    private void populateOptionalStandardFields(CertificationResultOptionalStandard cros) {
        String displayValueToSearch = "";
        if (!StringUtils.isEmpty(cros.getUserEnteredValue())) {
            displayValueToSearch = cros.getUserEnteredValue();
        } else if (cros.getOptionalStandard() != null
                && !StringUtils.isEmpty(cros.getOptionalStandard().getDisplayValue())) {
            displayValueToSearch = cros.getOptionalStandard().getDisplayValue();
        } else if (cros.getOptionalStandard() != null
                && !StringUtils.isEmpty(cros.getOptionalStandard().getCitation())) {
            displayValueToSearch = cros.getOptionalStandard().getCitation();
        }

        if (!StringUtils.isEmpty(displayValueToSearch)) {
            OptionalStandard optionalStandard = optionalStandardDao.getByDisplayValue(displayValueToSearch);
            if (optionalStandard == null || optionalStandard.getId() == null) {
                optionalStandard = optionalStandardDao.getByCitation(displayValueToSearch);
            }

            if (optionalStandard != null) {
                cros.setOptionalStandard(optionalStandard);
            }
        }
    }

    private void lookForFuzzyMatch(CertificationResult certResult, CertificationResultOptionalStandard unknownOptionalStandard) {
        if (unknownOptionalStandard == null || StringUtils.isEmpty(unknownOptionalStandard.getUserEnteredValue())) {
            return;
        }

        String topFuzzyChoice = fuzzyChoicesManager.getTopFuzzyChoice(unknownOptionalStandard.getUserEnteredValue(),
                FuzzyType.OPTIONAL_STANDARD,
                certResult.getCriterion());
        if (!StringUtils.isEmpty(topFuzzyChoice)) {
            OptionalStandard optionalStandard = optionalStandardDao.getByDisplayValue(topFuzzyChoice);
            if (optionalStandard != null) {
                unknownOptionalStandard.setOptionalStandard(optionalStandard);
            }
        }
    }
}
