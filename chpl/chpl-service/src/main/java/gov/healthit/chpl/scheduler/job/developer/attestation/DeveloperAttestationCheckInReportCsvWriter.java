package gov.healthit.chpl.scheduler.job.developer.attestation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class DeveloperAttestationCheckInReportCsvWriter {
    private String reportFileName;

    @Autowired
    public DeveloperAttestationCheckInReportCsvWriter(@Value("${developer.attestation.report.filename}") String reportFileName) {
        this.reportFileName = reportFileName;
    }

    public File generateFile(List<DeveloperAttestationCheckInReport> rows) {
        File outputFile = getOutputFile();
        if (rows == null || rows.size() == 0) {
            return outputFile;
        }

        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(outputFile),
                Charset.forName("UTF-8").newEncoder());
                CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.EXCEL)) {
            writer.write('\ufeff');
            //csvPrinter.printRecord(rows.get(0).getHeaders());
            rows.stream()
                    .forEach(row -> {
                        try {
                            csvPrinter.printRecord(row.toListOfStrings());
                        } catch (Exception e) {
                            LOGGER.error(e);
                        }

                    });
        } catch (Exception e) {
            LOGGER.error(e);
        }
        return outputFile;
    }

    private File getOutputFile() {
        File temp = null;
        try {
            temp = File.createTempFile(reportFileName, ".csv");
            temp.deleteOnExit();
        } catch (IOException ex) {
            LOGGER.error("Could not create temporary file " + ex.getMessage(), ex);
        }

        return temp;
    }

}
