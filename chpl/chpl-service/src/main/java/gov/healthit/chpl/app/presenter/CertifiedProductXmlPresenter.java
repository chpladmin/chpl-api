package gov.healthit.chpl.app.presenter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.transform.stream.StreamResult;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import gov.healthit.chpl.app.App;
import gov.healthit.chpl.domain.CertifiedProductDownloadResponse;

public class CertifiedProductXmlPresenter implements CertifiedProductPresenter {
	private static final Logger logger = LogManager.getLogger(App.class);

	@Override
	public void presentAsFile(File file, CertifiedProductDownloadResponse cpList) {
		FileOutputStream os = null;
        try {
            os = new FileOutputStream(file);
            Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
            marshaller.setClassesToBeBound(cpList.getClass());
            marshaller.marshal(cpList, new StreamResult(os));

        } catch(FileNotFoundException ex) {
        	logger.error("file not found " + file);
        } finally {
            if (os != null) {
                try { os.close(); } catch(IOException ignore) {}
            }
        }
	}

}
