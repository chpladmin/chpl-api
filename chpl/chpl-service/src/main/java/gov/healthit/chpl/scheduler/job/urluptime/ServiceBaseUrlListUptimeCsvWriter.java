package gov.healthit.chpl.scheduler.job.urluptime;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.domain.CertificationBody;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2(topic = "serviceBaseUrlListUptimeEmailJobLogger")
public class ServiceBaseUrlListUptimeCsvWriter {
    private CertificationBodyDAO certificationBodyDAO;
    private String reportFileName;

    @Autowired
    public ServiceBaseUrlListUptimeCsvWriter(@Value("${serviceBaseUrlListUptime.report.filename}") String reportFileName, CertificationBodyDAO certificationBodyDAO) {
        this.reportFileName = reportFileName;
        this.certificationBodyDAO = certificationBodyDAO;
    }

    public File generateFile(List<ServiceBaseUrlListUptimeReport> rows) {
        File outputFile = getOutputFile();
        if (rows == null || rows.size() == 0) {
            return outputFile;
        }

        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(outputFile),
                Charset.forName("UTF-8").newEncoder());
                CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.EXCEL)) {

            List<CertificationBody> activeAcbs = certificationBodyDAO.findAllActive();

            writer.write('\ufeff');
            csvPrinter.printRecord(ServiceBaseUrlListUptimeReport.getHeaders(activeAcbs));
            rows.stream()
                    .forEach(row -> {
                        try {
                            csvPrinter.printRecord(row.toListOfStrings(activeAcbs));
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
            temp = File.createTempFile(reportFileName + " " + LocalDate.now().toString() + " ", ".csv");
            temp.deleteOnExit();
        } catch (IOException ex) {
            LOGGER.error("Could not create temporary file " + ex.getMessage(), ex);
        }

        return temp;
    }
}
