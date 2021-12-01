package gov.healthit.chpl.upload.listing.normalizer;

import java.util.ArrayList;
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
    private List<SvapCriteriaMap> svapCriteriaMap = new ArrayList<SvapCriteriaMap>();

    @Autowired
    public SvapNormalizer(SvapDAO svapDao) {
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
        populateSvapsFields(certResult.getCriterion(), certResult.getSvaps());
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

    private void populateSvapsFields(CertificationCriterion criterion, List<CertificationResultSvap> svaps) {
        if (!CollectionUtils.isEmpty(svaps)) {
            svaps.stream()
                .forEach(svap -> populateSvapFields(criterion, svap));
        }
    }

    private void populateSvapFields(CertificationCriterion criterion, CertificationResultSvap certResultSvap) {
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
