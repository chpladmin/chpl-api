package gov.healthit.chpl.scheduler.job.urluptime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.Query;
import javax.transaction.Transactional;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.contact.PointOfContact;
import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.email.footer.AdminFooter;
import gov.healthit.chpl.entity.auth.UserContactEntity;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.scheduler.job.QuartzJob;
import lombok.NoArgsConstructor;
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
    private DeveloperAccessDAO developerAccessDAO;

    @Autowired
    private JpaTransactionManager txManager;

    @Autowired
    private Environment env;

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
                    sendEmail(context, getReportRows());
                } catch (Exception e) {
                    LOGGER.error(e);
                }
            }
        });
        LOGGER.info("********* Completed the Service Base Url List Uptime Email job *********");
    }

    private List<ServiceBaseUrlListUptimeReport> getReportRows() {
        List<ServiceBaseUrlListUptimeReport> reportRows = serviceBaseUrlListUptimeCalculator.calculateRowsForReport();
        reportRows.forEach(row -> row.setDeveloperEmails(developerAccessDAO.getContactForDeveloperUsers(row.getDeveloperId())));
        return reportRows;
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

    @Component
    @NoArgsConstructor
    private static class DeveloperAccessDAO extends BaseDAOImpl {

        @Transactional
        public List<PointOfContact> getContactForDeveloperUsers(Long developerId) {
            List<PointOfContact> contacts = new ArrayList<PointOfContact>();
            Query query = entityManager.createQuery("SELECT contact "
                    + "FROM UserDeveloperMapEntity udm "
                    + "JOIN udm.developer developer "
                    + "JOIN udm.user u "
                    + "JOIN u.contact contact "
                    + "WHERE udm.deleted = false "
                    + "AND developer.deleted = false "
                    + "AND u.deleted = false "
                    + "AND u.accountExpired = false "
                    + "AND u.accountEnabled = true "
                    + "AND contact.deleted = false "
                    + "AND (developer.id = :developerId)", UserContactEntity.class);
            query.setParameter("developerId", developerId);
            List<UserContactEntity> queryResults = query.getResultList();
            if (queryResults == null || queryResults.size() == 0) {
                return contacts;
            }
            for (UserContactEntity queryResult : queryResults) {
                PointOfContact contact = new PointOfContact();
                contact.setEmail(queryResult.getEmail());
                contact.setFullName(queryResult.getFullName());
                contacts.add(contact);
            }
            return contacts;
        }
    }
}
