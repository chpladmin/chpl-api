package gov.healthit.chpl.app.resource;

import gov.healthit.chpl.dao.EntityRetrievalException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public abstract class SEDDownloadableResourceCreatorApp extends DownloadableResourceCreatorApp{
	
	protected abstract ArrayList<SEDRow> getRelevantListings() throws EntityRetrievalException;

    protected abstract void writeToFile(File downloadFolder, ArrayList<SEDRow> result)
           throws IOException;

    protected void runJob(String[] args) throws Exception {
        File downloadFolder = getDownloadFolder();
        ArrayList<SEDRow> listings = getRelevantListings();
        writeToFile(downloadFolder, listings);
    }
}
