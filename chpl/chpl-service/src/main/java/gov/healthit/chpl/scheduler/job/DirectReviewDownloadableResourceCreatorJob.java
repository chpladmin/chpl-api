package gov.healthit.chpl.scheduler.job;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.exception.JiraRequestFailedException;
import gov.healthit.chpl.manager.DeveloperManager;
import gov.healthit.chpl.scheduler.presenter.DirectReviewCsvPresenter;
import gov.healthit.chpl.service.DirectReviewService;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

@DisallowConcurrentExecution
public class DirectReviewDownloadableResourceCreatorJob extends DownloadableResourceCreatorJob {
    private static final Logger LOGGER = LogManager.getLogger("directReviewDownloadableResourceCreatorJobLogger");

    @Autowired
    private Environment env;

    @Autowired
    private DeveloperManager devManager;

    @Autowired
    private DirectReviewService directReviewService;

    public DirectReviewDownloadableResourceCreatorJob() throws Exception {
        super(LOGGER);
    }

    @Override
    public void execute(final JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Direct Review Downloadable Resource Creator job. *********");
        try {
            CacheManager manager = CacheManager.getInstance();
            Cache drCache = manager.getCache(CacheNames.DIRECT_REVIEWS);

            //re-populate the DR cache
            try {
                directReviewService.populateDirectReviewsCache();
            } catch (JiraRequestFailedException ex) {
                LOGGER.error("Request to Jira to populate all direct reviews failed.", ex);
                if (drCache.getKeys() == null || drCache.getKeys().size() == 0) {
                    LOGGER.fatal("No Direct Reviews found in the cache. Not writing out a file.");
                    return;
                }
            }

            File downloadFolder = getDownloadFolder();
            String csvFilename = downloadFolder.getAbsolutePath()
                    + File.separator
                    + env.getProperty("directReviewsReportName") + "-"
                    + getFilenameTimestampFormat().format(new Date())
                    + ".csv";
            File csvFile = getFile(csvFilename);
            DirectReviewCsvPresenter csvPresenter = new DirectReviewCsvPresenter(env, devManager);
            csvPresenter.presentAsFile(csvFile);
        } catch (Exception e) {
            LOGGER.error(e);
        } finally {
            LOGGER.info("********* Completed the Direct Review Downloadable Resource Creator job. *********");
        }
    }

    private File getFile(final String fileName) throws IOException {
        File file = new File(fileName);
        if (file.exists()) {
            if (!file.delete()) {
                throw new IOException("File exists; cannot delete");
            }
        }
        if (!file.createNewFile()) {
            throw new IOException("File can not be created");
        }
        return file;
    }
}
