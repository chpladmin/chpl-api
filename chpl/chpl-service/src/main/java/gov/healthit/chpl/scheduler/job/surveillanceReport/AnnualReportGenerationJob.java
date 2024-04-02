package gov.healthit.chpl.scheduler.job.surveillanceReport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.email.EmailBuilder;
import gov.healthit.chpl.email.footer.AdminFooter;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.scheduler.SecurityContextCapableJob;
import gov.healthit.chpl.surveillance.report.SurveillanceReportManager;
import gov.healthit.chpl.surveillance.report.builder.AnnualReportBuilderXlsx;
import gov.healthit.chpl.surveillance.report.builder.ReportBuilderFactory;
import gov.healthit.chpl.surveillance.report.builder.SurveillanceReportWorkbookWrapper;
import gov.healthit.chpl.surveillance.report.domain.AnnualReport;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@DisallowConcurrentExecution
@Log4j2(topic = "annualReportGenerationJobLogger")
public class AnnualReportGenerationJob extends SecurityContextCapableJob implements Job {
    public static final String JOB_NAME = "annualReportGenerationJob";
    public static final String ANNUAL_REPORT_ID_KEY = "annualReportId";
    public static final String USER_KEY = "user";

    @Autowired
    private ChplHtmlEmailBuilder chplHtmlEmailBuilder;

    @Value("${chpl.email.valediction}")
    private String chplEmailValediction;

    @Value("${contact.acbatlUrl}")
    private String acbatlFeedbackUrl;

    @Autowired
    private JpaTransactionManager txManager;

    @Autowired
    private ErrorMessageUtil msgUtil;

    @Autowired
    private SurveillanceReportManager reportManager;

    @Autowired
    private ReportBuilderFactory reportBuilderFactory;

    @Autowired
    private ActivityManager activityManager;

    @Autowired
    private Environment env;

    @Autowired
    private ChplEmailFactory chplEmailFactory;

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        LOGGER.info("********* Starting the Annual Report Generation job. *********");

        JobDataMap jobDataMap = jobContext.getMergedJobDataMap();
        boolean isJobDataValid = isJobDataValid(jobDataMap);
        if (isJobDataValid) {
            UserDTO user = (UserDTO) jobDataMap.get(USER_KEY);
            setSecurityContext(user);
            Long annualReportId = (Long) jobDataMap.get(ANNUAL_REPORT_ID_KEY);

            TransactionTemplate txTemplate = new TransactionTemplate(txManager);
            txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            txTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    AnnualReport report = null;
                    try {
                        report = reportManager.getAnnualReport(annualReportId);
                    } catch (Exception ex) {
                        String msg = msgUtil.getMessage("report.annualSurveillance.export.badId", annualReportId);
                        LOGGER.error(msg, ex);
                        sendEmail(user.getEmail(), env.getProperty("surveillance.annualReport.failure.subject"),
                                env.getProperty("surveillance.annualReport.reportNotFound.htmlBody"), null);
                    }

                    if (report != null) {
                        SurveillanceReportWorkbookWrapper workbookWrapper = createWorkbook(report);
                        if (workbookWrapper == null) {
                            sendEmail(user.getEmail(), env.getProperty("surveillance.annualReport.failure.subject"),
                                    env.getProperty("surveillance.annualReport.fileError.htmlBody"), null);
                        } else {
                            File writtenFile = writeWorkbookAsFile(report, workbookWrapper);
                            if (writtenFile == null) {
                                sendEmail(user.getEmail(), env.getProperty("surveillance.annualReport.failure.subject"),
                                        env.getProperty("surveillance.annualReport.fileError.htmlBody"), null);
                            } else {
                                List<File> fileAttachments = new ArrayList<File>();
                                if (writtenFile != null) {
                                    fileAttachments.add(writtenFile);
                                }
                                LOGGER.info("Sending success email to " + user.getEmail());
                                sendEmail(user.getEmail(), env.getProperty("surveillance.annualReport.success.subject"),
                                        env.getProperty("surveillance.annualReport.success.htmlBody"), fileAttachments);
                                try {
                                    activityManager.addActivity(ActivityConcept.ANNUAL_REPORT, annualReportId,
                                        "Exported annual report.", null, report);
                                } catch (JsonProcessingException | EntityRetrievalException | EntityCreationException ex) {
                                    LOGGER.error("Error adding annual report activity.", ex);
                                }
                            }
                        }
                        workbookWrapper.close();
                    }
                }
            });
        } else {
            UserDTO user = (UserDTO) jobDataMap.get(USER_KEY);
            if (user != null && user.getEmail() != null) {
                sendEmail(user.getEmail(), env.getProperty("surveillance.annualReport.failure.subject"),
                        env.getProperty("surveillance.annualReport.badJobData.htmlBody"), null);
            }
        }
        LOGGER.info("********* Completed the Annual Report Generation job. *********");
    }

    private boolean isJobDataValid(JobDataMap jobDataMap) {
        boolean isValid = true;
        UserDTO user = (UserDTO) jobDataMap.get(USER_KEY);
        if (user == null) {
            isValid = false;
            LOGGER.fatal("No user could be found in the job data.");
        }

        Long annualReportId = (Long) jobDataMap.get(ANNUAL_REPORT_ID_KEY);
        if (annualReportId == null) {
            isValid = false;
            LOGGER.fatal("No annual report ID could be found in the job data.");
        }
        return isValid;
    }

    private SurveillanceReportWorkbookWrapper createWorkbook(AnnualReport report) {
        SurveillanceReportWorkbookWrapper workbook = null;
        try {
                AnnualReportBuilderXlsx reportBuilder = reportBuilderFactory.getReportBuilder(report);
                if (reportBuilder != null) {
                    workbook = reportBuilder.buildXlsx(report, LOGGER);
                } else {
                    String msg = msgUtil.getMessage("report.annualSurveillance.builderNotFound");
                    LOGGER.error(msg + " Report id " + report.getId());
                }
        } catch (IOException io) {
            String msg = msgUtil.getMessage("report.annualSurveillance.export.builder.buildError");
            LOGGER.error(msg, io);
        } catch (Exception general) {
            //catch any other type of exception
            String msg = msgUtil.getMessage("report.annualSurveillance.export.builder.buildError");
            LOGGER.error(msg, general);
        }
        return workbook;
    }

    private File writeWorkbookAsFile(AnnualReport report, SurveillanceReportWorkbookWrapper workbookWrapper) {
        File writtenFile = null;
        String filename = getFilename(report);
        //write out the workbook contents to this file
        OutputStream outputStream = null;
        try {
            writtenFile = File.createTempFile(filename, ".xlsx");
            outputStream = new FileOutputStream(writtenFile);
            LOGGER.info("Writing annual report file to " + writtenFile.getAbsolutePath());
            workbookWrapper.getWorkbook().write(outputStream);
        } catch (Exception ex) {
            String msg = msgUtil.getMessage("report.annualSurveillance.export.writeError");
            LOGGER.error(msg, ex);
        } finally {
            try {
                outputStream.flush();
            } catch (Exception ignore) {
            }
            try {
                outputStream.close();
            } catch (Exception ignore) {
            }
        }
        return writtenFile;
    }

    private String getFilename(AnnualReport report) {
        return report.getYear() + "-" + report.getAcb().getName() + "-annual-report";
    }

    private void sendEmail(String recipientEmail, String subject, String htmlContent, List<File> attachments)  {
        LOGGER.info("Sending email to: " + recipientEmail);
        LOGGER.info("Message to be sent: " + htmlContent);

        try {
            EmailBuilder emailBuilder = chplEmailFactory.emailBuilder();
            emailBuilder.recipient(recipientEmail)
                    .subject(subject)
                    .htmlMessage(chplHtmlEmailBuilder.initialize()
                            .heading(subject)
                            .paragraph("", htmlContent)
                            .paragraph("", String.format(chplEmailValediction, acbatlFeedbackUrl))
                            .footer(AdminFooter.class)
                            .build())
                    .fileAttachments(attachments)
                    .sendEmail();
        } catch (EmailNotSentException ex) {
            LOGGER.error("Could not send email to " + recipientEmail, ex);
        }
    }
}
