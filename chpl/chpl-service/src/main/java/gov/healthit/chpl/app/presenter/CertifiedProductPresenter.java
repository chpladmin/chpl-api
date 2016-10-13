package gov.healthit.chpl.app.presenter;

import java.io.File;

import gov.healthit.chpl.domain.CertifiedProductDownloadResponse;

public interface CertifiedProductPresenter {
	public void presentAsFile(File file, CertifiedProductDownloadResponse cpList);
}
