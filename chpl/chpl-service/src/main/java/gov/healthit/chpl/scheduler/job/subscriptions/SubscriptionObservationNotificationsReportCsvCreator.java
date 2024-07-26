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

import gov.healthit.chpl.subscription.domain.SubscriptionObservationNotification;
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

    public File createCsvFile(List<SubscriptionObservationNotification> notifications) throws IOException {
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.builder()
                .setRecordSeparator(NEW_LINE_SEPARATOR)
                .build();

        File csvFile = getOutputFile();
        try (FileWriter fileWriter = new FileWriter(csvFile);
                CSVPrinter csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat)) {

            fileWriter.write('\ufeff');
            csvFilePrinter.printRecord(getHeaderRow());

            notifications.stream()
                .sorted((n1, n2) -> n1.getNotificationDate().compareTo(n2.getNotificationDate()))
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
                "ONC-ACBs",
                "Developer",
                "Product",
                "Subscribed Item Name",
                "Subject",
                "Notification Time");
    }

    private void printRows(CSVPrinter csvFilePrinter, SubscriptionObservationNotification notification) {
            List<String> rowForItem = getRow(notification);
            printRow(csvFilePrinter, rowForItem);
    }

    private List<String> getRow(SubscriptionObservationNotification notification) {
        List<String> row = new ArrayList<String>();
        row.add(notification.getSubscriberEmail());
        row.add(notification.getSubscriberRole());
        row.add(notification.getSubscriptionObjectType());
        row.add(notification.getAcbName() != null ? notification.getAcbName() : "");
        row.add(notification.getDeveloperName() != null ? notification.getDeveloperName() : "");
        row.add(notification.getProductName() != null ? notification.getProductName() : "");
        row.add(notification.getSubscribedObjectName());
        row.add(notification.getSubscriptionSubject());
        row.add(printTimestamp(notification.getNotificationDate()));
        return row;
    }

    private String printTimestamp(LocalDateTime value) {
        if (value != null) {
            return DateUtil.formatInEasternTime(DateUtil.fromSystemToEastern(value));
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
