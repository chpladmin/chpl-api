package gov.healthit.chpl.upload.listing.normalizer;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.svap.dao.SvapDAO;
import gov.healthit.chpl.svap.domain.CertificationResultSvap;
import gov.healthit.chpl.svap.domain.Svap;
import gov.healthit.chpl.svap.domain.SvapCriteriaMap;
import gov.healthit.chpl.util.Util;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class SvapNormalizer {
    private SvapDAO svapDao;

    @Autowired
    public SvapNormalizer(SvapDAO svapDao) {
        this.svapDao = svapDao;
    }

    public void normalize(CertifiedProductSearchDetails listing) {
        try {
            List<SvapCriteriaMap> svapCriteriaMap = svapDao.getAllSvapCriteriaMap();
            if (!CollectionUtils.isEmpty(listing.getCertificationResults())) {
                listing.getCertificationResults().stream()
                    .forEach(certResult -> fillInSvapData(certResult, svapCriteriaMap));
            }
        } catch (EntityRetrievalException ex) {
            LOGGER.error("Could not initialize svap criteria map for flexible upload.", ex);
        }
    }

    private void fillInSvapData(CertificationResult certResult, List<SvapCriteriaMap> svapCriteriaMap) {
        populateAllowedSvaps(certResult, svapCriteriaMap);
        populateSvapsFields(certResult.getCriterion(), certResult.getSvaps(), svapCriteriaMap);
    }

    private void populateAllowedSvaps(CertificationResult certResult, List<SvapCriteriaMap> svapCriteriaMap) {
        if (certResult != null && certResult.getCriterion() != null
                && certResult.getCriterion().getId() != null) {
            List<Svap> allowedSvaps = svapCriteriaMap.stream()
                    .filter(scm -> scm.getCriterion().getId().equals(certResult.getCriterion().getId()))
                    .map(scm -> scm.getSvap())
                    .collect(Collectors.toList());
            certResult.setAllowedSvaps(allowedSvaps);
        }
    }

    private void populateSvapsFields(CertificationCriterion criterion, List<CertificationResultSvap> svaps, List<SvapCriteriaMap> svapCriteriaMap) {
        if (!CollectionUtils.isEmpty(svaps)) {
            svaps.stream()
                .forEach(svap -> populateSvapFields(criterion, svap, svapCriteriaMap));
        }
    }

    private void populateSvapFields(CertificationCriterion criterion, CertificationResultSvap certResultSvap, List<SvapCriteriaMap> svapCriteriaMap) {
        Optional<SvapCriteriaMap> matchedSvap = svapCriteriaMap.stream()
            .filter(scm -> scm.getCriterion().getId().equals(criterion.getId()))
            .filter(scm -> scm.getSvap().getRegulatoryTextCitation().equalsIgnoreCase(certResultSvap.getRegulatoryTextCitation()))
            .findFirst();
        if (matchedSvap.isPresent()) {
            certResultSvap.setApprovedStandardVersion(matchedSvap.get().getSvap().getApprovedStandardVersion());
            certResultSvap.setReplaced(matchedSvap.get().getSvap().isReplaced());
            certResultSvap.setSvapId(matchedSvap.get().getSvap().getSvapId());
        } else {
            LOGGER.warn("Could not find SVAP for criteria " + Util.formatCriteriaNumber(criterion) + " and Regulatory Text " + certResultSvap.getRegulatoryTextCitation());
        }
    }
}
