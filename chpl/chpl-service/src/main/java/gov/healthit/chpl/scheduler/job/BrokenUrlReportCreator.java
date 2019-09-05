package gov.healthit.chpl.scheduler.job;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.Response;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.dao.scheduler.UrlCheckerDao;
import gov.healthit.chpl.dto.scheduler.UrlResultDTO;
import gov.healthit.chpl.scheduler.UrlCallerAsync;

/**
 * Quartz job to check every URl in the system and log its response code to the database.
 * @author kekey
 *
 */
public class BrokenUrlReportCreator extends QuartzJob implements InterruptableJob {
    private static final Logger LOGGER = LogManager.getLogger("brokenUrlReportCreatorJobLogger");
    private static final long DAYS_TO_MILLIS = 24 * 60 * 60 * 1000;

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
    private Date checkedDate;
    private AsyncHttpClient httpClient;
    private Map<UrlResultDTO, Future<Response>> urlRequestFuturesMap;
    private boolean interrupted;

    public BrokenUrlReportCreator() {
        interrupted = false;
        this.checkedDate = new Date();
        urlRequestFuturesMap = new LinkedHashMap<UrlResultDTO, Future<Response>>();
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

            //add any urls to the result table that aren't already there and add them
            //will have null last checked date, response code, and response time initially
            for (UrlResultDTO systemUrl : allSystemUrls) {
                boolean alreadyExists = false;
                for (UrlResultDTO existingUrlResult : existingUrlResults) {
                    if (existingUrlResult.equals(systemUrl)) {
                        alreadyExists = true;
                        systemUrl.setId(existingUrlResult.getId());
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

            //determine which of the system urls should be checked now
            //and check them - updates each result with checked date, response time, response code
            //TODO: are there likely to be duplicates that we are wasting our time checking?
            for (UrlResultDTO systemUrl : allSystemUrls) {
                if (shouldUrlBeChecked(systemUrl)) {
                    LOGGER.info("The URL " + systemUrl.getUrl()
                        + " for the type " + systemUrl.getUrlType().getName()
                        + " will be checked for validity.");
                    try {
                       Future<Response> urlFuture =
                               urlCallerAsync.getUrlResponse(systemUrl, httpClient, LOGGER);
                       urlRequestFuturesMap.put(systemUrl, urlFuture);
                    } catch (final Exception ex) {
                        LOGGER.error("Could not check URL " + systemUrl.getUrl() + " due to exception " + ex.getMessage(), ex);
                    }
                } else {
                    LOGGER.info("The URL " + systemUrl.getUrl()
                    + " for the type " + systemUrl.getUrlType().getName()
                    + " is up-to-date and will not be checked.");
                }
            }

            for (UrlResultDTO activeRequest : urlRequestFuturesMap.keySet()) {
                if (interrupted) {
                    break;
                }
                Future<Response> futureResponse = urlRequestFuturesMap.get(activeRequest);
                try {
                    Response response = futureResponse.get();
                    LOGGER.info("Completed check of URL " + activeRequest.getUrl() + " with status " + response.getStatusCode());
                    activeRequest.setLastChecked(checkedDate);
                    activeRequest.setResponseCode(response.getStatusCode());
                    urlCheckerDao.updateUrlResult(activeRequest);
                } catch (Exception ex) {
                    LOGGER.error("Error checking URL " +  activeRequest.getUrl() + ex.getMessage(), ex);
                }
            }

            if (interrupted) {
                LOGGER.info("Job was interrupted.");
            }

        } catch (Exception ex) {
            LOGGER.error("Unable to complete job: " + ex.getMessage(), ex);
        }
        LOGGER.info("********* Completed the Broken URL Report Creator job. *********");
    }

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

        DefaultAsyncHttpClientConfig.Builder clientBuilder = Dsl.config()
                .setConnectTimeout(connectTimeoutSeconds*1000)
                .setRequestTimeout(requestTimeoutSeconds*1000);
        httpClient = Dsl.asyncHttpClient(clientBuilder);
    }

    /**
     * Determines if a URL should be checked again.
     *  has the url been checked before?
     *  was the last check within the last <property> amount of time and successful?
     *  was the last check within the last <property> amount of time and failed?
     * @param systemUrl
     * @return
     */
    private boolean shouldUrlBeChecked(final UrlResultDTO systemUrl) {
        if (systemUrl.getLastChecked() == null) {
            return true;
        }
        long successCheckIntervalMillis = systemUrl.getLastChecked().getTime() + (successCheckIntervalDays*DAYS_TO_MILLIS);
        long failureCheckIntervalMillis = systemUrl.getLastChecked().getTime() + (failureCheckIntervalDays*DAYS_TO_MILLIS);
        if (isSuccess(systemUrl.getResponseCode())
                && System.currentTimeMillis() >= successCheckIntervalMillis) {
            return true;
        } else if (!isSuccess(systemUrl.getResponseCode())
                && System.currentTimeMillis() >= failureCheckIntervalMillis) {
            return true;
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
