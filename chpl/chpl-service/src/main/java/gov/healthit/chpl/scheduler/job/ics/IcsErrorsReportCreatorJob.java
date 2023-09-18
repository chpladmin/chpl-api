package gov.healthit.chpl.scheduler.job.ics;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.scheduler.job.QuartzJob;
import gov.healthit.chpl.search.ListingSearchService;
import gov.healthit.chpl.search.domain.ListingSearchResult;
import gov.healthit.chpl.search.domain.SearchRequest;
import gov.healthit.chpl.util.CertificationStatusUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "icsErrorsReportCreatorJobLogger")
@DisallowConcurrentExecution
public class IcsErrorsReportCreatorJob extends QuartzJob {

    @Autowired
    private ListingSearchService listingSearchService;

    @Autowired
    private ListingIcsErrorDiscoveryService icsErrorDiscoveryService;

    @Autowired
    private IcsErrorsReportDao icsErrorsReportDao;

    @Autowired
    private CertifiedProductDetailsManager certifiedProductDetailsManager;

    @Autowired
    private CertificationBodyDAO certificationBodyDAO;

    @Autowired
    private JpaTransactionManager txManager;

    @Autowired
    private Environment env;

    public IcsErrorsReportCreatorJob() throws Exception {
        super();
    }

    @Override
    @Transactional
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        LOGGER.info("********* Starting the Inheritance Error Report Creator job. *********");

        List<ListingSearchResult> relevantListings = new ArrayList<ListingSearchResult>();
        try {
            relevantListings = listingSearchService.getAllPagesOfSearchResults(SearchRequest.builder()
                .certificationStatuses(CertificationStatusUtil.getActiveStatusNames().stream().collect(Collectors.toSet()))
                .build());
        } catch (ValidationException ex) {
            LOGGER.fatal("Invalid search request provided.", ex);
        }

        LOGGER.info("Checking " + relevantListings.size() + " for ICS Errors.");

        ExecutorService executorService = null;
        try {
            Integer threadPoolSize = getThreadCountForJob();
            executorService = Executors.newFixedThreadPool(threadPoolSize);
            List<IcsErrorsReportItem> allIcsErrorsReportItems = new ArrayList<IcsErrorsReportItem>();

            List<CompletableFuture<Void>> futures = new ArrayList<CompletableFuture<Void>>();
            for (ListingSearchResult result : relevantListings) {
                futures.add(CompletableFuture.supplyAsync(() -> getCertifiedProductSearchDetails(result.getId()), executorService)
                        .thenApply(cp -> check(cp))
                        .thenAccept(listingIcsErrorsReportItems -> {
                            if (!CollectionUtils.isEmpty(listingIcsErrorsReportItems)) {
                                allIcsErrorsReportItems.addAll(listingIcsErrorsReportItems);
                            }
                        }));
            }

            CompletableFuture<Void> combinedFutures = CompletableFuture
                    .allOf(futures.toArray(new CompletableFuture[futures.size()]));

            // This is not blocking - presumably because the job executes using it's own ExecutorService
            // This is necessary so that the system can indicate that the job and it's threads are still running
            combinedFutures.get();
            LOGGER.info("All processes have completed");
            saveAllIcsErrorReports(allIcsErrorsReportItems);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            executorService.shutdown();
        }
        LOGGER.info("Completed the Inheritance Error Report Creator job. *********");
    }

    private CertifiedProductSearchDetails getCertifiedProductSearchDetails(Long id) {
        CertifiedProductSearchDetails cp = null;
        try {
            cp = certifiedProductDetailsManager.getCertifiedProductDetails(id);
            LOGGER.info("Completed retrieval of listing [" + cp.getChplProductNumber() + "]");
        } catch (Exception e) {
            LOGGER.error("Could not retrieve listing [" + id + "] - " + e.getMessage(), e);
        }
        return cp;
    }

    private List<IcsErrorsReportItem> check(CertifiedProductSearchDetails listing) {
        if (listing == null) {
            return null;
        }
        LOGGER.info("Checking listing [" + listing.getId() + "]");

        List<IcsErrorsReportItem> reportItems = new ArrayList<IcsErrorsReportItem>();
        try {
            List<String> icsErrorMessages = icsErrorDiscoveryService.getIcsErrorMessages(listing);
            if (!CollectionUtils.isEmpty(icsErrorMessages)) {
                reportItems = icsErrorMessages.stream()
                        .filter(errorMessage -> !StringUtils.isEmpty(errorMessage))
                        .map(errorMessage -> createIcsErrorsReportItem(listing, errorMessage))
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        LOGGER.info("Completed check of listing [" + listing.getId() + "]");
        return reportItems;
    }

    private IcsErrorsReportItem createIcsErrorsReportItem(CertifiedProductSearchDetails listing, String icsErrorMessage) {
        LOGGER.info("Found ICS Error for listing [" + listing.getId() + "]: " + icsErrorMessage);

        CertificationBody acb = getCertificationBody(Long.parseLong(
                listing.getCertifyingBody().get(CertifiedProductSearchDetails.ACB_ID_KEY).toString()));

        return IcsErrorsReportItem.builder()
                .listingId(listing.getId())
                .certificationBody(acb)
                .reason(icsErrorMessage)
                .build();
    }

    private void saveAllIcsErrorReports(List<IcsErrorsReportItem> allIcsErrorReportItems) {

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
                    LOGGER.info("Deleting {} OBE inheritance errors", icsErrorsReportDao.findAll().size());
                    icsErrorsReportDao.deleteAll();
                    LOGGER.info("Creating {} current inheritance errors", allIcsErrorReportItems.size());
                    icsErrorsReportDao.create(allIcsErrorReportItems);
                } catch (Exception e) {
                    LOGGER.error("Error updating to latest inheritance errors. Rolling back transaction.", e);
                    status.setRollbackOnly();
                }
            }
        });
    }

    private CertificationBody getCertificationBody(Long certificationBodyId) {
        CertificationBody acb = null;
        try {
            acb = certificationBodyDAO.getById(certificationBodyId);
        } catch (Exception ex) {
            LOGGER.error("No ACB could be found with ID " + certificationBodyId);
        }
        return acb;
    }

    private Integer getThreadCountForJob() throws NumberFormatException {
        return Integer.parseInt(env.getProperty("executorThreadCountForQuartzJobs"));
    }
}
