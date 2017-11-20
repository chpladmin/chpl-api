package gov.healthit.chpl.app.resource;

import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CertifiedProductDownloadResponse;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class CertifiedProductDownloadableResourceCreatorApp extends DownloadableResourceCreatorApp{
	private static final Logger LOGGER = LogManager.getLogger(CertifiedProductDownloadableResourceCreatorApp.class);
	
	protected abstract List<CertifiedProductDetailsDTO> getRelevantListings() throws EntityRetrievalException;

    protected abstract void writeToFile(File downloadFolder, CertifiedProductDownloadResponse results)
            throws IOException;

    protected void runJob(String[] args) throws Exception {
        File downloadFolder = getDownloadFolder();
        List<CertifiedProductDetailsDTO> listings = getRelevantListings();

        CertifiedProductDownloadResponse results = new CertifiedProductDownloadResponse();
        for (CertifiedProductDetailsDTO currListing : listings) {
            try {
                LOGGER.info("Getting details for listing ID " + currListing.getId());
                Date start = new Date();
                CertifiedProductSearchDetails product = getCpdManager().getCertifiedProductDetails(currListing.getId());
                Date end = new Date();
                LOGGER.info("Got details for listing ID " + currListing.getId() + " in "
                        + (end.getTime() - start.getTime()) / 1000 + " seconds");
                results.getListings().add(product);
            } catch (final EntityRetrievalException ex) {
                LOGGER.error("Could not get details for certified product " + currListing.getId());
            }
        }
        writeToFile(downloadFolder, results);
    }
}
