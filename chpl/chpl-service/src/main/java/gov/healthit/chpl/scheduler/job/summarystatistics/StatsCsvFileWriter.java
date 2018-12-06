package gov.healthit.chpl.scheduler.job.summarystatistics;

import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.healthit.chpl.domain.statistics.Statistics;

public class StatsCsvFileWriter {
    private Logger logger;
    // Delimiter used in CSV file
    private static final String NEW_LINE_SEPARATOR = "\n";

    // CSV file header
    private static final Object[] FILE_HEADER = {
            "Date", "Total Developers", "Total Developers With 2014 Listings", "Total Developers With 2015 Listings",
            "Total Unique Products", "Total Products With Active 2014 Listings",
            "Total Products With Active 2015 Listings", "Total Products With Active Listings", "Total Listings",
            "Total 2014 Listings", "Total 2015 Listings", "Total 2011 Listings", "Total Surveillance Activities",
            "Total Open Surveillance Activities", "Total Closed Surveillance Activities", "Total NonConformities",
            "Total Open NonConformities", "Total Closed NonConformities"
    };

    public void writeCsvFile(String fileName, List<Statistics> statsCsvOutput) {
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR);
        SimpleDateFormat dateFormat = new SimpleDateFormat("E MMM dd yyyy");

        try (FileWriter fileWriter = new FileWriter(fileName);
                CSVPrinter csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat)) {

            csvFilePrinter.printRecord(FILE_HEADER);

            // Write a new StatisticsCSVOutput object list to the CSV file
            for (Statistics stat : statsCsvOutput) {
                List<String> statRecord = new ArrayList<String>();
                dateFormat.setTimeZone(TimeZone.getTimeZone(ZoneOffset.UTC));
                String dateString = dateFormat.format(stat.getDateRange().getEndDate());
                statRecord.add(dateString);
                statRecord.add(String.valueOf(stat.getTotalDevelopers()));
                statRecord.add(String.valueOf(stat.getTotalDevelopersWith2014Listings()));
                statRecord.add(String.valueOf(stat.getTotalDevelopersWith2015Listings()));
                statRecord.add(String.valueOf(stat.getTotalCertifiedProducts()));
                statRecord.add(String.valueOf(stat.getTotalCPsActive2014Listings()));
                statRecord.add(String.valueOf(stat.getTotalCPsActive2015Listings()));
                statRecord.add(String.valueOf(stat.getTotalCPsActiveListings()));
                statRecord.add(String.valueOf(stat.getTotalListings()));
                statRecord.add(String.valueOf(stat.getTotal2014Listings()));
                statRecord.add(String.valueOf(stat.getTotal2015Listings()));
                statRecord.add(String.valueOf(stat.getTotal2011Listings()));
                statRecord.add(String.valueOf(stat.getTotalSurveillanceActivities()));
                statRecord.add(String.valueOf(stat.getTotalOpenSurveillanceActivities()));
                statRecord.add(String.valueOf(stat.getTotalClosedSurveillanceActivities()));
                statRecord.add(String.valueOf(stat.getTotalNonConformities()));
                statRecord.add(String.valueOf(stat.getTotalOpenNonconformities()));
                statRecord.add(String.valueOf(stat.getTotalClosedNonconformities()));
                csvFilePrinter.printRecord(statRecord);
            }
            getLogger().info("CSV file was created successfully!");
        } catch (Exception e) {
            getLogger().error(e);
        }
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public Logger getLogger() {
        if (logger == null) {
            logger = LogManager.getLogger(StatsCsvFileWriter.class);
        }
        return logger;
    }
}
