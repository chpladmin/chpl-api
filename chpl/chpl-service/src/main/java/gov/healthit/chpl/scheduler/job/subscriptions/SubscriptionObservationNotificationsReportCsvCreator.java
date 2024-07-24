package gov.healthit.chpl.scheduler.job.subscriptions;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.subscription.domain.SubscriptionObservation;
import gov.healthit.chpl.util.DateUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "subscriptionObservationNotificationsReportEmailJobLogger")
@Component
public class SubscriptionObservationNotificationsReportCsvCreator {

    private Environment env;

    @Autowired
    public SubscriptionObservationNotificationsReportCsvCreator(Environment env) {
        this.env = env;
    }

    private static final String NEW_LINE_SEPARATOR = "\n";

    public File createCsvFile(List<SubscriptionObservation> observations) throws IOException {
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.builder()
                .setRecordSeparator(NEW_LINE_SEPARATOR)
                .build();

        File csvFile = getOutputFile();
        try (FileWriter fileWriter = new FileWriter(csvFile);
                CSVPrinter csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat)) {

            fileWriter.write('\ufeff');
            csvFilePrinter.printRecord(getHeaderRow());

            observations.stream()
                .sorted((obs1, obs2) -> obs1.getNotificationSentTimestamp().compareTo(obs2.getNotificationSentTimestamp()))
                .forEach(obs -> printRows(csvFilePrinter, obs));
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

    private List<String> getHeaderRow() {
        return Arrays.asList(
                "Subscriber",
                "Role",
                "Subscribed Item Type",
                "Subscribed Item Name",
                "Subject",
                "Notification Time");
    }

    private void printRows(CSVPrinter csvFilePrinter, SubscriptionObservation observation) {
            List<String> rowForItem = getRow(observation);
            printRow(csvFilePrinter, rowForItem);
    }

    private List<String> getRow(SubscriptionObservation observation) {
        List<String> row = new ArrayList<String>();
        row.add(observation.getSubscriber().getEmail());
        row.add(observation.getSubscriber().getRole().getName());
        row.add(observation.getSubscription().getSubject().getType().getName());
        row.add(observation.getSubscription().getSubscribedObjectId().toString());
        row.add(observation.getSubscription().getSubject().getSubject());
        row.add(printTimestamp(observation.getNotificationSentTimestamp()));
        return row;
    }

    private String printTimestamp(LocalDateTime value) {
        if (value != null) {
            return DateUtil.formatInEasternTime(value);
        }
        return "";
    }

    private void printRow(CSVPrinter csvFilePrinter, List<String> row) {
        try {
            csvFilePrinter.printRecord(row);
        } catch (IOException e) {
            LOGGER.catching(e);
        }
    }

    private String getFilename() {
        return env.getProperty("subscriptionObservationNotificationsReport.filename") + LocalDate.now().toString();
    }
}
