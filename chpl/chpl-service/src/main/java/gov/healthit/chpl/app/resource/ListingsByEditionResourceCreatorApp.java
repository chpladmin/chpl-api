package gov.healthit.chpl.app.resource;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.app.App;
import gov.healthit.chpl.app.AppConfig;
import gov.healthit.chpl.app.presenter.CertifiedProduct2014CsvPresenter;
import gov.healthit.chpl.app.presenter.CertifiedProductCsvPresenter;
import gov.healthit.chpl.app.presenter.CertifiedProductXmlPresenter;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CertifiedProductDownloadResponse;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;

@Component("listingsByEditionResourceCreatorApp")
public class ListingsByEditionResourceCreatorApp extends App {
	private static final Logger logger = LogManager.getLogger(ListingsByEditionResourceCreatorApp.class);

	private SimpleDateFormat timestampFormat;
	private String edition;
	private CertifiedProductDetailsManager cpdManager;
	private CertifiedProductDAO certifiedProductDAO;
	private CertificationCriterionDAO criteriaDao;
	
    public ListingsByEditionResourceCreatorApp() {
    	timestampFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
    }
    
    public ListingsByEditionResourceCreatorApp(String edition) {
    	this();
    	this.edition = edition;
    }
    
	protected void initiateSpringBeans(AbstractApplicationContext context) throws IOException {
		this.setCpdManager((CertifiedProductDetailsManager)context.getBean("certifiedProductDetailsManager"));
		this.setCertifiedProductDAO((CertifiedProductDAO)context.getBean("certifiedProductDAO"));
		this.setCriteriaDao((CertificationCriterionDAO)context.getBean("certificationCriterionDAO"));
	}
	
	public static void main(String[] args) throws Exception {
		if(args == null || args.length < 2) {
			logger.error("The edition is a required argument to run the application.");
			return;
		}
		
		String edition = args[1].trim();
		ListingsByEditionResourceCreatorApp app = new ListingsByEditionResourceCreatorApp(edition);
		app.setLocalContext();
		AbstractApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
		app.initiateSpringBeans(context);
		
		String downloadFolderPath = app.getDownloadFolderPath(args);
		File downloadFolder = app.getDownloadFolder(downloadFolderPath);

		CertifiedProductDownloadResponse results = new CertifiedProductDownloadResponse();
		logger.info("Finding all listings for edition " + app.getEdition() + ".");
		Date start = new Date();
		List<CertifiedProductDetailsDTO> listingsForEdition = app.getCertifiedProductDAO().findByEdition(app.getEdition());
		Date end = new Date();
		logger.info("Found the " + listingsForEdition.size() + " listings from " + app.getEdition() + " in " + (end.getTime() - start.getTime())/1000 + " seconds");
		for(CertifiedProductDetailsDTO currListing : listingsForEdition) {
			try {
				logger.info("Getting details for listing ID " + currListing.getId());
				start = new Date();
				CertifiedProductSearchDetails product = app.getCpdManager().getCertifiedProductDetails(currListing.getId());
				end = new Date();
				logger.info("Got details for listing ID " + currListing.getId() + " in " + (end.getTime() - start.getTime())/1000 + " seconds"); 
				results.getListings().add(product);				
			} catch(EntityRetrievalException ex) {
				logger.error("Could not get details for certified product " + currListing.getId());
			}
		}
		
		Date now = new Date();
		//write out a download file for this edition
		String xmlFilename = downloadFolder.getAbsolutePath() + File.separator + 
				"chpl-" + app.getEdition() + "-" + app.getTimestampFormat().format(now) + ".xml";
		File xmlFile = new File(xmlFilename);
		if(!xmlFile.exists()) {
			xmlFile.createNewFile();
		} else {
			xmlFile.delete();
		}
		CertifiedProductXmlPresenter xmlPresenter = new CertifiedProductXmlPresenter();
		logger.info("Writing " + app.getEdition() + " XML file");
		start = new Date();
		xmlPresenter.presentAsFile(xmlFile, results);
		end = new Date();
		logger.info("Wrote " + app.getEdition() + " XML file in " + (end.getTime() - start.getTime())/1000 + " seconds");
		
		//present as csv
		String csvFilename = downloadFolder.getAbsolutePath() + File.separator + 
				"chpl-" + app.getEdition() + "-" + app.getTimestampFormat().format(now) + ".csv";
		File csvFile = new File(csvFilename);
		if(!csvFile.exists()) {
			csvFile.createNewFile();
		} else {
			csvFile.delete();
		}
		CertifiedProductCsvPresenter csvPresenter = null;
		if(app.getEdition().equals("2014")) {
			csvPresenter = new CertifiedProduct2014CsvPresenter();
		} else {
			csvPresenter = new CertifiedProductCsvPresenter();
		}
		List<CertificationCriterionDTO> criteria = app.getCriteriaDao().findByCertificationEditionYear(app.getEdition());
		csvPresenter.setApplicableCriteria(criteria);
		
		logger.info("Writing " + app.getEdition() + " CSV file");
		start = new Date();
		csvPresenter.presentAsFile(csvFile, results);
		end = new Date();
		logger.info("Wrote " + app.getEdition() + " CSV file in " + (end.getTime() - start.getTime())/1000 + " seconds");
		context.close();
	}
	
	public CertifiedProductDAO getCertifiedProductDAO() {
		return certifiedProductDAO;
	}

	public void setCertifiedProductDAO(CertifiedProductDAO certifiedProductDAO) {
		this.certifiedProductDAO = certifiedProductDAO;
	}

	public SimpleDateFormat getTimestampFormat() {
		return timestampFormat;
	}

	public void setTimestampFormat(SimpleDateFormat timestampFormat) {
		this.timestampFormat = timestampFormat;
	}

	public CertifiedProductDetailsManager getCpdManager() {
		return cpdManager;
	}

	public void setCpdManager(CertifiedProductDetailsManager cpdManager) {
		this.cpdManager = cpdManager;
	}

	public CertificationCriterionDAO getCriteriaDao() {
		return criteriaDao;
	}

	public void setCriteriaDao(CertificationCriterionDAO criteriaDao) {
		this.criteriaDao = criteriaDao;
	}

	public String getEdition() {
		return edition;
	}

	public void setEdition(String edition) {
		this.edition = edition;
	}
}
