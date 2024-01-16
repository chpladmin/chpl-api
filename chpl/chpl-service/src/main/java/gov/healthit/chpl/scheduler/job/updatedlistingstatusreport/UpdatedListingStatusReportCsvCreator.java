package gov.healthit.chpl.scheduler.job.updatedlistingstatusreport;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class UpdatedListingStatusReportCsvCreator {
    private Environment env;

    @Autowired
    public UpdatedListingStatusReportCsvCreator(Environment env) {
        this.env = env;
    }

    private static final String NEW_LINE_SEPARATOR = "\n";

    public File createCsvFile(List<UpdatedListingStatusReport> reports) throws IOException {
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.builder()
                .setRecordSeparator(NEW_LINE_SEPARATOR)
                .build();

        File csvFile = getOutputFile();
        try (FileWriter fileWriter = new FileWriter(csvFile);
                CSVPrinter csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat)) {

            csvFilePrinter.printRecord(getHeaderRow());

            reports.stream()
                .sorted(Comparator.comparing(UpdatedListingStatusReport::getChplProductNumber))
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
                "CHPL Database Id",
                "CHPL Product Number",
                "Product",
                "Version",
                "Developer",
                "ONC-ACB",
                "Certification Status",
                "Criteria Require Update Count",
                "Days Updated Early");
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
                report.getCriteriaRequireUpdateCount().toString(),
                report.getDaysUpdatedEarly().toString());
    }

    private void printRow(CSVPrinter csvFilePrinter, UpdatedListingStatusReport report) {
        try {
            csvFilePrinter.printRecord(getRow(report));
        } catch (IOException e) {
            LOGGER.catching(e);
        }
    }

    private String getFilename() {
        return env.getProperty("updatedListingStatusReport.fileName") + LocalDate.now().toString();
    }
}
