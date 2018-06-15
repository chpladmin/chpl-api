package gov.healthit.chpl.scheduler.presenter;

import java.io.File;
import gov.healthit.chpl.domain.CertifiedProductDownloadResponse;

/**
 * Interface for presenting certified products as a file.
 * @author alarned
 *
 */
public interface CertifiedProductPresenter {
    /**
     * Output domain objects to file.
     * @param file target file
     * @param cpList domain objects
     * @return the number of objects written out
     */
    int presentAsFile(File file, CertifiedProductDownloadResponse cpList);
}
