package gov.healthit.chpl.upload.listing.normalizer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.conformanceMethod.domain.CertificationResultConformanceMethod;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultAdditionalSoftware;
import gov.healthit.chpl.domain.CertificationResultTestData;
import gov.healthit.chpl.domain.CertificationResultTestProcedure;
import gov.healthit.chpl.domain.CertificationResultTestStandard;
import gov.healthit.chpl.domain.CertificationResultTestTool;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.functionalityTested.CertificationResultFunctionalityTested;
import gov.healthit.chpl.optionalStandard.domain.CertificationResultOptionalStandard;
import gov.healthit.chpl.svap.domain.CertificationResultSvap;
import gov.healthit.chpl.util.CertificationResultRules;

@Component
public class CertificationCriterionNormalizer {

    private CertificationCriterionDAO criterionDao;
    private CertificationResultRules certResultRules;

    @Autowired
    public CertificationCriterionNormalizer(CertificationCriterionDAO criterionDao,
            CertificationResultRules certResultRules) {
        this.criterionDao = criterionDao;
        this.certResultRules = certResultRules;
    }

    public void normalize(CertifiedProductSearchDetails listing) {
        addEditionCriteriaNotPresentInListing(listing);
        nullifyNotApplicableFieldsInCertificationResults(listing);
    }

    private void addEditionCriteriaNotPresentInListing(CertifiedProductSearchDetails listing) {
        List<CertificationCriterion> all2015Criteria = criterionDao.findByCertificationEditionYear(
                CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear());
        if (listing != null && listing.getCertificationResults() != null) {
            List<CertificationCriterion> criteriaToAdd = all2015Criteria.stream()
                .filter(criterion -> !existsInListing(listing.getCertificationResults(), criterion))
                .collect(Collectors.toList());
            criteriaToAdd.stream()
                .forEach(criterionToAdd -> {
                    listing.getCertificationResults().add(buildCertificationResult(criterionToAdd));
                });
        }
    }

    private boolean existsInListing(List<CertificationResult> listingCertResults, CertificationCriterion criterion) {
        if (listingCertResults == null || listingCertResults.size() == 0) {
            return false;
        }
        return listingCertResults.stream()
                    .filter(certResult -> certResult.getCriterion() != null && certResult.getCriterion().getId() != null
                        && certResult.getCriterion().getId().equals(criterion.getId()))
                    .findAny().isPresent();
    }

    private CertificationResult buildCertificationResult(CertificationCriterion criterion) {
        return CertificationResult.builder()
            .criterion(criterion)
            .success(Boolean.FALSE)
        .build();
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
