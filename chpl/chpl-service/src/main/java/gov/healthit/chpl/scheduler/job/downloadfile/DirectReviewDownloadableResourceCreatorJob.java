package gov.healthit.chpl.scheduler.job.downloadfile;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.compliance.directreview.DirectReviewCachingService;
import gov.healthit.chpl.compliance.directreview.DirectReviewSearchService;
import gov.healthit.chpl.manager.DeveloperManager;
import gov.healthit.chpl.scheduler.presenter.DirectReviewCsvPresenter;
import lombok.extern.log4j.Log4j2;

@DisallowConcurrentExecution
@Log4j2(topic = "directReviewDownloadableResourceCreatorJobLogger")
public class DirectReviewDownloadableResourceCreatorJob extends DownloadableResourceCreatorJob {
    @Autowired
    private Environment env;

    @Autowired
    private DeveloperManager devManager;

    @Autowired
    private DirectReviewCachingService drCachingService;

    @Autowired
    private DirectReviewSearchService drSearchService;

    public DirectReviewDownloadableResourceCreatorJob() throws Exception {
        super(LOGGER);
    }

    @Override
    public void execute(final JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Direct Review Downloadable Resource Creator job. *********");
        try {
            try {
                LOGGER.info("Repopulating the direct reviews cache.");
                drCachingService.populateDirectReviewsCache(LOGGER);
                LOGGER.info("Completed repopulating the direct reviews cache.");
            } catch (Exception ex) {
                LOGGER.error("Repopulating direct reviews cache failed. Not writing out a file.", ex);
                return;
            }

            if (!drSearchService.doesCacheHaveAnyOkData()) {
                LOGGER.fatal("Direct Reviews cache is not available. "
                        + "There may have been an error retreiving direct reviews from Jira. "
                        + "Not writing out a file.");
                return;
            }

            File downloadFolder = getDownloadFolder();
            String csvFilename = downloadFolder.getAbsolutePath()
                    + File.separator
                    + env.getProperty("directReviewsReportName") + "-"
                    + getFilenameTimestampFormat().format(new Date())
                    + ".csv";
            File csvFile = getFile(csvFilename);
            DirectReviewCsvPresenter csvPresenter = new DirectReviewCsvPresenter(env, devManager, drSearchService);
            LOGGER.info("Writing Direct Review data to CSV file.");
            csvPresenter.presentAsFile(csvFile);
            LOGGER.info("Completed writing Direct Review data to CSV file.");
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
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
        LOGGER.info("Created file " + file.getAbsolutePath());
        return file;
    }
}
