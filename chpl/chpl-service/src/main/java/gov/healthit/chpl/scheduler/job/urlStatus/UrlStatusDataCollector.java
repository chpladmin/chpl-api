package gov.healthit.chpl.scheduler.job.urlStatus;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.lang3.StringUtils;
import org.htmlunit.BrowserVersion;
import org.htmlunit.WebClient;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.datadog.api.client.ApiClient;

import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.scheduler.job.QuartzJob;
import gov.healthit.chpl.scheduler.job.urlStatus.data.UrlCallerAsync;
import gov.healthit.chpl.scheduler.job.urlStatus.data.UrlCheckerDao;
import gov.healthit.chpl.scheduler.job.urlStatus.data.UrlResult;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "urlStatusDataCollectorJobLogger")
public class UrlStatusDataCollector extends QuartzJob {
    private static final int SECONDS_TO_MILLIS = 1000;
    private static final long DAYS_TO_MILLIS = 24 * 60 * 60 * SECONDS_TO_MILLIS;
    private static final int BATCH_SIZE = 100;
    private static final int DEFAULT_INTERVAL_DAYS = 1;
    private static final int DEFAULT_TIMEOUT_SECONDS = 10;

    @Autowired
    private Environment env;

    @Autowired
    private UrlCheckerDao urlCheckerDao;

    @Autowired
    private UrlCallerAsync urlCallerAsync;

    private ExecutorService executorService;

    private int successCheckIntervalDays = DEFAULT_INTERVAL_DAYS;
    private int failureCheckIntervalDays = DEFAULT_INTERVAL_DAYS;
    private int redirectCheckIntervalDays = DEFAULT_INTERVAL_DAYS;
    private int requestTimeoutSeconds = DEFAULT_TIMEOUT_SECONDS;
    private  WebClient webClient;
    private Map<UrlResult, Future<Integer>> urlResponseCodeFuturesMap;

    public UrlStatusDataCollector() {
        urlResponseCodeFuturesMap = new LinkedHashMap<UrlResult, Future<Integer>>();
    }

    @Override
    @Transactional
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the URL Status Data Collector job. *********");

        ApiClient defaultClient = ApiClient.getDefaultApiClient();
        String datadogDefaultClientUserAgent = defaultClient.getUserAgent();
        LOGGER.info("User-Agent: " + datadogDefaultClientUserAgent);

        completeSetup();

        try {
            //get all urls in the system
            List<UrlResult> allSystemUrls = urlCheckerDao.getAllSystemUrls(LOGGER);
            LOGGER.info("Found " + allSystemUrls.size() + " urls in the system.");
            List<UrlResult> existingUrlResults = urlCheckerDao.getAllUrlResults();

            removeObsoleteUrlResults(existingUrlResults, allSystemUrls);
            addNewUrlResults(existingUrlResults, allSystemUrls);

            //determine which of the system urls should be checked now and check them in batches.
            int batchCount = (allSystemUrls.size() / BATCH_SIZE) + 1;
            LOGGER.info("Querying all system URLs in " + batchCount + " batches.");
            for (int currBatch = 0; currBatch < batchCount; currBatch++) {
                int batchBegin = currBatch * BATCH_SIZE;
                int batchEnd = Math.min(batchBegin + BATCH_SIZE, allSystemUrls.size());
                LOGGER.info("*** Batch " + currBatch + " (" + batchBegin + " - " + batchEnd + " ) ***");
                processUrls(currBatch, allSystemUrls.subList(batchBegin, batchEnd));
            }
        } catch (Exception ex) {
            LOGGER.error("Unable to complete job: " + ex.getMessage(), ex);
        } finally {
            try {
                webClient.close();
            } catch (final Exception ex) {
                LOGGER.error("Error closing the httpClient: " + ex.getMessage(), ex);
            }
        }
        LOGGER.info("********* Completed the URL Status Data Collector job. *********");
    }

    /**
     * Looks for any URL that we are currently saving a result for
     * but is no longer in the system.
     * @param existingUrlResults all the saved url results that currently exist
     * @param allSystemUrls all the urls that are in the system
     * @throws EntityRetrievalException if the url result to delete cannot be found in the database
     */
    private void removeObsoleteUrlResults(List<UrlResult> existingUrlResults,
            List<UrlResult> allSystemUrls) throws EntityRetrievalException {
        //determine if any url results are no longer needed because it no longer exists in the system
        for (UrlResult existingUrlResult : existingUrlResults) {
            boolean stillExists = false;
            for (UrlResult systemUrl : allSystemUrls) {
                if (existingUrlResult.equals(systemUrl)) {
                    stillExists = true;
                }
            }
            if (!stillExists) {
                LOGGER.info("The URL " + existingUrlResult.getUrl()
                    + " for the type " + existingUrlResult.getUrlType().getName()
                    + " no longer exists in the system and will not be checked.");
                urlCheckerDao.deleteUrlResult(existingUrlResult.getId());
            }
        }
    }

    /**
     * Check the currently saved URL results against the list of all URLs in the system.
     * If any system URL is found that does not have a result object then this method creates one.
     * @param existingUrlResults all the saved url results that currently exist
     * @param allSystemUrls all the urls that are in the system
     * @throws EntityCreationException if the new url result cannot be created in the database
     */
    private void addNewUrlResults(List<UrlResult> existingUrlResults,
            List<UrlResult> allSystemUrls) throws EntityCreationException {
        //add any urls to the result table that aren't already there and add them
        //will have null last checked date, response code, and response message initially
        for (UrlResult systemUrl : allSystemUrls) {
            boolean alreadyExists = false;
            for (UrlResult existingUrlResult : existingUrlResults) {
                if (existingUrlResult.equals(systemUrl)) {
                    alreadyExists = true;
                    BeanUtils.copyProperties(existingUrlResult, systemUrl);
                }
            }
            if (!alreadyExists) {
                LOGGER.info("The URL " + systemUrl.getUrl()
                    + " for the type " + systemUrl.getUrlType().getName()
                    + " will be added to the system.");
                UrlResult created = urlCheckerDao.createUrlResult(systemUrl);
                systemUrl.setId(created.getId());
            } else {
                LOGGER.info("The URL " + systemUrl.getUrl()
                + " for the type " + systemUrl.getUrlType().getName()
                + " exists in the system.");
            }
        }
    }

    /**
     * Query a list of URLs and handle their responses asynchronously.
     * @param batchNum the batch numbers, used for logging purposes
     * @param urlList the list of URLs to query and record response codes for
     * @throws EntityRetrievalException
     */
    private void processUrls(int batchNum, List<UrlResult> urlList) throws EntityRetrievalException {
        //make async requests for each url in the batch
        for (int batchIndex = 0; batchIndex < urlList.size(); batchIndex++) {
            UrlResult systemUrl = urlList.get(batchIndex);
            if (shouldUrlBeChecked(systemUrl)) {
                LOGGER.info(systemUrl.getUrl() + " for the type " + systemUrl.getUrlType().getName()
                    + " will be checked for validity.");
                try {
                   CompletableFuture<Integer> responseCodeFuture =
                           urlCallerAsync.getUrlResponseCodeFuture(systemUrl, webClient, executorService, LOGGER);
                   urlResponseCodeFuturesMap.put(systemUrl, responseCodeFuture);
                } catch (final Exception ex) {
                    LOGGER.info("Could not check URL " + systemUrl.getUrl()
                        + " due to exception " + ex.getMessage(), ex);
                }
            } else {
                LOGGER.info("Not checking " + systemUrl.getUrlType().getName() + " URL " + systemUrl.getUrl());
            }
        }

        //get all the results from this batch of URLs
        int completedUrls = 0;
        for (UrlResult activeRequest : urlResponseCodeFuturesMap.keySet()) {
            Future<Integer> futureResponseCode = urlResponseCodeFuturesMap.get(activeRequest);
            try {
                Integer responseCode = futureResponseCode.get();
                LOGGER.info("Completed " + batchNum + "[" + completedUrls + "] check of URL "
                        + activeRequest.getUrl() + " with status " + responseCode);
                activeRequest.setLastChecked(new Date());
                activeRequest.setResponseCode(responseCode);
                activeRequest.setResponseMessage(null);
                urlCheckerDao.updateUrlResult(activeRequest);
            } catch (Exception ex) {
                LOGGER.info("Error checking URL " +  activeRequest.getUrl() + " " + ex.getMessage(), ex);
                //We could not complete the request for some reason... timeout, no host, some other error
                //Save the exception message as the response_message field so at least we have something for the report.
                activeRequest.setLastChecked(new Date());
                activeRequest.setResponseCode(null);
                activeRequest.setResponseMessage(ex.getMessage());
                urlCheckerDao.updateUrlResult(activeRequest);
            } finally {
                completedUrls++;
            }
        }
        urlResponseCodeFuturesMap.clear();
    }

    private void completeSetup() {
        if (this.env != null) {
            String successCheckIntervalDaysStr = env.getProperty("job.urlStatusChecker.successCheckIntervalDays");
            String failureCheckIntervalDaysStr = env.getProperty("job.urlStatusChecker.failureCheckIntervalDays");
            String redirectCheckIntervalDaysStr = env.getProperty("job.urlStatusChecker.redirectCheckIntervalDays");
            if (!StringUtils.isEmpty(successCheckIntervalDaysStr)) {
                try {
                    successCheckIntervalDays = Integer.parseInt(successCheckIntervalDaysStr);
                } catch (NumberFormatException ex) {
                    LOGGER.warn("Cannot parse job.urlStatusChecker.successCheckIntervalDays property value "
                            + successCheckIntervalDaysStr + " as number.");
                }
            } else {
                LOGGER.warn("No value found for property job.urlStatusChecker.successCheckIntervalDays. "
                        + "Using the default value of " + successCheckIntervalDays);
            }

            if (!StringUtils.isEmpty(failureCheckIntervalDaysStr)) {
                try {
                    failureCheckIntervalDays = Integer.parseInt(failureCheckIntervalDaysStr);
                } catch (NumberFormatException ex) {
                    LOGGER.warn("Cannot parse job.urlStatusChecker.failureCheckIntervalDays property value "
                            + failureCheckIntervalDaysStr + " as number.");
                }
            } else {
                LOGGER.warn("No value found for property job.urlStatusChecker.failureCheckIntervalDays. "
                        + "Using the default value of " + failureCheckIntervalDays);
            }

            if (!StringUtils.isEmpty(redirectCheckIntervalDaysStr)) {
                try {
                    redirectCheckIntervalDays = Integer.parseInt(redirectCheckIntervalDaysStr);
                } catch (NumberFormatException ex) {
                    LOGGER.warn("Cannot parse job.urlStatusChecker.redirectCheckIntervalDays property value "
                            + redirectCheckIntervalDaysStr + " as number.");
                }
            } else {
                LOGGER.warn("No value found for property job.urlStatusChecker.redirectCheckIntervalDays. "
                        + "Using the default value of " + redirectCheckIntervalDays);
            }

            String requestTimeoutSecondsStr = env.getProperty("job.urlStatusChecker.requestTimeoutSeconds");
            if (!StringUtils.isEmpty(requestTimeoutSecondsStr)) {
                try {
                    requestTimeoutSeconds = Integer.parseInt(requestTimeoutSecondsStr);
                } catch (NumberFormatException ex) {
                    LOGGER.warn("Cannot parse job.urlStatusChecker.requestTimeoutSeconds property value "
                            + requestTimeoutSecondsStr + " as number.");
                }
            } else {
                LOGGER.warn("No value found for property job.urlStatusChecker.requestTimeoutSeconds. "
                        + "Using the default value of " + requestTimeoutSeconds);
            }
        } else {
            LOGGER.error("The spring environment was null.");
        }

        webClient = new WebClient(BrowserVersion.CHROME, false, null, -1);
        webClient.getOptions().setRedirectEnabled(true);
        webClient.getOptions().setTimeout(requestTimeoutSeconds * SECONDS_TO_MILLIS);
        //if we throw exceptions on any error then many websites don't load because they have some
        //javascript error or some link, like GTM, that doesn't work, so we have to ignore these (as the browser does)
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        //many websites also have weird issues with their certificates and browsers seem to let you view them anyway
        webClient.getOptions().setUseInsecureSSL(true);
        initializeExecutorService();
    }

    /**
     * Determines if a URL is eligible to be checked using the following criteria
     *  - Has the url been checked before?
     *  - Was the last check within the last <property> amount of time and successful?
     *  - Was the last check within the last <property> amount of time and failed?
     */
    private boolean shouldUrlBeChecked(final UrlResult systemUrl) {
        if (systemUrl.getLastChecked() == null) {
            return true;
        } else {
            long successNextCheckMillis = systemUrl.getLastChecked().getTime() + (successCheckIntervalDays * DAYS_TO_MILLIS);
            long redirectNextCheckMillis = systemUrl.getLastChecked().getTime() + (redirectCheckIntervalDays * DAYS_TO_MILLIS);
            long failureNextCheckMillis = systemUrl.getLastChecked().getTime() + (failureCheckIntervalDays * DAYS_TO_MILLIS);
            if (isSuccess(systemUrl.getResponseCode())
                    && System.currentTimeMillis() >= successNextCheckMillis) {
                return true;
            } else if (isRedirect(systemUrl.getResponseCode())
                    && System.currentTimeMillis() >= redirectNextCheckMillis) {
                return true;
            } else if (!isSuccess(systemUrl.getResponseCode())
                    && !isRedirect(systemUrl.getResponseCode())
                    && System.currentTimeMillis() >= failureNextCheckMillis) {
                return true;
            }
        }
        return false;
    }

    private boolean isSuccess(final Integer responseCode) {
        if (responseCode == null) {
            return false;
        }
        if (responseCode.toString().startsWith("2")) {
            return true;
        }
        return false;
    }

    private boolean isRedirect(final Integer responseCode) {
        if (responseCode == null) {
            return false;
        }
        if (responseCode.toString().startsWith("3")) {
            return true;
        }
        return false;
    }

    private Integer getThreadCountForJob() throws NumberFormatException {
        return Integer.parseInt(env.getProperty("executorThreadCountForQuartzJobs"));
    }

    private void initializeExecutorService() {
        executorService = Executors.newFixedThreadPool(getThreadCountForJob());
    }
}
