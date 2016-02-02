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

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import javax.xml.transform.stream.StreamResult;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dao.AdditionalSoftwareDAO;
import gov.healthit.chpl.dao.CQMResultDetailsDAO;
import gov.healthit.chpl.dao.CertificationResultDetailsDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.CertifiedProductSearchResultDAO;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CertifiedProductDownloadDetails;
import gov.healthit.chpl.domain.CertifiedProductDownloadResponse;
import gov.healthit.chpl.dto.AdditionalSoftwareDTO;
import gov.healthit.chpl.dto.CQMResultDetailsDTO;
import gov.healthit.chpl.dto.CertificationResultDetailsDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;

@Component("app")
public class App {
    private static final String DEFAULT_PROPERTIES_FILE = "environment.properties";
	private static final Logger logger = LogManager.getLogger(App.class);

	private SimpleDateFormat timestampFormat;
	private CertifiedProductDAO certifiedProductDAO;
	private CertifiedProductSearchResultDAO certifiedProductSearchResultDAO;
    private AdditionalSoftwareDAO additionalSoftwareDAO;
    private CertificationResultDetailsDAO certificationResultDetailsDAO;
    private CQMResultDetailsDAO cqmResultDetailsDAO;
	
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
		 app.setCertifiedProductDAO((CertifiedProductDAO)context.getBean("certifiedProductDAO"));
		 app.setCertifiedProductSearchResultDAO((CertifiedProductSearchResultDAO)context.getBean("certifiedProductSearchResultDAO"));
		 app.setAdditionalSoftwareDAO((AdditionalSoftwareDAO)context.getBean("additionalSoftwareDAO"));
		 app.setCertificationResultDetailsDAO((CertificationResultDetailsDAO)context.getBean("certificationResultDetailsDAO"));
		 app.setCqmResultDetailsDAO((CQMResultDetailsDAO)context.getBean("cqmResultDetailsDAO"));
        
        CertifiedProductDownloadResponse result = new CertifiedProductDownloadResponse();
        
        //write out the file to a different location so as not to 
        //overwrite the existing download file
        List<CertifiedProductDetailsDTO> allCertifiedProducts = app.getCertifiedProductDAO().findAll();
		for(CertifiedProductDetailsDTO currProduct : allCertifiedProducts) {
		//for(int i = 1; i < 10; i++) {
		//	CertifiedProductDetailsDTO currProduct = allCertifiedProducts.get(i);
			try {
				
				CertifiedProductDetailsDTO dto = app.getCertifiedProductSearchResultDAO().getById(currProduct.getId());
				CertifiedProductDownloadDetails downloadDetails = new CertifiedProductDownloadDetails(dto);
				
				//additional software
				List<AdditionalSoftwareDTO> additionalSoftwareDTOs = app.getAdditionalSoftwareDAO().findByCertifiedProductId(dto.getId());
				if(additionalSoftwareDTOs != null && additionalSoftwareDTOs.size() > 0) {
					StringBuffer additionalSoftwareBuf = new StringBuffer();
					for(AdditionalSoftwareDTO currSoftware : additionalSoftwareDTOs) {
						if(additionalSoftwareBuf.length() > 0) {
							additionalSoftwareBuf.append(";");
						}
						additionalSoftwareBuf.append(currSoftware.getName());
						if(!StringUtils.isEmpty(currSoftware.getVersion()) &&
								!currSoftware.getVersion().equals("-1")) {
							additionalSoftwareBuf.append(" v." + currSoftware.getVersion());
						}
					}
					downloadDetails.setAdditionalSoftware(additionalSoftwareBuf.toString());
				}
				
				//certs, call these methods by reflection
				List<CertificationResultDetailsDTO> certResultDTOs = app.getCertificationResultDetailsDAO().getCertificationResultDetailsByCertifiedProductId(dto.getId());
				for (CertificationResultDetailsDTO certResult : certResultDTOs){
					downloadDetails.setCertificationSuccess(certResult.getNumber(), certResult.getSuccess().booleanValue());
				}
				
				//cqm results
				List<CQMResultDetailsDTO> cqmResultDTOs = app.getCqmResultDetailsDAO().getCQMResultDetailsByCertifiedProductId(dto.getId());
				for (CQMResultDetailsDTO cqmResultDTO : cqmResultDTOs) { 
					if(dto.getYear().equals("2014") && !StringUtils.isEmpty(cqmResultDTO.getCmsId())) {
						downloadDetails.addCmsVersion(cqmResultDTO.getCmsId(), cqmResultDTO.getVersion());
					} else if(dto.getYear().equals("2011") && !StringUtils.isEmpty(cqmResultDTO.getNqfNumber())) {
						downloadDetails.setNqfSuccess(cqmResultDTO.getNqfNumber(), cqmResultDTO.getSuccess().booleanValue());
					}
				}	
				
				result.getProducts().add(downloadDetails);
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

	public CertifiedProductSearchResultDAO getCertifiedProductSearchResultDAO() {
		return certifiedProductSearchResultDAO;
	}

	public void setCertifiedProductSearchResultDAO(CertifiedProductSearchResultDAO certifiedProductSearchResultDAO) {
		this.certifiedProductSearchResultDAO = certifiedProductSearchResultDAO;
	}

	public AdditionalSoftwareDAO getAdditionalSoftwareDAO() {
		return additionalSoftwareDAO;
	}

	public void setAdditionalSoftwareDAO(AdditionalSoftwareDAO additionalSoftwareDAO) {
		this.additionalSoftwareDAO = additionalSoftwareDAO;
	}

	public CertificationResultDetailsDAO getCertificationResultDetailsDAO() {
		return certificationResultDetailsDAO;
	}

	public void setCertificationResultDetailsDAO(CertificationResultDetailsDAO certificationResultDetailsDAO) {
		this.certificationResultDetailsDAO = certificationResultDetailsDAO;
	}

	public CQMResultDetailsDAO getCqmResultDetailsDAO() {
		return cqmResultDetailsDAO;
	}

	public void setCqmResultDetailsDAO(CQMResultDetailsDAO cqmResultDetailsDAO) {
		this.cqmResultDetailsDAO = cqmResultDetailsDAO;
	}

	public SimpleDateFormat getTimestampFormat() {
		return timestampFormat;
	}

	public void setTimestampFormat(SimpleDateFormat timestampFormat) {
		this.timestampFormat = timestampFormat;
	}
}
