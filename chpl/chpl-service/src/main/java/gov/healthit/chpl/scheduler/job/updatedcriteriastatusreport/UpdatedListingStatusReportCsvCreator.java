package gov.healthit.chpl.scheduler.job.updatedcriteriastatusreport;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.report.criteriauptodate.CriteriaUpToDateStatusReportDateService;
import gov.healthit.chpl.report.criteriauptodate.UpdatedCriterionStatusReport;
import gov.healthit.chpl.report.criteriauptodate.UpdatedCriterionStatusReportDao;
import gov.healthit.chpl.report.criteriauptodate.UpdatedListingStatusReport;
import lombok.extern.log4j.Log4j2;

//This report rolls up to a listing level the count of all updates needed
@Log4j2(topic = "updatedCriteriaStatusReportEmailJobLogger")
@Component
public class UpdatedListingStatusReportCsvCreator {

    private UpdatedCriterionStatusReportDao updatedCriterionStatusReportDao;
    private CriteriaUpToDateStatusReportDateService reportDateService;
    private Environment env;

    @Autowired
    public UpdatedListingStatusReportCsvCreator(UpdatedCriterionStatusReportDao updatedCriterionStatusReportDao,
            CriteriaUpToDateStatusReportDateService reportDateService,
            Environment env) {
        this.updatedCriterionStatusReportDao = updatedCriterionStatusReportDao;
        this.reportDateService = reportDateService;
        this.env = env;
    }

    private static final String NEW_LINE_SEPARATOR = "\n";

    public File createCsvFile(List<Long> acbIds) throws IOException {
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.builder()
                .setRecordSeparator(NEW_LINE_SEPARATOR)
                .build();

        File csvFile = getOutputFile();

        try (FileWriter fileWriter = new FileWriter(csvFile);
                CSVPrinter csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat)) {

            csvFilePrinter.printRecord(getHeaderRow());

            List<UpdatedCriterionStatusReport> criteriaReports = getReportData().stream()
                    .filter(data -> acbIds.contains(data.getCertificationBodyId()))
                    .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(criteriaReports)) {
                LOGGER.info("Grouping all required criteria updates by listing...");
                List<UpdatedListingStatusReport> listingReport = groupCriteriaUpdatesByListing(criteriaReports);
                LOGGER.info("Completed grouping all required criteria updates by listing.");
                LOGGER.info("Generating the CSV");
                listingReport.stream()
                    .sorted(Comparator.comparing(UpdatedListingStatusReport::getChplProductNumber))
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

    private List<UpdatedCriterionStatusReport> getReportData() {
        LOGGER.info("Getting report data for the listing CSV file");
        List<UpdatedCriterionStatusReport> reportData = updatedCriterionStatusReportDao.getUpdatedCriterionStatusReportsByDay(
                reportDateService.findClosestDateWithSummaryStatisticsAndUpdatedCriterionStatusData(LocalDate.now()));
        reportData = reportData.stream()
                .filter(dataRecord -> !dataRecord.getCertificationCriterion().isRemoved())
                .collect(Collectors.toList());
        return reportData;
    }

    private List<UpdatedListingStatusReport> groupCriteriaUpdatesByListing(List<UpdatedCriterionStatusReport> criteriaReports) {
        Map<Long, List<UpdatedCriterionStatusReport>> criteriaReportsGroupedByListing = criteriaReports.stream()
            .collect(Collectors.groupingBy(UpdatedCriterionStatusReport::getCertifiedProductId));
        return criteriaReportsGroupedByListing.keySet().stream()
                .map(listingId -> UpdatedListingStatusReport.builder()
                        .certificationBody(criteriaReportsGroupedByListing.get(listingId).get(0).getCertificationBody())
                        .certificationBodyId(criteriaReportsGroupedByListing.get(listingId).get(0).getCertificationBodyId())
                        .certificationStatus(criteriaReportsGroupedByListing.get(listingId).get(0).getCertificationStatus())
                        .certificationStatusId(criteriaReportsGroupedByListing.get(listingId).get(0).getCertificationStatusId())
                        .certifiedProductId(criteriaReportsGroupedByListing.get(listingId).get(0).getCertifiedProductId())
                        .chplProductNumber(criteriaReportsGroupedByListing.get(listingId).get(0).getChplProductNumber())
                        .developer(criteriaReportsGroupedByListing.get(listingId).get(0).getDeveloper())
                        .developerId(criteriaReportsGroupedByListing.get(listingId).get(0).getDeveloperId())
                        .product(criteriaReportsGroupedByListing.get(listingId).get(0).getProduct())
                        .reportDay(criteriaReportsGroupedByListing.get(listingId).get(0).getReportDay())
                        .version(criteriaReportsGroupedByListing.get(listingId).get(0).getVersion())
                        .totalUpdatesRequired(criteriaReportsGroupedByListing.get(listingId).size())
                        .build())
                .collect(Collectors.toList());
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
                "# Updates Required");
    }

    private List<String> getRow(UpdatedListingStatusReport report) {
        return Arrays.asList(
                report.getCertifiedProductId().toString(),
                report.getChplProductNumber(),
                report.getProduct(),
                report.getVersion(),
                report.getDeveloper(),
                report.getCertificationBody(),
                report.getCertificationStatus(),
                report.getTotalUpdatesRequired() == null ? "0" : report.getTotalUpdatesRequired() + "");
    }

    private void printRow(CSVPrinter csvFilePrinter, UpdatedListingStatusReport report) {
        try {
            csvFilePrinter.printRecord(getRow(report));
        } catch (IOException e) {
            LOGGER.catching(e);
        }
    }

    private String getFilename() {
        return env.getProperty("updatedCriteriaStatusReport.aggregatedByListing.fileName") + LocalDate.now().toString();
    }
}
