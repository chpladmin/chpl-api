package gov.healthit.chpl.upload.listing.normalizer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
            .number(criterion.getNumber())
            .title(criterion.getTitle())
            .success(Boolean.FALSE)
            .additionalSoftware(!isFieldAllowed(criterion, CertificationResultRules.ADDITIONAL_SOFTWARE) ? null : new ArrayList<CertificationResultAdditionalSoftware>())
            .apiDocumentation(!isFieldAllowed(criterion, CertificationResultRules.API_DOCUMENTATION) ? null : "")
            .attestationAnswer(!isFieldAllowed(criterion, CertificationResultRules.ATTESTATION_ANSWER) ? null : false)
            .conformanceMethods(!isFieldAllowed(criterion, CertificationResultRules.CONFORMANCE_METHOD) ? null : new ArrayList<CertificationResultConformanceMethod>())
            .documentationUrl(!isFieldAllowed(criterion, CertificationResultRules.DOCUMENTATION_URL) ? null : "")
            .exportDocumentation(!isFieldAllowed(criterion, CertificationResultRules.EXPORT_DOCUMENTATION) ? null : "")
            .testFunctionality(!isFieldAllowed(criterion, CertificationResultRules.FUNCTIONALITY_TESTED) ? null : new ArrayList<CertificationResultTestFunctionality>())
            .g1Success(!isFieldAllowed(criterion, CertificationResultRules.G1_SUCCESS) ? null : false)
            .g2Success(!isFieldAllowed(criterion, CertificationResultRules.G2_SUCCESS) ? null : false)
            .gap(!isFieldAllowed(criterion, CertificationResultRules.GAP) ? null : false)
            .optionalStandards(!isFieldAllowed(criterion, CertificationResultRules.OPTIONAL_STANDARD) ? null : new ArrayList<CertificationResultOptionalStandard>())
            .privacySecurityFramework(!isFieldAllowed(criterion, CertificationResultRules.PRIVACY_SECURITY) ? null : "")
            .sed(!isFieldAllowed(criterion, CertificationResultRules.SED) ? null : false)
            .serviceBaseUrlList(!isFieldAllowed(criterion, CertificationResultRules.SERVICE_BASE_URL_LIST) ? null : "")
            .testStandards(!isFieldAllowed(criterion, CertificationResultRules.STANDARDS_TESTED) ? null : new ArrayList<CertificationResultTestStandard>())
            .svaps(!isFieldAllowed(criterion, CertificationResultRules.SVAP) ? null : new ArrayList<CertificationResultSvap>())
            .testDataUsed(!isFieldAllowed(criterion, CertificationResultRules.TEST_DATA) ? null : new ArrayList<CertificationResultTestData>())
            .testProcedures(!isFieldAllowed(criterion, CertificationResultRules.TEST_PROCEDURE) ? null : new ArrayList<CertificationResultTestProcedure>())
            .testToolsUsed(!isFieldAllowed(criterion, CertificationResultRules.TEST_TOOLS_USED) ? null : new ArrayList<CertificationResultTestTool>())
            .useCases(!isFieldAllowed(criterion, CertificationResultRules.USE_CASES) ? null : "")
        .build();
    }

    private boolean isFieldAllowed(CertificationCriterion criterion, String field) {
        return certResultRules.hasCertOption(criterion.getNumber(), field);
    }
}
