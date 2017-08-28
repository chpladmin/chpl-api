package gov.healthit.chpl.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.app.presenter.CertifiedProduct2014CsvPresenter;
import gov.healthit.chpl.app.presenter.CertifiedProductCsvPresenter;
import gov.healthit.chpl.app.presenter.CertifiedProductXmlPresenter;
import gov.healthit.chpl.app.surveillance.presenter.NonconformityCsvPresenter;
import gov.healthit.chpl.app.surveillance.presenter.SurveillanceCsvPresenter;
import gov.healthit.chpl.app.surveillance.presenter.SurveillanceReportCsvPresenter;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CertifiedProductDownloadResponse;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;

@Component("resourceApp")
public class DownloadableResourceCreatorApp {
    private static final String DEFAULT_PROPERTIES_FILE = "environment.properties";
	private static final Logger logger = LogManager.getLogger(DownloadableResourceCreatorApp.class);

	private SimpleDateFormat timestampFormat;
	private CertifiedProductDetailsManager cpdManager;
	private CertifiedProductDAO certifiedProductDAO;
	private CertificationCriterionDAO criteriaDao;
	
    public DownloadableResourceCreatorApp() {
    	timestampFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
    }
    
	public static void main( String[] args ) throws Exception {	
		//read in properties - we need these to set up the data source context
		Properties props = null;
		InputStream in = DownloadableResourceCreatorApp.class.getClassLoader().getResourceAsStream(DEFAULT_PROPERTIES_FILE);
		
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
		DownloadableResourceCreatorApp app = new DownloadableResourceCreatorApp();
		app.setCpdManager((CertifiedProductDetailsManager)context.getBean("certifiedProductDetailsManager"));
		app.setCertifiedProductDAO((CertifiedProductDAO)context.getBean("certifiedProductDAO"));
		app.setCriteriaDao((CertificationCriterionDAO)context.getBean("certificationCriterionDAO"));
		
		//maps the year to the list of products for that year
		Map<String, CertifiedProductDownloadResponse> resultMap = 
				new HashMap<String, CertifiedProductDownloadResponse>();
		 
		logger.info("Finding the list of all certified products.");
		Date start = new Date();
		List<CertifiedProductDetailsDTO> allCertifiedProducts = app.getCertifiedProductDAO().findAll();
		Date end = new Date();
		logger.info("Found the " + allCertifiedProducts.size() + " certified products in " + (end.getTime() - start.getTime())/1000 + " seconds");
		
		//List<CertifiedProductDetailsDTO> allCertifiedProducts = app.getCertifiedProductDAO().findWithSurveillance();
		for(CertifiedProductDetailsDTO currProduct : allCertifiedProducts) {
		//for(int i = 1; i < 10; i++) {
			//CertifiedProductDetailsDTO currProduct = allCertifiedProducts.get(i);
			try {
				logger.info("Getting details for listing ID " + currProduct.getId());
				start = new Date();
				CertifiedProductSearchDetails product = app.getCpdManager().getCertifiedProductDetails(currProduct.getId());
				end = new Date();
				logger.info("Got details for listing ID " + currProduct.getId() + " in " + (end.getTime() - start.getTime())/1000 + " seconds"); 
				
				String certificationYear = product.getCertificationEdition().get("name").toString();
				certificationYear = certificationYear.trim();
				if(!certificationYear.startsWith("20")) {
					certificationYear = "20" + certificationYear;
				}
				
				if(resultMap.get(certificationYear) == null) {
					CertifiedProductDownloadResponse yearResult = new CertifiedProductDownloadResponse();
					resultMap.put(certificationYear, yearResult);
				}
				((CertifiedProductDownloadResponse)resultMap.get(certificationYear)).getListings().add(product);
				
			} catch(EntityRetrievalException ex) {
				logger.error("Could not certified product details for certified product " + currProduct.getId());
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
        //write out a separate file for each edition
        for(String year : resultMap.keySet()) {
	        //present as xml
	        String xmlFilename = downloadFolder.getAbsolutePath() + File.separator + 
	        		"chpl-" + year + "-" + app.getTimestampFormat().format(now) + ".xml";
	        File xmlFile = new File(xmlFilename);
	        if(!xmlFile.exists()) {
	        	xmlFile.createNewFile();
	        } else {
	        	xmlFile.delete();
	        }
	        CertifiedProductXmlPresenter xmlPresenter = new CertifiedProductXmlPresenter();
	        logger.info("Writing " + year + " XML file");
	        start = new Date();
	        xmlPresenter.presentAsFile(xmlFile, resultMap.get(year));
	        end = new Date();
	        logger.info("Wrote " + year + " XML file in " + (end.getTime() - start.getTime())/1000 + " seconds");
	        
	        //present as csv
	        String csvFilename = downloadFolder.getAbsolutePath() + File.separator + 
	        		"chpl-" + year + "-" + app.getTimestampFormat().format(now) + ".csv";
	        File csvFile = new File(csvFilename);
	        if(!csvFile.exists()) {
	        	csvFile.createNewFile();
	        } else {
	        	csvFile.delete();
	        }
	        CertifiedProductCsvPresenter csvPresenter = null;
	        if(year.equals("2014")) {
	        	csvPresenter = new CertifiedProduct2014CsvPresenter();
	        } else {
	        	csvPresenter = new CertifiedProductCsvPresenter();
	        }
	        List<CertificationCriterionDTO> criteria = app.getCriteriaDao().findByCertificationEditionYear(year);
	        csvPresenter.setApplicableCriteria(criteria);
	        
	        logger.info("Writing " + year + " CSV file");
	        start = new Date();
	        csvPresenter.presentAsFile(csvFile, resultMap.get(year));
	        end = new Date();
	        logger.info("Wrote " + year + " CSV file in " + (end.getTime() - start.getTime())/1000 + " seconds");
        }
        
        //put all of the products together
        CertifiedProductDownloadResponse allResults = new CertifiedProductDownloadResponse();
        for(String year : resultMap.keySet()) { 
        	CertifiedProductDownloadResponse result = resultMap.get(year);
        	allResults.getListings().addAll(result.getListings());
        	result.getListings().clear();
        }
        
        //write out an xml file with all product data
        String allCpXmlFilename = downloadFolder.getAbsolutePath() + File.separator + 
        		"chpl-all-" + app.getTimestampFormat().format(now) + ".xml";
        File allCpXmlFile = new File(allCpXmlFilename);
        if(!allCpXmlFile.exists()) {
        	allCpXmlFile.createNewFile();
        } else {
        	allCpXmlFile.delete();
        }
        CertifiedProductXmlPresenter presenter = new CertifiedProductXmlPresenter();
        
        logger.info("Writing ALL XML file");
        start = new Date();
        presenter.presentAsFile(allCpXmlFile, allResults);
        end = new Date();
        logger.info("Wrote ALL XML file in " + (end.getTime() - start.getTime())/1000 + " seconds");
        
        //write out a csv file containing all surveillance
        String allSurvCsvFilename = downloadFolder.getAbsolutePath() + File.separator + 
        		"surveillance-all.csv";
        File allSurvCsvFile = new File(allSurvCsvFilename);
        if(!allSurvCsvFile.exists()) {
        	allSurvCsvFile.createNewFile();
        } else {
        	allSurvCsvFile.delete();
        }
        SurveillanceCsvPresenter survCsvPresenter = new SurveillanceCsvPresenter();
        survCsvPresenter.setProps(props);
        
        logger.info("Writing all surveillance CSV file");
        start = new Date();
        survCsvPresenter.presentAsFile(allSurvCsvFile, allResults);
        end = new Date();
        logger.info("Wrote all surveillance CSV file in " + (end.getTime() - start.getTime())/1000 + " seconds");
        
        //write out a csv file containing surveillance with nonconformities       
        String nonconformityCsvFilename = downloadFolder.getAbsolutePath() + File.separator + 
        		"surveillance-with-nonconformities.csv";
        File nonconformityCsvFile = new File(nonconformityCsvFilename);
        if(!nonconformityCsvFile.exists()) {
        	nonconformityCsvFile.createNewFile();
        } else {
        	nonconformityCsvFile.delete();
        }
        
        NonconformityCsvPresenter ncCsvPresenter = new NonconformityCsvPresenter();
        ncCsvPresenter.setProps(props);
        logger.info("Writing nonconformity CSV file");
        start = new Date();
        ncCsvPresenter.presentAsFile(nonconformityCsvFile, allResults);
        end = new Date();
        logger.info("Wrote nonconformity CSV file in " + (end.getTime() - start.getTime())/1000 + " seconds");
        
        //write out a csv file containing surveillance basic report     
        String basicReportCsvName = downloadFolder.getAbsolutePath() + File.separator + 
        		"surveillance-basic-report.csv";
        File basicReportCsvFile = new File(basicReportCsvName);
        if(!basicReportCsvFile.exists()) {
        	basicReportCsvFile.createNewFile();
        } else {
        	basicReportCsvFile.delete();
        }
        
        SurveillanceReportCsvPresenter basicReportCsvPresenter = new SurveillanceReportCsvPresenter();
        basicReportCsvPresenter.setProps(props);
        logger.info("Writing basic surveillance report file");
        start = new Date();
        basicReportCsvPresenter.presentAsFile(basicReportCsvFile, allResults);
        end = new Date();
        logger.info("Wrote basic surveillance report file in " + (end.getTime() - start.getTime())/1000 + " seconds");
        
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

	public CertificationCriterionDAO getCriteriaDao() {
		return criteriaDao;
	}

	public void setCriteriaDao(CertificationCriterionDAO criteriaDao) {
		this.criteriaDao = criteriaDao;
	}
}
