package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.dto.CertificationResultDetailsDTO;
import gov.healthit.chpl.svap.domain.CertificationResultSvap;
import gov.healthit.chpl.svap.domain.Svap;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Singular;

/**
 * Criteria to which a given listing attests.
 */
@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@AllArgsConstructor
public class CertificationResult implements Serializable {
    private static final long serialVersionUID = -4917413876078419868L;
    public static final String PRIVACY_SECURITY_FRAMEWORK_DELIMITER = ";";

    @XmlTransient
    private Long id;

    /**
     * Criteria number, i.e. 170.314 (a)(1). This field is deprecated and will be removed in a future release. Users
     * should reference criterion.number instead.
     */
    @Deprecated
    @XmlElement(required = true)
    private String number;

    /**
     * Short description of the criteria. This field is deprecated and will be removed in a future release. Users should
     * reference criterion.title instead.
     */
    @Deprecated
    @XmlElement(required = true)
    private String title;

    /**
     * Whether or not this criteria was met.
     */
    @XmlElement(required = true)
    private Boolean success;

    @XmlTransient
    @JsonIgnore
    private String successStr;

    /**
     * This variable indicates if the certification criteria was gap certified. It is a binary variable that takes true
     * or false value, and is applicable to 2014 and 2015 Edition.
     */
    @XmlElement(required = false, nillable = true)
    private Boolean gap;

    @XmlTransient
    @JsonIgnore
    private String gapStr;

    /**
     * This variable indicates if the corresponding certification criteria was submitted for safety-enhanced design
     * attestation during certification testing. It is a binary variable that takes either true or false value, and is
     * only applicable to 2014 Edition.
     */
    @XmlElement(required = false, nillable = true)
    private Boolean sed;

    /**
     * This variable indicates if the corresponding certification criteria was successfully tested for automated
     * numerator recording. It is applicable for the 2014 edition, and it is a binary variable that takes either true or
     * false value.
     */
    @XmlElement(required = false, nillable = true)
    private Boolean g1Success;

    /**
     * This variable indicates if the corresponding certification criteria was successfully tested for automated measure
     * calculation. It is a binary variable that takes either true or false, and is applicable to 2014 Edition.
     */
    @XmlElement(required = false, nillable = true)
    private Boolean g2Success;

    /**
     * This variable indicates if the corresponding certification criteria has an attestation answer. It is a binary
     * value that takes either true or false, and is applicable to 2015 Edition.
     */
    @XmlElement(required = false, nillable = true)
    private Boolean attestationAnswer;

    @XmlTransient
    private String attestationAnswerStr;

    /**
     * The hyperlink to access an application programming interface (API)'s documentation and terms of use. This
     * variable is applicable for only 2015 Edition. It is fully qualified URL which is reachable via web browser
     * validation and verification.
     */
    @XmlElement(required = false, nillable = true)
    private String apiDocumentation;

    /**
     * The hyperlink to access export documentation. This variable is applicable for only 2015 Edition. It is fully
     * qualified URL which is reachable via web browser validation and verification.
     */
    @XmlElement(required = false, nillable = true)
    private String exportDocumentation;

    /**
     * The hyperlink to access a documentation URL. This variable is applicable for only 2015 Edition. It is fully
     * qualified URL which is reachable via web browser validation and verification.
     */
    @XmlElement(required = false, nillable = true)
    private String documentationUrl;

    /**
     * The hyperlink to access Use Case(s). This variable is applicable for only 2015 Edition. It is fully qualified URL
     * which is reachable via web browser validation and verification.
     */
    @XmlElement(required = false, nillable = true)
    private String useCases;

    /**
     * The publicly accessible hyperlink to the list of service base URLs for a Health IT Module
     * certified to § 170.315(g)(10) that can be used by patients to access their electronic health
     * information. It is a fully qualified URL which is reachable via web browser validation and verification.
     */
    @XmlElement(required = false, nillable = true)
    private String serviceBaseUrlList;

    /**
     * This variable explains the way in which each privacy and security criterion was addressed for the purposes of
     * certification. It is applicable for 2015 Edition and takes either of Approach 1 and Approach 2.
     */
    @XmlElement(required = false, nillable = true)
    private String privacySecurityFramework;

    @XmlTransient
    private List<TestFunctionality> allowedTestFunctionalities;

    @XmlTransient
    private List<Svap> allowedSvaps;

    /**
     * Any optional, alternative, ambulatory (2015 only), or inpatient (2015 only) capabilities within a certification
     * criterion to which the Health IT module was tested and certified. For example, within the 2015 certification
     * criteria 170.315(a), the optional functionality to include a 'reason for order' field should be denoted as
     * (a)(1)(ii). You can find a list of potential values in the 2014 or 2015 Functionality and Standards Reference
     * Tables. It is applicable for 2014 and 2015 Edition.
     */
    @XmlElementWrapper(name = "testFunctionalityList", nillable = true, required = false)
    @XmlElement(name = "testFunctionality")
    @Singular("testFunctionalitySingle")
    private List<CertificationResultTestFunctionality> testFunctionality = new ArrayList<CertificationResultTestFunctionality>();

    /**
     * The test procedures used for the certification criteria
     */
    @XmlElementWrapper(name = "testProcedures", nillable = true, required = false)
    @XmlElement(name = "testProcedure")
    private List<CertificationResultTestProcedure> testProcedures = new ArrayList<CertificationResultTestProcedure>();

    /**
     * The versions of the test data being used for the certification criteria
     */
    @XmlElementWrapper(name = "testDataList", nillable = true, required = false)
    @XmlElement(name = "testData")
    private List<CertificationResultTestData> testDataUsed = new ArrayList<CertificationResultTestData>();

    /**
     * This variable indicates if any additional software is relied upon by the Health IT Module to demonstrate its
     * compliance with a certification criterion or criteria. It is applicable for 2014 and 2015 Edition.
     */
    @XmlElementWrapper(name = "additionalSoftwareList", nillable = true, required = false)
    @XmlElement(name = "additionalSoftware")
    private List<CertificationResultAdditionalSoftware> additionalSoftware = new ArrayList<CertificationResultAdditionalSoftware>();

    /**
     * A standard used to meet a certification criterion for 2014 and 2015 Edition. You can find a list of potential
     * values in the 2014 or 2015 Functionality and Standards Reference Tables. Allowed values are the corresponding
     * paragraph number for the standard within the regulation.
     */
    @XmlElementWrapper(name = "testStandards", nillable = true, required = false)
    @XmlElement(name = "testStandard")
    @Singular
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
    @XmlElementWrapper(name = "testTools", nillable = true, required = false)
    @XmlElement(name = "testTool")
    private List<CertificationResultTestTool> testToolsUsed = new ArrayList<CertificationResultTestTool>();

    /**
     * ONC has established the Standards Version Advancement Process (SVAP) to enable health IT developers’
     * ability to incorporate newer versions of Secretary-adopted standards and implementation specifications,
     * as part of the "Real World Testing" Condition and Maintenance of Certification requirement (§170.405)
     * of the 21st Century Cures Act
     */
    @XmlElementWrapper(name = "svaps", nillable = true, required = false)
    @XmlElement(name = "svap")
    @Singular
    private List<CertificationResultSvap> svaps = new ArrayList<CertificationResultSvap>();

    /**
     * Detailed information about the relevant certification criterion.
     */
    @XmlElement(name = "criterion")
    private CertificationCriterion criterion;

    public CertificationResult() {
    }

    public CertificationResult(final CertificationResultDetailsDTO certResult) {
        this();
        this.setId(certResult.getId());
        this.setNumber(certResult.getNumber());
        this.setSuccess(certResult.getSuccess());
        this.setTitle(certResult.getTitle());
        this.setGap(certResult.getGap() == null ? Boolean.FALSE : certResult.getGap());
        this.setSed(certResult.getSed() == null ? Boolean.FALSE : certResult.getSed());
        this.setG1Success(certResult.getG1Success() == null ? Boolean.FALSE : certResult.getG1Success());
        this.setG2Success(certResult.getG2Success() == null ? Boolean.FALSE : certResult.getG2Success());
        this.setAttestationAnswer(certResult.getAttestationAnswer() == null ? Boolean.FALSE : certResult.getAttestationAnswer());
        this.setApiDocumentation(certResult.getApiDocumentation());
        this.setExportDocumentation(certResult.getExportDocumentation());
        this.setDocumentationUrl(certResult.getDocumentationUrl());
        this.setUseCases(certResult.getUseCases());
        this.setPrivacySecurityFramework(certResult.getPrivacySecurityFramework());
        if (certResult.getCriterion() != null) {
            this.criterion = new CertificationCriterion(certResult.getCriterion());
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public List<CertificationResultTestProcedure> getTestProcedures() {
        return testProcedures;
    }

    public void setTestProcedures(final List<CertificationResultTestProcedure> testProcedures) {
        this.testProcedures = testProcedures;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(final String number) {
        this.number = number;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public Boolean isSuccess() {
        return success;
    }

    public void setSuccess(final Boolean successful) {
        this.success = successful;
    }

    public List<CertificationResultAdditionalSoftware> getAdditionalSoftware() {
        return additionalSoftware;
    }

    public void setAdditionalSoftware(final List<CertificationResultAdditionalSoftware> additionalSoftware) {
        this.additionalSoftware = additionalSoftware;
    }

    public Boolean isGap() {
        return gap;
    }

    public void setGap(final Boolean gap) {
        this.gap = gap;
    }

    public Boolean isSed() {
        return sed;
    }

    public void setSed(final Boolean sed) {
        this.sed = sed;
    }

    public Boolean isG1Success() {
        return g1Success;
    }

    public void setG1Success(final Boolean g1Success) {
        this.g1Success = g1Success;
    }

    public Boolean isG2Success() {
        return g2Success;
    }

    public void setG2Success(final Boolean g2Success) {
        this.g2Success = g2Success;
    }

    public List<CertificationResultTestTool> getTestToolsUsed() {
        return testToolsUsed;
    }

    public void setTestToolsUsed(final List<CertificationResultTestTool> testToolsUsed) {
        this.testToolsUsed = testToolsUsed;
    }

    public List<CertificationResultTestStandard> getTestStandards() {
        return testStandards;
    }

    public void setTestStandards(final List<CertificationResultTestStandard> testStandards) {
        this.testStandards = testStandards;
    }

    public List<CertificationResultTestData> getTestDataUsed() {
        return testDataUsed;
    }

    public void setTestDataUsed(final List<CertificationResultTestData> testDataUsed) {
        this.testDataUsed = testDataUsed;
    }

    public List<CertificationResultTestFunctionality> getTestFunctionality() {
        return testFunctionality;
    }

    public void setTestFunctionality(final List<CertificationResultTestFunctionality> testFunctionality) {
        this.testFunctionality = testFunctionality;
    }

    public String getApiDocumentation() {
        return apiDocumentation;
    }

    public void setApiDocumentation(final String apiDocumentation) {
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

    public void setPrivacySecurityFramework(final String privacySecurityFramework) {
        this.privacySecurityFramework = privacySecurityFramework;
    }

    public List<TestFunctionality> getAllowedTestFunctionalities() {
        return allowedTestFunctionalities;
    }

    public void setAllowedTestFunctionalities(final List<TestFunctionality> testFunctionalities) {
        this.allowedTestFunctionalities = testFunctionalities;
    }

    public CertificationCriterion getCriterion() {
        return criterion;
    }

    public void setCriterion(final CertificationCriterion criterion) {
        this.criterion = criterion;
    }

    public static String formatPrivacyAndSecurityFramework(final String privacyAndSecurityFramework) {
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

    public List<Svap> getAllowedSvaps() {
        return allowedSvaps;
    }

    public void setAllowedSvaps(List<Svap> allowedSvaps) {
        this.allowedSvaps = allowedSvaps;
    }

    public List<CertificationResultSvap> getSvaps() {
        return svaps;
    }

    public void setSvaps(List<CertificationResultSvap> svaps) {
        this.svaps = svaps;
    }
}
