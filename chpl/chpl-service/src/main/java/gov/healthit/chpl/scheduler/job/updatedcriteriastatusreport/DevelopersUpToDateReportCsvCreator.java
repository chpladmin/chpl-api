package gov.healthit.chpl.scheduler.job.updatedcriteriastatusreport;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.developer.search.ActiveListingSearchOptions;
import gov.healthit.chpl.developer.search.DeveloperSearchRequest;
import gov.healthit.chpl.developer.search.DeveloperSearchResult;
import gov.healthit.chpl.developer.search.DeveloperSearchService;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.report.criteriauptodate.CriteriaUpToDateStatusReportDateService;
import gov.healthit.chpl.report.criteriauptodate.UpdatedCriterionStatusReport;
import gov.healthit.chpl.report.criteriauptodate.UpdatedCriterionStatusReportDao;
import gov.healthit.chpl.report.criteriauptodate.UpdatedDeveloperStatusReport;
import gov.healthit.chpl.search.ListingSearchService;
import gov.healthit.chpl.search.domain.ListingSearchResult;
import gov.healthit.chpl.search.domain.SearchRequest;
import gov.healthit.chpl.search.domain.SearchSetOperator;
import gov.healthit.chpl.util.CertificationStatusUtil;
import gov.healthit.chpl.util.DateUtil;
import lombok.extern.log4j.Log4j2;

//This report rolls up to a listing level the count of all updates needed
@Log4j2(topic = "updatedCriteriaStatusReportEmailJobLogger")
@Component
public class DevelopersUpToDateReportCsvCreator {

    private UpdatedCriterionStatusReportDao updatedCriterionStatusReportDao;
    private CriteriaUpToDateStatusReportDateService reportDateService;
    private DeveloperSearchService developerSearchService;
    private ListingSearchService listingSearchService;
    private CertificationBodyDAO acbDao;
    private Environment env;

    @Autowired
    public DevelopersUpToDateReportCsvCreator(UpdatedCriterionStatusReportDao updatedCriterionStatusReportDao,
            CriteriaUpToDateStatusReportDateService reportDateService,
            DeveloperSearchService developerSearchService,
            ListingSearchService listingSearchService,
            CertificationBodyDAO acbDao,
            Environment env) {
        this.updatedCriterionStatusReportDao = updatedCriterionStatusReportDao;
        this.reportDateService = reportDateService;
        this.developerSearchService = developerSearchService;
        this.listingSearchService = listingSearchService;
        this.acbDao = acbDao;
        this.env = env;
    }

    private static final String NEW_LINE_SEPARATOR = "\n";

    public File createCsvFile(List<Long> acbIds, Pair<LocalDate, LocalDate> requiredByDateRange) throws IOException {
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.builder()
                .setRecordSeparator(NEW_LINE_SEPARATOR)
                .get();

        File csvFile = getOutputFile();
        List<CertificationBody> allAcbs = acbDao.findAll();
        List<Long> relevantDeveloperIds = developerSearchService.getAllPagesOfSearchResults(DeveloperSearchRequest.builder()
                .acbsForActiveListings(allAcbs.stream()
                        .filter(acb -> acbIds.contains(acb.getId()))
                        .map(acb -> acb.getName())
                        .collect(Collectors.toSet()))
                .activeListingsOptions(Stream.of(ActiveListingSearchOptions.HAS_ANY_ACTIVE).collect(Collectors.toSet()))
                .activeListingsOptionsOperator(SearchSetOperator.AND)
                .build(), LOGGER).stream()
                .map(dev -> dev.getId())
                .collect(Collectors.toList());
        try (FileWriter fileWriter = new FileWriter(csvFile);
                CSVPrinter csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat)) {

            csvFilePrinter.printRecord(getHeaderRow());

            List<UpdatedCriterionStatusReport> criteriaReports = getReportData(requiredByDateRange).stream()
                    .filter(data -> relevantDeveloperIds.contains(data.getDeveloperId()))
                    .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(criteriaReports)) {
                LOGGER.info("Grouping all required criteria updates by developer...");
                List<UpdatedDeveloperStatusReport> developerReports = groupCriteriaUpdatesByDeveloper(criteriaReports, relevantDeveloperIds);
                LOGGER.info("Completed grouping all required criteria updates by developer.");
                LOGGER.info("Generating the CSV");
                developerReports.stream()
                    .sorted(Comparator.comparing(UpdatedDeveloperStatusReport::getDeveloperId))
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
        LOGGER.info("Getting report data for the developer CSV file");
        List<UpdatedCriterionStatusReport> reportData = updatedCriterionStatusReportDao.getUpdatedCriterionStatusReportsByDay(
                reportDateService.findClosestDateWithSummaryStatisticsAndUpdatedCriterionStatusData(LocalDate.now()));
        reportData = reportData.stream()
                .filter(dataRecord -> !dataRecord.getCertificationCriterion().isRemoved())
                .filter(dataRecord -> DateUtil.isDateBetweenInclusive(requiredByDateRange, dataRecord.getRequiredDay()))
                .collect(Collectors.toList());
        return reportData;
    }

    private List<UpdatedDeveloperStatusReport> groupCriteriaUpdatesByDeveloper(List<UpdatedCriterionStatusReport> criteriaReports,
            List<Long> relevantDeveloperIds) {
        //add "report" objects for all other developers so we have data for every developer with active listings
        List<DeveloperSearchResult> allDevelopersForReport = developerSearchService.getAllPagesOfSearchResults(
                DeveloperSearchRequest.builder().developerIds(relevantDeveloperIds.stream().collect(Collectors.toSet())).build(),
                LOGGER);

        Map<Long, List<UpdatedCriterionStatusReport>> criteriaReportsGroupedByDeveloper = criteriaReports.stream()
            .collect(Collectors.groupingBy(UpdatedCriterionStatusReport::getDeveloperId));

        List<UpdatedDeveloperStatusReport> developersRequiringUpdate = allDevelopersForReport.stream()
                .filter(devForReport -> criteriaReportsGroupedByDeveloper.keySet().contains(devForReport.getId()))
                .map(devForReport -> UpdatedDeveloperStatusReport.builder()
                        .developerId(devForReport.getId())
                        .developerDetailsUrl(devForReport.getDeveloperDetailsUrl())
                        .developerName(devForReport.getName())
                        .reportDay(criteriaReportsGroupedByDeveloper.get(devForReport.getId()).get(0).getReportDay())
                        .totalListingsRequiringUpdate(
                                getListingsRequiringUpdate(criteriaReportsGroupedByDeveloper.get(devForReport.getId()))
                                .size())
                        .totalListingsUpToDate(
                                getActiveListingsFromDeveloperNotRequiringUpdate(devForReport.getId(),
                                        criteriaReportsGroupedByDeveloper.get(devForReport.getId()).stream()
                                        .map(report -> report.getCertifiedProductId())
                                        .collect(Collectors.toList()))
                                        .size())
                        .build())
                .collect(Collectors.toList());

        List<UpdatedDeveloperStatusReport> developersNotRequiringUpdate = allDevelopersForReport.stream()
                .filter(devForReport -> !criteriaReportsGroupedByDeveloper.keySet().contains(devForReport.getId()))
                .map(devForReport -> UpdatedDeveloperStatusReport.builder()
                        .developerId(devForReport.getId())
                        .developerName(devForReport.getName())
                        .developerDetailsUrl(devForReport.getDeveloperDetailsUrl())
                        .reportDay(LocalDate.now())
                        .totalListingsRequiringUpdate(0)
                        .totalListingsUpToDate(devForReport.getCurrentActiveListingCount())
                        .build())
                .collect(Collectors.toList());
        return Stream.of(developersRequiringUpdate, developersNotRequiringUpdate)
                .flatMap(report -> report.stream())
                .collect(Collectors.toList());
    }

    private Set<Long> getListingsRequiringUpdate(List<UpdatedCriterionStatusReport> criteriaRequiringUpdatesForDeveloper) {
        return criteriaRequiringUpdatesForDeveloper.stream()
                .collect(Collectors.groupingBy(UpdatedCriterionStatusReport::getCertifiedProductId))
                .keySet();
    }

    private List<Long> getActiveListingsFromDeveloperNotRequiringUpdate(Long developerId, List<Long> listingsForDeveloperRequiringUpdate) {
        List<ListingSearchResult> activeListingsForDeveloper = listingSearchService.getAllPagesOfSearchResults(SearchRequest.builder()
                .developerId(developerId)
                .certificationStatuses(CertificationStatusUtil.getActiveStatusNames().stream().collect(Collectors.toSet()))
                .build(), LOGGER);
        return activeListingsForDeveloper.stream()
                .filter(listing -> !listingsForDeveloperRequiringUpdate.contains(listing.getId()))
                .map(listing -> listing.getId())
                .collect(Collectors.toList());
    }

    private List<String> getHeaderRow() {
        return Arrays.asList(
                "Developer",
                "CHPL URL",
                "# Listings Up-To-Date",
                "# Listings Not Up-To-Date");
    }

    private List<String> getRow(UpdatedDeveloperStatusReport report) {
        return Arrays.asList(
                report.getDeveloperName(),
                report.getDeveloperDetailsUrl(),
                report.getTotalListingsUpToDate() == null ? "0" : report.getTotalListingsUpToDate() + "",
                report.getTotalListingsRequiringUpdate() == null ? "0" : report.getTotalListingsRequiringUpdate() + "");
    }

    private void printRow(CSVPrinter csvFilePrinter, UpdatedDeveloperStatusReport report) {
        try {
            csvFilePrinter.printRecord(getRow(report));
        } catch (IOException e) {
            LOGGER.catching(e);
        }
    }

    private String getFilename() {
        return env.getProperty("updatedCriteriaStatusReport.aggregatedByDeveloper.fileName") + "_" + LocalDate.now().toString();
    }
}
