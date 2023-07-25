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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.email.footer.AdminFooter;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.surveillance.report.SurveillanceReportManager;
import gov.healthit.chpl.surveillance.report.builder.QuarterlyReportBuilderXlsx;
import gov.healthit.chpl.surveillance.report.builder.ReportBuilderFactory;
import gov.healthit.chpl.surveillance.report.builder.SurveillanceReportWorkbookWrapper;
import gov.healthit.chpl.surveillance.report.dto.QuarterlyReportDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@DisallowConcurrentExecution
@Log4j2(topic = "quarterlyReportGenerationJobLogger")
public class QuarterlyReportGenerationJob implements Job {
    public static final String JOB_NAME = "quarterlyReportGenerationJob";
    public static final String QUARTERLY_REPORT_ID_KEY = "quarterLyReportId";
    public static final String USER_KEY = "user";

    @Autowired
    private ChplHtmlEmailBuilder chplHtmlEmailBuilder;

    @Value("${chpl.email.valediction}")
    private String chplEmailValediction;

    @Value("${contact.acbatlUrl}")
    private String acbatlFeedbackUrl;

    @Value("${surveillance.quarterlyReport.success.subject}")
    private String quarterlyReportSubject;

    @Value("${surveillance.quarterlyReport.failure.subject}")
    private String quarterlyReportFailureSubject;

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

        LOGGER.info("********* Starting the Quarterly Report Generation job. *********");

        JobDataMap jobDataMap = jobContext.getMergedJobDataMap();
        boolean isJobDataValid = isJobDataValid(jobDataMap);
        if (isJobDataValid) {
            UserDTO user = (UserDTO) jobDataMap.get(USER_KEY);
            setSecurityContext(user);
            Long quarterlyReportId = (Long) jobDataMap.get(QUARTERLY_REPORT_ID_KEY);

            TransactionTemplate txTemplate = new TransactionTemplate(txManager);
            txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            txTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    QuarterlyReportDTO report = null;
                    try {
                        report = reportManager.getQuarterlyReport(quarterlyReportId);
                    } catch (EntityRetrievalException ex) {
                        String msg = msgUtil.getMessage("report.quarterlySurveillance.export.badId", quarterlyReportId);
                        LOGGER.error(msg, ex);
                        sendEmail(user.getEmail(), quarterlyReportFailureSubject,
                                env.getProperty("surveillance.quarterlyReport.reportNotFound.htmlBody"), null);
                    }

                    if (report != null) {
                        SurveillanceReportWorkbookWrapper workbookWrapper = createWorkbook(report);
                        if (workbookWrapper == null) {
                            sendEmail(user.getEmail(), quarterlyReportFailureSubject,
                                    env.getProperty("surveillance.quarterlyReport.fileError.htmlBody"), null);
                        } else {
                            File writtenFile = writeWorkbookAsFile(report, workbookWrapper);
                            if (writtenFile == null) {
                                sendEmail(user.getEmail(), quarterlyReportFailureSubject,
                                        env.getProperty("surveillance.quarterlyReport.fileError.htmlBody"), null);
                            } else {
                                List<File> fileAttachments = new ArrayList<File>();
                                if (writtenFile != null) {
                                    fileAttachments.add(writtenFile);
                                }
                                LOGGER.info("Sending success email to " + user.getEmail());
                                sendEmail(user.getEmail(),
                                        String.format(quarterlyReportSubject, report.getQuarter().getName()),
                                        env.getProperty("surveillance.quarterlyReport.success.htmlBody"), fileAttachments);
                                try {
                                    activityManager.addActivity(ActivityConcept.QUARTERLY_REPORT, quarterlyReportId,
                                        "Exported quarterly report.", null, report);
                                } catch (JsonProcessingException | EntityRetrievalException | EntityCreationException ex) {
                                    LOGGER.error("Error adding quarterly report activity.", ex);
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
                sendEmail(user.getEmail(), quarterlyReportFailureSubject,
                        env.getProperty("surveillance.quarterlyReport.badJobData.htmlBody"), null);
            }
        }
        LOGGER.info("********* Completed the Quarterly Report Generation job. *********");
    }

    private boolean isJobDataValid(JobDataMap jobDataMap) {
        boolean isValid = true;
        UserDTO user = (UserDTO) jobDataMap.get(USER_KEY);
        if (user == null) {
            isValid = false;
            LOGGER.fatal("No user could be found in the job data.");
        }

        Long quarterlyReportId = (Long) jobDataMap.get(QUARTERLY_REPORT_ID_KEY);
        if (quarterlyReportId == null) {
            isValid = false;
            LOGGER.fatal("No quarterly report ID could be found in the job data.");
        }
        return isValid;
    }

    private SurveillanceReportWorkbookWrapper createWorkbook(QuarterlyReportDTO report) {
        SurveillanceReportWorkbookWrapper workbook = null;
        try {
                QuarterlyReportBuilderXlsx reportBuilder = reportBuilderFactory.getReportBuilder(report);
                if (reportBuilder != null) {
                    workbook = reportBuilder.buildXlsx(report);
                } else {
                    String msg = msgUtil.getMessage("report.quarterlySurveillance.builderNotFound");
                    LOGGER.error(msg + " Report id " + report.getId());
                }
        } catch (IOException io) {
            String msg = msgUtil.getMessage("report.quarterlySurveillance.export.builder.buildError");
            LOGGER.error(msg, io);
        } catch (Exception general) {
            //catch any other type of exception
            String msg = msgUtil.getMessage("report.annualSurveillance.export.builder.buildError");
            LOGGER.error(msg, general);
        }
        return workbook;
    }

    private File writeWorkbookAsFile(QuarterlyReportDTO report, SurveillanceReportWorkbookWrapper workbookWrapper) {
        File writtenFile = null;
        String filename = getFilename(report);
        //write out the workbook contents to this file
        OutputStream outputStream = null;
        try {
            writtenFile = File.createTempFile(filename, ".xlsx");
            outputStream = new FileOutputStream(writtenFile);
            LOGGER.info("Writing quarterly report file to " + writtenFile.getAbsolutePath());
            workbookWrapper.getWorkbook().write(outputStream);
        } catch (Exception ex) {
            String msg = msgUtil.getMessage("report.quarterlySurveillance.export.writeError");
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

    private String getFilename(QuarterlyReportDTO report) {
        return report.getQuarter().getName() + "-" + report.getYear() + "-" + report.getAcb().getName() + "-quarterly-report";
    }

    private void setSecurityContext(UserDTO user) {
        JWTAuthenticatedUser mergeUser = new JWTAuthenticatedUser();
        mergeUser.setFullName(user.getFullName());
        mergeUser.setId(user.getId());
        mergeUser.setFriendlyName(user.getFriendlyName());
        mergeUser.setSubjectName(user.getUsername());
        mergeUser.getPermissions().add(user.getPermission().getGrantedPermission());

        SecurityContextHolder.getContext().setAuthentication(mergeUser);
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }

    private void sendEmail(String recipientEmail, String subject, String htmlContent, List<File> attachments)  {
        LOGGER.info("Sending email to: " + recipientEmail);
        LOGGER.info("Message to be sent: " + htmlContent);

        try {
            chplEmailFactory.emailBuilder()
                    .recipient(recipientEmail)
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
