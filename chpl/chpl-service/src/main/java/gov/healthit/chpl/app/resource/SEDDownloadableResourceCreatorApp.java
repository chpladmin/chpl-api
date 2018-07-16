package gov.healthit.chpl.app.resource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import gov.healthit.chpl.exception.EntityRetrievalException;

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
