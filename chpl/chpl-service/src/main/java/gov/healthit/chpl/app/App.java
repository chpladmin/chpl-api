package gov.healthit.chpl.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CertifiedProductDownloadResponse;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;

@Component("app")
public class App {
    private static final String DEFAULT_PROPERTIES_FILE = "environment.properties";
	private static final Logger logger = LogManager.getLogger(App.class);

	private SimpleDateFormat timestampFormat;
	private CertifiedProductDetailsManager cpdManager;
	private CertifiedProductDAO certifiedProductDAO;
	
    public App() {
    	timestampFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
    }
    
	public static void main( String[] args ) throws Exception {	
		//read in properties - we need these to set up the data source context
		Properties props = null;
		InputStream in = App.class.getClassLoader().getResourceAsStream(DEFAULT_PROPERTIES_FILE);
		
		if (in == null) {
			props = null;
			throw new FileNotFoundException("Environment Properties File not found in class path.");
		} else {
			props = new Properties();
			props.load(in);
			in.close();
		}

		//set up data source context
		 LocalContext ctx = LocalContextFactory.createLocalContext(props.getProperty("dbDriverClass"));
		 ctx.addDataSource(props.getProperty("dataSourceName"),props.getProperty("dataSourceConnection"), 
				 props.getProperty("dataSourceUsername"), props.getProperty("dataSourcePassword"));
		 
		 //init spring classes
		 AbstractApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
		 System.out.println(context.getClassLoader());
		 App app = new App();
		 app.setCpdManager((CertifiedProductDetailsManager)context.getBean("certifiedProductDetailsManager"));
		 app.setCertifiedProductDAO((CertifiedProductDAO)context.getBean("certifiedProductDAO"));
        
        CertifiedProductDownloadResponse result = new CertifiedProductDownloadResponse();
        
        //write out the file to a different location so as not to 
        //overwrite the existing download file
        List<CertifiedProductDetailsDTO> allCertifiedProducts = app.getCertifiedProductDAO().findAll();
		for(CertifiedProductDetailsDTO currProduct : allCertifiedProducts) {
		//for(int i = 1; i < 10; i++) {
		//	CertifiedProductDetailsDTO currProduct = allCertifiedProducts.get(i);
			try {
				
				CertifiedProductSearchDetails product = app.getCpdManager().getCertifiedProductDetails(currProduct.getId());
				result.getProducts().add(product);
				
			} catch(EntityRetrievalException ex) {
				//logger.error("Could not certified product details for certified product " + currProduct.getId());
			}
		}
        
        String downloadFolderPath;
        if (args.length > 0) {
        	downloadFolderPath = args[0];
        } else {
        	downloadFolderPath = props.getProperty("downloadFolderPath");
        }
        File downloadFolder = new File(downloadFolderPath);
        if(!downloadFolder.exists()) {
        	downloadFolder.mkdirs();
        }
        
        Date now = new Date();
        String newFileName = downloadFolder.getAbsolutePath() + File.separator + "chpl-" + app.getTimestampFormat().format(now) + ".xml";
        File newFile = new File(newFileName);
        if(!newFile.exists()) {
        	newFile.createNewFile();
        } else {
        	newFile.delete();
        }
        
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(newFile);
            Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
            marshaller.setClassesToBeBound(result.getClass());
            marshaller.marshal(result, new StreamResult(os));

        } catch(FileNotFoundException ex) {
        	logger.error("file not found " + newFile);
        } finally {
            if (os != null) {
                try { os.close(); } catch(IOException ignore) {}
            }
        }
        
        context.close();
	}
	
	public CertifiedProductDAO getCertifiedProductDAO() {
		return certifiedProductDAO;
	}

	public void setCertifiedProductDAO(CertifiedProductDAO certifiedProductDAO) {
		this.certifiedProductDAO = certifiedProductDAO;
	}

	public SimpleDateFormat getTimestampFormat() {
		return timestampFormat;
	}

	public void setTimestampFormat(SimpleDateFormat timestampFormat) {
		this.timestampFormat = timestampFormat;
	}

	public CertifiedProductDetailsManager getCpdManager() {
		return cpdManager;
	}

	public void setCpdManager(CertifiedProductDetailsManager cpdManager) {
		this.cpdManager = cpdManager;
	}
}
