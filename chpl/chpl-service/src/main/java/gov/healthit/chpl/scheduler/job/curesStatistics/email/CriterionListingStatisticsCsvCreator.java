package gov.healthit.chpl.scheduler.job.curesStatistics.email;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.statistics.CriterionListingStatisticsDAO;
import gov.healthit.chpl.dto.statistics.CriterionListingCountStatisticDTO;
import gov.healthit.chpl.service.CertificationCriterionService;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2(topic = "curesStatisticsEmailJobLogger")
public class CriterionListingStatisticsCsvCreator {
    private static final String NEW_LINE_SEPARATOR = "\n";
    private static final String[] HEADING = {"Certification Criterion", "Listing Count"};
    private CriterionListingStatisticsDAO criterionListingStatisticsDao;
    private String filename;

    @Autowired
    public CriterionListingStatisticsCsvCreator(CriterionListingStatisticsDAO criterionListingStatisticsDao,
            @Value("{curesStatisticsReport.listingCriterionStatistics.fileName}") String filename) {
        this.criterionListingStatisticsDao = criterionListingStatisticsDao;
        this.filename = filename;
    }

    public File createCsvFile() throws IOException {
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR);
        File csvFile = getOutputFile();
        try (FileWriter fileWriter = new FileWriter(csvFile);
                CSVPrinter csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat)) {

            csvFilePrinter.printRecord(generateHeader());
            LocalDate statisticDate = criterionListingStatisticsDao.getDateOfMostRecentStatistics();
            if (statisticDate != null) {
                LOGGER.info("Most recent statistic date is " + statisticDate);
                List<CriterionListingCountStatisticDTO> statisticsForDate = criterionListingStatisticsDao.getStatisticsForDate(statisticDate);
                statisticsForDate.stream()
                    .forEach(statistic -> printRow(csvFilePrinter, statistic));
                LOGGER.info("Completed generating records for " + statisticsForDate.size() + " statistics.");
            } else {
                LOGGER.error("No most recent statistic date was found.");
            }
        }
        return csvFile;
    }

    private void printRow(CSVPrinter csvFilePrinter, CriterionListingCountStatisticDTO statistic) {
        try {
            csvFilePrinter.printRecord(generateRow(statistic));
        } catch (IOException e) {
            LOGGER.catching(e);
        }
    }

    private List<String> generateHeader() {
        return Stream.of(HEADING).collect(Collectors.toList());
    }

    private List<String> generateRow(CriterionListingCountStatisticDTO statistic) {
        return Stream.of(CertificationCriterionService.formatCriteriaNumber(statistic.getCriterion()),
        statistic.getListingsCertifyingToCriterionCount().toString()).collect(Collectors.toList());
    }

    private File getOutputFile() {
        File temp = null;
        try {
            temp = File.createTempFile(filename, ".csv");
            temp.deleteOnExit();
        } catch (IOException ex) {
            LOGGER.error("Could not create temporary file " + ex.getMessage(), ex);
        }

        return temp;
    }
}
