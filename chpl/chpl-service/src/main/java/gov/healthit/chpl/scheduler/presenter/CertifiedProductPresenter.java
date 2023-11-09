package gov.healthit.chpl.scheduler.presenter;

import java.io.File;
import java.io.IOException;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;

public abstract class CertifiedProductPresenter {

    public void open(File dataFile, File definitionFile) throws IOException {
        open(dataFile);
    }

    public abstract void open(File file) throws IOException;
    public abstract void add(CertifiedProductSearchDetails cp) throws IOException;
}
