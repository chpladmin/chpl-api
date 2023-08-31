package gov.healthit.chpl.upload.listing.normalizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.conformanceMethod.dao.ConformanceMethodDAO;
import gov.healthit.chpl.conformanceMethod.domain.CertificationResultConformanceMethod;
import gov.healthit.chpl.conformanceMethod.domain.ConformanceMethod;
import gov.healthit.chpl.conformanceMethod.domain.ConformanceMethodCriteriaMap;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class ConformanceMethodNormalizer {
    private List<ConformanceMethodCriteriaMap> conformanceMethodCriteriaMap = new ArrayList<ConformanceMethodCriteriaMap>();

    @Autowired
    public ConformanceMethodNormalizer(ConformanceMethodDAO conformanceMethodDao) {
        try {
            this.conformanceMethodCriteriaMap = conformanceMethodDao.getAllConformanceMethodCriteriaMap();
        } catch (EntityRetrievalException ex) {
            LOGGER.error("Could not initialize conformance method criteria map for flexible upload.", ex);
        }
    }

    public void normalize(CertifiedProductSearchDetails listing) {
        if (!CollectionUtils.isEmpty(listing.getCertificationResults())) {
            clearDataForUnattestedCriteria(listing);
            listing.getCertificationResults().stream()
                .forEach(certResult -> fillInConformanceMethodData(listing, certResult));
        }
    }

    private void clearDataForUnattestedCriteria(CertifiedProductSearchDetails listing) {
        listing.getCertificationResults().stream()
            .filter(certResult -> (certResult.isSuccess() == null || BooleanUtils.isFalse(certResult.isSuccess()))
                    && certResult.getConformanceMethods() != null && certResult.getConformanceMethods().size() > 0)
            .forEach(unattestedCertResult -> unattestedCertResult.getConformanceMethods().clear());

        listing.getCertificationResults().stream()
            .filter(certResult -> (certResult.isSuccess() == null || BooleanUtils.isFalse(certResult.isSuccess()))
                    && certResult.getTestProcedures() != null && certResult.getTestProcedures().size() > 0)
            .forEach(unattestedCertResult -> unattestedCertResult.getTestProcedures().clear());
    }

    private void fillInConformanceMethodData(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        populateAllowedConformanceMethods(certResult);
        populateConformanceMethodIds(certResult.getCriterion(), certResult.getConformanceMethods());
        populateConformanceMethodRemovalDates(certResult.getCriterion(), certResult.getConformanceMethods());
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
                .filter(conformanceMethod -> isConformanceMethodIdMissing(conformanceMethod))
                .forEach(conformanceMethod -> populateConformanceMethodId(criterion, conformanceMethod));
        }
    }

    private boolean isConformanceMethodIdMissing(CertificationResultConformanceMethod conformanceMethod) {
        return conformanceMethod.getConformanceMethod() != null
                && !StringUtils.isEmpty(conformanceMethod.getConformanceMethod().getName())
                && conformanceMethod.getConformanceMethod().getId() == null;
    }

    private void populateConformanceMethodId(CertificationCriterion criterion, CertificationResultConformanceMethod conformanceMethod) {
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

    private void populateConformanceMethodRemovalDates(CertificationCriterion criterion, List<CertificationResultConformanceMethod> conformanceMethods) {
        if (!CollectionUtils.isEmpty(conformanceMethods)) {
            conformanceMethods.stream()
                .filter(conformanceMethod -> isConformanceMethodIdMissing(conformanceMethod))
                .forEach(conformanceMethod -> populateConformanceMethodId(criterion, conformanceMethod));
            conformanceMethods.stream()
                .filter(conformanceMethod -> isConformanceMethodRemovalDateMissing(conformanceMethod))
                .forEach(conformanceMethod -> populateConformanceMethodRemovalDate(criterion, conformanceMethod));
        }
    }

    private boolean isConformanceMethodRemovalDateMissing(CertificationResultConformanceMethod conformanceMethod) {
        return conformanceMethod.getConformanceMethod() != null
                && !StringUtils.isEmpty(conformanceMethod.getConformanceMethod().getName())
                && conformanceMethod.getConformanceMethod().getRemovalDate() == null;
    }

    private void populateConformanceMethodRemovalDate(CertificationCriterion criterion, CertificationResultConformanceMethod conformanceMethod) {
        List<ConformanceMethod> allowedConformanceMethodsForCriterion = this.conformanceMethodCriteriaMap.stream()
            .filter(cmcMap -> cmcMap.getCriterion().getId().equals(criterion.getId()))
            .map(cmcMap -> cmcMap.getConformanceMethod())
            .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(allowedConformanceMethodsForCriterion)) {
            Optional<ConformanceMethod> cmWithName = allowedConformanceMethodsForCriterion.stream()
                .filter(cm -> cm.getName().equals(conformanceMethod.getConformanceMethod().getName()))
                .findFirst();
            if (cmWithName.isPresent()) {
                conformanceMethod.getConformanceMethod().setRemovalDate(cmWithName.get().getRemovalDate());
            }
        }
    }
}
