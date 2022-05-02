package gov.healthit.chpl.upload.listing.normalizer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultAdditionalSoftware;
import gov.healthit.chpl.domain.CertificationResultConformanceMethod;
import gov.healthit.chpl.domain.CertificationResultTestData;
import gov.healthit.chpl.domain.CertificationResultTestFunctionality;
import gov.healthit.chpl.domain.CertificationResultTestProcedure;
import gov.healthit.chpl.domain.CertificationResultTestStandard;
import gov.healthit.chpl.domain.CertificationResultTestTool;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
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
        nullifyNotApplicableFieldsInUnattestedCriteria(listing);
    }

    private void addEditionCriteriaNotPresentInListing(CertifiedProductSearchDetails listing) {
        List<CertificationCriterionDTO> all2015Criteria = criterionDao.findByCertificationEditionYear(
                CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear());
        if (listing != null && listing.getCertificationResults() != null) {
            List<CertificationCriterionDTO> criteriaToAdd = all2015Criteria.stream()
                .filter(criterionDto -> !existsInListing(listing.getCertificationResults(), criterionDto))
                .collect(Collectors.toList());
            criteriaToAdd.stream()
                .map(criterionDto -> new CertificationCriterion(criterionDto))
                .forEach(criterionToAdd -> {
                    listing.getCertificationResults().add(buildCertificationResult(criterionToAdd));
                });
        }
    }

    private boolean existsInListing(List<CertificationResult> listingCertResults, CertificationCriterionDTO criterionDto) {
        if (listingCertResults == null || listingCertResults.size() == 0) {
            return false;
        }
        return listingCertResults.stream()
                    .filter(certResult -> certResult.getCriterion() != null && certResult.getCriterion().getId() != null
                        && certResult.getCriterion().getId().equals(criterionDto.getId()))
                    .findAny().isPresent();
    }

    private CertificationResult buildCertificationResult(CertificationCriterion criterion) {
        return CertificationResult.builder()
            .criterion(criterion)
            .success(Boolean.FALSE)
        .build();
    }

    private void nullifyNotApplicableFieldsInUnattestedCriteria(CertifiedProductSearchDetails listing) {
        listing.getCertificationResults().stream()
            .filter(certResult -> BooleanUtils.isFalse(certResult.isSuccess()))
            .forEach(unattestedCertResult -> nullifyNotApplicableFields(unattestedCertResult));
    }

    private void nullifyNotApplicableFields(CertificationResult certResult) {
        if (!isFieldAllowed(certResult.getCriterion(), CertificationResultRules.ADDITIONAL_SOFTWARE)) {
            certResult.setAdditionalSoftware(null);
        } else if (certResult.getAdditionalSoftware() == null) {
            certResult.setAdditionalSoftware(new ArrayList<CertificationResultAdditionalSoftware>());
        }

        if (!isFieldAllowed(certResult.getCriterion(), CertificationResultRules.API_DOCUMENTATION)) {
            certResult.setApiDocumentation(null);
        } else if (certResult.getApiDocumentation() == null) {
            certResult.setApiDocumentation("");
        }

        if (!isFieldAllowed(certResult.getCriterion(), CertificationResultRules.ATTESTATION_ANSWER)) {
            certResult.setAttestationAnswer(null);
        } else if (certResult.getAttestationAnswer() == null) {
            certResult.setAttestationAnswer(false);
        }

        if (!isFieldAllowed(certResult.getCriterion(), CertificationResultRules.CONFORMANCE_METHOD)) {
            certResult.setConformanceMethods(null);
        } else if (certResult.getConformanceMethods() == null) {
            certResult.setConformanceMethods(new ArrayList<CertificationResultConformanceMethod>());
        }

        if (!isFieldAllowed(certResult.getCriterion(), CertificationResultRules.DOCUMENTATION_URL)) {
            certResult.setDocumentationUrl(null);
        } else if (certResult.getDocumentationUrl() == null) {
            certResult.setDocumentationUrl("");
        }

        if (!isFieldAllowed(certResult.getCriterion(), CertificationResultRules.EXPORT_DOCUMENTATION)) {
            certResult.setExportDocumentation(null);
        } else if (certResult.getExportDocumentation() == null) {
            certResult.setExportDocumentation("");
        }

        if (!isFieldAllowed(certResult.getCriterion(), CertificationResultRules.FUNCTIONALITY_TESTED)) {
            certResult.setTestFunctionality(null);
        } else if (certResult.getTestFunctionality() == null) {
            certResult.setTestFunctionality(new ArrayList<CertificationResultTestFunctionality>());
        }

        if (!isFieldAllowed(certResult.getCriterion(), CertificationResultRules.G1_SUCCESS)) {
            certResult.setG1Success(null);
        } else if (certResult.isG1Success() == null) {
            certResult.setG1Success(false);
        }

        if (!isFieldAllowed(certResult.getCriterion(), CertificationResultRules.G2_SUCCESS)) {
            certResult.setG2Success(null);
        } else if (certResult.isG2Success() == null) {
            certResult.setG2Success(false);
        }

        if (!isFieldAllowed(certResult.getCriterion(), CertificationResultRules.GAP)) {
            certResult.setGap(null);
        } else if (certResult.isGap() == null) {
            certResult.setGap(false);
        }

        if (!isFieldAllowed(certResult.getCriterion(), CertificationResultRules.OPTIONAL_STANDARD)) {
            certResult.setOptionalStandards(null);
        } else if (certResult.getOptionalStandards() == null) {
            certResult.setOptionalStandards(new ArrayList<CertificationResultOptionalStandard>());
        }

        if (!isFieldAllowed(certResult.getCriterion(), CertificationResultRules.PRIVACY_SECURITY)) {
            certResult.setPrivacySecurityFramework(null);
        } else if (certResult.getPrivacySecurityFramework() == null) {
            certResult.setPrivacySecurityFramework("");
        }

        if (!isFieldAllowed(certResult.getCriterion(), CertificationResultRules.SED)) {
            certResult.setSed(null);
        } else if (certResult.isSed() == null) {
            certResult.setSed(false);
        }

        if (!isFieldAllowed(certResult.getCriterion(), CertificationResultRules.SERVICE_BASE_URL_LIST)) {
            certResult.setServiceBaseUrlList(null);
        } else if (certResult.getServiceBaseUrlList() == null) {
            certResult.setServiceBaseUrlList("");
        }

        if (!isFieldAllowed(certResult.getCriterion(), CertificationResultRules.STANDARDS_TESTED)) {
            certResult.setTestStandards(null);
        } else if (certResult.getTestStandards() == null) {
            certResult.setTestStandards(new ArrayList<CertificationResultTestStandard>());
        }

        if (!isFieldAllowed(certResult.getCriterion(), CertificationResultRules.SVAP)) {
            certResult.setSvaps(null);
        } else if (certResult.getSvaps() == null) {
            certResult.setSvaps(new ArrayList<CertificationResultSvap>());
        }

        if (!isFieldAllowed(certResult.getCriterion(), CertificationResultRules.TEST_DATA)) {
            certResult.setTestDataUsed(null);
        } else if (certResult.getTestDataUsed() == null) {
            certResult.setTestDataUsed(new ArrayList<CertificationResultTestData>());
        }

        if (!isFieldAllowed(certResult.getCriterion(), CertificationResultRules.TEST_PROCEDURE)) {
            certResult.setTestProcedures(null);
        } else if (certResult.getTestProcedures() == null) {
            certResult.setTestProcedures(new ArrayList<CertificationResultTestProcedure>());
        }

        if (!isFieldAllowed(certResult.getCriterion(), CertificationResultRules.TEST_TOOLS_USED)) {
            certResult.setTestToolsUsed(null);
        } else if (certResult.getTestToolsUsed() == null) {
            certResult.setTestToolsUsed(new ArrayList<CertificationResultTestTool>());
        }

        if (!isFieldAllowed(certResult.getCriterion(), CertificationResultRules.USE_CASES)) {
            certResult.setUseCases(null);
        } else if (certResult.getUseCases() == null) {
            certResult.setUseCases("");
        }
    }

    private boolean isFieldAllowed(CertificationCriterion criterion, String field) {
        return certResultRules.hasCertOption(criterion.getNumber(), field);
    }
}
