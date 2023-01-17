package gov.healthit.chpl.scheduler.job.ics;

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

import org.apache.commons.collections.CollectionUtils;
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
import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.SchedulerManager;
import gov.healthit.chpl.scheduler.job.QuartzJob;
import gov.healthit.chpl.util.Util;

public class IcsErrorsReportEmailJob extends QuartzJob {
    private static final Logger LOGGER = LogManager.getLogger("icsErrorsReportEmailJobLogger");

    @Autowired
    private IcsErrorsReportDao icsErrorsReportDao;

    @Autowired
    private CertificationBodyDAO certificationBodyDAO;

    @Autowired
    private Environment env;

    @Autowired
    private ChplEmailFactory chplEmailFactory;

    @Autowired
    private ChplHtmlEmailBuilder chplHtmlEmailBuilder;

    public IcsErrorsReportEmailJob() throws Exception {
        super();
    }

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        LOGGER.info("********* Starting the ICS Errors Report Email job. *********");
        LOGGER.info("Sending email to: " + jobContext.getMergedJobDataMap().getString("email"));

        List<IcsErrorsReport> errors = getAppropriateErrors(jobContext);
        File output = null;
        List<File> files = new ArrayList<File>();
        if (errors.size() > 0) {
            output = getOutputFile(errors);
            files.add(output);
        }
        String to = jobContext.getMergedJobDataMap().getString("email");
        String subject = env.getProperty("icsErrorsReportEmailSubject");
        try {
            String htmlMessage = createHtmlEmailBody(errors, jobContext);
            LOGGER.info("Message to be sent: " + htmlMessage);

            List<String> addresses = new ArrayList<String>();
            addresses.add(to);

            chplEmailFactory.emailBuilder().recipients(addresses)
                    .subject(subject)
                    .htmlMessage(htmlMessage)
                    .fileAttachments(files)
                    .sendEmail();
        } catch (IOException | EmailNotSentException e) {
            LOGGER.error(e);
        }
        LOGGER.info("********* Completed the ICS Errors Report Email job. *********");
    }

    private List<IcsErrorsReport> getAppropriateErrors(JobExecutionContext jobContext) {
        List<IcsErrorsReport> allErrors = icsErrorsReportDao.findAll();
        List<IcsErrorsReport> errors = new ArrayList<IcsErrorsReport>();
        List<Long> acbIds =
                Arrays.asList(
                        jobContext.getMergedJobDataMap().getString(QuartzJob.JOB_DATA_KEY_ACB).split(SchedulerManager.DATA_DELIMITER)).stream()
                .map(acb -> Long.parseLong(acb))
                .collect(Collectors.toList());

        errors = allErrors.stream()
                .filter(error -> acbIds.contains(error.getCertificationBody().getId()))
                .collect(Collectors.toList());
        return errors;
    }

    private File getOutputFile(List<IcsErrorsReport> errors) {
        String reportFilename = env.getProperty("icsErrorsReportEmailFileName");
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
                for (IcsErrorsReport error : errors) {
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

    private List<String> generateRowValue(IcsErrorsReport data) {
        List<String> result = new ArrayList<String>();
        result.add(data.getChplProductNumber());
        result.add(data.getDeveloper());
        result.add(data.getProduct());
        result.add(data.getVersion());
        result.add(data.getCertificationBody().getName());
        result.add(env.getProperty("chplUrlBegin").trim() + env.getProperty("listingDetailsUrl") + data.getListingId());
        result.add(data.getReason());
        return result;
    }

    private String createHtmlEmailBody(List<IcsErrorsReport> icsErrors, JobExecutionContext jobContext) throws IOException {
        String htmlMessage = "";
        if (CollectionUtils.isEmpty(icsErrors)) {
            htmlMessage = chplHtmlEmailBuilder.initialize()
                    .heading(env.getProperty("icsErrorsReportEmailHeading"))
                    .paragraph(null, String.format(env.getProperty("icsErrorsReportEmailParagraph1"), getAcbNamesAsCommaSeparatedList(jobContext)))
                    .paragraph(null, env.getProperty("icsErrorsReportEmailNoContent"))
                    .build();
        } else {
            htmlMessage = chplHtmlEmailBuilder.initialize()
                .heading(env.getProperty("icsErrorsReportEmailHeading"))
                .paragraph(null, String.format(env.getProperty("icsErrorsReportEmailParagraph1"), getAcbNamesAsCommaSeparatedList(jobContext)))
                .paragraph(null, String.format(env.getProperty("icsErrorsReportEmailParagraph2"), icsErrors.size(),
                            (icsErrors.size() == 1 ? "" : "s"),
                            (icsErrors.size() == 1 ? "was" : "were")))
                .build();
        }
        return htmlMessage;
    }

    private String getAcbNamesAsCommaSeparatedList(JobExecutionContext jobContext) {
        if (Objects.nonNull(jobContext.getMergedJobDataMap().getString(QuartzJob.JOB_DATA_KEY_ACB))) {
            List<String> acbNames =
                    Arrays.asList(jobContext.getMergedJobDataMap().getString(QuartzJob.JOB_DATA_KEY_ACB).split(SchedulerManager.DATA_DELIMITER)).stream()
                    .map(acbId -> {
                        try {
                            return certificationBodyDAO.getById(Long.parseLong(acbId)).getName();
                        } catch (NumberFormatException | EntityRetrievalException e) {
                            LOGGER.error("Could not retreive ACB name based on value: " + acbId, e);
                            return "";
                        }
                    })
                    .collect(Collectors.toList());
            return Util.joinListGrammatically(acbNames);
        } else {
            return "";
        }
    }

}
