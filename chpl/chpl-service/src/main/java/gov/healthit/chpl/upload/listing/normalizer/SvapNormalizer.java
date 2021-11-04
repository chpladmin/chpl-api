package gov.healthit.chpl.upload.listing.normalizer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.svap.dao.SvapDAO;
import gov.healthit.chpl.svap.domain.CertificationResultSvap;
import gov.healthit.chpl.svap.domain.Svap;
import gov.healthit.chpl.svap.domain.SvapCriteriaMap;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class SvapNormalizer {
    private SvapDAO svapDao;
    private List<SvapCriteriaMap> svapCriteriaMap = new ArrayList<SvapCriteriaMap>();

    @Autowired
    public SvapNormalizer(SvapDAO svapDao) {
        this.svapDao = svapDao;
        try {
            svapCriteriaMap = svapDao.getAllSvapCriteriaMap();
        } catch (EntityRetrievalException ex) {
            LOGGER.error("Could not initialize svap criteria map for flexible upload.", ex);
        }
    }

    public void normalize(CertifiedProductSearchDetails listing) {
        if (!CollectionUtils.isEmpty(listing.getCertificationResults())) {
            listing.getCertificationResults().stream()
                .forEach(certResult -> fillInSvapData(certResult));
        }
    }

    private void fillInSvapData(CertificationResult certResult) {
        populateAllowedSvaps(certResult);
        populateSvapIds(certResult.getSvaps());
    }

    private void populateAllowedSvaps(CertificationResult certResult) {
        if (certResult != null && certResult.getCriterion() != null
                && certResult.getCriterion().getId() != null) {
            List<Svap> allowedSvaps = svapCriteriaMap.stream()
                    .filter(scm -> scm.getCriterion().getId().equals(certResult.getCriterion().getId()))
                    .map(scm -> scm.getSvap())
                    .collect(Collectors.toList());
            certResult.setAllowedSvaps(allowedSvaps);
        }
    }

    private void populateSvapIds(List<CertificationResultSvap> svaps) {
        if (!CollectionUtils.isEmpty(svaps)) {
            svaps.stream()
                .forEach(svap -> populateSvapId(svap));
        }
    }

    private void populateSvapId(CertificationResultSvap svap) {
        //TODO in OCD-3780
    }
}
