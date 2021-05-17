package gov.healthit.chpl.upload.listing.handler;

import java.util.ArrayList;
import java.util.List;

import javax.validation.ValidationException;

import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestFunctionality;
import gov.healthit.chpl.domain.CertificationResultTestStandard;
import gov.healthit.chpl.upload.listing.Headings;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;

@Component("certificationResultUploadHandler")
public class CertificationResultUploadHandler {
    private CertificationCriterionUploadHandler criterionHandler;
    private AdditionalSoftwareUploadHandler additionalSoftwareHandler;
    private TestProcedureUploadHandler testProcedureHandler;
    private TestToolUploadHandler testToolHandler;
    private TestDataUploadHandler testDataHandler;
    private ListingUploadHandlerUtil uploadUtil;

    @Autowired
    public CertificationResultUploadHandler(CertificationCriterionUploadHandler criterionHandler,
            AdditionalSoftwareUploadHandler additionalSoftwareHandler,
            TestProcedureUploadHandler testProcedureHandler,
            TestToolUploadHandler testToolHandler,
            TestDataUploadHandler testDataHandler,
            ListingUploadHandlerUtil uploadUtil) {
        this.criterionHandler = criterionHandler;
        this.additionalSoftwareHandler = additionalSoftwareHandler;
        this.testProcedureHandler = testProcedureHandler;
        this.testToolHandler = testToolHandler;
        this.testDataHandler = testDataHandler;
        this.uploadUtil = uploadUtil;
    }

    public CertificationResult parseAsCertificationResult(CSVRecord certHeadingRecord, List<CSVRecord> certResultRecords)
        throws ValidationException {
        CertificationResult certResult = CertificationResult.builder()
                .criterion(criterionHandler.handle(certHeadingRecord))
                .successStr(parseSuccessStr(certHeadingRecord, certResultRecords))
                .success(parseSuccess(certHeadingRecord, certResultRecords))
                .gap(parseGap(certHeadingRecord, certResultRecords))
                .gapStr(parseGapStr(certHeadingRecord, certResultRecords))
                .privacySecurityFramework(parsePrivacyAndSecurityFramework(certHeadingRecord, certResultRecords))
                .testFunctionality(parseTestFunctionalities(certHeadingRecord, certResultRecords))
                .testStandards(parseTestStandards(certHeadingRecord, certResultRecords))
                .additionalSoftware(additionalSoftwareHandler.handle(certHeadingRecord, certResultRecords))
                .testDataUsed(testDataHandler.handle(certHeadingRecord, certResultRecords))
                .testProcedures(testProcedureHandler.handle(certHeadingRecord, certResultRecords))
                .testToolsUsed(testToolHandler.handle(certHeadingRecord, certResultRecords))
                .exportDocumentation(parseExportDocumentation(certHeadingRecord, certResultRecords))
                .attestationAnswer(parseAttestationAnswer(certHeadingRecord, certResultRecords))
                .attestationAnswerStr(parseAttestationAnswerStr(certHeadingRecord, certResultRecords))
                .documentationUrl(parseDocumentationUrl(certHeadingRecord, certResultRecords))
                .useCases(parseUseCases(certHeadingRecord, certResultRecords))
                .serviceBaseUrlList(parseServiceBaseUrlList(certHeadingRecord, certResultRecords))
                .apiDocumentation(parseApiDocumentation(certHeadingRecord, certResultRecords))
            .build();

        if (certResult.getCriterion() != null) {
            certResult.setNumber(certResult.getCriterion().getNumber());
            certResult.setTitle(certResult.getCriterion().getTitle());
        }
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
            result = uploadUtil.parseSingleRowFieldAsBoolean(Headings.GAP, certHeadingRecord, certResultRecords);
        } catch (Exception e) {
        }
        return result;
    }

    private String parseGapStr(CSVRecord certHeadingRecord, List<CSVRecord> certResultRecords) {
        return uploadUtil.parseSingleRowField(Headings.GAP, certHeadingRecord, certResultRecords);
    }

    private String parsePrivacyAndSecurityFramework(CSVRecord certHeadingRecord, List<CSVRecord> certResultRecords) {
        return uploadUtil.parseSingleRowField(Headings.PRIVACY_AND_SECURITY, certHeadingRecord, certResultRecords);
    }

    private String parseExportDocumentation(CSVRecord certHeadingRecord, List<CSVRecord> certResultRecords) {
        return uploadUtil.parseSingleRowField(Headings.EXPORT_DOCUMENTATION, certHeadingRecord, certResultRecords);
    }

    private Boolean parseAttestationAnswer(CSVRecord certHeadingRecord, List<CSVRecord> certResultRecords) {
        Boolean result = null;
        try {
            result = uploadUtil.parseSingleRowFieldAsBoolean(Headings.ATTESTATION_ANSWER, certHeadingRecord, certResultRecords);
        } catch (Exception e) {
        }
        return result;
    }

    private String parseAttestationAnswerStr(CSVRecord certHeadingRecord, List<CSVRecord> certResultRecords) {
        return uploadUtil.parseSingleRowField(Headings.ATTESTATION_ANSWER, certHeadingRecord, certResultRecords);
    }

    private String parseDocumentationUrl(CSVRecord certHeadingRecord, List<CSVRecord> certResultRecords) {
        return uploadUtil.parseSingleRowField(Headings.DOCUMENTATION_URL, certHeadingRecord, certResultRecords);
    }

    private String parseUseCases(CSVRecord certHeadingRecord, List<CSVRecord> certResultRecords) {
        return uploadUtil.parseSingleRowField(Headings.USE_CASES, certHeadingRecord, certResultRecords);
    }

    private String parseServiceBaseUrlList(CSVRecord certHeadingRecord, List<CSVRecord> certResultRecords) {
        return uploadUtil.parseSingleRowField(Headings.SERVICE_BASE_URL_LIST, certHeadingRecord, certResultRecords);
    }

    private String parseApiDocumentation(CSVRecord certHeadingRecord, List<CSVRecord> certResultRecords) {
        return uploadUtil.parseSingleRowField(Headings.API_DOCUMENTATION_LINK, certHeadingRecord, certResultRecords);
    }

    private List<CertificationResultTestFunctionality> parseTestFunctionalities(
            CSVRecord certHeadingRecord, List<CSVRecord> certResultRecords) {
        List<CertificationResultTestFunctionality> testFunctionalities = new ArrayList<CertificationResultTestFunctionality>();
        List<String> testFuncNames = uploadUtil.parseMultiRowFieldWithoutEmptyValues(
                Headings.TEST_FUNCTIONALITY, certHeadingRecord, certResultRecords);
        if (testFuncNames != null && testFuncNames.size() > 0) {
            testFuncNames.stream().forEach(testFuncName -> {
                CertificationResultTestFunctionality testFunc = CertificationResultTestFunctionality.builder()
                        .name(testFuncName)
                        .build();
                testFunctionalities.add(testFunc);
            });
        }
        return testFunctionalities;
    }

    private List<CertificationResultTestStandard> parseTestStandards(
            CSVRecord certHeadingRecord, List<CSVRecord> certResultRecords) {
        List<CertificationResultTestStandard> testStandards = new ArrayList<CertificationResultTestStandard>();
        List<String> testStandardNames = uploadUtil.parseMultiRowFieldWithoutEmptyValues(
                Headings.TEST_STANDARD, certHeadingRecord, certResultRecords);
        if (testStandardNames != null && testStandardNames.size() > 0) {
            testStandardNames.stream().forEach(testStandardName -> {
                CertificationResultTestStandard testStandard = CertificationResultTestStandard.builder()
                        .testStandardName(testStandardName)
                        .build();
                testStandards.add(testStandard);
            });
        }
        return testStandards;
    }
}
