package gov.healthit.chpl.validation.listing.reviewer;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.conformanceMethod.dao.ConformanceMethodDAO;
import gov.healthit.chpl.conformanceMethod.domain.ConformanceMethod;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultConformanceMethod;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;

@Component("conformanceMethodReviewer")
public class ConformanceMethodReviewer extends PermissionBasedReviewer {
    private ConformanceMethodDAO conformanceMethodDAO;
    private FF4j ff4j;
    private static final String CM_MUST_NOT_HAVE_OTHER_DATA = "Attestation";

    @Autowired
    public ConformanceMethodReviewer(ConformanceMethodDAO conformanceMethodDAO, ErrorMessageUtil msgUtil, ResourcePermissions resourcePermissions, FF4j ff4j) {
        super(msgUtil, resourcePermissions);
        this.conformanceMethodDAO = conformanceMethodDAO;
        this.msgUtil = msgUtil;
        this.resourcePermissions = resourcePermissions;
        this.ff4j = ff4j;
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        if (listing.getCertificationResults().stream()
                .filter(cr -> cr.isSuccess() && cr.getConformanceMethods() != null && cr.getConformanceMethods().size() > 0)
                .count() > 0
                && !ff4j.check(FeatureList.CONFORMANCE_METHOD)) {
            listing.getErrorMessages().add("Conformance Methods are not implemented yet");
        } else {
            listing.getCertificationResults().stream()
                    .filter(cr -> cr.isSuccess() && cr.getConformanceMethods() != null && cr.getConformanceMethods().size() > 0)
                    .forEach(cert -> cert.getConformanceMethods().stream()
                            .forEach(conformanceMethod -> reviewConformanceMethod(listing, cert, conformanceMethod)));
        }
    }

    private void reviewConformanceMethod(CertifiedProductSearchDetails listing, CertificationResult certResult,
            CertificationResultConformanceMethod conformanceMethod) {

        checkIfConformanceMethodIsAllowed(listing, certResult, conformanceMethod);
        checkIfConformanceMethodHasAVersion(listing, certResult, conformanceMethod);
//        checkIfConformanceMethodHasExtraData(listing, certResult, conformanceMethod);
    }

    private void checkIfConformanceMethodIsAllowed(CertifiedProductSearchDetails listing, CertificationResult certResult,
            CertificationResultConformanceMethod conformanceMethod) {
        Optional<ConformanceMethod> allowedConformanceMethod
        = conformanceMethodDAO.getByCriterionId(certResult.getCriterion().getId()).stream()
            .filter(cm -> cm.getId().equals(conformanceMethod.getConformanceMethod().getId()))
            .findAny();
        if (!allowedConformanceMethod.isPresent()) {
            addCriterionErrorOrWarningByPermission(listing, certResult,
                    "listing.criteria.conformanceMethod.invalidCriteria",
                    conformanceMethod.getConformanceMethod().getName(),
                    Util.formatCriteriaNumber(certResult.getCriterion()));
        }
    }

    private void checkIfConformanceMethodHasAVersion(CertifiedProductSearchDetails listing, CertificationResult certResult,
            CertificationResultConformanceMethod conformanceMethod) {
            if (conformanceMethod.getConformanceMethod() != null
                    && !StringUtils.isEmpty(conformanceMethod.getConformanceMethod().getName())
                    && StringUtils.isEmpty(conformanceMethod.getConformanceMethodVersion())) {
                addCriterionErrorOrWarningByPermission(listing, certResult,
                        "listing.criteria.conformanceMethod.missingConformanceMethodVersion",
                        Util.formatCriteriaNumber(certResult.getCriterion()));
            }
    }

    /*
    private void checkIfConformanceMethodHasExtraData(CertifiedProductSearchDetails listing, CertificationResult certResult,
            CertificationResultConformanceMethod conformanceMethod) {
            if (conformanceMethod.getConformanceMethod() != null
                    && conformanceMethod.getConformanceMethod().getName().equalsIgnoreCase(CM_MUST_NOT_HAVE_OTHER_DATA)) {
                if (certResult.getTestDataUsed() != null) {
                    listing.getWarningMessages().add(msgUtil.getMessage(
                            "listing.criteria.conformanceMethod.mayNotHaveTestData",
                            Util.formatCriteriaNumber(certResult.getCriterion())));
                }
                if (certResult.getTestToolsUsed() != null) {
                    listing.getWarningMessages().add(msgUtil.getMessage(
                            "listing.criteria.conformanceMethod.mayNotHaveTestTools",
                            Util.formatCriteriaNumber(certResult.getCriterion())));
                }
            }
    }
    */
}
