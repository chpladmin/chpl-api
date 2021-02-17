package gov.healthit.chpl.scheduler.job.surveillanceReport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;

import org.apache.poi.ss.usermodel.Workbook;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.surveillance.report.SurveillanceReportManager;
import gov.healthit.chpl.surveillance.report.builder.AnnualReportBuilderXlsx;
import gov.healthit.chpl.surveillance.report.builder.ReportBuilderFactory;
import gov.healthit.chpl.surveillance.report.dto.AnnualReportDTO;
import gov.healthit.chpl.util.EmailBuilder;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@DisallowConcurrentExecution
//TODO: add to log4j.xml
@Log4j2(topic = "annualReportGenerationJobLogger")
public class AnnualReportGenerationJob implements Job {
    public static final String JOB_NAME = "annualReportGenerationJob";
    public static final String ANNUAL_REPORT_ID_KEY = "annualReportId";

    @Autowired
    private ErrorMessageUtil msgUtil;

    @Autowired
    private SurveillanceReportManager reportManager;

    @Autowired
    private ReportBuilderFactory reportBuilderFactory;

    @Autowired
    private Environment env;

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        LOGGER.info("********* Starting the Annual Report Generation job. *********");

        JobDataMap jobDataMap = jobContext.getMergedJobDataMap();
        Long annualReportId = (Long) jobDataMap.get(ANNUAL_REPORT_ID_KEY);
        if (annualReportId == null) {
            LOGGER.fatal("No annual report ID could be found in the job data.");
        } else {
            File writtenFile = null;
            AnnualReportDTO report = null;
            Workbook workbook = null;
            try {
                report = reportManager.getAnnualReport(annualReportId);
                if (report != null) {
                    AnnualReportBuilderXlsx reportBuilder = reportBuilderFactory.getReportBuilder(report);
                    if (reportBuilder != null) {
                        workbook = reportBuilder.buildXlsx(report);
                    } else {
                        String msg = msgUtil.getMessage("report.annualSurveillance.builderNotFound");
                        LOGGER.error(msg + " Report id " + annualReportId);
                    }
                }
            } catch (EntityRetrievalException ex) {
                String msg = msgUtil.getMessage("report.annualSurveillance.export.badId", annualReportId);
                LOGGER.error(msg);
            } catch (IOException io) {
                String msg = msgUtil.getMessage("report.annualSurveillance.export.builder.buildError");
                LOGGER.error(msg);
            } catch (Exception general) {
                //catch any other type of exception
                String msg = msgUtil.getMessage("report.annualSurveillance.export.builder.buildError");
                LOGGER.error(msg);
            }

            if (workbook != null && report != null) {
                String filename = report.getYear() + "-" + report.getAcb().getName() + "-annual-report";
                //write out the workbook contents to this file
                OutputStream outputStream = null;
                try {
                    writtenFile = File.createTempFile(filename, ".xlsx");
                    outputStream = new FileOutputStream(writtenFile);
                    LOGGER.info("Writing annual report file to " + writtenFile.getAbsolutePath());
                    workbook.write(outputStream);
                } catch (final Exception ex) {
                    String msg = msgUtil.getMessage("report.annualSurveillance.export.writeError");
                    LOGGER.error(msg);
                } finally {
                    try { outputStream.flush(); } catch (Exception ignore) {}
                    try { outputStream.close(); } catch (Exception ignore) {}
                }
            }
            else {
                //TODO: send email with failure, figure out how to send email in all failure states
            }

            List<File> fileAttachments = new ArrayList<File>();
            if (writtenFile != null) {
                fileAttachments.add(writtenFile);
            }
            //TODO: send email with attachments
        }
        LOGGER.info("********* Completed the Annual Report Generation job. *********");
    }

    private void sendEmail(String recipientEmail, String subject, String htmlMessage)
            throws MessagingException {
        LOGGER.info("Sending email to: " + recipientEmail);
        LOGGER.info("Message to be sent: " + htmlMessage);

        EmailBuilder emailBuilder = new EmailBuilder(env);
        emailBuilder.recipient(recipientEmail)
                .subject(subject)
                .htmlMessage(htmlMessage)
                .acbAtlHtmlFooter()
                .sendEmail();
    }
}
