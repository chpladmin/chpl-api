package gov.healthit.chpl.upload.listing.normalizer;

import java.util.ArrayList;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.conformanceMethod.domain.CertificationResultConformanceMethod;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultAdditionalSoftware;
import gov.healthit.chpl.domain.CertificationResultTestData;
import gov.healthit.chpl.domain.CertificationResultTestProcedure;
import gov.healthit.chpl.domain.CertificationResultTestStandard;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.functionalitytested.CertificationResultFunctionalityTested;
import gov.healthit.chpl.optionalStandard.domain.CertificationResultOptionalStandard;
import gov.healthit.chpl.svap.domain.CertificationResultSvap;
import gov.healthit.chpl.testtool.CertificationResultTestTool;
import gov.healthit.chpl.util.CertificationResultRules;

@Component
public class CertificationCriterionNormalizer {

    private CertificationResultRules certResultRules;

    @Autowired
    public CertificationCriterionNormalizer(CertificationResultRules certResultRules) {
        this.certResultRules = certResultRules;
    }

    public void normalize(CertifiedProductSearchDetails listing) {
        removeUnattestedToCriteria(listing);
        nullifyNotApplicableFieldsInCertificationResults(listing);
    }

    private void removeUnattestedToCriteria(CertifiedProductSearchDetails listing) {
        //There is a business case for not removing unattested to criteria for 2011 & 2014 listings.  Unattested to
        //criteria can still g1_success and g2_success values.
        if (!isListing2011Or2014Edition(listing)) {
            listing.getCertificationResults().removeIf(cr -> cr.isSuccess() == null || !cr.isSuccess());
        }
    }

    private Boolean isListing2011Or2014Edition(CertifiedProductSearchDetails listing) {
        return listing.getEdition() != null
                && listing.getEdition().getName() != null
                && (listing.getEdition().getName().equals(CertificationEditionConcept.CERTIFICATION_EDITION_2011.getYear())
                        || listing.getEdition().getName().equals(CertificationEditionConcept.CERTIFICATION_EDITION_2014.getYear()));
    }

    private void nullifyNotApplicableFieldsInCertificationResults(CertifiedProductSearchDetails listing) {
        listing.getCertificationResults().stream()
            .forEach(certResult -> nullifyNotApplicableFields(certResult));
    }

    private void nullifyNotApplicableFields(CertificationResult certResult) {
        if (BooleanUtils.isFalse(certResult.isSuccess())
                && !isFieldAllowed(certResult.getCriterion(), CertificationResultRules.ADDITIONAL_SOFTWARE)) {
            certResult.setAdditionalSoftware(null);
        } else if (isFieldAllowed(certResult.getCriterion(), CertificationResultRules.ADDITIONAL_SOFTWARE)
                && certResult.getAdditionalSoftware() == null) {
            certResult.setAdditionalSoftware(new ArrayList<CertificationResultAdditionalSoftware>());
        }

        if (BooleanUtils.isFalse(certResult.isSuccess())
                && !isFieldAllowed(certResult.getCriterion(), CertificationResultRules.API_DOCUMENTATION)) {
            certResult.setApiDocumentation(null);
        } else if (isFieldAllowed(certResult.getCriterion(), CertificationResultRules.API_DOCUMENTATION)
                && certResult.getApiDocumentation() == null) {
            certResult.setApiDocumentation("");
        }

        if (BooleanUtils.isFalse(certResult.isSuccess())
                && !isFieldAllowed(certResult.getCriterion(), CertificationResultRules.ATTESTATION_ANSWER)) {
            certResult.setAttestationAnswer(null);
        } else if (isFieldAllowed(certResult.getCriterion(), CertificationResultRules.ATTESTATION_ANSWER)
                && certResult.getAttestationAnswer() == null) {
            certResult.setAttestationAnswer(false);
        }

        if (BooleanUtils.isFalse(certResult.isSuccess())
                && !isFieldAllowed(certResult.getCriterion(), CertificationResultRules.DOCUMENTATION_URL)) {
            certResult.setDocumentationUrl(null);
        } else if (isFieldAllowed(certResult.getCriterion(), CertificationResultRules.DOCUMENTATION_URL)
                && certResult.getDocumentationUrl() == null) {
            certResult.setDocumentationUrl("");
        }

        if (BooleanUtils.isFalse(certResult.isSuccess())
                && !isFieldAllowed(certResult.getCriterion(), CertificationResultRules.EXPORT_DOCUMENTATION)) {
            certResult.setExportDocumentation(null);
        } else if (isFieldAllowed(certResult.getCriterion(), CertificationResultRules.EXPORT_DOCUMENTATION)
                && certResult.getExportDocumentation() == null) {
            certResult.setExportDocumentation("");
        }

        if (BooleanUtils.isFalse(certResult.isSuccess())
                && !isFieldAllowed(certResult.getCriterion(), CertificationResultRules.FUNCTIONALITY_TESTED)) {
            certResult.setFunctionalitiesTested(null);
        } else if (isFieldAllowed(certResult.getCriterion(), CertificationResultRules.FUNCTIONALITY_TESTED)
                && certResult.getFunctionalitiesTested() == null) {
            certResult.setFunctionalitiesTested(new ArrayList<CertificationResultFunctionalityTested>());
        }

        if (BooleanUtils.isFalse(certResult.isSuccess())
                && !isFieldAllowed(certResult.getCriterion(), CertificationResultRules.G1_SUCCESS)) {
            certResult.setG1Success(null);
        }

        if (BooleanUtils.isFalse(certResult.isSuccess())
                && !isFieldAllowed(certResult.getCriterion(), CertificationResultRules.G2_SUCCESS)) {
            certResult.setG2Success(null);
        }

        if (BooleanUtils.isFalse(certResult.isSuccess())
                && !isFieldAllowed(certResult.getCriterion(), CertificationResultRules.GAP)) {
            certResult.setGap(null);
        }

        if (BooleanUtils.isFalse(certResult.isSuccess())
                && !isFieldAllowed(certResult.getCriterion(), CertificationResultRules.OPTIONAL_STANDARD)) {
            certResult.setOptionalStandards(null);
        } else if (isFieldAllowed(certResult.getCriterion(), CertificationResultRules.OPTIONAL_STANDARD)
                && certResult.getOptionalStandards() == null) {
            certResult.setOptionalStandards(new ArrayList<CertificationResultOptionalStandard>());
        }

        if (BooleanUtils.isFalse(certResult.isSuccess())
                && !isFieldAllowed(certResult.getCriterion(), CertificationResultRules.PRIVACY_SECURITY)) {
            certResult.setPrivacySecurityFramework(null);
        } else if (isFieldAllowed(certResult.getCriterion(), CertificationResultRules.PRIVACY_SECURITY)
                && certResult.getPrivacySecurityFramework() == null) {
            certResult.setPrivacySecurityFramework("");
        }

        if (BooleanUtils.isFalse(certResult.isSuccess())
                && !isFieldAllowed(certResult.getCriterion(), CertificationResultRules.SED)) {
            certResult.setSed(null);
        }

        if (BooleanUtils.isFalse(certResult.isSuccess())
                && !isFieldAllowed(certResult.getCriterion(), CertificationResultRules.SERVICE_BASE_URL_LIST)) {
            certResult.setServiceBaseUrlList(null);
        } else if (isFieldAllowed(certResult.getCriterion(), CertificationResultRules.SERVICE_BASE_URL_LIST)
                && certResult.getServiceBaseUrlList() == null) {
            certResult.setServiceBaseUrlList("");
        }

        if (BooleanUtils.isFalse(certResult.isSuccess())
                && !isFieldAllowed(certResult.getCriterion(), CertificationResultRules.STANDARDS_TESTED)) {
            certResult.setTestStandards(null);
        } else if (isFieldAllowed(certResult.getCriterion(), CertificationResultRules.STANDARDS_TESTED)
                && certResult.getTestStandards() == null) {
            certResult.setTestStandards(new ArrayList<CertificationResultTestStandard>());
        }

        if (BooleanUtils.isFalse(certResult.isSuccess())
                && !isFieldAllowed(certResult.getCriterion(), CertificationResultRules.SVAP)) {
            certResult.setSvaps(null);
        } else if (isFieldAllowed(certResult.getCriterion(), CertificationResultRules.SVAP)
                && certResult.getSvaps() == null) {
            certResult.setSvaps(new ArrayList<CertificationResultSvap>());
        }

        if (BooleanUtils.isFalse(certResult.isSuccess())
                && !isFieldAllowed(certResult.getCriterion(), CertificationResultRules.TEST_DATA)) {
            certResult.setTestDataUsed(null);
        } else if (isFieldAllowed(certResult.getCriterion(), CertificationResultRules.TEST_DATA)
                && certResult.getTestDataUsed() == null) {
            certResult.setTestDataUsed(new ArrayList<CertificationResultTestData>());
        }

        if (BooleanUtils.isFalse(certResult.isSuccess())
                && !isFieldAllowed(certResult.getCriterion(), CertificationResultRules.TEST_PROCEDURE)) {
            certResult.setTestProcedures(null);
        } else if (isFieldAllowed(certResult.getCriterion(), CertificationResultRules.TEST_PROCEDURE)
                && certResult.getTestProcedures() == null) {
            certResult.setTestProcedures(new ArrayList<CertificationResultTestProcedure>());
        }

        if (BooleanUtils.isFalse(certResult.isSuccess())
                && !isFieldAllowed(certResult.getCriterion(), CertificationResultRules.CONFORMANCE_METHOD)) {
            certResult.setConformanceMethods(null);
        } else if (isFieldAllowed(certResult.getCriterion(), CertificationResultRules.CONFORMANCE_METHOD)
                && certResult.getConformanceMethods() == null) {
            certResult.setConformanceMethods(new ArrayList<CertificationResultConformanceMethod>());
        }

        if (BooleanUtils.isFalse(certResult.isSuccess())
                && !isFieldAllowed(certResult.getCriterion(), CertificationResultRules.TEST_TOOLS_USED)) {
            certResult.setTestToolsUsed(null);
        } else if (isFieldAllowed(certResult.getCriterion(), CertificationResultRules.TEST_TOOLS_USED)
                && certResult.getTestToolsUsed() == null) {
            certResult.setTestToolsUsed(new ArrayList<CertificationResultTestTool>());
        }

        if (BooleanUtils.isFalse(certResult.isSuccess())
                && !isFieldAllowed(certResult.getCriterion(), CertificationResultRules.USE_CASES)) {
            certResult.setUseCases(null);
        } else if (isFieldAllowed(certResult.getCriterion(), CertificationResultRules.USE_CASES)
                && certResult.getUseCases() == null) {
            certResult.setUseCases("");
        }
    }

    private boolean isFieldAllowed(CertificationCriterion criterion, String field) {
        return certResultRules.hasCertOption(criterion.getId(), field);
    }
}
