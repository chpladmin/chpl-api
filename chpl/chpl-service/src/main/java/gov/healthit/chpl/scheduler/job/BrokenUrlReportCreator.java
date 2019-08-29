package gov.healthit.chpl.scheduler.job;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.dao.scheduler.UrlCheckerDao;
import gov.healthit.chpl.dao.statistics.DeveloperStatisticsDAO;
import gov.healthit.chpl.domain.DateRange;
import gov.healthit.chpl.dto.scheduler.UrlResultDTO;

/**
 * Quartz job to check every URl in the system and log its response code to the database.
 * @author kekey
 *
 */
public class BrokenUrlReportCreator extends QuartzJob implements InterruptableJob {
    private static final Logger LOGGER = LogManager.getLogger("brokenUrlReportCreatorJobLogger");
    private static final long DAYS_TO_MILLIS = 24 * 60 * 60 * 1000;
    private UrlCheckerDao urlCheckerDao;

    private int successCheckIntervalDays = 1;
    private int failureCheckIntervalDays = 1;
    private boolean interrupted;

    @Autowired
    public BrokenUrlReportCreator(final Environment env,
            final UrlCheckerDao urlCheckerDao) {
        this.urlCheckerDao = urlCheckerDao;
        interrupted = false;
        if (env != null) {
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
        }
    }

    @Override
    @Transactional
    public void execute(final JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        try {
            //get all urls in the system and determine which ones should be checked now
            List<UrlResultDTO> urlsToCheck = new ArrayList<UrlResultDTO>();
            List<UrlResultDTO> allSystemUrls = urlCheckerDao.getAllSystemUrls();
            for (UrlResultDTO systemUrl : allSystemUrls) {
                //determine if this URL should be checked now
                if (shouldUrlBeChecked(systemUrl)) {
                    urlsToCheck.add(systemUrl);
                }
            }

            //check each url and save its results asynchronously

            //determine if any url results are no longer needed
            //because the url no longer exists in the system
            List<UrlResultDTO> allUrlResults = urlCheckerDao.getAllUrlResults();
            for (UrlResultDTO urlResult : allUrlResults) {
                boolean stillExists = false;
                for (UrlResultDTO systemUrl : allSystemUrls) {
                    if (urlResult.equals(systemUrl)) {
                        stillExists = true;
                    }
                }
                if (!stillExists) {
                    urlCheckerDao.deleteUrlResult(urlResult.getId());
                }
            }
        } catch (Exception ex) {
            LOGGER.debug("Unable to complete job: " + ex.getMessage(), ex);
        }
    }

    @Transactional
    @Async("jobAsyncDataExecutor")
    public Future<Long> checkUrl(final String url) {
//        Long total = developerStatisticsDAO.getTotalDevelopers(dateRange);
//        return new AsyncResult<Long>(total);
        //kick off the async operations to query all the urls
        return null;
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
