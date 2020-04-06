package gov.healthit.chpl.scheduler.job;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.scheduler.presenter.CertifiedProduct2014CsvPresenter;
import gov.healthit.chpl.scheduler.presenter.CertifiedProductCsvPresenter;
import gov.healthit.chpl.scheduler.presenter.CertifiedProductXmlPresenter;
import gov.healthit.chpl.service.CertificationCriterionService;

@DisallowConcurrentExecution
public class CertifiedProductDownloadableResourceCreatorJob
        extends DownloadableResourceCreatorJob implements InterruptableJob {
    private static final Logger LOGGER = LogManager.getLogger("certifiedProductDownloadableResourceCreatorJobLogger");
    private static final int MILLIS_PER_SECOND = 1000;
    private static final int SECONDS_PER_MINUTE = 60;
    private String edition;
    private boolean interrupted;

    @Autowired
    private CertificationCriterionService criterionService;

    public CertifiedProductDownloadableResourceCreatorJob() throws Exception {
        super(LOGGER);
        edition = "";
    }

    @Override
    public void execute(final JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        Date start = new Date();
        edition = jobContext.getMergedJobDataMap().getString("edition");
        interrupted = false;
        LOGGER.info("********* Starting the Certified Product Downloadable Resource Creator job for {}. *********",
                edition);

        try (CertifiedProductXmlPresenter xmlPresenter = new CertifiedProductXmlPresenter();
                CertifiedProductCsvPresenter csvPresenter = getCsvPresenter()) {

            List<CertifiedProductDetailsDTO> listings = getRelevantListings();
            List<Future<CertifiedProductSearchDetails>> futures = getCertifiedProductSearchDetailsFutures(listings);

            initializeWritingToFiles(xmlPresenter, csvPresenter);
            for (Future<CertifiedProductSearchDetails> future : futures) {
                if (interrupted) {
                    break;
                }
                CertifiedProductSearchDetails details = future.get();
                LOGGER.info("Complete retrieving details for id: " + details.getId());
                xmlPresenter.add(details);
                csvPresenter.add(details);
            }

            // Closing of xmlPresenter and csvPresenter happen due to try-with-resources

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        Date end = new Date();
        if (interrupted) {
            LOGGER.info("Interrupted before files for {} edition after {} seconds, or {} minutes",
                    edition,
                    (end.getTime() - start.getTime()) / MILLIS_PER_SECOND,
                    (end.getTime() - start.getTime()) / MILLIS_PER_SECOND / SECONDS_PER_MINUTE);
        } else {
            LOGGER.info("Time to create file(s) for {} edition: {} seconds, or {} minutes",
                    edition,
                    (end.getTime() - start.getTime()) / MILLIS_PER_SECOND,
                    (end.getTime() - start.getTime()) / MILLIS_PER_SECOND / SECONDS_PER_MINUTE);
            LOGGER.info("********* Completed the Certified Product Downloadable Resource Creator job for {}. *********",
                    edition);
        }
    }

    private void initializeWritingToFiles(final CertifiedProductXmlPresenter xmlPresenter,
            final CertifiedProductCsvPresenter csvPresenter) throws IOException {
        xmlPresenter.setLogger(LOGGER);
        xmlPresenter.open(getXmlFile());

        csvPresenter.setLogger(LOGGER);
        List<CertificationCriterionDTO> criteria = getCriteriaDao().findByCertificationEditionYear(edition)
                .stream()
                .filter(cr -> !cr.getRemoved())
                .sorted((crA, crB) -> criterionService.sortCriteria(crA, crB))
                .collect(Collectors.<CertificationCriterionDTO>toList());
        csvPresenter.setApplicableCriteria(criteria);
        csvPresenter.open(getCsvFile());
    }

    private File getXmlFile() throws IOException {
        File downloadFolder = getDownloadFolder();
        String xmlFilename = getFileName(downloadFolder.getAbsolutePath(),
                getFilenameTimestampFormat().format(new Date()), "xml");
        return getFile(xmlFilename);
    }

    private File getCsvFile() throws IOException {
        File downloadFolder = getDownloadFolder();
        String xmlFilename = getFileName(downloadFolder.getAbsolutePath(),
                getFilenameTimestampFormat().format(new Date()), "csv");
        return getFile(xmlFilename);
    }

    private List<CertifiedProductDetailsDTO> getRelevantListings() throws EntityRetrievalException {
        LOGGER.info("Finding all listings for edition " + edition + ".");
        Date start = new Date();
        List<CertifiedProductDetailsDTO> listingsForEdition = getCertifiedProductDao().findByEdition(edition);
        Date end = new Date();
        LOGGER.info("Found the " + listingsForEdition.size() + " listings from " + edition + " in "
                + ((end.getTime() - start.getTime()) / MILLIS_PER_SECOND) + " seconds");
        return listingsForEdition;
    }

    private CertifiedProductCsvPresenter getCsvPresenter() {
        CertifiedProductCsvPresenter presenter = null;
        if (edition.equals("2014")) {
            presenter = new CertifiedProduct2014CsvPresenter();
        } else {
            presenter = new CertifiedProductCsvPresenter();
        }
        return presenter;
    }

    private String getFileName(final String path, final String timeStamp, final String extension) {
        return path + File.separator + "chpl-" + edition + "-" + timeStamp + "." + extension;
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

    @Override
    public void interrupt() throws UnableToInterruptJobException {
        LOGGER.info("Certified Product download job for edition {} interrupted", edition);
        interrupted = true;
    }
}
