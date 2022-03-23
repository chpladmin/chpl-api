package gov.healthit.chpl.upload.listing.normalizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.conformanceMethod.dao.ConformanceMethodDAO;
import gov.healthit.chpl.conformanceMethod.domain.ConformanceMethod;
import gov.healthit.chpl.conformanceMethod.domain.ConformanceMethodCriteriaMap;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultConformanceMethod;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class ConformanceMethodNormalizer {
    private FF4j ff4j;
    private List<ConformanceMethodCriteriaMap> conformanceMethodCriteriaMap = new ArrayList<ConformanceMethodCriteriaMap>();

    @Autowired
    public ConformanceMethodNormalizer(FF4j ff4j, ConformanceMethodDAO conformanceMethodDao) {
        this.ff4j = ff4j;
        try {
            this.conformanceMethodCriteriaMap = conformanceMethodDao.getAllConformanceMethodCriteriaMap();
        } catch (EntityRetrievalException ex) {
            LOGGER.error("Could not initialize conformance method criteria map for flexible upload.", ex);
        }
    }

    public void normalize(CertifiedProductSearchDetails listing) {
        if (ff4j.check(FeatureList.CONFORMANCE_METHOD) && !CollectionUtils.isEmpty(listing.getCertificationResults())) {
            listing.getCertificationResults().stream()
                .forEach(certResult -> fillInConformanceMethodData(certResult));
        }
    }

    private void fillInConformanceMethodData(CertificationResult certResult) {
        populateAllowedConformanceMethods(certResult);
        populateConformanceMethodIds(certResult.getCriterion(), certResult.getConformanceMethods());
    }

    private void populateAllowedConformanceMethods(CertificationResult certResult) {
        if (certResult != null && certResult.getCriterion() != null
                && certResult.getCriterion().getId() != null) {
            List<ConformanceMethod> allowedConformanceMethds = conformanceMethodCriteriaMap.stream()
                    .filter(cmcm -> cmcm.getCriterion().getId().equals(certResult.getCriterion().getId()))
                    .map(cmcm -> cmcm.getConformanceMethod())
                    .collect(Collectors.toList());
            certResult.setAllowedConformanceMethods(allowedConformanceMethds);
        }
    }

    private void populateConformanceMethodIds(CertificationCriterion criterion, List<CertificationResultConformanceMethod> conformanceMethods) {
        if (!CollectionUtils.isEmpty(conformanceMethods)) {
            conformanceMethods.stream()
                .forEach(conformanceMethod -> populateConformanceMethodId(criterion, conformanceMethod));
        }
    }

    private void populateConformanceMethodId(CertificationCriterion criterion, CertificationResultConformanceMethod conformanceMethod) {
        if (conformanceMethod != null && conformanceMethod.getConformanceMethod() != null
                && !StringUtils.isEmpty(conformanceMethod.getConformanceMethod().getName())) {
            List<ConformanceMethod> allowedConformanceMethodsForCriterion = this.conformanceMethodCriteriaMap.stream()
                .filter(cmcMap -> cmcMap.getCriterion().getId().equals(criterion.getId()))
                .map(cmcMap -> cmcMap.getConformanceMethod())
                .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(allowedConformanceMethodsForCriterion)) {
                Optional<ConformanceMethod> cmWithName = allowedConformanceMethodsForCriterion.stream()
                    .filter(cm -> cm.getName().equals(conformanceMethod.getConformanceMethod().getName()))
                    .findFirst();
                if (cmWithName.isPresent()) {
                    conformanceMethod.getConformanceMethod().setId(cmWithName.get().getId());
                }
            }
        }
    }
}
