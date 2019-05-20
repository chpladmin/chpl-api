package gov.healthit.chpl.scheduler.job;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.MessagingException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.dao.scheduler.InheritanceErrorsReportDAO;
import gov.healthit.chpl.dto.scheduler.InheritanceErrorsReportDTO;
import gov.healthit.chpl.util.EmailBuilder;

/**
 * The InheritanceErrorsReportEmailJob implements a Quartz job and is available to ROLE_ADMIN and ROLE_ACB. When
 * invoked it emails relevant individuals with ICS error reports.
 * @author alarned
 *
 */
public class InheritanceErrorsReportEmailJob extends QuartzJob {
    private static final Logger LOGGER = LogManager.getLogger("inheritanceErrorsReportEmailJobLogger");
    private static final String DEFAULT_PROPERTIES_FILE = "environment.properties";
    private Properties props;

    @Autowired
    private InheritanceErrorsReportDAO inheritanceErrorsReportDAO;

    @Autowired
    private Environment env;
    /**
     * Constructor that initializes the InheritanceErrorsReportEmailJob object.
     * @throws Exception if thrown
     */
    public InheritanceErrorsReportEmailJob() throws Exception {
        super();
        loadProperties();
    }

    @Override
    public void execute(final JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        LOGGER.info("********* Starting the Inheritance Error Report Email job. *********");
        LOGGER.info("Sending email to: " + jobContext.getMergedJobDataMap().getString("email"));

        List<InheritanceErrorsReportDTO> errors = getAppropriateErrors(jobContext);
        File output = null;
        List<File> files = new ArrayList<File>();
        if (errors.size() > 0) {
            output = getOutputFile(errors);
            files.add(output);
        }
        String to = jobContext.getMergedJobDataMap().getString("email");
        String subject = props.getProperty("inheritanceReportEmailWeeklySubject");
        String htmlMessage;
        if (jobContext.getMergedJobDataMap().getBoolean("acbSpecific")) {
            subject = jobContext.getMergedJobDataMap().getString("acb").replaceAll("\u263A", ", ") + " " + subject;
            htmlMessage = props.getProperty("inheritanceReportEmailAcbWeeklyHtmlMessage");
        } else {
            htmlMessage = props.getProperty("inheritanceReportEmailWeeklyHtmlMessage");
        }
        LOGGER.info("Message to be sent: " + htmlMessage);

        try {
            htmlMessage += createHtmlEmailBody(errors.size(),
                    props.getProperty("inheritanceReportEmailWeeklyNoContent"));

            List<String> addresses = new ArrayList<String>();
            addresses.add(to);

            EmailBuilder emailBuilder = new EmailBuilder(env);
            emailBuilder.recipients(addresses)
            .subject(subject)
            .htmlMessage(htmlMessage)
            .fileAttachments(files)
            .sendEmail();
        } catch (IOException | MessagingException e) {
            LOGGER.error(e);
        }
        LOGGER.info("********* Completed the Inheritance Error Report Email job. *********");
    }

    private List<InheritanceErrorsReportDTO> getAppropriateErrors(final JobExecutionContext jobContext) {
        List<InheritanceErrorsReportDTO> allErrors = inheritanceErrorsReportDAO.findAll();
        List<InheritanceErrorsReportDTO> errors = new ArrayList<InheritanceErrorsReportDTO>();
        if (jobContext.getMergedJobDataMap().getBooleanValue("acbSpecific")) {
            for (InheritanceErrorsReportDTO error : allErrors) {
                if (jobContext.getMergedJobDataMap().getString("acb").indexOf(error.getAcb()) > -1) {
                    errors.add(error);
                }
            }
        } else {
            errors.addAll(allErrors);
        }
        return errors;
    }

    private File getOutputFile(final List<InheritanceErrorsReportDTO> errors) {
        String reportFilename = props.getProperty("inheritanceReportEmailWeeklyFileName");
        File temp = null;
        try {
            temp = File.createTempFile(reportFilename, ".csv");
            temp.deleteOnExit();
        } catch (IOException ex) {
            LOGGER.error("Error creating temporary file: " + ex.getMessage(), ex);
        }

        if (temp != null) {
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(temp), Charset.forName("UTF-8").newEncoder());
                CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.EXCEL)) {
                csvPrinter.printRecord(getHeaderRow());
                for (InheritanceErrorsReportDTO error : errors) {
                    List<String> rowValue = generateRowValue(error);
                    csvPrinter.printRecord(rowValue);
                }
            } catch (IOException e) {
                LOGGER.error(e);
            }
        }
        return temp;
    }

    private List<String> getHeaderRow() {
        List<String> result = new ArrayList<String>();
        result.add("CHPL ID");
        result.add("Developer");
        result.add("Product");
        result.add("Version");
        result.add("ONC-ACB");
        result.add("URL");
        result.add("Reason for Inclusion");
        return result;
    }

    private List<String> generateRowValue(final InheritanceErrorsReportDTO data) {
        List<String> result = new ArrayList<String>();
        result.add(data.getChplProductNumber());
        result.add(data.getDeveloper());
        result.add(data.getProduct());
        result.add(data.getVersion());
        result.add(data.getAcb());
        result.add(data.getUrl());
        result.add(data.getReason());
        return result;
    }

    private String createHtmlEmailBody(final int numRecords, final String noContentMsg) throws IOException {
        String htmlMessage = "";
        if (numRecords == 0) {
            htmlMessage = noContentMsg;
        } else {
            htmlMessage = "<p>" + numRecords + " inheritance error" + (numRecords > 1 ? "s were" : " was") + " found.";
        }
        return htmlMessage;
    }

    private Properties loadProperties() throws IOException {
        InputStream in =
                InheritanceErrorsReportEmailJob.class.getClassLoader().getResourceAsStream(DEFAULT_PROPERTIES_FILE);
        if (in == null) {
            props = null;
            throw new FileNotFoundException("Environment Properties File not found in class path.");
        } else {
            props = new Properties();
            props.load(in);
            in.close();
        }
        return props;
    }
}
