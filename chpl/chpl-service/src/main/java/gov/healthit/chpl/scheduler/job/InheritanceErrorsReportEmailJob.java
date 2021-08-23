package gov.healthit.chpl.scheduler.job;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.scheduler.InheritanceErrorsReportDAO;
import gov.healthit.chpl.dto.scheduler.InheritanceErrorsReportDTO;
import gov.healthit.chpl.email.EmailBuilder;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.SchedulerManager;

public class InheritanceErrorsReportEmailJob extends QuartzJob {
    private static final Logger LOGGER = LogManager.getLogger("inheritanceErrorsReportEmailJobLogger");

    @Autowired
    private InheritanceErrorsReportDAO inheritanceErrorsReportDAO;

    @Autowired
    private CertificationBodyDAO certificationBodyDAO;

    @Autowired
    private Environment env;

    public InheritanceErrorsReportEmailJob() throws Exception {
        super();
    }

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
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
        String subject = env.getProperty("inheritanceReportEmailWeeklySubject");
        String htmlMessage;
        if (jobContext.getMergedJobDataMap().getBoolean("acbSpecific")) {
            htmlMessage = String.format(env.getProperty("inheritanceReportEmailAcbWeeklyHtmlMessage"), getAcbNamesAsCommaSeparatedList(jobContext));
        } else {
            htmlMessage = String.format(env.getProperty("inheritanceReportEmailWeeklyHtmlMessage"), getAcbNamesAsCommaSeparatedList(jobContext));
        }
        LOGGER.info("Message to be sent: " + htmlMessage);

        try {
            htmlMessage += createHtmlEmailBody(errors.size(),
                    env.getProperty("inheritanceReportEmailWeeklyNoContent"));

            List<String> addresses = new ArrayList<String>();
            addresses.add(to);

            EmailBuilder emailBuilder = new EmailBuilder(env);
            emailBuilder.recipients(addresses)
            .subject(subject)
            .htmlMessage(htmlMessage)
            .fileAttachments(files)
            .sendEmail();
        } catch (IOException | EmailNotSentException e) {
            LOGGER.error(e);
        }
        LOGGER.info("********* Completed the Inheritance Error Report Email job. *********");
    }

    private List<InheritanceErrorsReportDTO> getAppropriateErrors(JobExecutionContext jobContext) {
        List<InheritanceErrorsReportDTO> allErrors = inheritanceErrorsReportDAO.findAll();
        List<InheritanceErrorsReportDTO> errors = new ArrayList<InheritanceErrorsReportDTO>();
        List<Long> acbIds =
                Arrays.asList(
                        jobContext.getMergedJobDataMap().getString("acb").split(SchedulerManager.DATA_DELIMITER)).stream()
                .map(acb -> Long.parseLong(acb))
                .collect(Collectors.toList());

        errors = allErrors.stream()
                .filter(error -> acbIds.contains(error.getCertificationBody().getId()))
                .collect(Collectors.toList());
        return errors;
    }

    private File getOutputFile(List<InheritanceErrorsReportDTO> errors) {
        String reportFilename = env.getProperty("inheritanceReportEmailWeeklyFileName");
        File temp = null;
        try {
            temp = File.createTempFile(reportFilename, ".csv");
            temp.deleteOnExit();
        } catch (IOException ex) {
            LOGGER.error("Error creating temporary file: " + ex.getMessage(), ex);
        }

        if (temp != null) {
            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(temp),
                    Charset.forName("UTF-8").newEncoder());
                    CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.EXCEL)) {
                writer.write('\ufeff');
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

    private List<String> generateRowValue(InheritanceErrorsReportDTO data) {
        List<String> result = new ArrayList<String>();
        result.add(data.getChplProductNumber());
        result.add(data.getDeveloper());
        result.add(data.getProduct());
        result.add(data.getVersion());
        result.add(data.getCertificationBody().getName());
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

    private String getAcbNamesAsCommaSeparatedList(JobExecutionContext jobContext) {
        if (Objects.nonNull(jobContext.getMergedJobDataMap().getString("acb"))) {
            return Arrays.asList(
                    jobContext.getMergedJobDataMap().getString("acb").split(SchedulerManager.DATA_DELIMITER)).stream()
                    .map(acbId -> {
                        try {
                            return certificationBodyDAO.getById(Long.parseLong(acbId)).getName();
                        } catch (NumberFormatException | EntityRetrievalException e) {
                            LOGGER.error("Could not retreive ACB name based on value: " + acbId, e);
                            return "";
                        }
                    })
                    .collect(Collectors.joining(", "));
        } else {
            return "";
        }
    }

}
