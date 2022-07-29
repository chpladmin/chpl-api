package gov.healthit.chpl.scheduler.job.certificationId;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.SimpleCertificationId;
import gov.healthit.chpl.domain.SimpleCertificationIdWithProducts;
import gov.healthit.chpl.util.DateUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "certificationIdEmailJobLogger")
@Component
public class CertificationIdCsvCreator {

    private Environment env;

    @Autowired
    public CertificationIdCsvCreator(Environment env) {
        this.env = env;
    }

    private static final String NEW_LINE_SEPARATOR = "\n";

    public File createCsvFile(List<SimpleCertificationId> certificationIds) throws IOException {
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.builder()
                .setRecordSeparator(NEW_LINE_SEPARATOR)
                .build();

        File csvFile = getOutputFile();
        try (FileWriter fileWriter = new FileWriter(csvFile);
                CSVPrinter csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat)) {

            fileWriter.write('\ufeff');
            if (includesProducts(certificationIds)) {
                csvFilePrinter.printRecord(getHeaderRow(true));
            }

            certificationIds.stream()
                .sorted((certId1, certId2) -> certId1.getCertificationId().compareTo(certId2.getCertificationId()))
                .forEach(certId -> printRow(csvFilePrinter, certId));
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

    private boolean includesProducts(List<SimpleCertificationId> certificationIds) {
        return certificationIds.stream()
                .filter(certIdObj -> certIdObj instanceof SimpleCertificationIdWithProducts)
                .findAny().isPresent();
    }

    private List<String> getHeaderRow(boolean includeProducts) {
        List<String> headings = new ArrayList<String>();
        headings.add("CMS ID");
        headings.add("Creation Date");
        if (includeProducts) {
            headings.add("CHPL Product(s)");
        }
        return headings;
    }

    private void printRow(CSVPrinter csvFilePrinter, SimpleCertificationId certificationId) {
        try {
            csvFilePrinter.printRecord(getRow(certificationId));
        } catch (IOException e) {
            LOGGER.catching(e);
        }
    }

    private List<String> getRow(SimpleCertificationId certificationId) {
        List<String> row = new ArrayList<String>();
        row.add(certificationId.getCertificationId());
        row.add(DateUtil.format(DateUtil.toLocalDate(certificationId.getCreated().getTime())));
        if (certificationId instanceof SimpleCertificationIdWithProducts) {
            SimpleCertificationIdWithProducts certIdWithProducts = (SimpleCertificationIdWithProducts) certificationId;
            row.add(certIdWithProducts.getProducts());
        }
        return row;
    }

    private String getFilename() {
        return env.getProperty("certificationIdReport.filename") + LocalDate.now().toString();
    }
}