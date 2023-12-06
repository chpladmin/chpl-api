package gov.healthit.chpl.upload.listing.normalizer;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.standard.CertificationResultStandard;
import gov.healthit.chpl.standard.Standard;
import gov.healthit.chpl.standard.StandardDAO;

@Component
public class StandardNormalizer {
    private StandardDAO standardDao;

    @Autowired
    public StandardNormalizer(StandardDAO standardDao) {
        this.standardDao = standardDao;
    }

    public void normalize(CertifiedProductSearchDetails listing) {
        if (listing.getCertificationResults() != null && listing.getCertificationResults().size() > 0) {
            clearDataForUnattestedCriteria(listing);

            listing.getCertificationResults().stream()
                .forEach(certResult -> fillInStandardsData(listing, certResult));
        }
    }

    private void clearDataForUnattestedCriteria(CertifiedProductSearchDetails listing) {
        listing.getCertificationResults().stream()
            .filter(certResult -> (certResult.isSuccess() == null || BooleanUtils.isFalse(certResult.isSuccess()))
                    && certResult.getStandards() != null
                    && certResult.getStandards().size() > 0)
            .forEach(unattestedCertResult -> unattestedCertResult.getStandards().clear());
    }

    private void fillInStandardsData(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        populateStandards(listing, certResult, certResult.getStandards());
    }

    private void populateStandards(CertifiedProductSearchDetails listing, CertificationResult certResult, List<CertificationResultStandard> standards) {
        if (standards != null && standards.size() > 0) {
            standards.stream()
                .filter(standard -> standard.getId() == null)
                .forEach(standard -> populateStandard(listing, certResult, standard));
        }
    }

    private void populateStandard(CertifiedProductSearchDetails listing, CertificationResult certResult, CertificationResultStandard standard) {
        if (!StringUtils.isEmpty(standard.getStandard().getRegulatoryTextCitation())) {
            Standard foundStandard =
                    getStandard(standard.getStandard().getRegulatoryTextCitation(), certResult.getCriterion().getId());
            if (foundStandard != null) {
                standard.setStandard(foundStandard);
            }
        }
    }

    private Standard getStandard(String regulatoryTextCitation, Long criterionId) {
        Map<Long, List<Standard>> standardMappings = standardDao.getStandardCriteriaMaps();
        if (!standardMappings.containsKey(criterionId)) {
            return null;
        }
        List<Standard> standardForCriterion = standardMappings.get(criterionId);
        Optional<Standard> standardOpt = standardForCriterion.stream()
            .filter(standard -> standard.getRegulatoryTextCitation().equalsIgnoreCase(regulatoryTextCitation))
            .findAny();
        return standardOpt.isPresent() ? standardOpt.get() : null;
    }
}
