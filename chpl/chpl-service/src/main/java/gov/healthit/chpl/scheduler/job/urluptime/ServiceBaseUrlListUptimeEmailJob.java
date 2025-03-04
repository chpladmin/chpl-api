package gov.healthit.chpl.scheduler.job.urluptime;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
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
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.email.footer.AdminFooter;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.CertificationBodyManager;
import gov.healthit.chpl.manager.DeveloperManager;
import gov.healthit.chpl.scheduler.job.QuartzJob;
import gov.healthit.chpl.search.ListingSearchService;
import gov.healthit.chpl.search.domain.ListingSearchResult;
import gov.healthit.chpl.search.domain.SearchRequest;
import gov.healthit.chpl.util.CertificationStatusUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "serviceBaseUrlListUptimeEmailJobLogger")
public class ServiceBaseUrlListUptimeEmailJob extends QuartzJob {
    private static final Integer MAX_PAGE_SIZE = 100;

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
    private DeveloperManager developerManager;

    @Autowired
    private CertificationBodyManager certificationBodyManager;

    @Autowired
    private ListingSearchService listingSearchService;

    @Autowired
    private JpaTransactionManager txManager;


    @Autowired
    private Environment env;

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
                    //developerIdAndCertificationBodyMap = getDeveloperIdAndCertificationBodyMap();
                    activeAcbs = certificationBodyDAO.findAllActive();

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
        reportRows.forEach(row -> {
            row.setDeveloperEmails(developerDAO.getContactForDeveloperUsers(row.getDeveloperId()));
            row.setApplicableAcbsMap(getApplicableAcbsForDeveloper(row.getDeveloperId()));
        });

        return reportRows;
    }

    private Map<Long, Boolean> getApplicableAcbsForDeveloper(Long developerId) {
        Set<CertificationBody> acbsForDeveloper = getAssociatedCertificationBodies(developerId);
        if (CollectionUtils.isEmpty(acbsForDeveloper)) {
            LOGGER.warn("The developer " + developerId + " has no associated ACBs and will not be included in the report.");
            return new HashMap<Long, Boolean>();
        }

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
        .htmlMessage(createHtmlMessage())
        .fileAttachments(Arrays.asList(serviceBaseUrlListUptimeCsvWriter.generateFile(rows)))
        .sendEmail();
        LOGGER.info("Completed Sending email to: " + context.getMergedJobDataMap().getString("email"));
    }

    private String createHtmlMessage() {
        return chplHtmlEmailBuilder.initialize()
                .heading(env.getProperty("serviceBaseUrlListUptime.report.subject"))
                .paragraph("", env.getProperty("serviceBaseUrlListUptime.report.paragraph1.body"))
                .paragraph("", env.getProperty("serviceBaseUrlListUptime.report.paragraph2.body"))
                .paragraph("", env.getProperty("serviceBaseUrlListUptime.report.paragraph3.body"))
                .footer(AdminFooter.class)
                .build();
    }

    public Set<CertificationBody> getAssociatedCertificationBodies(Long developerId) {
        try {
            return getListingDataForDeveloper(developerManager.getById(developerId)).stream()
                    .map(listing -> listing.getCertificationBody())
                    .collect(Collectors.toSet()).stream()
                    .map(pair -> getCertificationBody(pair.getId()))
                    .collect(Collectors.toSet());
        } catch (ValidationException | EntityRetrievalException e) {
            LOGGER.error("Could not identify Certification Body for Developer with id: {}", developerId);
            return null;
        }
    }

    private CertificationBody getCertificationBody(Long acbId) {
        try {
            return certificationBodyManager.getById(acbId);
        } catch (Exception e) {
            LOGGER.error("Could not identify Certification Body with id: {}", acbId);
            return null;
        }
    }

    private List<ListingSearchResult> getListingDataForDeveloper(Developer developer) throws ValidationException {
        SearchRequest request = SearchRequest.builder()
                .certificationStatuses(CertificationStatusUtil.getActiveStatuses().stream().map(status -> status.getName()).collect(Collectors.toSet()))
                .developer(developer.getName())
                .pageSize(MAX_PAGE_SIZE)
                .build();
        return listingSearchService.getAllPagesOfSearchResults(request);
    }
}
