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
public class ListingsByEditionResourceCreatorApp extends DownloadableResourceCreatorApp {
	private static final Logger logger = LogManager.getLogger(ListingsByEditionResourceCreatorApp.class);

	private String edition;

	public ListingsByEditionResourceCreatorApp() {
		super();
	}

	public ListingsByEditionResourceCreatorApp(String edition) {
		this();
		this.edition = edition;
	}

	protected List<CertifiedProductDetailsDTO> getRelevantListings() {
		logger.info("Finding all listings for edition " + getEdition() + ".");
		Date start = new Date();
		List<CertifiedProductDetailsDTO> listingsForEdition = getCertifiedProductDao().findByEdition(getEdition());
		Date end = new Date();
		logger.info("Found the " + listingsForEdition.size() + " listings from " + getEdition() + " in " + (end.getTime() - start.getTime())/1000 + " seconds");
		return listingsForEdition;
	}

	protected void writeToFile(File downloadFolder, CertifiedProductDownloadResponse results) throws IOException {
		Date now = new Date();
		//write out a download file for this edition
		String xmlFilename = downloadFolder.getAbsolutePath() + File.separator +
				"chpl-" + getEdition() + "-" + getTimestampFormat().format(now) + ".xml";
		File xmlFile = new File(xmlFilename);
		if(!xmlFile.exists()) {
			xmlFile.createNewFile();
		} else {
			xmlFile.delete();
		}
		CertifiedProductXmlPresenter xmlPresenter = new CertifiedProductXmlPresenter();
		logger.info("Writing " + getEdition() + " XML file");
		Date start = new Date();
		xmlPresenter.presentAsFile(xmlFile, results);
		Date end = new Date();
		logger.info("Wrote " + getEdition() + " XML file in " + (end.getTime() - start.getTime())/1000 + " seconds");

		//present as csv
		String csvFilename = downloadFolder.getAbsolutePath() + File.separator +
				"chpl-" + getEdition() + "-" + getTimestampFormat().format(now) + ".csv";
		File csvFile = new File(csvFilename);
		if(!csvFile.exists()) {
			csvFile.createNewFile();
		} else {
			csvFile.delete();
		}
		CertifiedProductCsvPresenter csvPresenter = null;
		if(getEdition().equals("2014")) {
			csvPresenter = new CertifiedProduct2014CsvPresenter();
		} else {
			csvPresenter = new CertifiedProductCsvPresenter();
		}
		List<CertificationCriterionDTO> criteria = getCriteriaDao().findByCertificationEditionYear(getEdition());
		csvPresenter.setApplicableCriteria(criteria);

		logger.info("Writing " + getEdition() + " CSV file");
		start = new Date();
		csvPresenter.presentAsFile(csvFile, results);
		end = new Date();
		logger.info("Wrote " + getEdition() + " CSV file in " + (end.getTime() - start.getTime())/1000 + " seconds");
	}

	public String getEdition() {
		return edition;
	}

	public void setEdition(String edition) {
		this.edition = edition;
	}

	public static void main(String[] args) throws Exception {
		if(args == null || args.length < 1) {
			logger.error("ListingsByEditionResourceCreatorApp HELP: \n"
					+ "\tListingsByEditionResourceCreatorApp 2014\n"
					+ "\tListingsByEditionResourceCreatorApp expects an argument that is the edition (2011, 2014, or 2015)");
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
}
