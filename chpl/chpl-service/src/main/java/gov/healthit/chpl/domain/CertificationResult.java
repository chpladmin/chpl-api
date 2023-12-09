package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import gov.healthit.chpl.api.deprecatedUsage.DeprecatedResponseField;
import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.conformanceMethod.CertificationResultConformanceMethodComparator;
import gov.healthit.chpl.conformanceMethod.domain.CertificationResultConformanceMethod;
import gov.healthit.chpl.conformanceMethod.domain.ConformanceMethod;
import gov.healthit.chpl.domain.comparator.CertificationResultAdditionalSoftwareComparator;
import gov.healthit.chpl.domain.comparator.CertificationResultTestDataComparator;
import gov.healthit.chpl.domain.comparator.CertificationResultTestProcedureComparator;
import gov.healthit.chpl.domain.comparator.CertificationResultTestStandardComparator;
import gov.healthit.chpl.dto.CertificationResultDetailsDTO;
import gov.healthit.chpl.functionalitytested.CertificationResultFunctionalityTested;
import gov.healthit.chpl.functionalitytested.CertificationResultFunctionalityTestedComparator;
import gov.healthit.chpl.optionalStandard.CertificationResultOptionalStandardComparator;
import gov.healthit.chpl.optionalStandard.domain.CertificationResultOptionalStandard;
import gov.healthit.chpl.optionalStandard.domain.OptionalStandard;
import gov.healthit.chpl.standard.CertificationResultStandard;
import gov.healthit.chpl.standard.CertificationResultStandardComparator;
import gov.healthit.chpl.svap.domain.CertificationResultSvap;
import gov.healthit.chpl.svap.domain.CertificationResultSvapComparator;
import gov.healthit.chpl.svap.domain.Svap;
import gov.healthit.chpl.testtool.CertificationResultTestTool;
import gov.healthit.chpl.testtool.CertificationResultTestToolComparator;
import gov.healthit.chpl.testtool.TestTool;
import gov.healthit.chpl.util.CertificationResultRules;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@Builder
@Data
@ToString
public class CertificationResult implements Serializable {
    private static final long serialVersionUID = -4917413876078419868L;
    public static final String PRIVACY_SECURITY_FRAMEWORK_DELIMITER = ";";

    private Long id;

    @Schema(description = "Whether or not this criteria was met.")
    private Boolean success;

    @JsonIgnore
    private String successStr;

    @Schema(description = "This variable indicates if the certification criteria was gap certified. "
            + "It is a binary variable that takes true or false value.")
    private Boolean gap;

    @JsonIgnore
    private String gapStr;

    @JsonIgnore
    private Boolean hasAdditionalSoftware;

    @JsonIgnore
    private String hasAdditionalSoftwareStr;

    @Schema(description = "This variable indicates if the corresponding certification criteria was submitted for safety-enhanced design "
            + "attestation during certification testing. It is applicable for the 2014 Edition, and it is a binary variable that takes "
            + "either true or false value.")
    private Boolean sed;

    @Schema(description = "This variable indicates if the corresponding certification criteria was successfully tested for automated "
            + "numerator recording. It is applicable for the 2014 Edition, and it is a binary variable that takes either true or "
            + "false value.")
    private Boolean g1Success;

    @Schema(description = "This variable indicates if the corresponding certification criteria was successfully tested for automated measure "
            + "calculation. It is applicable for the 2014 Edition, and it is a binary variable that takes either true or false.")
    private Boolean g2Success;

    @Schema(description = "This variable indicates if the corresponding certification criteria has an attestation answer. "
            + "It is a binary value that takes either true or false.")
    private Boolean attestationAnswer;

    private String attestationAnswerStr;

    @Schema(description = "The hyperlink to access an application programming interface (API)'s documentation and terms of use. "
            + "It is a fully qualified URL which is reachable via web browser validation and verification.")
    private String apiDocumentation;

    @Schema(description = "The hyperlink to access export documentation. "
            + "It is a fully qualified URL which is reachable via web browser validation and verification.")
    private String exportDocumentation;

    @Schema(description = "The hyperlink to access a documentation URL."
            + " It is a fully qualified URL which is reachable via web browser validation and verification.")
    private String documentationUrl;

    @Schema(description = "The hyperlink to access Use Case(s). "
            + "It is a fully qualified URL which is reachable via web browser validation and verification.")
    private String useCases;

    @Schema(description = "The publicly accessible hyperlink to the list of service base URLs for a Health IT Module "
            + "certified to § 170.315(g)(10) that can be used by patients to access their electronic health "
            + "information. It is a fully qualified URL which is reachable via web browser validation and verification.")
    private String serviceBaseUrlList;

    @Schema(description = "The hyperlink to access Risk Management Summary Information. It is a fully qualified URL "
            + "which is reachable via web browser.")
    private String riskManagementSummaryInformation;

    @Schema(description = "This variable explains the way in which each privacy and security criterion was addressed for "
            + "the purposes of certification. It takes either of Approach 1 and Approach 2.")
    private String privacySecurityFramework;

    @Deprecated
    @DeprecatedResponseField(message = "This field is deprecated and will be removed. This data can be found via a GET request to the endpoint /conformance-methods.",
        removalDate = "2024-01-01")
    private List<ConformanceMethod> allowedConformanceMethods;

    @Deprecated
    @DeprecatedResponseField(message = "This field is deprecated and will be removed. This data can be found via a GET request to the endpoint /svaps.",
        removalDate = "2024-01-01")
    private List<Svap> allowedSvaps;

    @Deprecated
    @DeprecatedResponseField(message = "This field is deprecated and will be removed. This data can be found via a GET request to the endpoint /optional-standards.",
        removalDate = "2024-01-01")
    private List<OptionalStandard> allowedOptionalStandards;

    @Deprecated
    @DeprecatedResponseField(message = "This field is deprecated and will be removed. This data can be found via a GET request to the endpoint /test-tools.",
        removalDate = "2024-01-01")
    private List<TestTool> allowedTestTools;

    @Schema(description = "Any optional, alternative, ambulatory (2015 only), or inpatient (2015 only) capabilities within a certification "
            + "criterion to which the Health IT module was tested and certified. For example, within the 2015 certification "
            + "criteria 170.315(a), the optional functionality to include a 'reason for order' field should be denoted as "
            + "(a)(1)(ii). You can find a list of potential values in the 2014 or 2015 Functionality and Standards Reference Tables.")
    @Builder.Default
    private List<CertificationResultFunctionalityTested> functionalitiesTested = new ArrayList<CertificationResultFunctionalityTested>();

    @Schema(description = "The methods used to evaluate compliance with the certification criterion.")
    private List<CertificationResultConformanceMethod> conformanceMethods = new ArrayList<CertificationResultConformanceMethod>();

    @Schema(description = "The test procedures used for the certification criteria")
    private List<CertificationResultTestProcedure> testProcedures = new ArrayList<CertificationResultTestProcedure>();

    @Schema(description = "The versions of the test data being used for the certification criteria")
    private List<CertificationResultTestData> testDataUsed = new ArrayList<CertificationResultTestData>();

    @Schema(description = "This variable indicates if any additional software is relied upon by the Health IT Module to demonstrate its "
            + "compliance with a certification criterion or criteria.")
    private List<CertificationResultAdditionalSoftware> additionalSoftware = new ArrayList<CertificationResultAdditionalSoftware>();

    @Schema(description = "An optional standard used to meet a certification criterion. You can find a list of potential "
            + "values in the 2015 Functionality and Standards Reference Tables. Allowed values are the corresponding "
            + "paragraph number for the standard within the regulation.")
    @Builder.Default
    private List<CertificationResultOptionalStandard> optionalStandards = new ArrayList<CertificationResultOptionalStandard>();

    @Schema(description = "A standard used to meet a certification criterion.You can find a list of potential "
            + "values in the 2014 or 2015 Functionality and Standards Reference Tables. Allowed values are the corresponding "
            + "paragraph number for the standard within the regulation.")
    @Builder.Default
    private List<CertificationResultTestStandard> testStandards = new ArrayList<CertificationResultTestStandard>();

    @Schema(description = "The test tool used to certify the Health IT Module to the corresponding ONC certification criteria.")
    @Builder.Default
    private List<CertificationResultTestTool> testToolsUsed = new ArrayList<CertificationResultTestTool>();

    @Schema(description = "ONC has established the Standards Version Advancement Process (SVAP) to enable health IT developers’ "
            + "ability to incorporate newer versions of Secretary-adopted standards and implementation specifications, "
            + "as part of the \"Real World Testing\" Condition and Maintenance of Certification requirement (§170.405) "
            + "of the 21st Century Cures Act")
    @Builder.Default
    private List<CertificationResultSvap> svaps = new ArrayList<CertificationResultSvap>();

    // TODO - Need this text for OCD-4333
    @Builder.Default
    private List<CertificationResultStandard> standards = new ArrayList<CertificationResultStandard>();

    @Schema(description = "Detailed information about the relevant certification criterion.")
    private CertificationCriterion criterion;

    /**
     * This property exists solely to be able to deserialize listing activity events from very old data.
     * Our Activity Explorer classes sometimes need to look at the "number" and "title" fields
     * and deduce which criterion we were using so we need to be bale to read this value.
     * This property should not be visible in the generated XSD or any response from an API call.
     * Do not use it in any code unless you are specifically referencing legacy listing activity data.
     */
    @JsonProperty(access = Access.WRITE_ONLY)
    private String number;

    private CertificationResultStandardComparator standardComparator;
    private CertificationResultSvapComparator svapComparator;
    private CertificationResultOptionalStandardComparator osComparator;
    private CertificationResultConformanceMethodComparator cmComparator;
    private CertificationResultFunctionalityTestedComparator funcTestedComparator;
    private CertificationResultTestProcedureComparator testProcComparator;
    private CertificationResultTestDataComparator testDataComparator;
    private CertificationResultTestToolComparator testToolComparator;
    private CertificationResultTestStandardComparator testStandardComparator;
    private CertificationResultAdditionalSoftwareComparator asComparator;

    public CertificationResult() {
        this.functionalitiesTested = new ArrayList<CertificationResultFunctionalityTested>();
        this.testToolsUsed = new ArrayList<CertificationResultTestTool>();
        this.testStandards = new ArrayList<CertificationResultTestStandard>();
        this.optionalStandards = new ArrayList<CertificationResultOptionalStandard>();
        this.additionalSoftware = new ArrayList<CertificationResultAdditionalSoftware>();
        this.testDataUsed = new ArrayList<CertificationResultTestData>();
        this.conformanceMethods = new ArrayList<CertificationResultConformanceMethod>();
        this.testProcedures = new ArrayList<CertificationResultTestProcedure>();
        this.svaps = new ArrayList<CertificationResultSvap>();

        this.standardComparator = new CertificationResultStandardComparator();
        this.svapComparator = new CertificationResultSvapComparator();
        this.osComparator = new CertificationResultOptionalStandardComparator();
        this.cmComparator = new CertificationResultConformanceMethodComparator();
        this.funcTestedComparator = new CertificationResultFunctionalityTestedComparator();
        this.testProcComparator = new CertificationResultTestProcedureComparator();
        this.testDataComparator = new CertificationResultTestDataComparator();
        this.testToolComparator = new CertificationResultTestToolComparator();
        this.testStandardComparator = new CertificationResultTestStandardComparator();
        this.asComparator = new CertificationResultAdditionalSoftwareComparator();
    }

    public CertificationResult(CertificationResultDetailsDTO certResult) {
        this();
        this.setId(certResult.getId());
        this.setSuccess(certResult.getSuccess());
        this.setGap(certResult.getGap() == null ? Boolean.FALSE : certResult.getGap());
        this.setSed(certResult.getSed() == null ? Boolean.FALSE : certResult.getSed());
        this.setG1Success(certResult.getG1Success() == null ? Boolean.FALSE : certResult.getG1Success());
        this.setG2Success(certResult.getG2Success() == null ? Boolean.FALSE : certResult.getG2Success());
        this.setAttestationAnswer(certResult.getAttestationAnswer() == null ? Boolean.FALSE : certResult.getAttestationAnswer());
        this.setApiDocumentation(certResult.getApiDocumentation());
        this.setExportDocumentation(certResult.getExportDocumentation());
        this.setDocumentationUrl(certResult.getDocumentationUrl());
        this.setUseCases(certResult.getUseCases());
        this.setServiceBaseUrlList(certResult.getServiceBaseUrlList());
        this.setPrivacySecurityFramework(certResult.getPrivacySecurityFramework());
        if (certResult.getCriterion() != null) {
            this.criterion = certResult.getCriterion();
        }
    }

    //Correctly handles setting the values based on Certification rules
    public CertificationResult(CertificationResultDetailsDTO certResult, CertificationResultRules certRules) {
        this();
        this.setId(certResult.getId());
        this.setSuccess(certResult.getSuccess());
        this.setSed(certResult.getSed() == null ? Boolean.FALSE : certResult.getSed());
        if (!certRules.hasCertOption(certResult.getCertificationCriterionId(), CertificationResultRules.GAP)) {
            this.setGap(null);
        } else if (certResult.getGap() == null) {
            this.setGap(Boolean.FALSE);
        } else {
            this.setGap(certResult.getGap());
        }
        if (!certRules.hasCertOption(certResult.getCertificationCriterionId(), CertificationResultRules.G1_SUCCESS)) {
            this.setG1Success(null);
        } else if (certResult.getG1Success() == null) {
            this.setG1Success(Boolean.FALSE);
        } else {
            this.setG1Success(certResult.getG1Success());
        }
        if (!certRules.hasCertOption(certResult.getCertificationCriterionId(), CertificationResultRules.G2_SUCCESS)) {
            this.setG2Success(null);
        } else if (certResult.getG2Success() == null) {
            this.setG2Success(Boolean.FALSE);
        } else {
            this.setG2Success(certResult.getG2Success());
        }
        if (!certRules.hasCertOption(certResult.getCertificationCriterionId(), CertificationResultRules.ATTESTATION_ANSWER)) {
            this.setAttestationAnswer(null);
        } else if (certResult.getAttestationAnswer() == null) {
            this.setAttestationAnswer(false);
        } else {
            this.setAttestationAnswer(certResult.getAttestationAnswer());
        }
        if (!certRules.hasCertOption(certResult.getCertificationCriterionId(), CertificationResultRules.API_DOCUMENTATION)) {
            this.setApiDocumentation(null);
        } else if (certResult.getApiDocumentation() == null) {
            this.setApiDocumentation("");
        } else {
            this.setApiDocumentation(certResult.getApiDocumentation());
        }
        if (!certRules.hasCertOption(certResult.getCertificationCriterionId(), CertificationResultRules.EXPORT_DOCUMENTATION)) {
            this.setExportDocumentation(null);
        } else if (certResult.getExportDocumentation() == null) {
            this.setExportDocumentation("");
        } else {
            this.setExportDocumentation(certResult.getExportDocumentation());
        }
        if (!certRules.hasCertOption(certResult.getCertificationCriterionId(), CertificationResultRules.DOCUMENTATION_URL)) {
            this.setDocumentationUrl(null);
        } else if (certResult.getDocumentationUrl() == null) {
            this.setDocumentationUrl("");
        } else {
            this.setDocumentationUrl(certResult.getDocumentationUrl());
        }
        if (!certRules.hasCertOption(certResult.getCertificationCriterionId(), CertificationResultRules.USE_CASES)) {
            this.setUseCases(null);
        } else if (certResult.getUseCases() == null) {
            this.setUseCases("");
        } else {
            this.setUseCases(certResult.getUseCases());
        }
        if (!certRules.hasCertOption(certResult.getCertificationCriterionId(), CertificationResultRules.RISK_MANAGEMENT_SUMMARY_INFORMATION)) {
            this.setRiskManagementSummaryInformation(null);
        } else if (certResult.getRiskManagementSummaryInformation() == null) {
            this.setRiskManagementSummaryInformation("");
        } else {
            this.setRiskManagementSummaryInformation(certResult.getRiskManagementSummaryInformation());
        }
        if (!certRules.hasCertOption(certResult.getCertificationCriterionId(), CertificationResultRules.SERVICE_BASE_URL_LIST)) {
            this.setServiceBaseUrlList(null);
        } else if (certResult.getServiceBaseUrlList() == null) {
            this.setServiceBaseUrlList("");
        } else {
            this.setServiceBaseUrlList(certResult.getServiceBaseUrlList());
        }
        if (!certRules.hasCertOption(certResult.getCertificationCriterionId(), CertificationResultRules.PRIVACY_SECURITY)) {
            this.setPrivacySecurityFramework(null);
        } else if (certResult.getPrivacySecurityFramework() == null) {
            this.setPrivacySecurityFramework("");
        } else {
            this.setPrivacySecurityFramework(certResult.getPrivacySecurityFramework());
        }
        this.criterion = certResult.getCriterion();

        this.setOptionalStandards(getOptionalStandards(certResult, certRules));
        this.setFunctionalitiesTested(getFunctionalitiesTested(certResult, certRules));
        this.setConformanceMethods(getConformanceMethods(certResult, certRules));
        this.setTestProcedures(getTestProcedures(certResult, certRules));
        this.setTestDataUsed(getTestData(certResult, certRules));
        this.setTestToolsUsed(getTestTools(certResult, certRules));
        this.setTestStandards(getTestStandards(certResult, certRules));
        this.setAdditionalSoftware(getAdditionalSoftware(certResult, certRules));
        this.setSvaps(getSvaps(certResult, certRules));
        this.setStandards(getStandards(certResult, certRules));
    }


    private List<CertificationResultStandard> getStandards(CertificationResultDetailsDTO certResult, CertificationResultRules certRules) {
        if (certRules.hasCertOption(certResult.getCertificationCriterionId(), CertificationResultRules.STANDARD)) {
            return certResult.getStandards().stream()
                    .sorted(standardComparator)
                    .collect(Collectors.toList());
        } else {
            return null;
        }
    }

    private List<CertificationResultSvap> getSvaps(CertificationResultDetailsDTO certResult, CertificationResultRules certRules) {
        if (certRules.hasCertOption(certResult.getCertificationCriterionId(), CertificationResultRules.SVAP)) {
            return certResult.getSvaps().stream()
                    .sorted(svapComparator)
                    .collect(Collectors.toList());
        } else {
            return null;
        }
    }

    private List<CertificationResultOptionalStandard> getOptionalStandards(CertificationResultDetailsDTO certResult, CertificationResultRules certRules) {
        if (certRules.hasCertOption(certResult.getCertificationCriterionId(), CertificationResultRules.OPTIONAL_STANDARD)) {
            return certResult.getOptionalStandards().stream()
                    .sorted(osComparator)
                    .collect(Collectors.toList());
        } else {
            return null;
        }
    }

    private List<CertificationResultFunctionalityTested> getFunctionalitiesTested(CertificationResultDetailsDTO certResult, CertificationResultRules certRules) {
        if (certRules.hasCertOption(certResult.getCertificationCriterionId(), CertificationResultRules.FUNCTIONALITY_TESTED)) {
            return certResult.getFunctionalitiesTested().stream()
                    .sorted(funcTestedComparator)
                    .collect(Collectors.toList());
        } else {
            return null;
        }
    }

    private List<CertificationResultConformanceMethod> getConformanceMethods(CertificationResultDetailsDTO certResult, CertificationResultRules certRules) {
        if (certRules.hasCertOption(certResult.getCertificationCriterionId(), CertificationResultRules.CONFORMANCE_METHOD)) {
            return certResult.getConformanceMethods().stream()
                    .map(item -> new CertificationResultConformanceMethod(item))
                    .sorted(cmComparator)
                    .collect(Collectors.toList());
        } else {
            return null;
        }
    }

    private List<CertificationResultTestProcedure> getTestProcedures(CertificationResultDetailsDTO certResult, CertificationResultRules certRules) {
        if (certRules.hasCertOption(certResult.getCertificationCriterionId(), CertificationResultRules.TEST_PROCEDURE)) {
            return certResult.getTestProcedures().stream()
                    .map(item -> new CertificationResultTestProcedure(item))
                    .sorted(testProcComparator)
                    .collect(Collectors.toList());
        } else {
            return null;
        }
    }


    private List<CertificationResultTestData> getTestData(CertificationResultDetailsDTO certResult, CertificationResultRules certRules) {
        if (certRules.hasCertOption(certResult.getCertificationCriterionId(), CertificationResultRules.TEST_DATA)) {
            return certResult.getTestData().stream()
                    .map(item -> new CertificationResultTestData(item))
                    .sorted(testDataComparator)
                    .collect(Collectors.toList());
        } else {
            return null;
        }
    }

    private List<CertificationResultTestTool> getTestTools(CertificationResultDetailsDTO certResult, CertificationResultRules certRules) {
        if (certRules.hasCertOption(certResult.getCertificationCriterionId(), CertificationResultRules.TEST_TOOLS_USED)) {
            return certResult.getTestTools().stream()
                    .sorted(testToolComparator)
                    .collect(Collectors.toList());
        } else {
            return null;
        }
    }

    private List<CertificationResultTestStandard> getTestStandards(CertificationResultDetailsDTO certResult, CertificationResultRules certRules) {
        if (certRules.hasCertOption(certResult.getCertificationCriterionId(), CertificationResultRules.STANDARDS_TESTED)) {
            return certResult.getTestStandards().stream()
                    .map(item -> new CertificationResultTestStandard(item))
                    .sorted(testStandardComparator)
                    .collect(Collectors.toList());
        } else {
            return null;
        }
    }

    private List<CertificationResultAdditionalSoftware> getAdditionalSoftware(CertificationResultDetailsDTO certResult, CertificationResultRules certRules) {
        if (certRules.hasCertOption(certResult.getCertificationCriterionId(), CertificationResultRules.ADDITIONAL_SOFTWARE)) {
            return certResult.getAdditionalSoftware().stream()
                    .map(item -> new CertificationResultAdditionalSoftware(item))
                    .sorted(asComparator)
                    .collect(Collectors.toList());
        } else {
            return null;
        }
    }

    public static String formatPrivacyAndSecurityFramework(String privacyAndSecurityFramework) {
        if (StringUtils.isEmpty(privacyAndSecurityFramework)) {
            return privacyAndSecurityFramework;
        }
        String privacyAndSecurityFrameworkFormatted = privacyAndSecurityFramework.replace(",",
                PRIVACY_SECURITY_FRAMEWORK_DELIMITER);
        StringBuilder result = new StringBuilder();
        String[] frameworks = privacyAndSecurityFrameworkFormatted.split(PRIVACY_SECURITY_FRAMEWORK_DELIMITER);
        for (int i = 0; i < frameworks.length; i++) {
            if (result.length() > 0) {
                result.append(PRIVACY_SECURITY_FRAMEWORK_DELIMITER);
            }
            result.append(frameworks[i].trim());
        }
        return result.toString();
    }
}
