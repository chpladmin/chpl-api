package gov.healthit.chpl.scheduler.job.listingvalidation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "listingValidationReportEmailJobLogger")
@Component
public class ListingValidationReportCsvCreator {

    private Environment env;

    @Autowired
    public ListingValidationReportCsvCreator(Environment env) {
        this.env = env;
    }

    private static final String NEW_LINE_SEPARATOR = "\n";

    public File createCsvFile(List<ListingValidationReport> reports) throws IOException {
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR);

        File csvFile = getOutputFile();
        try (FileWriter fileWriter = new FileWriter(csvFile);
                CSVPrinter csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat)) {

            csvFilePrinter.printRecord(getHeaderRow());

            reports.stream()
                .sorted(Comparator.comparing(ListingValidationReport::getChplProductNumber))
                .forEach(report -> printRow(csvFilePrinter, report));
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
                "CHPL ID",
                "CHPL Product Name",
                "Certification Status",
                "Last Modified Date",
                "Error Message");
    }

    private List<String> getRow(ListingValidationReport report) {
        return Arrays.asList(
                report.getChplProductNumber(),
                report.getProductName(),
                report.getCertificationStatusName(),
                formatDate(report.getListingModifiedDate()),
                report.getErrorMessage());
    }

    private void printRow(CSVPrinter csvFilePrinter, ListingValidationReport report) {
        try {
            csvFilePrinter.printRecord(getRow(report));
        } catch (IOException e) {
            LOGGER.catching(e);
        }
    }

    private String getFilename() {
        return env.getProperty("listingValidationReport.fileName") + LocalDate.now().toString();
    }

    private String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
        return sdf.format(date);
    }
}
