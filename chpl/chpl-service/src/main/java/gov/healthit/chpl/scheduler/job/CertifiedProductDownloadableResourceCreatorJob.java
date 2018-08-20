package gov.healthit.chpl.scheduler.job;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.BeansException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import gov.healthit.chpl.domain.CertifiedProductDownloadResponse;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.scheduler.JobConfig;
import gov.healthit.chpl.scheduler.SchedulerCertifiedProductSearchDetailsAsync;
import gov.healthit.chpl.scheduler.presenter.CertifiedProduct2014CsvPresenter;
import gov.healthit.chpl.scheduler.presenter.CertifiedProductCsvPresenter;
import gov.healthit.chpl.scheduler.presenter.CertifiedProductXmlPresenter;

/**
 * Quartz job to generate download files by edition.
 * @author alarned
 *
 */
@DisallowConcurrentExecution
public class CertifiedProductDownloadableResourceCreatorJob extends DownloadableResourceCreatorJob {
    private static final Logger LOGGER = LogManager.getLogger(CertifiedProductDownloadableResourceCreatorJob.class);
    private static final int MILLIS_PER_SECOND = 1000;
    private String edition;
    private AbstractApplicationContext context;

    /**
     * Default constructor.
     * @throws Exception if issue with context
     */
    public CertifiedProductDownloadableResourceCreatorJob() throws Exception {
        super();
        setLocalContext();
        context = new AnnotationConfigApplicationContext(JobConfig.class);
        initiateSpringBeans(context);
    }

    @Override
    public void execute(final JobExecutionContext jobContext) throws JobExecutionException {
        Date start = new Date();
        edition = jobContext.getMergedJobDataMap().getString("edition");
        try {
            List<CertifiedProductDetailsDTO> listings = getRelevantListings();

            List<Future<CertifiedProductSearchDetails>> futures = getCertifiedProductSearchDetailsFutures(listings);
            Map<Long, CertifiedProductSearchDetails> cpMap = getMapFromFutures(futures);

            CertifiedProductDownloadResponse results = new CertifiedProductDownloadResponse();
            results.setListings(createOrderedListOfCertifiedProducts(
                    cpMap,
                    getOriginalCertifiedProductOrder(listings)));

            File downloadFolder = getDownloadFolder();
            writeToFile(downloadFolder, results);
        } catch (Exception e) {
            LOGGER.error(e);
        }
        Date end = new Date();
        LOGGER.info("Time to create file(s) for " + edition + " edition: "
                + ((end.getTime() - start.getTime()) / MILLIS_PER_SECOND) + " seconds");
        context.close();
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

    private List<Future<CertifiedProductSearchDetails>> getCertifiedProductSearchDetailsFutures(
            final List<CertifiedProductDetailsDTO> listings) throws Exception {

        List<Future<CertifiedProductSearchDetails>> futures = new ArrayList<Future<CertifiedProductSearchDetails>>();
        SchedulerCertifiedProductSearchDetailsAsync cpsdAsync = getCertifiedProductDetailsAsyncRetrievalHelper();
        
        for (CertifiedProductDetailsDTO currListing : listings) {
            try {
                futures.add(cpsdAsync.getCertifiedProductDetail(currListing.getId(),
                        getCertifiedProductDetailsManager()));
            } catch (EntityRetrievalException e) {
                LOGGER.error("Could not retrieve certified product details for id: " + currListing.getId(), e);
            }
        }
        return futures;
    }

    private Map<Long, CertifiedProductSearchDetails> getMapFromFutures(
            final List<Future<CertifiedProductSearchDetails>> futures) {
        Map<Long, CertifiedProductSearchDetails> cpMap = new HashMap<Long, CertifiedProductSearchDetails>();
        for (Future<CertifiedProductSearchDetails> future : futures) {
            try {
                cpMap.put(future.get().getId(), future.get());
            } catch (InterruptedException | ExecutionException e) {
                LOGGER.error("Could not retrieve certified product details for unknown id.", e);
            }
        }
        return cpMap;
    }

    private List<CertifiedProductSearchDetails> createOrderedListOfCertifiedProducts(
            final Map<Long, CertifiedProductSearchDetails> certifiedProducts, final List<Long> orderedIds) {

        List<CertifiedProductSearchDetails> ordered = new ArrayList<CertifiedProductSearchDetails>();

        for (Long id : orderedIds) {
            if (certifiedProducts.containsKey(id)) {
                ordered.add(certifiedProducts.get(id));
            }
        }

        return ordered;
    }

    private List<Long> getOriginalCertifiedProductOrder(final List<CertifiedProductDetailsDTO> listings) {
        List<Long> order = new ArrayList<Long>();

        for (CertifiedProductDetailsDTO cp : listings) {
            order.add(cp.getId());
        }

        return order;
    }

    private void writeToFile(final File downloadFolder, final CertifiedProductDownloadResponse results)
            throws IOException {
        writeToCsv(downloadFolder, results);
        writeToXml(downloadFolder, results);
    }

    private void writeToXml(final File downloadFolder, final CertifiedProductDownloadResponse results)
            throws IOException {
        Date start = new Date();
        String xmlFilename = getFileName(downloadFolder.getAbsolutePath(),
                getTimestampFormat().format(new Date()), "xml");
        File xmlFile = getFile(xmlFilename);
        CertifiedProductXmlPresenter xmlPresenter = new CertifiedProductXmlPresenter();
        xmlPresenter.presentAsFile(xmlFile, results);
        Date end = new Date();
        LOGGER.info("Wrote " + edition + " XML file in "
                + ((end.getTime() - start.getTime()) / MILLIS_PER_SECOND) + " seconds");
    }

    private void writeToCsv(final File downloadFolder, final CertifiedProductDownloadResponse results)
            throws IOException {
        Date start = new Date();
        String csvFilename = getFileName(downloadFolder.getAbsolutePath(),
                getTimestampFormat().format(new Date()), "csv");
        File csvFile = getFile(csvFilename);
        CertifiedProductCsvPresenter csvPresenter = getCsvPresenter();
        List<CertificationCriterionDTO> criteria = getCriteriaDao().findByCertificationEditionYear(edition);
        csvPresenter.setApplicableCriteria(criteria);
        csvPresenter.presentAsFile(csvFile, results);
        Date end = new Date();
        LOGGER.info("Wrote " + edition  + " CSV file in "
                + ((end.getTime() - start.getTime()) / MILLIS_PER_SECOND) + " seconds");
    }

    private CertifiedProductCsvPresenter getCsvPresenter() {
        CertifiedProductCsvPresenter csvPresenter = null;
        if (edition.equals("2014")) {
            csvPresenter = new CertifiedProduct2014CsvPresenter();
        } else {
            csvPresenter = new CertifiedProductCsvPresenter();
        }
        return csvPresenter;
    }

    private String getFileName(final String path, final String timeStamp, final String extension) {
        return path + File.separator + "chpl-" + edition  + "-" + timeStamp + "." + extension;
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

    private SchedulerCertifiedProductSearchDetailsAsync getCertifiedProductDetailsAsyncRetrievalHelper()
            throws BeansException {
        return (SchedulerCertifiedProductSearchDetailsAsync)
                getApplicationContext().getBean("schedulerCertifiedProductSearchDetailsAsync");
    }

    private CertifiedProductDetailsManager getCertifiedProductDetailsManager() throws BeansException {
        return (CertifiedProductDetailsManager) getApplicationContext().getBean("certifiedProductDetailsManager");
    }
}
