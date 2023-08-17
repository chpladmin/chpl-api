package gov.healthit.chpl.scheduler.presenter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.env.Environment;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;

public abstract class SurveillanceCsvPresenter implements AutoCloseable {
    private Environment env;
    private DateTimeFormatter dateFormatter;
    private DateTimeFormatter dateTimeFormatter;
    private OutputStreamWriter writer = null;
    private CSVPrinter csvPrinter = null;
    private Logger logger;
    private File tempFile;

    public SurveillanceCsvPresenter(Environment env) {
        dateFormatter = DateTimeFormatter.ofPattern("uuuu/MM/dd");
        dateTimeFormatter = DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm Z");
        this.env = env;
    }

    public void open() throws IOException {
        getLogger().info("Opening file, initializing " + getPresenterName() + " CSV doc.");
        writer = new OutputStreamWriter(new FileOutputStream(tempFile), StandardCharsets.UTF_8);
        writer.write('\ufeff');
        csvPrinter = new CSVPrinter(writer, CSVFormat.EXCEL);
        csvPrinter.printRecord(generateHeaderValues());
        csvPrinter.flush();
    }

    @Override
    public void close() throws IOException {
        getLogger().info("Closing the " + getPresenterName() + " CSV file.");
        csvPrinter.close();
        writer.close();
    }

    public CSVPrinter getCsvPrinter() {
        return csvPrinter;
    }

    public DateTimeFormatter getDateFormatter() {
        return this.dateFormatter;
    }

    public DateTimeFormatter getDateTimeFormatter() {
        return this.dateTimeFormatter;
    }

    public Environment getEnvironment() {
        return this.env;
    }

    public void setLogger(final Logger logger) {
        this.logger = logger;
    }

    public Logger getLogger() {
        if (logger == null) {
            logger = LogManager.getLogger(CertifiedProductCsvPresenter.class);
        }
        return logger;
    }

    public File getTempFile() {
        return tempFile;
    }

    public void setTempFile(File tempFile) {
        this.tempFile = tempFile;
    }

    public abstract String getPresenterName();
    public abstract List<String> generateHeaderValues();
    public abstract void add(CertifiedProductSearchDetails data) throws IOException;
    public abstract String getFileName();
}
