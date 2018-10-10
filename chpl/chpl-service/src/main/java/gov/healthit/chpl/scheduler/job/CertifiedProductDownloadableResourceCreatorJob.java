package gov.healthit.chpl.scheduler.job;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.xml.stream.XMLStreamException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.job.SimpleObjectPool;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
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
    private CertifiedProductXmlPresenter xmlPresenter;
    private CertifiedProductCsvPresenter csvPresenter;
    private static final int SECONDS_PER_MINUTE = 60;
    private String edition;
    
    @Autowired
    private CertifiedProductDetailsManager cpdManager;
    
    /**
     * Default constructor.
     * @throws Exception if issue with context
     */
    public CertifiedProductDownloadableResourceCreatorJob() throws Exception {
        super(LOGGER);
        edition = "";
    }

    @Override
    public void execute(final JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        getCertifiedProductDetailsAsyncRetrievalHelper().setLogger(LOGGER);

        Date start = new Date();
        edition = jobContext.getMergedJobDataMap().getString("edition");
        LOGGER.info("********* Starting the Certified Product Downloadable Resource Creator job for {}. *********",
                edition);
        try {
            List<CertifiedProductDetailsDTO> listings = getRelevantListings();
            
            initializeWritingToFiles();

            List<CompletableFuture<CertifiedProductSearchDetails>> futures = 
                    new ArrayList<CompletableFuture<CertifiedProductSearchDetails>>();
            
            //This should be initialized to a number greater than the number of threads being used
            SimpleObjectPool<CertifiedProductSearchDetails> pool = getInitializedObjectPool(5);
            
            for (CertifiedProductDetailsDTO dto : listings) {
                CompletableFuture<CertifiedProductSearchDetails> cpCompletableFuture =
                    CompletableFuture.supplyAsync(new Supplier<CertifiedProductSearchDetails>() {
                        @Override
                        public CertifiedProductSearchDetails get() {
                            CertifiedProductSearchDetails cpDTO = null;
                            try {
                                cpDTO = pool.borrow();
                                cpDTO = cpdManager.getCertifiedProductDetails(dto.getId());
                                LOGGER.info("Finishing Details for: " + dto.getId());
                                
                            } catch (Exception e) {
                                LOGGER.error(e);
                            }
                            return cpDTO;
                        }
                    });
                    cpCompletableFuture.thenAccept(new Consumer<CertifiedProductSearchDetails>() {
                        @Override
                        public void accept(CertifiedProductSearchDetails cp) {
                            try {
                                xmlPresenter.add(cp);
                                csvPresenter.add(cp);
                                pool.giveBack(cp);
                            } catch (Exception e) {
                                LOGGER.error(e.getMessage(), e);
                            }
                        }
                    });
                    futures.add(cpCompletableFuture);
            }
            
            //Block execution until all of the listings have been written
            CompletableFuture<Void> allTasks =
                    CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]));
            
            allTasks.get();
            
            //This is horrible - need to figure out a way around it...
            //Think this is because async tasks are complete, but the last 'accept' has not completed 
            Thread.sleep(1000);
            
            //Finish writing files and close streams
            completeWritingToFiles();
            
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        Date end = new Date();
        LOGGER.info("Time to create file(s) for {} edition: {} seconds, or {} minutes",
                edition,
                (end.getTime() - start.getTime()) / MILLIS_PER_SECOND,
                (end.getTime() - start.getTime()) / MILLIS_PER_SECOND / SECONDS_PER_MINUTE);
        LOGGER.info("********* Completed the Certified Product Downloadable Resource Creator job for {}. *********",
                edition);
    }

    private SimpleObjectPool<CertifiedProductSearchDetails> getInitializedObjectPool(Integer size) {
        List<CertifiedProductSearchDetails> x = new ArrayList<CertifiedProductSearchDetails>();
        for (int i = 1 ; i <= size ; i++) {
            x.add(new CertifiedProductSearchDetails());
        }
        return new SimpleObjectPool<CertifiedProductSearchDetails>(x);
    }
    private void initializeWritingToFiles() throws IOException {
        xmlPresenter = new CertifiedProductXmlPresenter();
        xmlPresenter.setLogger(LOGGER);
        xmlPresenter.open(getXmlFile());
        
        csvPresenter = getCsvPresenter();
        csvPresenter.setLogger(LOGGER);
        List<CertificationCriterionDTO> criteria = getCriteriaDao().findByCertificationEditionYear(edition);
        csvPresenter.setApplicableCriteria(criteria);
        csvPresenter.open(getCsvFile());
        
    }
    
    private void completeWritingToFiles() throws IOException {
        xmlPresenter.close();
        csvPresenter.close();
    }
    
    private void processListingFutures(List<Future<CertifiedProductSearchDetails>> futures) 
            throws InterruptedException, ExecutionException, XMLStreamException, IOException {
        
        
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
    
    static <T> List<List<T>> chopped(List<T> list, final int size) {
        List<List<T>> parts = new ArrayList<List<T>>();
        final int N = list.size();
        for (int i = 0; i < N; i += size) {
            parts.add(new ArrayList<T>(
                list.subList(i, Math.min(N, i + size)))
            );
        }
        return parts;
    }
}
