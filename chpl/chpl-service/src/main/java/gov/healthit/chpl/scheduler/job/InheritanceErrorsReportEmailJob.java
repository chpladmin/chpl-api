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
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import gov.healthit.chpl.auth.SendMailUtil;
import gov.healthit.chpl.dao.scheduler.InheritanceErrorsReportDAO;
import gov.healthit.chpl.dto.scheduler.InheritanceErrorsReportDTO;
import gov.healthit.chpl.scheduler.JobConfig;

/**
 * The InheritanceErrorsReportEmailJob implements a Quartz job and is available to ROLE_ADMIN and ROLE_ACB. When
 * invoked it emails relevant individuals with ICS error reports.
 * @author alarned
 *
 */
public class InheritanceErrorsReportEmailJob extends QuartzJob {
    private static final Logger LOGGER = LogManager.getLogger(InheritanceErrorsReportEmailJob.class);
    private static final String DEFAULT_PROPERTIES_FILE = "environment.properties";
    private InheritanceErrorsReportDAO inheritanceErrorsReportDAO;
    private Properties props;
    private AbstractApplicationContext context;

    /**
     * Constructor that initializes the InheritanceErrorsReportEmailJob object.
     * @throws Exception if thrown
     */
    public InheritanceErrorsReportEmailJob() throws Exception {
        super();
        setLocalContext();
        context = new AnnotationConfigApplicationContext(JobConfig.class);
        initiateSpringBeans(context);
        loadProperties();
    }

    @Override
    public void execute(final JobExecutionContext jobContext) throws JobExecutionException {
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
            htmlMessage = props.getProperty("inheritanceReportEmailAcbWeeklyHtmlMessage");
        } else {
            htmlMessage = props.getProperty("inheritanceReportEmailWeeklyHtmlMessage");
        }
        SendMailUtil mailUtil = new SendMailUtil();
        try {
            htmlMessage += createHtmlEmailBody(errors.size(),
                    props.getProperty("inheritanceReportEmailWeeklyNoContent"));
            mailUtil.sendEmail(to, subject, htmlMessage, files, props);
        } catch (IOException | MessagingException e) {
            LOGGER.error(e);
        } finally {
            context.close();
        }
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
        OutputStreamWriter writer = null;
        CSVPrinter csvPrinter = null;
        try {
            temp = File.createTempFile(reportFilename, ".csv");
            temp.deleteOnExit();
            writer = new OutputStreamWriter(
                    new FileOutputStream(temp),
                    Charset.forName("UTF-8").newEncoder()
                    );
            csvPrinter = new CSVPrinter(writer, CSVFormat.EXCEL);
            csvPrinter.printRecord(getHeaderRow());
            for (InheritanceErrorsReportDTO error : errors) {
                List<String> rowValue = generateRowValue(error);
                csvPrinter.printRecord(rowValue);            }
        } catch (IOException e) {
            LOGGER.error(e);
        } finally {
            try {
                if (csvPrinter != null) {
                    csvPrinter.flush();
                    csvPrinter.close();
                }
                if (writer != null) {
                    writer.flush();
                    writer.close();
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

    @Override
    protected void initiateSpringBeans(final AbstractApplicationContext context) throws IOException {
        setInheritanceErrorsReportDAO((InheritanceErrorsReportDAO) context.getBean("inheritanceErrorsReportDAO"));
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

    private void setInheritanceErrorsReportDAO(final InheritanceErrorsReportDAO inheritanceErrorsReportDAO) {
        this.inheritanceErrorsReportDAO = inheritanceErrorsReportDAO;
    }
}
