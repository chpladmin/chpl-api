package gov.healthit.chpl.scheduler.job.ics;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.scheduler.job.QuartzJob;
import gov.healthit.chpl.search.ListingSearchService;
import gov.healthit.chpl.search.domain.ListingSearchResponse;
import gov.healthit.chpl.search.domain.ListingSearchResult;
import gov.healthit.chpl.search.domain.SearchRequest;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "icsErrorsReportCreatorJobLogger")
@DisallowConcurrentExecution
public class IcsErrorsReportCreatorJob extends QuartzJob {
    private static final String EDITION_2015 = "2015";

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

        List<ListingSearchResult> relevantListings = getAllPagesOfSearchResults(SearchRequest.builder()
                .certificationEditions(Stream.of(EDITION_2015).collect(Collectors.toSet()))
                .build());
        LOGGER.info("Checking " + relevantListings.size() + " for ICS Errors.");

        ExecutorService executorService = null;
        try {
            Integer threadPoolSize = getThreadCountForJob();
            executorService = Executors.newFixedThreadPool(threadPoolSize);
            List<IcsErrorsReport> allIcsErrors = new ArrayList<IcsErrorsReport>();

            List<CompletableFuture<Void>> futures = new ArrayList<CompletableFuture<Void>>();
            for (ListingSearchResult result : relevantListings) {
                futures.add(CompletableFuture.supplyAsync(() -> getCertifiedProductSearchDetails(result.getId()), executorService)
                        .thenApply(cp -> check(cp))
                        .thenAccept(error -> {
                            if (error != null) {
                                allIcsErrors.add(error);
                            }
                        }));
            }

            CompletableFuture<Void> combinedFutures = CompletableFuture
                    .allOf(futures.toArray(new CompletableFuture[futures.size()]));

            // This is not blocking - presumably because the job executes using it's own ExecutorService
            // This is necessary so that the system can indicate that the job and it's threads are still running
            combinedFutures.get();
            LOGGER.info("All processes have completed");
            saveAllIcsErrorReports(allIcsErrors);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            executorService.shutdown();
        }
        LOGGER.info("Completed the Inheritance Error Report Creator job. *********");
    }

    private List<ListingSearchResult> getAllPagesOfSearchResults(SearchRequest searchRequest) {
        List<ListingSearchResult> searchResults = new ArrayList<ListingSearchResult>();
        try {
            LOGGER.debug(searchRequest.toString());
            ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);
            searchResults.addAll(searchResponse.getResults());
            while (searchResponse.getRecordCount() > searchResults.size()) {
                searchRequest.setPageSize(searchResponse.getPageSize());
                searchRequest.setPageNumber(searchResponse.getPageNumber() + 1);
                LOGGER.debug(searchRequest.toString());
                searchResponse = listingSearchService.findListings(searchRequest);
                searchResults.addAll(searchResponse.getResults());
            }
        } catch (ValidationException ex) {
            LOGGER.error("Could not query all search results.", ex);
        }
        return searchResults;
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

    private IcsErrorsReport check(CertifiedProductSearchDetails listing) {
        if (listing == null) {
            return null;
        }
        LOGGER.info("Checking listing [" + listing.getChplProductNumber() + "]");
        IcsErrorsReport report = null;
        try {
            String icsErrorMessage = icsErrorDiscoveryService.getIcsErrorMessage(listing);
            if (!StringUtils.isEmpty(icsErrorMessage)) {
                report = new IcsErrorsReport();
                report.setListingId(listing.getId());
                report.setChplProductNumber(listing.getChplProductNumber());
                report.setDeveloper(listing.getDeveloper().getName());
                report.setProduct(listing.getProduct().getName());
                report.setVersion(listing.getVersion().getVersion());
                report.setCertificationBody(getCertificationBody(Long.parseLong(
                        listing.getCertifyingBody().get(CertifiedProductSearchDetails.ACB_ID_KEY).toString())));
                report.setReason(icsErrorMessage);
                LOGGER.info("Found ICS Error for listing [" + listing.getChplProductNumber() + "]: " + icsErrorMessage);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        LOGGER.info("Completed check of listing [" + listing.getChplProductNumber() + "]");
        return report;
    }

    private void saveAllIcsErrorReports(List<IcsErrorsReport> allIcsErrorReports) {

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
                    LOGGER.info("Creating {} current inheritance errors", allIcsErrorReports.size());
                    icsErrorsReportDao.create(allIcsErrorReports);
                } catch (Exception e) {
                    LOGGER.error("Error updating to latest inheritance errors. Rolling back transaction.", e);
                    status.setRollbackOnly();
                }
            }
        });
    }

    private CertificationBody getCertificationBody(Long certificationBodyId) throws EntityRetrievalException {
        CertificationBodyDTO acb = certificationBodyDAO.getById(certificationBodyId);
        return new CertificationBody(acb);
    }

    private Integer getThreadCountForJob() throws NumberFormatException {
        return Integer.parseInt(env.getProperty("executorThreadCountForQuartzJobs"));
    }
}
