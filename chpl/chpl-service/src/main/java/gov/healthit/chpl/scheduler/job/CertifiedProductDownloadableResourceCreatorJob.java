package gov.healthit.chpl.scheduler.job;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.xml.stream.XMLStreamException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
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
    private static final Logger LOGGER = LogManager.getLogger("certifiedProductDownloadableResourceCreatorJobLogger");
    private static final int MILLIS_PER_SECOND = 1000;
    private String edition;

    /**
     * Default constructor.
     * @throws Exception if issue with context
     */
    public CertifiedProductDownloadableResourceCreatorJob() throws Exception {
        super(LOGGER);
    }

    @Override
    public void execute(final JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        getCertifiedProductDetailsAsyncRetrievalHelper().setLogger(LOGGER);

        Date start = new Date();
        edition = jobContext.getMergedJobDataMap().getString("edition");
        LOGGER.info("********* Starting the Certified Product Downloadable Resource Creator job for " + edition + ". *********");
        try {
            List<CertifiedProductDetailsDTO> listings = getRelevantListings();
            List<Future<CertifiedProductSearchDetails>> futures = getCertifiedProductSearchDetailsFutures(listings);
            processListingFutures(futures);
        } catch (Exception e) {
            LOGGER.error(e);
        }
        Date end = new Date();
        LOGGER.info("Time to create file(s) for " + edition + " edition: "
                + ((end.getTime() - start.getTime()) / MILLIS_PER_SECOND) + " seconds");
        LOGGER.info("********* Completed the Certified Product Downloadable Resource Creator job for " + edition + ". *********");
    }

    private void processListingFutures(List<Future<CertifiedProductSearchDetails>> futures) 
            throws InterruptedException, ExecutionException, XMLStreamException, IOException {
        
        CertifiedProductXmlPresenter xmlPresenter = new CertifiedProductXmlPresenter();
        xmlPresenter.setLogger(LOGGER);
        xmlPresenter.open(getXmlFile());
        
        CertifiedProductCsvPresenter csvPresenter = getCsvPresenter();
        csvPresenter.setLogger(LOGGER);
        List<CertificationCriterionDTO> criteria = getCriteriaDao().findByCertificationEditionYear(edition);
        csvPresenter.setApplicableCriteria(criteria);
        csvPresenter.open(getCsvFile());
        
        CertifiedProductSearchDetails cp;
        /*
        for (Future<CertifiedProductSearchDetails> future : futures) {
            cp = future.get();
            xmlPresenter.add(cp);
            csvPresenter.add(cp);
        }
        */
        Iterator<Future<CertifiedProductSearchDetails>> futuresIterator = futures.iterator();
        while (futuresIterator.hasNext()) {
            cp = futuresIterator.next().get();
            xmlPresenter.add(cp);
            csvPresenter.add(cp);
            futuresIterator.remove();
        }
        xmlPresenter.close();
        csvPresenter.close();
    }
    
    private File getXmlFile() throws IOException {
        File downloadFolder = getDownloadFolder();
        String xmlFilename = getFileName(downloadFolder.getAbsolutePath(),
                getTimestampFormat().format(new Date()), "xml");
        return getFile(xmlFilename);
    }
    
    private File getCsvFile() throws IOException {
        File downloadFolder = getDownloadFolder();
        String xmlFilename = getFileName(downloadFolder.getAbsolutePath(),
                getTimestampFormat().format(new Date()), "csv");
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
}
