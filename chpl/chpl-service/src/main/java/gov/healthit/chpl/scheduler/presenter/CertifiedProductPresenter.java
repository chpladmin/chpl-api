package gov.healthit.chpl.scheduler.presenter;

import java.io.File;
import java.io.IOException;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;

/**
 * Interface for presenting certified products as a file.
 * @author alarned
 *
 */
public interface CertifiedProductPresenter {
    /**
     * Opens and initializes a file for Certified Products to be written to
     * @param file - The file where the data will be written
     * @throws IOException - Error opening or initializing the file
     */
    void open(File file) throws IOException;

    /**
     * Writes a Certified Product to the open file
     * @param cp - CertifiedProductSearchDetails
     * @throws IOException - Error writing to the open file
     */
    void add(CertifiedProductSearchDetails cp) throws IOException;

    /**
     * Finalizes any information for the file and closes the file
     * @throws IOException - Error writing to file or closing file 
     */
    void close() throws IOException;
}
