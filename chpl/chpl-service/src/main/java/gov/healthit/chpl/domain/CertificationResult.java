package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import gov.healthit.chpl.api.deprecatedUsage.DeprecatedResponseField;
import gov.healthit.chpl.conformanceMethod.CertificationResultConformanceMethodComparator;
import gov.healthit.chpl.conformanceMethod.domain.CertificationResultConformanceMethod;
import gov.healthit.chpl.conformanceMethod.domain.ConformanceMethod;
import gov.healthit.chpl.criteriaattribute.testtool.CertificationResultTestTool;
import gov.healthit.chpl.criteriaattribute.testtool.CertificationResultTestToolComparator;
import gov.healthit.chpl.criteriaattribute.testtool.TestTool;
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
import gov.healthit.chpl.svap.domain.CertificationResultSvap;
import gov.healthit.chpl.svap.domain.CertificationResultSvapComparator;
import gov.healthit.chpl.svap.domain.Svap;
import gov.healthit.chpl.util.CertificationResultRules;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;

/**
 * Criteria to which a given listing attests.
 */
@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@Builder
@ToString
public class CertificationResult implements Serializable {
    private static final long serialVersionUID = -4917413876078419868L;
    public static final String PRIVACY_SECURITY_FRAMEWORK_DELIMITER = ";";

    @XmlTransient
    private Long id;

    /**
     * Whether or not this criteria was met.
     */
    @Schema(description = "Whether or not this criteria was met.")
    @XmlElement(required = true)
    private Boolean success;

    @XmlTransient
    @JsonIgnore
    private String successStr;

    /**
     * This variable indicates if the certification criteria was gap certified. It is a binary variable that takes true
     * or false value, and is applicable to 2014 and 2015 Edition.
     */
    @Schema(description = "This variable indicates if the certification criteria was gap certified. It is a binary variable that takes true "
            + "or false value, and is applicable to 2014 and 2015 Edition.")
    @XmlElement(required = false, nillable = true)
    private Boolean gap;

    @XmlTransient
    @JsonIgnore
    private String gapStr;

    @XmlTransient
    @JsonIgnore
    private Boolean hasAdditionalSoftware;

    @XmlTransient
    @JsonIgnore
    private String hasAdditionalSoftwareStr;

    /**
     * This variable indicates if the corresponding certification criteria was submitted for safety-enhanced design
     * attestation during certification testing. It is a binary variable that takes either true or false value, and is
     * only applicable to 2014 Edition.
     */
    @Schema(description = "This variable indicates if the corresponding certification criteria was submitted for safety-enhanced design "
            + "attestation during certification testing. It is a binary variable that takes either true or false value, and is "
            + "only applicable to 2014 Edition.")
    @XmlElement(required = false, nillable = true)
    private Boolean sed;

    /**
     * This variable indicates if the corresponding certification criteria was successfully tested for automated
     * numerator recording. It is applicable for the 2014 edition, and it is a binary variable that takes either true or
     * false value.
     */
    @Schema(description = "This variable indicates if the corresponding certification criteria was successfully tested for automated "
            + "numerator recording. It is applicable for the 2014 edition, and it is a binary variable that takes either true or "
            + "false value.")
    @XmlElement(required = false, nillable = true)
    private Boolean g1Success;

    /**
     * This variable indicates if the corresponding certification criteria was successfully tested for automated measure
     * calculation. It is a binary variable that takes either true or false, and is applicable to 2014 Edition.
     */
    @Schema(description = "This variable indicates if the corresponding certification criteria was successfully tested for automated measure "
            + "calculation. It is a binary variable that takes either true or false, and is applicable to 2014 Edition.")
    @XmlElement(required = false, nillable = true)
    private Boolean g2Success;

    /**
     * This variable indicates if the corresponding certification criteria has an attestation answer. It is a binary
     * value that takes either true or false, and is applicable to 2015 Edition.
     */
    @Schema(description = "This variable indicates if the corresponding certification criteria has an attestation answer. It is a binary "
            + "value that takes either true or false, and is applicable to 2015 Edition.")
    @XmlElement(required = false, nillable = true)
    private Boolean attestationAnswer;

    @XmlTransient
    private String attestationAnswerStr;

    /**
     * The hyperlink to access an application programming interface (API)'s documentation and terms of use. This
     * variable is applicable for only 2015 Edition. It is fully qualified URL which is reachable via web browser
     * validation and verification.
     */
    @Schema(description = "The hyperlink to access an application programming interface (API)'s documentation and terms of use. This "
            + "variable is applicable for only 2015 Edition. It is fully qualified URL which is reachable via web browser "
            + "validation and verification.")
    @XmlElement(required = false, nillable = true)
    private String apiDocumentation;

    /**
     * The hyperlink to access export documentation. This variable is applicable for only 2015 Edition. It is fully
     * qualified URL which is reachable via web browser validation and verification.
     */
    @Schema(description = "The hyperlink to access export documentation. This variable is applicable for only 2015 Edition. It is fully "
            + "qualified URL which is reachable via web browser validation and verification.")
    @XmlElement(required = false, nillable = true)
    private String exportDocumentation;

    /**
     * The hyperlink to access a documentation URL. This variable is applicable for only 2015 Edition. It is fully
     * qualified URL which is reachable via web browser validation and verification.
     */
    @Schema(description = "The hyperlink to access a documentation URL. This variable is applicable for only 2015 Edition. It is fully "
            + "qualified URL which is reachable via web browser validation and verification.")
    @XmlElement(required = false, nillable = true)
    private String documentationUrl;

    /**
     * The hyperlink to access Use Case(s). This variable is applicable for only 2015 Edition. It is fully qualified URL
     * which is reachable via web browser validation and verification.
     */
    @Schema(description = "The hyperlink to access Use Case(s). This variable is applicable for only 2015 Edition. It is fully qualified URL "
            + "which is reachable via web browser validation and verification.")
    @XmlElement(required = false, nillable = true)
    private String useCases;

    /**
     * The publicly accessible hyperlink to the list of service base URLs for a Health IT Module
     * certified to § 170.315(g)(10) that can be used by patients to access their electronic health
     * information. It is a fully qualified URL which is reachable via web browser validation and verification.
     */
    @Schema(description = "The publicly accessible hyperlink to the list of service base URLs for a Health IT Module "
            + "certified to § 170.315(g)(10) that can be used by patients to access their electronic health "
            + "information. It is a fully qualified URL which is reachable via web browser validation and verification.")
    @XmlElement(required = false, nillable = true)
    private String serviceBaseUrlList;

    /**
     * This variable explains the way in which each privacy and security criterion was addressed for the purposes of
     * certification. It is applicable for 2015 Edition and takes either of Approach 1 and Approach 2.
     */
    @Schema(description = "This variable explains the way in which each privacy and security criterion was addressed for the purposes of "
            + "certification. It is applicable for 2015 Edition and takes either of Approach 1 and Approach 2.")
    @XmlElement(required = false, nillable = true)
    private String privacySecurityFramework;

    @XmlTransient
    private List<ConformanceMethod> allowedConformanceMethods;

    @XmlTransient
    private List<Svap> allowedSvaps;

    @XmlTransient
    private List<OptionalStandard> allowedOptionalStandards;

    @Deprecated
    @DeprecatedResponseField(message = "This field is deprecated and will be removed. This data can be found via a GET request to the endpoint /test-tools.",
        removalDate = "2024-01-01")
    @XmlTransient
    private List<TestTool> allowedTestTools;

    /**
     * Any optional, alternative, ambulatory (2015 only), or inpatient (2015 only) capabilities within a certification
     * criterion to which the Health IT module was tested and certified. For example, within the 2015 certification
     * criteria 170.315(a), the optional functionality to include a 'reason for order' field should be denoted as
     * (a)(1)(ii). You can find a list of potential values in the 2014 or 2015 Functionality and Standards Reference
     * Tables. It is applicable for 2014 and 2015 Edition.
     */
    @Schema(description = "Any optional, alternative, ambulatory (2015 only), or inpatient (2015 only) capabilities within a certification "
            + "criterion to which the Health IT module was tested and certified. For example, within the 2015 certification "
            + "criteria 170.315(a), the optional functionality to include a 'reason for order' field should be denoted as "
            + "(a)(1)(ii). You can find a list of potential values in the 2014 or 2015 Functionality and Standards Reference "
            + "Tables. It is applicable for 2014 and 2015 Edition.")
    @XmlElementWrapper(name = "functionalitiesTested", nillable = true, required = false)
    @XmlElement(name = "functionalityTested")
    @Builder.Default
    private List<CertificationResultFunctionalityTested> functionalitiesTested = new ArrayList<CertificationResultFunctionalityTested>();

    /**
     * The methods used to evaluate compliance with the certification criterion.
     */
    @Schema(description = "The methods used to evaluate compliance with the certification criterion.")
    @XmlElementWrapper(name = "conformanceMethods", nillable = true, required = false)
    @XmlElement(name = "conformanceMethod")
    private List<CertificationResultConformanceMethod> conformanceMethods = new ArrayList<CertificationResultConformanceMethod>();

    /**
     * The test procedures used for the certification criteria
     */
    @Schema(description = "The test procedures used for the certification criteria")
    @XmlElementWrapper(name = "testProcedures", nillable = true, required = false)
    @XmlElement(name = "testProcedure")
    private List<CertificationResultTestProcedure> testProcedures = new ArrayList<CertificationResultTestProcedure>();

    /**
     * The versions of the test data being used for the certification criteria
     */
    @Schema(description = "The versions of the test data being used for the certification criteria")
    @XmlElementWrapper(name = "testDataList", nillable = true, required = false)
    @XmlElement(name = "testData")
    private List<CertificationResultTestData> testDataUsed = new ArrayList<CertificationResultTestData>();

    /**
     * This variable indicates if any additional software is relied upon by the Health IT Module to demonstrate its
     * compliance with a certification criterion or criteria. It is applicable for 2014 and 2015 Edition.
     */
    @Schema(description = "This variable indicates if any additional software is relied upon by the Health IT Module to demonstrate its "
            + "compliance with a certification criterion or criteria. It is applicable for 2014 and 2015 Edition.")
    @XmlElementWrapper(name = "additionalSoftwareList", nillable = true, required = false)
    @XmlElement(name = "additionalSoftware")
    private List<CertificationResultAdditionalSoftware> additionalSoftware = new ArrayList<CertificationResultAdditionalSoftware>();

    /**
     * An optional standard used to meet a certification criterion for 2015 Edition. You can find a list of potential
     * values in the 2015 Functionality and Standards Reference Tables. Allowed values are the corresponding
     * paragraph number for the standard within the regulation.
     */
    @Schema(description = "An optional standard used to meet a certification criterion for 2015 Edition. You can find a list of potential "
            + "values in the 2015 Functionality and Standards Reference Tables. Allowed values are the corresponding "
            + "paragraph number for the standard within the regulation.")
    @XmlElementWrapper(name = "optionalStandards", nillable = true, required = false)
    @XmlElement(name = "optionalStandard")
    @Builder.Default
    private List<CertificationResultOptionalStandard> optionalStandards = new ArrayList<CertificationResultOptionalStandard>();

    /**
     * A standard used to meet a certification criterion for 2014 and 2015 Edition. You can find a list of potential
     * values in the 2014 or 2015 Functionality and Standards Reference Tables. Allowed values are the corresponding
     * paragraph number for the standard within the regulation.
     */
    @Schema(description = "A standard used to meet a certification criterion for 2014 and 2015 Edition. You can find a list of potential "
            + "values in the 2014 or 2015 Functionality and Standards Reference Tables. Allowed values are the corresponding "
            + "paragraph number for the standard within the regulation.")
    @XmlElementWrapper(name = "testStandards", nillable = true, required = false)
    @XmlElement(name = "testStandard")
    @Builder.Default
    private List<CertificationResultTestStandard> testStandards = new ArrayList<CertificationResultTestStandard>();

    /**
     * The test tool used to certify the Health IT Module to the corresponding certification criteria Allowable values
     * are based on the NIST 2014 and 2015 Edition Test Tools. This variable is applicable for 2014 and 2015 Edition,
     * and allowable values are based on the NIST 2014 and 2015 Edition Test Tools: HL7 CDA Cancer Registry Reporting
     * Validation Tool, HL7v2 Immunization Test Suite, HL7v2 Syndromic Surveillance Test Suite, HL7v2 Electronic
     * Laboratory Reporting Validation Tool, Electronic Prescribing, HL7 CDA National Health Care Surveys Validator,
     * Edge Test Tool, 2015 Direct Certificate Discovery Tool, Cypress, HL7 v2 Electronic Laboratory Reporting (ELR)
     * Validation Tool, HL7 v2 Immunization Information System (IIS) Reporting Validation Tool, HL7 v2 Laboratory
     * Results Interface (LRI) Validation Tool, HL7 v2 Syndromic Surveillance Reporting Validation Tool
     */
    @Schema(description = "The test tool used to certify the Health IT Module to the corresponding certification criteria Allowable values "
            + "are based on the NIST 2014 and 2015 Edition Test Tools. This variable is applicable for 2014 and 2015 Edition, "
            + "and allowable values are based on the NIST 2014 and 2015 Edition Test Tools: HL7 CDA Cancer Registry Reporting "
            + "Validation Tool, HL7v2 Immunization Test Suite, HL7v2 Syndromic Surveillance Test Suite, HL7v2 Electronic "
            + "Laboratory Reporting Validation Tool, Electronic Prescribing, HL7 CDA National Health Care Surveys Validator, "
            + "Edge Test Tool, 2015 Direct Certificate Discovery Tool, Cypress, HL7 v2 Electronic Laboratory Reporting (ELR) "
            + "Validation Tool, HL7 v2 Immunization Information System (IIS) Reporting Validation Tool, HL7 v2 Laboratory "
            + "Results Interface (LRI) Validation Tool, HL7 v2 Syndromic Surveillance Reporting Validation Tool")
    @XmlElementWrapper(name = "testTools", nillable = true, required = false)
    @XmlElement(name = "testTool")
    @Builder.Default
    private List<CertificationResultTestTool> testToolsUsed = new ArrayList<CertificationResultTestTool>();

    /**
     * ONC has established the Standards Version Advancement Process (SVAP) to enable health IT developers’
     * ability to incorporate newer versions of Secretary-adopted standards and implementation specifications,
     * as part of the "Real World Testing" Condition and Maintenance of Certification requirement (§170.405)
     * of the 21st Century Cures Act
     */
    @Schema(description = "ONC has established the Standards Version Advancement Process (SVAP) to enable health IT developers’ "
            + "ability to incorporate newer versions of Secretary-adopted standards and implementation specifications, "
            + "as part of the \"Real World Testing\" Condition and Maintenance of Certification requirement (§170.405) "
            + "of the 21st Century Cures Act")
    @XmlElementWrapper(name = "svaps", nillable = true, required = false)
    @XmlElement(name = "svap")
    @Builder.Default
    private List<CertificationResultSvap> svaps = new ArrayList<CertificationResultSvap>();

    /**
     * Detailed information about the relevant certification criterion.
     */
    @Schema(description = "Detailed information about the relevant certification criterion.")
    @XmlElement(name = "criterion")
    private CertificationCriterion criterion;

    /**
     * This property exists solely to be able to deserialize listing activity events from very old data.
     * Our Activity Explorer classes sometimes need to look at the "number" and "title" fields
     * and deduce which criterion we were using so we need to be bale to read this value.
     * This property should not be visible in the generated XSD or any response from an API call.
     * Do not use it in any code unless you are specifically referencing legacy listing activity data.
     */
    @JsonProperty(access = Access.WRITE_ONLY)
    @XmlTransient
    private String number;

    @XmlTransient
    private CertificationResultSvapComparator svapComparator;
    @XmlTransient
    private CertificationResultOptionalStandardComparator osComparator;
    @XmlTransient
    private CertificationResultConformanceMethodComparator cmComparator;
    @XmlTransient
    private CertificationResultFunctionalityTestedComparator funcTestedComparator;
    @XmlTransient
    private CertificationResultTestProcedureComparator testProcComparator;
    @XmlTransient
    private CertificationResultTestDataComparator testDataComparator;
    @XmlTransient
    private CertificationResultTestToolComparator testToolComparator;
    @XmlTransient
    private CertificationResultTestStandardComparator testStandardComparator;
    @XmlTransient
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<CertificationResultConformanceMethod> getConformanceMethods() {
        return conformanceMethods;
    }

    public void setConformanceMethods(List<CertificationResultConformanceMethod> conformanceMethods) {
        this.conformanceMethods = conformanceMethods;
    }

    public List<CertificationResultTestProcedure> getTestProcedures() {
        return testProcedures;
    }

    public void setTestProcedures(List<CertificationResultTestProcedure> testProcedures) {
        this.testProcedures = testProcedures;
    }

    public Boolean isSuccess() {
        return success;
    }

    public void setSuccess(Boolean successful) {
        this.success = successful;
    }

    public List<CertificationResultAdditionalSoftware> getAdditionalSoftware() {
        return additionalSoftware;
    }

    public void setAdditionalSoftware(List<CertificationResultAdditionalSoftware> additionalSoftware) {
        this.additionalSoftware = additionalSoftware;
    }

    public Boolean isGap() {
        return gap;
    }

    public void setGap(Boolean gap) {
        this.gap = gap;
    }

    public Boolean isSed() {
        return sed;
    }

    public void setSed(Boolean sed) {
        this.sed = sed;
    }

    public Boolean isG1Success() {
        return g1Success;
    }

    public void setG1Success(Boolean g1Success) {
        this.g1Success = g1Success;
    }

    public Boolean isG2Success() {
        return g2Success;
    }

    public void setG2Success(Boolean g2Success) {
        this.g2Success = g2Success;
    }

    public List<CertificationResultOptionalStandard> getOptionalStandards() {
        return optionalStandards;
    }

    public void setOptionalStandards(List<CertificationResultOptionalStandard> optionalStandards) {
        this.optionalStandards = optionalStandards;
    }

    public List<CertificationResultTestTool> getTestToolsUsed() {
        return testToolsUsed;
    }

    public void setTestToolsUsed(List<CertificationResultTestTool> testToolsUsed) {
        this.testToolsUsed = testToolsUsed;
    }

    public List<CertificationResultTestStandard> getTestStandards() {
        return testStandards;
    }

    public void setTestStandards(List<CertificationResultTestStandard> testStandards) {
        this.testStandards = testStandards;
    }

    public List<CertificationResultTestData> getTestDataUsed() {
        return testDataUsed;
    }

    public void setTestDataUsed(List<CertificationResultTestData> testDataUsed) {
        this.testDataUsed = testDataUsed;
    }

    public List<CertificationResultFunctionalityTested> getFunctionalitiesTested() {
        return functionalitiesTested;
    }

    public void setFunctionalitiesTested(List<CertificationResultFunctionalityTested> functionalitiesTested) {
        this.functionalitiesTested = functionalitiesTested;
    }

    public String getApiDocumentation() {
        return apiDocumentation;
    }

    public void setApiDocumentation(String apiDocumentation) {
        this.apiDocumentation = apiDocumentation;
    }

    public Boolean getAttestationAnswer() {
        return attestationAnswer;
    }

    public void setAttestationAnswer(Boolean attestationAnswer) {
        this.attestationAnswer = attestationAnswer;
    }

    public String getExportDocumentation() {
        return exportDocumentation;
    }

    public void setExportDocumentation(String exportDocumentation) {
        this.exportDocumentation = exportDocumentation;
    }

    public String getDocumentationUrl() {
        return documentationUrl;
    }

    public void setDocumentationUrl(String documentationUrl) {
        this.documentationUrl = documentationUrl;
    }

    public String getUseCases() {
        return useCases;
    }

    public void setUseCases(String useCases) {
        this.useCases = useCases;
    }

    public String getServiceBaseUrlList() {
        return serviceBaseUrlList;
    }

    public void setServiceBaseUrlList(String serviceBaseUrlList) {
        this.serviceBaseUrlList = serviceBaseUrlList;
    }

    public String getPrivacySecurityFramework() {
        return privacySecurityFramework;
    }

    public void setPrivacySecurityFramework(String privacySecurityFramework) {
        this.privacySecurityFramework = privacySecurityFramework;
    }

    public List<ConformanceMethod> getAllowedConformanceMethods() {
        return allowedConformanceMethods;
    }

    public void setAllowedConformanceMethods(List<ConformanceMethod> allowedConformanceMethods) {
        this.allowedConformanceMethods = allowedConformanceMethods;
    }

    public CertificationCriterion getCriterion() {
        return criterion;
    }

    public void setCriterion(CertificationCriterion criterion) {
        this.criterion = criterion;
    }

    public String getNumber() {
        return this.number;
    }

    public void setNumber(String number) {
        this.number = number;
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

    public String getGapStr() {
        return gapStr;
    }

    public void setGapStr(String gapStr) {
        this.gapStr = gapStr;
    }

    public String getAttestationAnswerStr() {
        return attestationAnswerStr;
    }

    public void setAttestationAnswerStr(String attestationAnswerStr) {
        this.attestationAnswerStr = attestationAnswerStr;
    }

    public String getSuccessStr() {
        return successStr;
    }

    public void setSuccessStr(String successStr) {
        this.successStr = successStr;
    }

    public List<OptionalStandard> getAllowedOptionalStandards() {
        return allowedOptionalStandards;
    }

    public void setAllowedOptionalStandards(List<OptionalStandard> allowedOptionalStandards) {
        this.allowedOptionalStandards = allowedOptionalStandards;
    }

    public List<Svap> getAllowedSvaps() {
        return allowedSvaps;
    }

    public void setAllowedSvaps(List<Svap> allowedSvaps) {
        this.allowedSvaps = allowedSvaps;
    }

    public List<TestTool> getAllowedTestTools() {
        return allowedTestTools;
    }

    public void setAllowedTestTools(List<TestTool> allowedTestTools) {
        this.allowedTestTools = allowedTestTools;
    }

    public List<CertificationResultSvap> getSvaps() {
        return svaps;
    }

    public void setSvaps(List<CertificationResultSvap> svaps) {
        this.svaps = svaps;
    }

    public Boolean getHasAdditionalSoftware() {
        return hasAdditionalSoftware;
    }

    public void setHasAdditionalSoftware(Boolean hasAdditionalSoftware) {
        this.hasAdditionalSoftware = hasAdditionalSoftware;
    }

    public String getHasAdditionalSoftwareStr() {
        return hasAdditionalSoftwareStr;
    }

    public void setHasAdditionalSoftwareStr(String hasAdditionalSoftwareStr) {
        this.hasAdditionalSoftwareStr = hasAdditionalSoftwareStr;
    }
}
