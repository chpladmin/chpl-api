package gov.healthit.chpl.app.resource;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.support.AbstractApplicationContext;

import gov.healthit.chpl.app.App;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CertifiedProductDownloadResponse;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;

public abstract class DownloadableResourceCreatorApp extends App {
	private static final Logger logger = LogManager.getLogger(DownloadableResourceCreatorApp.class);

	protected SimpleDateFormat timestampFormat;
	protected CertifiedProductDetailsManager cpdManager;
	protected CertifiedProductDAO certifiedProductDao;
	protected CertificationCriterionDAO criteriaDao;

    public DownloadableResourceCreatorApp() {
    	timestampFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
    }

	protected void initiateSpringBeans(AbstractApplicationContext context) throws IOException {
		this.setCpdManager((CertifiedProductDetailsManager)context.getBean("certifiedProductDetailsManager"));
		this.setCertifiedProductDao((CertifiedProductDAO)context.getBean("certifiedProductDAO"));
		this.setCriteriaDao((CertificationCriterionDAO)context.getBean("certificationCriterionDAO"));
	}

	protected abstract List<CertifiedProductDetailsDTO> getRelevantListings();
	protected abstract void writeToFile(File downloadFolder, CertifiedProductDownloadResponse results) throws IOException;

	protected void runJob(String[] args) throws Exception {
		File downloadFolder = getDownloadFolder();
		List<CertifiedProductDetailsDTO> listings = getRelevantListings();

		CertifiedProductDownloadResponse results = new CertifiedProductDownloadResponse();
		for(CertifiedProductDetailsDTO currListing : listings) {
			try {
				logger.info("Getting details for listing ID " + currListing.getId());
				Date start = new Date();
				CertifiedProductSearchDetails product = getCpdManager().getCertifiedProductDetails(currListing.getId());
				Date end = new Date();
				logger.info("Got details for listing ID " + currListing.getId() + " in " + (end.getTime() - start.getTime())/1000 + " seconds");
				results.getListings().add(product);
			} catch(EntityRetrievalException ex) {
				logger.error("Could not get details for certified product " + currListing.getId());
			}
		}
		writeToFile(downloadFolder, results);
	}

	public CertifiedProductDAO getCertifiedProductDao() {
		return certifiedProductDao;
	}

	public void setCertifiedProductDao(CertifiedProductDAO certifiedProductDAO) {
		this.certifiedProductDao = certifiedProductDAO;
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
}
