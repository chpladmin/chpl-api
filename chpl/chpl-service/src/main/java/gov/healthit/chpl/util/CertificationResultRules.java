package gov.healthit.chpl.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertificationCriterionAttributeDAO;
import gov.healthit.chpl.entity.CertificationCriterionAttributeEntity;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component("certificationResultRules")
public class CertificationResultRules {
    public static final String GAP = "gap";
    public static final String PRIVACY_SECURITY = "privacySecurity";
    public static final String CONFORMANCE_METHOD = "conformanceMethod";
    public static final String OPTIONAL_STANDARD = "optionalStandard";
    public static final String STANDARDS_TESTED = "standardsTested";
    public static final String FUNCTIONALITY_TESTED = "functionalityTested";
    public static final String API_DOCUMENTATION = "apiDocumentation";
    public static final String EXPORT_DOCUMENTATION = "exportDocumentation";
    public static final String DOCUMENTATION_URL = "documentationUrl";
    public static final String USE_CASES = "useCases";
    public static final String G1_SUCCESS = "g1Success";
    public static final String G2_SUCCESS = "g2Success";
    public static final String ATTESTATION_ANSWER = "attestationAnswer";
    public static final String ADDITIONAL_SOFTWARE = "additionalSoftware";
    public static final String TEST_TOOLS_USED = "testTool";
    public static final String TEST_PROCEDURE = "testProcedure";
    public static final String TEST_DATA = "testData";
    public static final String SED = "sed";
    public static final String SERVICE_BASE_URL_LIST = "serviceBaseUrlList";
    public static final String SVAP = "svap";
    public static final String STANDARD = "standard";

    private CertificationCriterionAttributeDAO certificationCriterionAttributeDao;
    private Map<Long, List<CertificationResultOption>> rules = new HashMap<Long, List<CertificationResultOption>>();

    @Autowired
    public CertificationResultRules(CertificationCriterionAttributeDAO certificationCriterionAttributeDao) {
        this.certificationCriterionAttributeDao = certificationCriterionAttributeDao;
        setRules();
    }

    private void setRules() {
        List<CertificationCriterionAttributeEntity> attributes = certificationCriterionAttributeDao.getAllCriteriaAttributes();
        for (CertificationCriterionAttributeEntity attribute : attributes) {
            if (rules.get(attribute.getCriterion().getId()) == null) {
                rules.put(attribute.getCriterion().getId(), new ArrayList<CertificationResultOption>());
            }

            rules.get(attribute.getCriterion().getId()).add(
                CertificationResultOption.builder()
                    .canHaveOption(attribute.getAdditionalSoftware())
                    .optionName(ADDITIONAL_SOFTWARE)
                .build());
            rules.get(attribute.getCriterion().getId()).add(
                CertificationResultOption.builder()
                    .canHaveOption(attribute.getApiDocumentation())
                    .optionName(API_DOCUMENTATION)
                .build());
            rules.get(attribute.getCriterion().getId()).add(
                CertificationResultOption.builder()
                    .canHaveOption(attribute.getAttestationAnswer())
                    .optionName(ATTESTATION_ANSWER)
                .build());
            rules.get(attribute.getCriterion().getId()).add(
                    CertificationResultOption.builder()
                    .canHaveOption(attribute.getConformanceMethod())
                    .optionName(CONFORMANCE_METHOD)
                    .build());
            rules.get(attribute.getCriterion().getId()).add(
                CertificationResultOption.builder()
                    .canHaveOption(attribute.getDocumentationUrl())
                    .optionName(DOCUMENTATION_URL)
                .build());
            rules.get(attribute.getCriterion().getId()).add(
                CertificationResultOption.builder()
                    .canHaveOption(attribute.getExportDocumentation())
                    .optionName(EXPORT_DOCUMENTATION)
                .build());
            rules.get(attribute.getCriterion().getId()).add(
                CertificationResultOption.builder()
                    .canHaveOption(attribute.getFunctionalityTested())
                    .optionName(FUNCTIONALITY_TESTED)
                .build());
            rules.get(attribute.getCriterion().getId()).add(
                CertificationResultOption.builder()
                    .canHaveOption(attribute.getGap())
                    .optionName(GAP)
                .build());
            rules.get(attribute.getCriterion().getId()).add(
                CertificationResultOption.builder()
                    .canHaveOption(attribute.getG1Success())
                    .optionName(G1_SUCCESS)
                .build());
            rules.get(attribute.getCriterion().getId()).add(
                CertificationResultOption.builder()
                    .canHaveOption(attribute.getG2Success())
                    .optionName(G2_SUCCESS)
                .build());
            rules.get(attribute.getCriterion().getId()).add(
                CertificationResultOption.builder()
                    .canHaveOption(attribute.getOptionalStandard())
                    .optionName(OPTIONAL_STANDARD)
                .build());
            rules.get(attribute.getCriterion().getId()).add(
                CertificationResultOption.builder()
                    .canHaveOption(attribute.getPrivacySecurityFramework())
                    .optionName(PRIVACY_SECURITY)
                .build());
            rules.get(attribute.getCriterion().getId()).add(
                CertificationResultOption.builder()
                    .canHaveOption(attribute.getSed())
                    .optionName(SED)
                .build());
            rules.get(attribute.getCriterion().getId()).add(
                CertificationResultOption.builder()
                    .canHaveOption(attribute.getServiceBaseUrlList())
                    .optionName(SERVICE_BASE_URL_LIST)
                .build());
            rules.get(attribute.getCriterion().getId()).add(
                CertificationResultOption.builder()
                    .canHaveOption(attribute.getTestStandard())
                    .optionName(STANDARDS_TESTED)
                .build());
            rules.get(attribute.getCriterion().getId()).add(
                CertificationResultOption.builder()
                    .canHaveOption(attribute.getSvap())
                    .optionName(SVAP)
                .build());
            rules.get(attribute.getCriterion().getId()).add(
                CertificationResultOption.builder()
                    .canHaveOption(attribute.getTestData())
                    .optionName(TEST_DATA)
                .build());
            rules.get(attribute.getCriterion().getId()).add(
                CertificationResultOption.builder()
                    .canHaveOption(attribute.getTestProcedure())
                    .optionName(TEST_PROCEDURE)
                .build());
            rules.get(attribute.getCriterion().getId()).add(
                CertificationResultOption.builder()
                    .canHaveOption(attribute.getTestTool())
                    .optionName(TEST_TOOLS_USED)
                .build());
            rules.get(attribute.getCriterion().getId()).add(
                CertificationResultOption.builder()
                    .canHaveOption(attribute.getUseCases())
                    .optionName(USE_CASES)
                .build());
            rules.get(attribute.getCriterion().getId()).add(
                CertificationResultOption.builder()
                    .canHaveOption(attribute.getStandard())
                    .optionName(STANDARD)
                .build());
        }
    }

    public boolean hasCertOption(Long certId, String optionName) {
        boolean result = false;

        List<CertificationResultOption> options = rules.get(certId);
        if (options != null && options.size() > 0) {
            for (CertificationResultOption option : options) {
                if (option.getOptionName().equalsIgnoreCase(optionName)) {
                    result = option.isCanHaveOption();
                }
            }
        }
        return result;
    }
}
