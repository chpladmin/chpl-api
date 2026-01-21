package gov.healthit.chpl.scheduler.job.updatedcriteriastatusreport;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.report.criteriauptodate.CriteriaUpToDateStatusReportDateService;
import gov.healthit.chpl.report.criteriauptodate.UpdatedCriterionStatusReport;
import gov.healthit.chpl.report.criteriauptodate.UpdatedCriterionStatusReportDao;
import gov.healthit.chpl.util.DateUtil;
import gov.healthit.chpl.util.Util;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "updatedCriteriaStatusReportEmailJobLogger")
@Component
public class UpdatedCriteriaStatusReportCsvCreator {

    private UpdatedCriterionStatusReportDao updatedCriterionStatusReportDao;
    private CriteriaUpToDateStatusReportDateService reportDateService;
    private Environment env;

    @Autowired
    public UpdatedCriteriaStatusReportCsvCreator(UpdatedCriterionStatusReportDao updatedCriterionStatusReportDao,
            CriteriaUpToDateStatusReportDateService reportDateService,
            Environment env) {
        this.updatedCriterionStatusReportDao = updatedCriterionStatusReportDao;
        this.reportDateService = reportDateService;
        this.env = env;
    }

    private static final String NEW_LINE_SEPARATOR = "\n";

    public File createCsvFile(List<Long> acbIds, Pair<LocalDate, LocalDate> requiredByDateRange) throws IOException {
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.builder()
                .setRecordSeparator(NEW_LINE_SEPARATOR)
                .build();

        File csvFile = getOutputFile();

        try (FileWriter fileWriter = new FileWriter(csvFile);
                CSVPrinter csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat)) {

            csvFilePrinter.printRecord(getHeaderRow());

            List<UpdatedCriterionStatusReport> reports = getReportData(requiredByDateRange).stream()
                    .filter(data -> acbIds.contains(data.getCertificationBodyId()))
                    .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(reports)) {
                LOGGER.info("Generating the CSV");
                reports.stream()
                    .sorted(Comparator.comparing(UpdatedCriterionStatusReport::getChplProductNumber))
                    .forEach(report -> printRow(csvFilePrinter, report));
                LOGGER.info("Completed generating the CSV");
            }
        }
        return csvFile;
    }

    private File getOutputFile() {
        File temp = null;
        try {
            temp = File.createTempFile(getFilename(), ".csv");
            temp.deleteOnExit();
        } catch (IOException ex) {
            LOGGER.error("Could not create temporary file " + ex.getMessage(), ex);
        }

        return temp;
    }

    private List<UpdatedCriterionStatusReport> getReportData(Pair<LocalDate, LocalDate> requiredByDateRange) {
        LOGGER.info("Getting report data for the criteria details CSV file");
        List<UpdatedCriterionStatusReport> reportData = updatedCriterionStatusReportDao.getUpdatedCriterionStatusReportsByDay(
                reportDateService.findClosestDateWithSummaryStatisticsAndUpdatedCriterionStatusData(LocalDate.now()));
        reportData = reportData.stream()
                .filter(dataRecord -> !dataRecord.getCertificationCriterion().isRemoved())
                .filter(dataRecord -> DateUtil.isDateBetweenInclusive(requiredByDateRange, dataRecord.getRequiredDay()))
                .collect(Collectors.toList());
        return reportData;
    }

    private List<String> getHeaderRow() {
        return Arrays.asList(
                "CHPL Database Id",
                "CHPL Product Number",
                "Product",
                "Version",
                "Developer",
                "ONC-ACB",
                "Certification Status",
                "Certification Criterion",
                "Update Required By",
                "Standard",
                "Standard Group",
                "Functionality Tested",
                "Code Set",
                "Reason Update is Required");
    }

    private List<String> getRow(UpdatedCriterionStatusReport report) {
        return Arrays.asList(
                report.getCertifiedProductId().toString(),
                report.getChplProductNumber(),
                report.getProduct(),
                report.getVersion(),
                report.getDeveloper(),
                report.getCertificationBody(),
                report.getCertificationStatus(),
                Util.formatCriteriaNumber(report.getCertificationCriterion()),
                report.getRequiredDay() != null ? report.getRequiredDay().toString() : "",
                report.getStandard() != null ? report.getStandard().getValue() : "",
                report.getStandardGroupName() != null ? report.getStandardGroupName() : "",
                report.getFunctionalityTested() != null ? report.getFunctionalityTested().getValue() : "",
                report.getCodeSet() != null ? report.getCodeSet().getName() : "",
                report.getCriterionNotUpToDateReason().getName());
    }

    private void printRow(CSVPrinter csvFilePrinter, UpdatedCriterionStatusReport report) {
        try {
            csvFilePrinter.printRecord(getRow(report));
        } catch (IOException e) {
            LOGGER.catching(e);
        }
    }

    private String getFilename() {
        return env.getProperty("updatedCriteriaStatusReport.details.fileName") + "_" + LocalDate.now().toString();
    }
}
