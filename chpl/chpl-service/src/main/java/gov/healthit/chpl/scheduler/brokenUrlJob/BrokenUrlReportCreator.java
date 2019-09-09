package gov.healthit.chpl.scheduler.brokenUrlJob;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import javax.net.ssl.SSLException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.Dsl;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.scheduler.job.QuartzJob;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

/**
 * Quartz job to check every URl in the system and log its response code to the database.
 * @author kekey
 *
 */
public class BrokenUrlReportCreator extends QuartzJob implements InterruptableJob {
    private static final Logger LOGGER = LogManager.getLogger("brokenUrlReportCreatorJobLogger");
    private static final long DAYS_TO_MILLIS = 24 * 60 * 60 * 1000;
    private static final int BATCH_SIZE = 100;

    @Autowired
    private Environment env;

    @Autowired
    private UrlCheckerDao urlCheckerDao;

    @Autowired
    private UrlCallerAsync urlCallerAsync;

    private int successCheckIntervalDays = 1;
    private int failureCheckIntervalDays = 1;
    private int connectTimeoutSeconds = 10;
    private int requestTimeoutSeconds = 10;
    private AsyncHttpClient httpClient;
    private Map<UrlResultDTO, Future<Integer>> urlResponseCodeFuturesMap;
    private boolean interrupted;

    public BrokenUrlReportCreator() {
        interrupted = false;
        urlResponseCodeFuturesMap = new LinkedHashMap<UrlResultDTO, Future<Integer>>();
    }

    @Override
    @Transactional
    public void execute(final JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Broken URL Report Creator job. *********");

        completeSetup();

        try {
            //get all urls in the system
            List<UrlResultDTO> allSystemUrls = urlCheckerDao.getAllSystemUrls();
            LOGGER.info("Found " + allSystemUrls.size() + " urls in the system.");
            List<UrlResultDTO> existingUrlResults = urlCheckerDao.getAllUrlResults();

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
                if (interrupted) {
                    break;
                }
            }
        } catch (Exception ex) {
            LOGGER.error("Unable to complete job: " + ex.getMessage(), ex);
        } finally {
            try {
                httpClient.close();
            } catch (final Exception ex) {
                LOGGER.error("Error closing the httpClient: " + ex.getMessage(), ex);
            }
        }
        LOGGER.info("********* Completed the Broken URL Report Creator job. *********");
    }

    /**
     * Looks for any URL that we are currently saving a result for
     * but is no longer in the system.
     * @param existingUrlResults all the saved url results that currently exist
     * @param allSystemUrls all the urls that are in the system
     * @throws EntityRetrievalException if the url result to delete cannot be found in the database
     */
    private void removeObsoleteUrlResults(final List<UrlResultDTO> existingUrlResults,
            final List<UrlResultDTO> allSystemUrls) throws EntityRetrievalException {
        //determine if any url results are no longer needed because it no longer exists in the system
        for (UrlResultDTO existingUrlResult : existingUrlResults) {
            boolean stillExists = false;
            for (UrlResultDTO systemUrl : allSystemUrls) {
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
    private void addNewUrlResults(final List<UrlResultDTO> existingUrlResults,
            final List<UrlResultDTO> allSystemUrls) throws EntityCreationException {
        //add any urls to the result table that aren't already there and add them
        //will have null last checked date, response code, and response message initially
        for (UrlResultDTO systemUrl : allSystemUrls) {
            boolean alreadyExists = false;
            for (UrlResultDTO existingUrlResult : existingUrlResults) {
                if (existingUrlResult.equals(systemUrl)) {
                    alreadyExists = true;
                    BeanUtils.copyProperties(existingUrlResult, systemUrl);
                }
            }
            if (!alreadyExists) {
                LOGGER.info("The URL " + systemUrl.getUrl()
                    + " for the type " + systemUrl.getUrlType().getName()
                    + " will be added to the system.");
                UrlResultDTO created = urlCheckerDao.createUrlResult(systemUrl);
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
    private void processUrls(final int batchNum, final List<UrlResultDTO> urlList) throws EntityRetrievalException {
        //make async requests for each url in the batch
        for (int batchIndex = 0; batchIndex < urlList.size(); batchIndex++) {
            UrlResultDTO systemUrl = urlList.get(batchIndex);
            if (shouldUrlBeChecked(systemUrl)) {
                LOGGER.info(systemUrl.getUrl() + " for the type " + systemUrl.getUrlType().getName()
                    + " will be checked for validity.");
                try {
                   Future<Integer> responseCodeFuture =
                           urlCallerAsync.getUrlResponseCode(systemUrl, httpClient, LOGGER);
                   urlResponseCodeFuturesMap.put(systemUrl, responseCodeFuture);
                } catch (final Exception ex) {
                    LOGGER.error("Could not check URL " + systemUrl.getUrl()
                        + " due to exception " + ex.getMessage(), ex);
                }
            } else {
                LOGGER.info("Not checking " + systemUrl.getUrlType().getName() + " URL " + systemUrl.getUrl());
            }
        }

        //get all the results from this batch of URLs
        int completedUrls = 0;
        for (UrlResultDTO activeRequest : urlResponseCodeFuturesMap.keySet()) {
            if (interrupted) {
                break;
            }
            Future<Integer> futureResponseCode = urlResponseCodeFuturesMap.get(activeRequest);
            try {
                Integer responseCode = futureResponseCode.get();
                LOGGER.info("Completed " + batchNum + "[" + completedUrls + "] check of URL "
                        + activeRequest.getUrl() + " with status " + responseCode);
                activeRequest.setLastChecked(new Date());
                activeRequest.setResponseCode(responseCode);
                urlCheckerDao.updateUrlResult(activeRequest);
            } catch (Exception ex) {
                LOGGER.error("Error checking URL " +  activeRequest.getUrl() + " " + ex.getMessage());
                //we could not complete the request for some reason... timeout, no host, some other error
                //save the exception message as the response_message field
                //so at least we have SOMETHING
                activeRequest.setLastChecked(new Date());
                activeRequest.setResponseMessage(ex.getMessage());
                urlCheckerDao.updateUrlResult(activeRequest);
            } finally {
                completedUrls++;
            }
        }
        urlResponseCodeFuturesMap.clear();
    }

    /**
     * Initialize values and objects needed by the job.
     * Read properties.
     */
    private void completeSetup() {
        if (this.env != null) {
            String successCheckIntervalDaysStr = env.getProperty("job.badUrlChecker.successCheckIntervalDays");
            String failureCheckIntervalDaysStr = env.getProperty("job.badUrlChecker.failureCheckIntervalDays");
            if (!StringUtils.isEmpty(successCheckIntervalDaysStr)) {
                try {
                    successCheckIntervalDays = Integer.parseInt(successCheckIntervalDaysStr);
                } catch (NumberFormatException ex) {
                    LOGGER.warn("Cannot parse job.badUrlChecker.successCheckIntervalDays property value "
                            + successCheckIntervalDaysStr + " as number.");
                }
            } else {
                LOGGER.warn("No value found for property job.badUrlChecker.successCheckIntervalDays. "
                        + "Using the default value of " + successCheckIntervalDays);
            }

            if (!StringUtils.isEmpty(failureCheckIntervalDaysStr)) {
                try {
                    failureCheckIntervalDays = Integer.parseInt(failureCheckIntervalDaysStr);
                } catch (NumberFormatException ex) {
                    LOGGER.warn("Cannot parse job.badUrlChecker.failureCheckIntervalDays property value "
                            + failureCheckIntervalDaysStr + " as number.");
                }
            } else {
                LOGGER.warn("No value found for property job.badUrlChecker.failureCheckIntervalDays. "
                        + "Using the default value of " + failureCheckIntervalDays);
            }

            String connectTimeoutSecondsStr = env.getProperty("job.badUrlChecker.connectTimeoutSeconds");
            String requestTimeoutSecondsStr = env.getProperty("job.badUrlChecker.requestTimeoutSeconds");
            if (!StringUtils.isEmpty(connectTimeoutSecondsStr)) {
                try {
                    connectTimeoutSeconds = Integer.parseInt(connectTimeoutSecondsStr);
                } catch (NumberFormatException ex) {
                    LOGGER.warn("Cannot parse job.badUrlChecker.connectTimeoutSeconds property value "
                            + connectTimeoutSecondsStr + " as number.");
                }
            } else {
                LOGGER.warn("No value found for property job.badUrlChecker.connectTimeoutSeconds. "
                        + "Using the default value of " + connectTimeoutSeconds);
            }
            if (!StringUtils.isEmpty(requestTimeoutSecondsStr)) {
                try {
                    requestTimeoutSeconds = Integer.parseInt(requestTimeoutSecondsStr);
                } catch (NumberFormatException ex) {
                    LOGGER.warn("Cannot parse job.badUrlChecker.requestTimeoutSeconds property value "
                            + requestTimeoutSecondsStr + " as number.");
                }
            } else {
                LOGGER.warn("No value found for property job.badUrlChecker.requestTimeoutSeconds. "
                        + "Using the default value of " + requestTimeoutSeconds);
            }
        } else {
            LOGGER.error("The spring environment was null.");
        }

        //create ssl context to accept ALL https connections
        //documentation says this isn't very secure... do we trust that the URLs uploaded are safe?
        //do we need to do something different here to be more secure?
        SslContext sslContext = null;
        try {
            sslContext = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        } catch (final SSLException ex) {
            LOGGER.error("Could not create ssl context; https requests may fail.", ex);
        }

        DefaultAsyncHttpClientConfig.Builder clientBuilder = Dsl.config()
                .setConnectTimeout(connectTimeoutSeconds*1000)
                .setRequestTimeout(requestTimeoutSeconds*1000)
                .setSslContext(sslContext);
        httpClient = Dsl.asyncHttpClient(clientBuilder);
    }

    /**
     * Determines if a URL is eligible to be checked using the following criteria
     *  - Has the url been checked before?
     *  - Was the last check within the last <property> amount of time and successful?
     *  - Was the last check within the last <property> amount of time and failed?
     * @param systemUrl
     * @return
     */
    private boolean shouldUrlBeChecked(final UrlResultDTO systemUrl) {
        if (systemUrl.getLastChecked() == null) {
            return true;
        } else {
            long successNextCheckMillis = systemUrl.getLastChecked().getTime() + (successCheckIntervalDays*DAYS_TO_MILLIS);
            long failureNextCheckMillis = systemUrl.getLastChecked().getTime() + (failureCheckIntervalDays*DAYS_TO_MILLIS);
            if (isSuccess(systemUrl.getResponseCode())
                    && System.currentTimeMillis() >= successNextCheckMillis) {
                return true;
            } else if (!isSuccess(systemUrl.getResponseCode())
                    && System.currentTimeMillis() >= failureNextCheckMillis) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if a given HTTP response code means "success"
     * @param responseCode
     * @return
     */
    private boolean isSuccess(final Integer responseCode) {
        if (responseCode == null) {
            return false;
        }
        if (responseCode.toString().startsWith("2")) {
            return true;
        }
        return false;
    }

    @Override
    public void interrupt() throws UnableToInterruptJobException {
        this.interrupted = true;
    }
}
