package gov.healthit.chpl.app.presenter;

import java.io.File;
import java.util.ArrayList;

import gov.healthit.chpl.domain.CertifiedProductDownloadResponse;
import gov.healthit.chpl.domain.SEDRow;

public interface CertifiedProductPresenter {
    public int presentAsFile(File file, CertifiedProductDownloadResponse cpList);

	public int presentAsFileSED(File file, ArrayList<SEDRow> result);
}
