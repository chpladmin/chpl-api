package gov.healthit.chpl.upload.listing.normalizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
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
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class ConformanceMethodNormalizer {
    private ErrorMessageUtil msgUtil;
    private FF4j ff4j;
    private List<ConformanceMethodCriteriaMap> conformanceMethodCriteriaMap = new ArrayList<ConformanceMethodCriteriaMap>();

    @Autowired
    public ConformanceMethodNormalizer(ConformanceMethodDAO conformanceMethodDao, ErrorMessageUtil msgUtil, FF4j ff4j) {
        this.msgUtil = msgUtil;
        this.ff4j = ff4j;

        try {
            this.conformanceMethodCriteriaMap = conformanceMethodDao.getAllConformanceMethodCriteriaMap();
        } catch (EntityRetrievalException ex) {
            LOGGER.error("Could not initialize conformance method criteria map for flexible upload.", ex);
        }
    }

    public void normalize(CertifiedProductSearchDetails listing) {
        if (!ff4j.check(FeatureList.CONFORMANCE_METHOD)) {
            return;
        }

        if (!CollectionUtils.isEmpty(listing.getCertificationResults())) {
            listing.getCertificationResults().stream()
                .forEach(certResult -> fillInConformanceMethodData(listing, certResult));
        }
    }

    private void fillInConformanceMethodData(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        populateAllowedConformanceMethods(certResult);
        if (BooleanUtils.isTrue(certResult.isSuccess())) {
            fillInDefaultConformanceMethods(listing, certResult);
        }
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

    private void fillInDefaultConformanceMethods(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (certResult.getConformanceMethods() == null) {
            certResult.setConformanceMethods(new ArrayList<CertificationResultConformanceMethod>());
        }

        //The current upload template doesn't have a column for conformance method name for some criteria
        //even though it is required for all criteria. So for any of the criteria that don't have a column
        //for name, you might get into this "if" block - there could not be any conformance methods parsed
        //inside of the Handler code in the absence of a column in the file.
        if (CollectionUtils.isEmpty(certResult.getConformanceMethods())
                && getDefaultConformanceMethodForCriteria(certResult.getCriterion()) != null) {
            certResult.getConformanceMethods().add(CertificationResultConformanceMethod.builder().build());
        }

        certResult.getConformanceMethods().stream()
            .filter(conformanceMethod -> isConformanceMethodNameMissing(conformanceMethod))
            .forEach(conformanceMethod -> fillInDefaultConformanceMethod(listing, certResult, conformanceMethod));
    }

    private boolean isConformanceMethodNameMissing(CertificationResultConformanceMethod conformanceMethod) {
        return conformanceMethod.getConformanceMethod() == null
                || StringUtils.isEmpty(conformanceMethod.getConformanceMethod().getName());
    }

    private void fillInDefaultConformanceMethod(CertifiedProductSearchDetails listing, CertificationResult certResult,
            CertificationResultConformanceMethod conformanceMethod) {
        ConformanceMethod defaultConformanceMethod = getDefaultConformanceMethodForCriteria(certResult.getCriterion());
        if (defaultConformanceMethod != null) {
            conformanceMethod.setConformanceMethod(defaultConformanceMethod);
            //I don't want to add warnings outside of reviewers, but this is a special case due to the upload template not having
            //a field for conformance method and/or having a field for CM version when none is allowed
            //for some of the criteria.
            //So we will add a default CM for the cert result here if there is only one possible choice for
            //conformance method but we have to tell the user that we did it.
            //This code can't go in the reviewer otherwise during Test Procedure -> Conformance Method conversion
            //a default CM gets added to the listing and may make the converter think that the listing already has a CM.
            listing.getWarningMessages().add(msgUtil.getMessage("listing.criteria.conformanceMethod.addedDefaultForCriterion",
                    Util.formatCriteriaNumber(certResult.getCriterion()),
                    defaultConformanceMethod.getName()));
        }
    }

    private ConformanceMethod getDefaultConformanceMethodForCriteria(CertificationCriterion criterion) {
        List<ConformanceMethod> allowedConformanceMethodsForCriterion = this.conformanceMethodCriteriaMap.stream()
                .filter(cmcMap -> cmcMap.getCriterion().getId().equals(criterion.getId()))
                .map(cmcMap -> cmcMap.getConformanceMethod())
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(allowedConformanceMethodsForCriterion)
                && allowedConformanceMethodsForCriterion.size() == 1) {
            return allowedConformanceMethodsForCriterion.get(0);
        }
        return null;
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
}
