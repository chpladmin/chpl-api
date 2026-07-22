package gov.healthit.chpl.scheduler.job.developer.attestation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.certificationCriteria.CertificationCriterionComparator;
import gov.healthit.chpl.realworldtesting.domain.RealWorldTestingReport.CriterionAndSvapData;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.service.CertificationCriterionService.Criteria2015;
import gov.healthit.chpl.service.realworldtesting.RealWorldTestingCriteriaService;
import gov.healthit.chpl.util.Util;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class CheckInReportCsvWriter {
    private List<CertificationCriterion> rwtResultsRequiredCriteria;
    private String reportFileName;

    @Autowired
    public CheckInReportCsvWriter(RealWorldTestingCriteriaService rwtCriteriaService,
            CertificationCriterionService criteriaService,
            CertificationCriterionComparator criteriaComparator,
            FF4j ff4j,
            @Value("${developer.attestation.checkin.report.filename}") String reportFileName) {
        this.reportFileName = reportFileName;
        Integer currentYear = Calendar.getInstance().get(Calendar.YEAR);
        if (ff4j.check(FeatureList.HTI_5_ERD)) {
            rwtResultsRequiredCriteria = rwtCriteriaService.getEligibleCriteria(currentYear);
        } else {
            rwtResultsRequiredCriteria = Stream.of(criteriaService.get(Criteria2015.G_7),
                    criteriaService.get(Criteria2015.G_9_CURES),
                    criteriaService.get(Criteria2015.G_10)).collect(Collectors.toList());
        }
        rwtResultsRequiredCriteria.stream().sorted(criteriaComparator);
    }

    public File generateFile(List<CheckInReport> rows) {
        File outputFile = getOutputFile();
        if (rows == null || rows.size() == 0) {
            return outputFile;
        }

        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(outputFile),
                Charset.forName("UTF-8").newEncoder());
                CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.EXCEL)) {
            writer.write('\ufeff');
            csvPrinter.printRecord(getHeaders());
            rows.stream()
                    .forEach(row -> {
                        try {
                            csvPrinter.printRecord(toListOfStrings(row));
                        } catch (Exception e) {
                            LOGGER.error(e);
                        }
                    });
        } catch (Exception e) {
            LOGGER.error(e);
        }
        return outputFile;
    }

    private List<String> getHeaders() {
        List<String> headers = List.of("Developer Name",
                "Developer Code",
                "Developer DBID",
                "Change Request Submitted Date",
                "Attestations Published?",
                "Change Request Current Status",
                "Change Request Last Status Change Date",
                "ONC-ACBs",
                "Attestations Period",
                "Information Blocking Response",
                "Information Blocking Optional Response",
                "Assurances Response",
                "Assurances Optional Response",
                "Communications Response",
                "Communications Optional Response",
                "Application Programming Interfaces Response",
                "Application Programming Interfaces Optional Response",
                "Real World Testing Response",
                "Real World Testing Optional Response",
                "Submitted by Name",
                "Submitted by Email",
                "Total Surveillance",
                "Total Surveillance Non-conformities",
                "Open Surveillance Non-conformities",
                "Total Direct Review Non-conformities",
                "Open Direct Review Non-conformities",
                "Has listing(s) with Assurances criteria (b)(10)",
                "Has listing(s) with API criteria",
                "Has listing(s) with RWT criteria");
        rwtResultsRequiredCriteria.stream()
            .forEach(criterion -> headers.add(Util.formatCriteriaNumber(criterion)));
        return headers;
    }

    private List<String> toListOfStrings(CheckInReport checkInReport) {
        List<String> csvRow = List.of(checkInReport.getDeveloperName(),
                checkInReport.getDeveloperCode(),
                checkInReport.getDeveloperId().toString(),
                checkInReport.getSubmittedDate() != null ? checkInReport.getSubmittedDate().toString() : "",
                checkInReport.getPublished() ? "Yes" : "No",
                checkInReport.getCurrentStatusName() != null ? checkInReport.getCurrentStatusName() : "",
                checkInReport.getLastStatusChangeDate() != null ? checkInReport.getLastStatusChangeDate().toString() : "",
                checkInReport.getRelevantAcbs() != null ? checkInReport.getRelevantAcbs() : "",
                checkInReport.getAttestationPeriod() != null ? checkInReport.getAttestationPeriod() : "",
                checkInReport.getInformationBlockingResponse() != null ? checkInReport.getInformationBlockingResponse() : "",
                checkInReport.getInformationBlockingNoncompliantResponse() != null ? checkInReport.getInformationBlockingNoncompliantResponse() : "",
                checkInReport.getAssurancesResponse() != null ? checkInReport.getAssurancesResponse() : "",
                checkInReport.getAssurancesNoncompliantResponse() != null ? checkInReport.getAssurancesNoncompliantResponse() : "",
                checkInReport.getCommunicationsResponse() != null ? checkInReport.getCommunicationsResponse() : "",
                checkInReport.getCommunicationsNoncompliantResponse() != null ? checkInReport.getCommunicationsNoncompliantResponse() : "",
                checkInReport.getApiResponse() != null ? checkInReport.getApiResponse() : "",
                checkInReport.getApiNoncompliantResponse() != null ? checkInReport.getApiNoncompliantResponse() : "",
                checkInReport.getRwtResponse() != null ? checkInReport.getRwtResponse() : "",
                checkInReport.getRwtNoncompliantResponse() != null ? checkInReport.getRwtNoncompliantResponse() : "",
                checkInReport.getSignature() != null ? checkInReport.getSignature() : "",
                checkInReport.getSignatureEmail() != null ? checkInReport.getSignatureEmail() : "",
                checkInReport.getTotalSurveillances() != null ? checkInReport.getTotalSurveillances().toString() : "0",
                checkInReport.getTotalSurveillanceNonconformities() != null ? checkInReport.getTotalSurveillanceNonconformities().toString() : "0",
                checkInReport.getOpenSurveillanceNonconformities() != null ? checkInReport.getOpenSurveillanceNonconformities().toString() : "0",
                checkInReport.getTotalDirectReviewNonconformities() != null ? checkInReport.getTotalDirectReviewNonconformities().toString() : "0",
                checkInReport.getOpenDirectReviewNonconformities() != null ? checkInReport.getOpenDirectReviewNonconformities().toString() : "0",
                checkInReport.getAssurancesValidation() != null ? checkInReport.getAssurancesValidation() : "",
                checkInReport.getApiValidation() != null ? checkInReport.getApiValidation() : "",
                checkInReport.getRealWorldTestingValidation() != null ? checkInReport.getRealWorldTestingValidation() : "");
        rwtResultsRequiredCriteria.stream()
            .forEach(criterion -> csvRow.add(determineCriteriaReportValue(checkInReport.getCriterionAndSvapData(), criterion)));
        return csvRow;
    }

    private String determineCriteriaReportValue(List<CriterionAndSvapData> criteriaAndSvapData, CertificationCriterion criterion) {
        CriterionAndSvapData criterionAndSvapData = criteriaAndSvapData.stream()
                .filter(item -> item.getCriterion().getId().equals(criterion.getId()))
                .findAny()
                .orElse(null);
        if (criterionAndSvapData == null) {
            return "FALSE";
        } else if (criterionAndSvapData.isGCriterion()) {
            return criterionAndSvapData.isAttested() ? "TRUE" : "FALSE";
        } else {
            return criterionAndSvapData.isAttested() && criterionAndSvapData.isUsesSvap() ? "TRUE" : "FALSE";
        }
    }

    private File getOutputFile() {
        File temp = null;
        try {
            temp = File.createTempFile(reportFileName + " " + LocalDate.now().toString() + " ", ".csv");
            temp.deleteOnExit();
        } catch (IOException ex) {
            LOGGER.error("Could not create temporary file " + ex.getMessage(), ex);
        }

        return temp;
    }

}
