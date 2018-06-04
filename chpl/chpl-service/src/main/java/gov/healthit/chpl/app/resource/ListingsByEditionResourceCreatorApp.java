package gov.healthit.chpl.app.resource;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.app.AppConfig;
import gov.healthit.chpl.app.presenter.CertifiedProduct2014CsvPresenter;
import gov.healthit.chpl.app.presenter.CertifiedProductCsvPresenter;
import gov.healthit.chpl.app.presenter.CertifiedProductXmlPresenter;
import gov.healthit.chpl.domain.CertifiedProductDownloadResponse;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;

@Component("listingsByEditionResourceCreatorApp")
public class ListingsByEditionResourceCreatorApp extends CertifiedProductDownloadableResourceCreatorApp {
    private static final Logger LOGGER = LogManager.getLogger(ListingsByEditionResourceCreatorApp.class);

    private String edition;

    public ListingsByEditionResourceCreatorApp() {
        super();
    }

    public ListingsByEditionResourceCreatorApp(String edition) {
        this();
        this.edition = edition;
    }

    public static void main(String[] args) throws Exception {
        if (args == null || args.length < 1) {
            LOGGER.error("ListingsByEditionResourceCreatorApp HELP: \n" + "\tListingsByEditionResourceCreatorApp 2014\n"
                    + "\tListingsByEditionResourceCreatorApp expects an argument that is "
                    + "the edition (2011, 2014, or 2015)");
            return;
        }

        String edition = args[0].trim();
        ListingsByEditionResourceCreatorApp app = new ListingsByEditionResourceCreatorApp(edition);
        app.setLocalContext();
        AbstractApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        app.initiateSpringBeans(context);
        app.runJob(args);
        context.close();
    }

    @Override
    protected List<CertifiedProductDetailsDTO> getRelevantListings() {
        LOGGER.info("Finding all listings for edition " + getEdition() + ".");
        Date start = new Date();
        List<CertifiedProductDetailsDTO> listingsForEdition = getCertifiedProductDao().findByEdition(getEdition());
        Date end = new Date();
        LOGGER.info("Found the " + listingsForEdition.size() + " listings from " + getEdition() + " in "
                + ((end.getTime() - start.getTime()) / 1000) + " seconds");
        return listingsForEdition;
    }

    @Override
    protected void writeToFile(final File downloadFolder, final CertifiedProductDownloadResponse results)
            throws IOException {
        writeToCsv(downloadFolder, results);
        writeToXml(downloadFolder, results);
    }

    private void writeToXml(final File downloadFolder, final CertifiedProductDownloadResponse results)
            throws IOException {
        Date start = new Date();
        String xmlFilename = getFileName(downloadFolder.getAbsolutePath(),
                getTimestampFormat().format(new Date()), "csv");
        File xmlFile = getFile(xmlFilename);
        CertifiedProductXmlPresenter xmlPresenter = new CertifiedProductXmlPresenter();
        xmlPresenter.presentAsFile(xmlFile, results);
        Date end = new Date();
        LOGGER.info("Wrote " + getEdition() + " XML file in " + (end.getTime() - start.getTime()) / 1000 + " seconds");
    }

    private void writeToCsv(final File downloadFolder, final CertifiedProductDownloadResponse results)
            throws IOException {
        Date start = new Date();
        String csvFilename = getFileName(downloadFolder.getAbsolutePath(),
                getTimestampFormat().format(new Date()), "csv");
        File csvFile = getFile(csvFilename);
        CertifiedProductCsvPresenter csvPresenter = getCsvPresenter(getEdition());
        List<CertificationCriterionDTO> criteria = getCriteriaDao().findByCertificationEditionYear(getEdition());
        csvPresenter.setApplicableCriteria(criteria);
        csvPresenter.presentAsFile(csvFile, results);
        Date end = new Date();
        LOGGER.info("Wrote " + getEdition() + " CSV file in " + (end.getTime() - start.getTime()) / 1000 + " seconds");
    }

    private CertifiedProductCsvPresenter getCsvPresenter(final String edition) {
        CertifiedProductCsvPresenter csvPresenter = null;
        if (edition.equals("2014")) {
            csvPresenter = new CertifiedProduct2014CsvPresenter();
        } else {
            csvPresenter = new CertifiedProductCsvPresenter();
        }
        return csvPresenter;
    }

    private String getFileName(final String path, final String timeStamp, final String extension) {
        return path + File.separator + "chpl-" + getEdition() + "-" + timeStamp + "." + extension;
    }

    private File getFile(final String fileName) throws IOException {
        File file = new File(fileName);
        if (!file.exists()) {
            file.createNewFile();
        } else {
            file.delete();
        }
        return file;
    }

    public String getEdition() {
        return edition;
    }

    public void setEdition(final String edition) {
        this.edition = edition;
    }

}
