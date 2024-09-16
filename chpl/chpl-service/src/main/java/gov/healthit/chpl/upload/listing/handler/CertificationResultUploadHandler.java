package gov.healthit.chpl.upload.listing.handler;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.codeset.CertificationResultCodeSet;
import gov.healthit.chpl.codeset.CodeSet;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.functionalitytested.CertificationResultFunctionalityTested;
import gov.healthit.chpl.functionalitytested.FunctionalityTested;
import gov.healthit.chpl.optionalStandard.domain.CertificationResultOptionalStandard;
import gov.healthit.chpl.optionalStandard.domain.OptionalStandard;
import gov.healthit.chpl.standard.CertificationResultStandard;
import gov.healthit.chpl.standard.Standard;
import gov.healthit.chpl.svap.domain.CertificationResultSvap;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import gov.healthit.chpl.upload.listing.ListingUploadHeadingUtil.Heading;
import jakarta.validation.ValidationException;

@Component("certificationResultUploadHandler")
public class CertificationResultUploadHandler {
    private CertificationCriterionUploadHandler criterionHandler;
    private AdditionalSoftwareUploadHandler additionalSoftwareHandler;
    private ConformanceMethodUploadHandler conformanceMethodHandler;
    private TestToolUploadHandler testToolHandler;
    private TestDataUploadHandler testDataHandler;
    private ListingUploadHandlerUtil uploadUtil;

    @Autowired
    public CertificationResultUploadHandler(CertificationCriterionUploadHandler criterionHandler,
            AdditionalSoftwareUploadHandler additionalSoftwareHandler,
            ConformanceMethodUploadHandler conformanceMethodHandler,
            TestToolUploadHandler testToolHandler,
            TestDataUploadHandler testDataHandler,
            ListingUploadHandlerUtil uploadUtil) {
        this.criterionHandler = criterionHandler;
        this.additionalSoftwareHandler = additionalSoftwareHandler;
        this.conformanceMethodHandler = conformanceMethodHandler;
        this.testToolHandler = testToolHandler;
        this.testDataHandler = testDataHandler;
        this.uploadUtil = uploadUtil;
    }

    public CertificationResult parseAsCertificationResult(CSVRecord certHeadingRecord, List<CSVRecord> certResultRecords,
            CertifiedProductSearchDetails listing)
        throws ValidationException {
        CertificationResult certResult = CertificationResult.builder()
                .criterion(criterionHandler.handle(certHeadingRecord, listing))
                .successStr(parseSuccessStr(certHeadingRecord, certResultRecords))
                .success(parseSuccess(certHeadingRecord, certResultRecords))
                .gap(parseGap(certHeadingRecord, certResultRecords))
                .gapStr(parseGapStr(certHeadingRecord, certResultRecords))
                .hasAdditionalSoftware(parseHasAdditionalSoftware(certHeadingRecord, certResultRecords))
                .hasAdditionalSoftwareStr(parseHasAdditionalSoftwareStr(certHeadingRecord, certResultRecords))
                .privacySecurityFramework(parsePrivacyAndSecurityFramework(certHeadingRecord, certResultRecords))
                .functionalitiesTested(parseFunctionalitiesTested(certHeadingRecord, certResultRecords))
                .optionalStandards(parseOptionalStandards(certHeadingRecord, certResultRecords))
                .additionalSoftware(additionalSoftwareHandler.handle(certHeadingRecord, certResultRecords))
                .testDataUsed(testDataHandler.handle(certHeadingRecord, certResultRecords))
                .conformanceMethods(conformanceMethodHandler.handle(certHeadingRecord, certResultRecords))
                .testToolsUsed(testToolHandler.handle(certHeadingRecord, certResultRecords))
                .exportDocumentation(parseExportDocumentation(certHeadingRecord, certResultRecords))
                .attestationAnswer(parseAttestationAnswer(certHeadingRecord, certResultRecords))
                .attestationAnswerStr(parseAttestationAnswerStr(certHeadingRecord, certResultRecords))
                .documentationUrl(parseDocumentationUrl(certHeadingRecord, certResultRecords))
                .useCases(parseUseCases(certHeadingRecord, certResultRecords))
                .serviceBaseUrlList(parseServiceBaseUrlList(certHeadingRecord, certResultRecords))
                .riskManagementSummaryInformation(parseRiskManagementSummaryInformation(certHeadingRecord, certResultRecords))
                .apiDocumentation(parseApiDocumentation(certHeadingRecord, certResultRecords))
                .svaps(parseSvaps(certHeadingRecord, certResultRecords))
                .standards(parseStandards(certHeadingRecord, certResultRecords))
                .codeSets(parseCodeSets(certHeadingRecord, certResultRecords))
            .build();
        return certResult;
    }

    //the first heading in a set of criteria fields must be the criteria itself.
    //this gives us the criteria number/cures designation and the success value
    private Boolean parseSuccess(CSVRecord certHeadingRecord, List<CSVRecord> certResultRecords) {
        String successField = uploadUtil.parseSingleRowFieldAtIndex(0, certHeadingRecord, certResultRecords);
        Boolean success = null;
        try {
            success = uploadUtil.parseBoolean(successField);
        } catch (Exception ex) {
        }
        return success;
    }

    private String parseSuccessStr(CSVRecord certHeadingRecord, List<CSVRecord> certResultRecords) {
        return uploadUtil.parseSingleRowFieldAtIndex(0, certHeadingRecord, certResultRecords);
    }

    private Boolean parseGap(CSVRecord certHeadingRecord, List<CSVRecord> certResultRecords) {
        Boolean result = null;
        try {
            result = uploadUtil.parseSingleRowFieldAsBoolean(Heading.GAP, certHeadingRecord, certResultRecords);
        } catch (Exception e) {
        }
        return result;
    }

    private String parseGapStr(CSVRecord certHeadingRecord, List<CSVRecord> certResultRecords) {
        return uploadUtil.parseSingleRowField(Heading.GAP, certHeadingRecord, certResultRecords);
    }

    private Boolean parseHasAdditionalSoftware(CSVRecord certHeadingRecord, List<CSVRecord> certResultRecords) {
        Boolean result = null;
        try {
            result = uploadUtil.parseSingleRowFieldAsBoolean(Heading.HAS_ADDITIONAL_SOFTWARE, certHeadingRecord, certResultRecords);
        } catch (Exception e) {
        }
        return result;
    }

    private String parseHasAdditionalSoftwareStr(CSVRecord certHeadingRecord, List<CSVRecord> certResultRecords) {
        return uploadUtil.parseSingleRowField(Heading.HAS_ADDITIONAL_SOFTWARE, certHeadingRecord, certResultRecords);
    }

    private String parsePrivacyAndSecurityFramework(CSVRecord certHeadingRecord, List<CSVRecord> certResultRecords) {
        return uploadUtil.parseSingleRowField(Heading.PRIVACY_AND_SECURITY, certHeadingRecord, certResultRecords);
    }

    private String parseExportDocumentation(CSVRecord certHeadingRecord, List<CSVRecord> certResultRecords) {
        return uploadUtil.parseSingleRowField(Heading.EXPORT_DOCUMENTATION, certHeadingRecord, certResultRecords);
    }

    private Boolean parseAttestationAnswer(CSVRecord certHeadingRecord, List<CSVRecord> certResultRecords) {
        Boolean result = null;
        try {
            result = uploadUtil.parseSingleRowFieldAsBoolean(Heading.ATTESTATION_ANSWER, certHeadingRecord, certResultRecords);
        } catch (Exception e) {
        }
        return result;
    }

    private String parseAttestationAnswerStr(CSVRecord certHeadingRecord, List<CSVRecord> certResultRecords) {
        return uploadUtil.parseSingleRowField(Heading.ATTESTATION_ANSWER, certHeadingRecord, certResultRecords);
    }

    private String parseDocumentationUrl(CSVRecord certHeadingRecord, List<CSVRecord> certResultRecords) {
        return uploadUtil.parseSingleRowField(Heading.DOCUMENTATION_URL, certHeadingRecord, certResultRecords);
    }

    private String parseUseCases(CSVRecord certHeadingRecord, List<CSVRecord> certResultRecords) {
        return uploadUtil.parseSingleRowField(Heading.USE_CASES, certHeadingRecord, certResultRecords);
    }

    private String parseServiceBaseUrlList(CSVRecord certHeadingRecord, List<CSVRecord> certResultRecords) {
        return uploadUtil.parseSingleRowField(Heading.SERVICE_BASE_URL_LIST, certHeadingRecord, certResultRecords);
    }

    private String parseRiskManagementSummaryInformation(CSVRecord certHeadingRecord, List<CSVRecord> certResultRecords) {
        return uploadUtil.parseSingleRowField(Heading.RISK_MANAGEMENT_SUMMARY_INFORMATION, certHeadingRecord, certResultRecords);
    }

    private String parseApiDocumentation(CSVRecord certHeadingRecord, List<CSVRecord> certResultRecords) {
        return uploadUtil.parseSingleRowField(Heading.API_DOCUMENTATION_LINK, certHeadingRecord, certResultRecords);
    }

    private List<CertificationResultFunctionalityTested> parseFunctionalitiesTested(CSVRecord certHeadingRecord, List<CSVRecord> certResultRecords) {
        List<CertificationResultFunctionalityTested> functionalitiesTested = new ArrayList<CertificationResultFunctionalityTested>();
        List<String> functionalitiesTestedNames = uploadUtil.parseMultiRowFieldWithoutEmptyValues(
                Heading.FUNCTIONALITIES_TESTED, certHeadingRecord, certResultRecords);
        if (functionalitiesTestedNames != null && functionalitiesTestedNames.size() > 0) {
            functionalitiesTestedNames.stream().forEach(functionalityTestedName -> {
                CertificationResultFunctionalityTested functionalityTested = CertificationResultFunctionalityTested.builder()
                        .functionalityTested(FunctionalityTested.builder()
                                .regulatoryTextCitation(functionalityTestedName)
                                .build())
                        .build();
                functionalitiesTested.add(functionalityTested);
            });
        }
        return functionalitiesTested;
    }

    private List<CertificationResultOptionalStandard> parseOptionalStandards(
            CSVRecord certHeadingRecord, List<CSVRecord> certResultRecords) {
        List<CertificationResultOptionalStandard> optionalStandards = new ArrayList<CertificationResultOptionalStandard>();
            List<String> optionalStandardNames = uploadUtil.parseMultiRowFieldWithoutEmptyValues(
                    Heading.OPTIONAL_STANDARD, certHeadingRecord, certResultRecords);
            if (!CollectionUtils.isEmpty(optionalStandardNames)) {
                optionalStandardNames.stream().forEach(optionalStandardName -> {
                    CertificationResultOptionalStandard optionalStandard = CertificationResultOptionalStandard.builder()
                            .optionalStandard(OptionalStandard.builder()
                                    .build())
                            .userEnteredValue(optionalStandardName)
                            .build();
                    optionalStandards.add(optionalStandard);
                });
        }
        return optionalStandards;
    }

    private List<CertificationResultSvap> parseSvaps(CSVRecord certHeadingRecord, List<CSVRecord> certResultRecords) {
        List<CertificationResultSvap> svaps = new ArrayList<CertificationResultSvap>();
            List<String> regulatoryTextCitations = uploadUtil.parseMultiRowFieldWithoutEmptyValues(
                    Heading.SVAP_REG_TEXT, certHeadingRecord, certResultRecords);
            if (!CollectionUtils.isEmpty(regulatoryTextCitations)) {
                regulatoryTextCitations.stream().forEach(regulatoryTextCitation -> {
                    CertificationResultSvap svap = CertificationResultSvap.builder()
                            .regulatoryTextCitation(regulatoryTextCitation)
                            .build();
                    svaps.add(svap);
                });
        }
        return svaps;
    }

    private List<CertificationResultStandard> parseStandards(CSVRecord certHeadingRecord, List<CSVRecord> certResultRecords) {
        List<CertificationResultStandard> standards = new ArrayList<CertificationResultStandard>();
            List<String> regulatoryTextCitations = uploadUtil.parseMultiRowFieldWithoutEmptyValues(
                    Heading.STANDARD, certHeadingRecord, certResultRecords);
            if (!CollectionUtils.isEmpty(regulatoryTextCitations)) {
                regulatoryTextCitations.stream().forEach(regulatoryTextCitation -> {
                    CertificationResultStandard standard = CertificationResultStandard.builder()
                            .standard(Standard.builder()
                                    .regulatoryTextCitation(regulatoryTextCitation)
                                    .build())
                            .build();
                    standards.add(standard);
                });
        }
        return standards;
    }

    private List<CertificationResultCodeSet> parseCodeSets(CSVRecord certHeadingRecord, List<CSVRecord> certResultRecords) {
        List<CertificationResultCodeSet> codeSets = new ArrayList<CertificationResultCodeSet>();
            List<String> codeSetsText = uploadUtil.parseMultiRowFieldWithoutEmptyValues(
                    Heading.CODE_SET, certHeadingRecord, certResultRecords);
            if (!CollectionUtils.isEmpty(codeSetsText)) {
                codeSetsText.stream().forEach(codeSetText -> {
                    CertificationResultCodeSet codeSet = CertificationResultCodeSet.builder()
                            .codeSet(CodeSet.builder()
                                    .userEnteredName(codeSetText)
                                    .build())
                            .build();
                    codeSets.add(codeSet);
                });
        }
        return codeSets;
    }
}
