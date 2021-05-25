package gov.healthit.chpl.scheduler.job.curesStatistics.email;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.statistics.CriterionUpgradedToCuresFromOriginalListingStatisticsDAO;
import gov.healthit.chpl.dto.statistics.CriterionUpgradedToCuresFromOriginalListingStatisticDTO;
import gov.healthit.chpl.service.CertificationCriterionService;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2(topic = "curesStatisticsEmailJobLogger")
public class OriginalCriterionUpgradedStatisticsCsvCreator {
    private static final String FILENAME_DATE_FORMAT = "yyyyMMdd";
    private static final String NEW_LINE_SEPARATOR = "\n";
    private static final String[] HEADING = {"Certification Criterion", "Listing Count"};
    private CriterionUpgradedToCuresFromOriginalListingStatisticsDAO criterionUpgradedToCuresFromOriginalStatisticsDao;
    private DateTimeFormatter dateFormatter;
    private String filename;

    @Autowired
    public OriginalCriterionUpgradedStatisticsCsvCreator(
            CriterionUpgradedToCuresFromOriginalListingStatisticsDAO criterionUpgradedToCuresFromOriginalStatisticsDao,
            @Value("{curesStatisticsReport.originalCriterionUpgraded.fileName}") String filename) {
        this.criterionUpgradedToCuresFromOriginalStatisticsDao = criterionUpgradedToCuresFromOriginalStatisticsDao;
        this.dateFormatter = DateTimeFormatter.ofPattern(FILENAME_DATE_FORMAT);
    }

    public File createCsvFile() throws IOException {
        File csvFile = null;
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR);
        LocalDate statisticDate = criterionUpgradedToCuresFromOriginalStatisticsDao.getDateOfMostRecentStatistics();
        if (statisticDate != null) {
            LOGGER.info("Most recent statistic date is " + statisticDate);
            csvFile = getOutputFile(statisticDate);
            try (FileWriter fileWriter = new FileWriter(csvFile);
                    CSVPrinter csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat)) {

                csvFilePrinter.printRecord(generateHeader());
                List<CriterionUpgradedToCuresFromOriginalListingStatisticDTO> statisticsForDate = criterionUpgradedToCuresFromOriginalStatisticsDao.getStatisticsForDate(statisticDate);
                statisticsForDate.stream()
                    .forEach(statistic -> printRow(csvFilePrinter, statistic));
                LOGGER.info("Completed generating records for " + statisticsForDate.size() + " statistics.");
            }
        } else {
            LOGGER.error("No most recent statistic date was found.");
        }
        return csvFile;
    }

    private void printRow(CSVPrinter csvFilePrinter, CriterionUpgradedToCuresFromOriginalListingStatisticDTO statistic) {
        try {
            csvFilePrinter.printRecord(generateRow(statistic));
        } catch (IOException e) {
            LOGGER.catching(e);
        }
    }

    private List<String> generateHeader() {
        return Stream.of(HEADING).collect(Collectors.toList());
    }

    private List<String> generateRow(CriterionUpgradedToCuresFromOriginalListingStatisticDTO statistic) {
        return Stream.of(CertificationCriterionService.formatCriteriaNumber(statistic.getCuresCriterion()),
        statistic.getListingsUpgradedFromOriginalCount().toString()).collect(Collectors.toList());
    }

    private File getOutputFile(LocalDate statisticDate) {
        File temp = null;
        try {
            temp = File.createTempFile(String.format(filename, dateFormatter.format(statisticDate)), ".csv");
            LOGGER.info("Created file " + temp.getAbsolutePath());
            temp.deleteOnExit();
        } catch (IOException ex) {
            LOGGER.error("Could not create temporary file " + ex.getMessage(), ex);
        }

        return temp;
    }
}
