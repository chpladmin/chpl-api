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
    void open(File file) throws IOException;
    
    void add(CertifiedProductSearchDetails cp) throws IOException;
    
    void close() throws IOException;
}
