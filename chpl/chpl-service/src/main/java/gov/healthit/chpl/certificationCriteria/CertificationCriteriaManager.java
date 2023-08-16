package gov.healthit.chpl.certificationCriteria;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.certificationCriteria.CertificationCriterionWithAttributes.AllowedAttributes;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.util.CertificationResultRules;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service("certificationCriteriaManager")
public class CertificationCriteriaManager {
    private CertificationCriterionDAO certificationCriterionDao;
    private CertificationResultRules rules;
    private CertificationCriterionComparator criterionComparator;

    @Autowired
    public CertificationCriteriaManager(CertificationCriterionDAO certificationCriterionDao,
            CertificationResultRules rules,
            CertificationCriterionComparator criterionComparator) {
        this.certificationCriterionDao = certificationCriterionDao;
        this.rules = rules;
        this.criterionComparator = criterionComparator;
    }

    @Transactional
    @Cacheable(value = CacheNames.CERTIFICATION_CRITERIA)
    public List<CertificationCriterionWithAttributes> getAllWithAttributes() {
        LOGGER.debug("Getting all criterion with attributes from the database (not cached).");
        List<CertificationCriterion> allCriteria = getAll();
        return allCriteria.stream()
                .map(criterion -> buildCertificationCriterionWithAttributes(criterion))
                .sorted(criterionComparator)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<CertificationCriterion> getAll() {
        return this.certificationCriterionDao.findAll().stream()
                .collect(Collectors.toList());
    }

    private CertificationCriterionWithAttributes buildCertificationCriterionWithAttributes(CertificationCriterion criterion) {
        return CertificationCriterionWithAttributes.builder()
                .id(criterion.getId())
                .certificationEdition(criterion.getCertificationEdition())
                .certificationEditionId(criterion.getCertificationEditionId())
                .description(criterion.getDescription())
                .endDay(criterion.getEndDay())
                .number(criterion.getNumber())
                .removed(criterion.getRemoved())
                .rule(criterion.getRule())
                .startDay(criterion.getStartDay())
                .title(criterion.getTitle())
                .attributes(AllowedAttributes.builder()
                        .additionalSoftware(rules.hasCertOption(criterion.getId(), CertificationResultRules.ADDITIONAL_SOFTWARE))
                        .apiDocumentation(rules.hasCertOption(criterion.getId(), CertificationResultRules.API_DOCUMENTATION))
                        .attestationAnswer(rules.hasCertOption(criterion.getId(), CertificationResultRules.ATTESTATION_ANSWER))
                        .conformanceMethod(rules.hasCertOption(criterion.getId(), CertificationResultRules.CONFORMANCE_METHOD))
                        .documentationUrl(rules.hasCertOption(criterion.getId(), CertificationResultRules.DOCUMENTATION_URL))
                        .exportDocumentation(rules.hasCertOption(criterion.getId(), CertificationResultRules.EXPORT_DOCUMENTATION))
                        .functionalityTested(rules.hasCertOption(criterion.getId(), CertificationResultRules.FUNCTIONALITY_TESTED))
                        .g1Success(rules.hasCertOption(criterion.getId(), CertificationResultRules.G1_SUCCESS))
                        .g2Success(rules.hasCertOption(criterion.getId(), CertificationResultRules.G2_SUCCESS))
                        .gap(rules.hasCertOption(criterion.getId(), CertificationResultRules.GAP))
                        .optionalStandard(rules.hasCertOption(criterion.getId(), CertificationResultRules.OPTIONAL_STANDARD))
                        .privacySecurityFramework(rules.hasCertOption(criterion.getId(), CertificationResultRules.PRIVACY_SECURITY))
                        .sed(rules.hasCertOption(criterion.getId(), CertificationResultRules.SED))
                        .serviceBaseUrlList(rules.hasCertOption(criterion.getId(), CertificationResultRules.SERVICE_BASE_URL_LIST))
                        .standardsTested(rules.hasCertOption(criterion.getId(), CertificationResultRules.STANDARDS_TESTED))
                        .svap(rules.hasCertOption(criterion.getId(), CertificationResultRules.SVAP))
                        .testData(rules.hasCertOption(criterion.getId(), CertificationResultRules.TEST_DATA))
                        .testProcedure(rules.hasCertOption(criterion.getId(), CertificationResultRules.TEST_PROCEDURE))
                        .testTool(rules.hasCertOption(criterion.getId(), CertificationResultRules.TEST_TOOLS_USED))
                        .useCases(rules.hasCertOption(criterion.getId(), CertificationResultRules.USE_CASES))
                        .build())
                .build();
    }
}
