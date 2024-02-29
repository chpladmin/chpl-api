package gov.healthit.chpl.scheduler.job.urluptime;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.email.footer.AdminFooter;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.scheduler.job.QuartzJob;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "serviceBaseUrlListUptimeEmailJobLogger")
public class ServiceBaseUrlListUptimeEmailJob extends QuartzJob {

    @Autowired
    private ChplHtmlEmailBuilder chplHtmlEmailBuilder;

    @Autowired
    private ChplEmailFactory chplEmailFactory;

    @Autowired
    private ServiceBaseUrlListUptimeCalculator serviceBaseUrlListUptimeCalculator;

    @Autowired
    private ServiceBaseUrlListUptimeCsvWriter serviceBaseUrlListUptimeCsvWriter;

    @Autowired
    private DeveloperDAO developerDAO;

    @Autowired
    private CertificationBodyDAO certificationBodyDAO;

    @Autowired
    private JpaTransactionManager txManager;

    @Autowired
    private Environment env;

    private Map<Long, Set<CertificationBody>> developerIdAndCertificationBodyMap;
    private List<CertificationBody> activeAcbs;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Service Base Url List Uptime Email job *********");
        TransactionTemplate txTemplate = new TransactionTemplate(txManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                    developerIdAndCertificationBodyMap = getDeveloperIdAndCertificationBodyMap();
                    activeAcbs = certificationBodyDAO.findAllActive();

                    sendEmail(context, getReportRows());
                } catch (Exception e) {
                    LOGGER.error(e);
                }
            }
        });
        LOGGER.info("********* Completed the Service Base Url List Uptime Email job *********");
    }

    private Map<Long, Set<CertificationBody>> getDeveloperIdAndCertificationBodyMap() {
        return developerDAO.findAllDevelopersWithAcbs().entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey().getId(), entry -> entry.getValue()));
    }

    private List<ServiceBaseUrlListUptimeReport> getReportRows() {
        List<ServiceBaseUrlListUptimeReport> reportRows = serviceBaseUrlListUptimeCalculator.calculateRowsForReport();
        reportRows.forEach(row -> {
            row.setDeveloperEmails(developerDAO.getContactForDeveloperUsers(row.getDeveloperId()));
            row.setApplicableAcbsMap(getApplicableAcbsForDeveloper(row.getDeveloperId()));
        });

        return reportRows;
    }

    private Map<Long, Boolean> getApplicableAcbsForDeveloper(Long developerId) {
        Set<CertificationBody> acbsForDeveloper = developerIdAndCertificationBodyMap.get(developerId);

        return activeAcbs.stream()
                .collect(Collectors.toMap(
                        acb -> acb.getId(),
                        acb -> acbsForDeveloper.stream()
                                .filter(acbForDev -> acbForDev.getId().equals(acb.getId()))
                                .findAny().isPresent()));

    }

    private void sendEmail(JobExecutionContext context, List<ServiceBaseUrlListUptimeReport> rows) throws EmailNotSentException, IOException {
        LOGGER.info("Sending email to: " + context.getMergedJobDataMap().getString("email"));
        chplEmailFactory.emailBuilder()
                .recipient(context.getMergedJobDataMap().getString("email"))
                .subject(env.getProperty("serviceBaseUrlListUptime.report.subject"))
                .htmlMessage(createHtmlMessage(context, rows.size()))
                .fileAttachments(Arrays.asList(serviceBaseUrlListUptimeCsvWriter.generateFile(rows)))
                .sendEmail();
        LOGGER.info("Completed Sending email to: " + context.getMergedJobDataMap().getString("email"));
    }

    private String createHtmlMessage(JobExecutionContext context, int errorCount) {
        return chplHtmlEmailBuilder.initialize()
                .heading(env.getProperty("serviceBaseUrlListUptime.report.subject"))
                .paragraph("", env.getProperty("serviceBaseUrlListUptime.report.paragraph1.body"))
                .paragraph("", env.getProperty("serviceBaseUrlListUptime.report.paragraph2.body"))
                .paragraph("", env.getProperty("serviceBaseUrlListUptime.report.paragraph3.body"))
                .footer(AdminFooter.class)
                .build();
    }
}
