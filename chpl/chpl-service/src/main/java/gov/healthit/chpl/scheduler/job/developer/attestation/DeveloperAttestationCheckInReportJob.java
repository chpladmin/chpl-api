package gov.healthit.chpl.scheduler.job.developer.attestation;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.dao.auth.UserDAO;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "developerAttestationCheckinReportJobLogger")
public class DeveloperAttestationCheckInReportJob implements Job {

    @Autowired
    private JpaTransactionManager txManager;

    @Autowired
    private CheckInReportDataCollector developerAttestationCheckInReportDataCollection;

    @Autowired
    private CheckInReportSummaryDataCollector developerAttestationCheckInReportSummaryDataCollection;

    @Autowired
    private CheckInReportCsvWriter developerAttestationCheckInReportCsvWriter;

    @Autowired
    private ChplHtmlEmailBuilder chplHtmlEmailBuilder;

    @Autowired
    private ChplEmailFactory chplEmailFactory;
    
    @Autowired
    private UserDAO userDao;

    @Value("${developer.attestation.checkin.report.subject}")
    private String emailSubject;

    @Value("${developer.attestation.checkin.report.body}")
    private String emailBody;

    @Value("${developer.attestation.checkin.report.body2}")
    private String emailBody2;

    @Value("${developer.attestation.checkin.report.body3}")
    private String emailBody3;

    @Value("${developer.attestation.checkin.report.sectionHeading}")
    private String sectionHeading;

    @Value("${chpl.email.greeting}")
    private String chplEmailGreeting;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        LOGGER.info("********* Starting Developer Attestation Check-in Report job. *********");

        // We need to manually create a transaction in this case because of how AOP works. When a method is
        // annotated with @Transactional, the transaction wrapper is only added if the object's proxy is called.
        // The object's proxy is not called when the method is called from within this class. The object's proxy
        // is called when the method is public and is called from a different object.
        // https://stackoverflow.com/questions/3037006/starting-new-transaction-in-spring-bean
        TransactionTemplate txTemplate = new TransactionTemplate(txManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        txTemplate.execute(new TransactionCallbackWithoutResult() {

            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                	setSecurityContext(userDao.getById(User.ADMIN_USER_ID));
                    
                    List<CheckInReport> reportRows = developerAttestationCheckInReportDataCollection.collect();
                    CheckInReportSummary reportSummary = developerAttestationCheckInReportSummaryDataCollection.collect(reportRows);
                    File csv = developerAttestationCheckInReportCsvWriter.generateFile(reportRows);
                    chplEmailFactory.emailBuilder()
                            .recipient(context.getMergedJobDataMap().getString("email"))
                            .subject(emailSubject)
                            .fileAttachments(Arrays.asList(csv))
                            .htmlMessage(chplHtmlEmailBuilder.initialize()
                                    .heading(sectionHeading)
                                    .paragraph("", emailBody)
                                    .paragraph("", String.format(emailBody2,
                                            reportSummary.getDeveloperCount(),
                                            reportSummary.doCountsEqualDeveloperCount() ? "" : "*",
                                            reportSummary.getAttestationsApprovedCount(),
                                            reportSummary.getPendingAcbActionCount(),
                                            reportSummary.getPendingDeveloperActionCount(),
                                            reportSummary.getNoSubmissionCount()))
                                    .paragraph("", reportSummary.doCountsEqualDeveloperCount() ? "" : emailBody3)
                                    .footer(true)
                                    .build())
                            .sendEmail();
                    LOGGER.info("Report sent to: {}", context.getMergedJobDataMap().getString("email"));
                } catch (Exception e) {
                    LOGGER.catching(e);
                }
            }
        });
        LOGGER.info("********* Completed Developer Attestation Check-in Report job. *********");
    }
    
    private void setSecurityContext(UserDTO user) {
        JWTAuthenticatedUser splitUser = new JWTAuthenticatedUser();
        splitUser.setFullName(user.getFullName());
        splitUser.setId(user.getId());
        splitUser.setFriendlyName(user.getFriendlyName());
        splitUser.setSubjectName(user.getUsername());
        splitUser.getPermissions().add(user.getPermission().getGrantedPermission());

        SecurityContextHolder.getContext().setAuthentication(splitUser);
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }

}
