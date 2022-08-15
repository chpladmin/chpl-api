package gov.healthit.chpl.scheduler.job.changerequest.presenter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.Logger;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;

public abstract class ChangeRequestCsvPresenter implements AutoCloseable {
    protected static final String DEV_NAME_HEADING = "Developer Name";
    protected static final String DEV_CODE_HEADING = "Developer Code";
    protected static final String DEV_CONTACT_NAME_HEADING = "Developer Contact Name";
    protected static final String DEV_CONTACT_EMAIL_HEADING = "Developer Contact Email";
    protected static final String DEV_CONTACT_PHONE_HEADING = "Developer Contact Phone Number";
    protected static final String DEV_ATTESTATIONS_PUBLISHED_HEADING = "Attestations Published?";
    protected static final String CR_TYPE_HEADING = "Change Request Type";
    protected static final String CR_STATUS_HEADING = "Change Request Status";
    protected static final String CR_CREATED_DATE_HEADING = "Change Request Open Date";
    protected static final String CR_LAST_UPDATED_DATE_HEADING = "Change Request Latest Change Date";
    protected static final String CR_ACBS_HEADING = "Relevant ONC-ACBs";
    protected static final String CR_QUESTION1_RESPONSE_HEADING = "Information Blocking Response";
    protected static final String CR_QUESTION1_OPTIONAL_RESPONSE_HEADING = "Information Blocking Optional Response";
    protected static final String CR_QUESTION2_RESPONSE_HEADING = "Assurances Response";
    protected static final String CR_QUESTION2_OPTIONAL_RESPONSE_HEADING = "Assurances Optional Response";
    protected static final String CR_QUESTION3_RESPONSE_HEADING = "Communications Response";
    protected static final String CR_QUESTION3_OPTIONAL_RESPONSE_HEADING = "Communications Optional Response";
    protected static final String CR_QUESTION4_RESPONSE_HEADING = "Application Programming Interfaces Response";
    protected static final String CR_QUESTION4_OPTIONAL_RESPONSE_HEADING = "Application Programming Interfaces Optional Response";
    protected static final String CR_QUESTION5_RESPONSE_HEADING = "Real World Testing Response";
    protected static final String CR_QUESTION5_OPTIONAL_RESPONSE_HEADING = "Real World Testing Optional Response";
    protected static final String CR_SUBMITTER_NAME_HEADING = "Submitted by Name";
    protected static final String CR_SUBMITTER_EMAIL_HEADING = "Submitted by Email";
    protected static final String CR_DAYS_OPEN_HEADING = "Change Request Days Open";
    protected static final String CR_DAYS_IN_STATE_HEADING = "Change Request Days In Current State";
    protected static final String CR_LAST_COMMENT_HEADING = "Change Request Latest Comment";

    private Logger logger;
    private OutputStreamWriter writer = null;
    private CSVPrinter csvPrinter = null;

    public ChangeRequestCsvPresenter(Logger logger) {
        this.logger = logger;
    }

    public void open(File file) throws IOException {
        logger.info("Opening " + file.getAbsolutePath() + ". Initializing CSV doc.");
        writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
        writer.write('\ufeff');
        csvPrinter = new CSVPrinter(writer, CSVFormat.EXCEL);
        csvPrinter.printRecord(generateHeaderValues());
        csvPrinter.flush();
    }

    protected abstract boolean isSupported(ChangeRequest data);

    public synchronized void add(ChangeRequest data) throws IOException {
        if (isSupported(data)) {
            logger.debug("Adding Change Request to CSV file: " + data.getId());
            List<String> rowValue = generateRowValue(data);
            if (rowValue != null) { // a subclass could return null to skip a row
                csvPrinter.printRecord(rowValue);
                csvPrinter.flush();
            }
        }
    }

    public void close() throws IOException {
        logger.info("Closing the CSV file.");
        csvPrinter.close();
        writer.close();
    }

    protected abstract List<String> generateHeaderValues();
    protected abstract List<String> generateRowValue(ChangeRequest changeRequest);
}
