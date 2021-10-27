package gov.healthit.chpl.upload.listing.normalizer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.conformanceMethod.dao.ConformanceMethodDAO;
import gov.healthit.chpl.conformanceMethod.domain.ConformanceMethod;
import gov.healthit.chpl.conformanceMethod.domain.ConformanceMethodCriteriaMap;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultConformanceMethod;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class ConformanceMethodNormalizer {
    private FF4j ff4j;
    private ConformanceMethodDAO conformanceMethodDao;
    private List<ConformanceMethodCriteriaMap> conformanceMethodCriteriaMap = new ArrayList<ConformanceMethodCriteriaMap>();

    @Autowired
    public ConformanceMethodNormalizer(FF4j ff4j, ConformanceMethodDAO conformanceMethodDao) {
        this.ff4j = ff4j;
        this.conformanceMethodDao = conformanceMethodDao;
        try {
            this.conformanceMethodCriteriaMap = conformanceMethodDao.getAllConformanceMethodCriteriaMap();
        } catch (EntityRetrievalException ex) {
            LOGGER.error("Could not initialize conformance method criteria map for flexible upload.", ex);
        }
    }

    public void normalize(CertifiedProductSearchDetails listing) {
        if (!CollectionUtils.isEmpty(listing.getCertificationResults())) {
            listing.getCertificationResults().stream()
                .forEach(certResult -> fillInConformanceMethodData(certResult));
        }
    }

    private void fillInConformanceMethodData(CertificationResult certResult) {
        populateAllowedConformanceMethods(certResult);
        populateConformanceMethodIds(certResult.getConformanceMethods());
    }

    private void populateAllowedConformanceMethods(CertificationResult certResult) {
        if (certResult != null && certResult.getCriterion() != null
                && certResult.getCriterion().getId() != null) {
            List<ConformanceMethod> allowedConformanceMethds = conformanceMethodCriteriaMap.stream()
                    .filter(cmcm -> ff4j.check(FeatureList.CONFORMANCE_METHOD)
                            && cmcm.getCriterion().getId().equals(certResult.getCriterion().getId()))
                    .map(cmcm -> cmcm.getConformanceMethod())
                    .collect(Collectors.toList());
            certResult.setAllowedConformanceMethods(allowedConformanceMethds);
        }
    }

    private void populateConformanceMethodIds(List<CertificationResultConformanceMethod> conformanceMethods) {
        if (!CollectionUtils.isEmpty(conformanceMethods)) {
            conformanceMethods.stream()
                .forEach(conformanceMethod -> populateConformanceMethodId(conformanceMethod));
        }
    }

    private void populateConformanceMethodId(CertificationResultConformanceMethod conformanceMethod) {
        //TODO in OCD-3751
    }
}
