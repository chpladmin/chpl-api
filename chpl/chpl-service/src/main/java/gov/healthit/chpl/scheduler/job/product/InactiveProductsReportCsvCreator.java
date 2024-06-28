package gov.healthit.chpl.scheduler.job.product;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.util.DateUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "inactiveProductsReportJobLogger")
@Component
public class InactiveProductsReportCsvCreator {

    private String unformattedChplDeveloperUrl;
    private String reportFilename;

    @Autowired
    public InactiveProductsReportCsvCreator(@Value("${inactiveProductsReport.fileName}") String reportFilename,
            @Value("${chplUrlBegin}") String chplUrlBegin,
            @Value("${developerUrlPart}") String developerUrlPart) {
        this.unformattedChplDeveloperUrl = chplUrlBegin + developerUrlPart;
        this.reportFilename = reportFilename;
    }

    private static final String NEW_LINE_SEPARATOR = "\n";

    public File createCsvFile(List<InactiveProduct> inactiveProducts) throws IOException {
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.builder()
                .setRecordSeparator(NEW_LINE_SEPARATOR)
                .build();

        File csvFile = getOutputFile();
        try (FileWriter fileWriter = new FileWriter(csvFile);
                CSVPrinter csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat)) {

            csvFilePrinter.printRecord(getHeaderRow());

            inactiveProducts.stream()
                .sorted(new InactiveProductComparator())
                .forEach(inactiveProduct -> printRow(csvFilePrinter, inactiveProduct));
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
                "Developer",
                "Product",
                "ONC-ACB(s)",
                "Inactive Date",
                "Developer Website",
                "Developer Link in CHPL");
    }

    private List<String> getRow(InactiveProduct inactiveProduct) {
        return Arrays.asList(
                inactiveProduct.getDeveloperName(),
                inactiveProduct.getProductName(),
                inactiveProduct.getProductAcbs().stream().collect(Collectors.joining("; ")),
                DateUtil.format(inactiveProduct.getInactiveDate()),
                inactiveProduct.getDeveloperWebsite(),
                buildDeveloperPageChplUrl(inactiveProduct.getDeveloperId()));
    }

    private void printRow(CSVPrinter csvFilePrinter, InactiveProduct inactiveProduct) {
        try {
            csvFilePrinter.printRecord(getRow(inactiveProduct));
        } catch (IOException e) {
            LOGGER.catching(e);
        }
    }

    private String getFilename() {
        return reportFilename + "-" + LocalDate.now().toString();
    }

    private String buildDeveloperPageChplUrl(Long developerId) {
        return String.format(unformattedChplDeveloperUrl, developerId.toString());
    }

    private static final class InactiveProductComparator implements Comparator<InactiveProduct> {

        @Override
        public int compare(InactiveProduct o1, InactiveProduct o2) {
            int devNameComparison = StringUtils.compare(o1.getDeveloperName().toUpperCase(), o2.getDeveloperName().toUpperCase());
            if (devNameComparison != 0) {
                return devNameComparison;
            } else {
                return StringUtils.compare(o1.getProductName().toUpperCase(), o2.getProductName().toUpperCase());
            }
        }

    }
}
