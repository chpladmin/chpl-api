package gov.healthit.chpl.scheduler.job.curesStatistics.email;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.statistics.ListingToCriterionForCuresAchievementStatisticsDAO;
import gov.healthit.chpl.domain.statistics.ListingToCriterionForCuresAchievementStatistic;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2(topic = "curesStatisticsEmailJobLogger")
public class ListingCriterionForCuresAchievementStatisticsCsvCreator {
    private static final String FILENAME_DATE_FORMAT = "yyyyMMdd";
    private static final String NEW_LINE_SEPARATOR = "\n";
    private static final String[] HEADING = {"# Criteria Needed", "Listings That Need This Many"};
    private ListingToCriterionForCuresAchievementStatisticsDAO listingToCriterionForCuresAchievementStatisticsDao;
    private DateTimeFormatter dateFormatter;
    private String filename;

    @Autowired
    public ListingCriterionForCuresAchievementStatisticsCsvCreator(
            ListingToCriterionForCuresAchievementStatisticsDAO listingToCriterionForCuresAchievementStatisticsDao,
            @Value("${curesStatisticsReport.listingCriterionForCuresAchievement.fileName}") String filename) {
        this.listingToCriterionForCuresAchievementStatisticsDao = listingToCriterionForCuresAchievementStatisticsDao;
        this.filename = filename;
        this.dateFormatter = DateTimeFormatter.ofPattern(FILENAME_DATE_FORMAT);
    }

    public File createCsvFile() throws IOException {
        File csvFile = null;
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR);
        LocalDate statisticDate = listingToCriterionForCuresAchievementStatisticsDao.getDateOfMostRecentStatistics();
        if (statisticDate != null) {
            LOGGER.info("Most recent statistic date is " + statisticDate);
            csvFile = getOutputFile(statisticDate);
            try (FileWriter fileWriter = new FileWriter(csvFile);
                    CSVPrinter csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat)) {
                csvFilePrinter.printRecord(generateHeader());
                List<ListingToCriterionForCuresAchievementStatistic> statisticsForDate = listingToCriterionForCuresAchievementStatisticsDao.getStatisticsForDate(statisticDate);
                Map<Long, List<ListingToCriterionForCuresAchievementStatistic>> statisticsByListing
                    = statisticsForDate.stream()
                    .collect(Collectors.groupingBy(ListingToCriterionForCuresAchievementStatistic::getListingId));
                Map<Long, Long> countedStatistics = new HashMap<Long, Long>();
                statisticsByListing.keySet().stream()
                    .forEach(listingId -> countStatistic(new Long(statisticsByListing.get(listingId).size()), countedStatistics));
                Map<Long, Long> sortedCountedStatistics = countedStatistics.entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                                (oldValue, newValue) -> oldValue, LinkedHashMap::new));
                sortedCountedStatistics.keySet().stream()
                    .forEach(key -> printRow(csvFilePrinter, key, sortedCountedStatistics.get(key)));
                LOGGER.info("Completed generating records for " + statisticsForDate.size() + " statistics.");
            }
        } else {
            LOGGER.error("No most recent statistic date was found.");
        }
        return csvFile;
    }

    private void countStatistic(Long count, Map<Long, Long> countedStatistics) {
        Long currCount = countedStatistics.get(count);
        if (currCount == null) {
            countedStatistics.put(count, 1L);
        } else {
            Long newCount = currCount + 1;
            countedStatistics.put(count, newCount);
        }
    }

    private void printRow(CSVPrinter csvFilePrinter, Long criteriaNeededCount, Long listingCount) {
        try {
            csvFilePrinter.printRecord(generateRow(criteriaNeededCount, listingCount));
        } catch (IOException e) {
            LOGGER.catching(e);
        }
    }

    private List<String> generateHeader() {
        return Stream.of(HEADING).collect(Collectors.toList());
    }

    private List<String> generateRow(Long criteriaNeededCount, Long listingCount) {
        return Stream.of(criteriaNeededCount.toString(), listingCount.toString()).collect(Collectors.toList());
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
