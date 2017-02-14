package gov.healthit.chpl.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import gov.healthit.chpl.app.surveillance.presenter.SurveillanceOversightCsvPresenter;
import gov.healthit.chpl.app.surveillance.presenter.SurveillanceReportCsvPresenter;
import gov.healthit.chpl.auth.SendMailUtil;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CertifiedProductDownloadResponse;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;

@Component("surveillanceApp")
public class SurveillanceOversightReportApp {
    private static final String DEFAULT_PROPERTIES_FILE = "environment.properties";
	private static final Logger logger = LogManager.getLogger(SurveillanceOversightReportApp.class);

	private SimpleDateFormat timestampFormat;
	private CertifiedProductDetailsManager cpdManager;
	private CertifiedProductDAO certifiedProductDAO;
	private SurveillanceOversightCsvPresenter presenter;
	private SendMailUtil mailUtils;
	
    public SurveillanceOversightReportApp() {
    	timestampFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
    }
    
	public static void main( String[] args ) throws Exception {
		int numDaysRuleBreaksBeforeMarkedOngoing = 1; // daily report
		if(args.length > 1) {
			//might also be 7 for a weekly report, 30 for monthly, etc
			try {
				Integer numDaysArg = new Integer(args[1]);
				if(numDaysArg != null && numDaysArg > 0) {
					numDaysRuleBreaksBeforeMarkedOngoing = numDaysArg.intValue();
				}
			} catch(NumberFormatException nfe) {
				logger.error("Could not parse " + args[1] + " as an integer.");
			}
		}
		//read in properties - we need these to set up the data source context
		Properties props = null;
		InputStream in = SurveillanceOversightReportApp.class.getClassLoader().getResourceAsStream(DEFAULT_PROPERTIES_FILE);
		
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
		 SurveillanceOversightReportApp app = new SurveillanceOversightReportApp();
		 app.setCpdManager((CertifiedProductDetailsManager)context.getBean("certifiedProductDetailsManager"));
		 app.setCertifiedProductDAO((CertifiedProductDAO)context.getBean("certifiedProductDAO"));
		 app.setPresenter((SurveillanceOversightCsvPresenter)context.getBean("surveillanceOversightCsvPresenter"));
		 app.getPresenter().setProps(props);
	     app.getPresenter().setNumDaysUntilOngoing(numDaysRuleBreaksBeforeMarkedOngoing);
		 app.setMailUtils((SendMailUtil)context.getBean("SendMailUtil"));
		 
		 //where to store this file
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
        
        List<CertifiedProductDetailsDTO> allCertifiedProducts = app.getCertifiedProductDAO().findWithSurveillance();
        List<CertifiedProductSearchDetails> allCertifiedProductDetails = new ArrayList<CertifiedProductSearchDetails>(allCertifiedProducts.size());
		for(CertifiedProductDetailsDTO currProduct : allCertifiedProducts) {
			try {
				CertifiedProductSearchDetails product = app.getCpdManager().getCertifiedProductDetails(currProduct.getId());
				allCertifiedProductDetails.add(product);
			} catch(EntityRetrievalException ex) {
				logger.error("Could not certified product details for certified product " + currProduct.getId());
			}
		}
		CertifiedProductDownloadResponse allCps = new CertifiedProductDownloadResponse();
		allCps.setProducts(allCertifiedProductDetails);
		
        //write out a csv file containing all surveillance
        String dailySurveillanceReportFilename = downloadFolder.getAbsolutePath() + File.separator + 
        		"surveillance-oversight-report-daily.csv";
        File dailySurveillanceReportFile = new File(dailySurveillanceReportFilename);
        if(!dailySurveillanceReportFile.exists()) {
        	dailySurveillanceReportFile.createNewFile();
        } else {
        	dailySurveillanceReportFile.delete();
        }
        app.getPresenter().presentAsFile(dailySurveillanceReportFile, allCps);
       
        String toEmailProp = props.getProperty("oversightEmailDailyTo");
        String[] toEmail = toEmailProp.split(";");
        String subject = props.getProperty("oversightEmailDailySubject");
        String htmlMessage = props.getProperty("oversighEmailDailyContent");
        List<File> files = new ArrayList<File>();
        files.add(dailySurveillanceReportFile);
        app.getMailUtils().sendEmail(toEmail, subject, htmlMessage, files, props);
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

	public SurveillanceOversightCsvPresenter getPresenter() {
		return presenter;
	}

	public void setPresenter(SurveillanceOversightCsvPresenter presenter) {
		this.presenter = presenter;
	}

	public SendMailUtil getMailUtils() {
		return mailUtils;
	}

	public void setMailUtils(SendMailUtil mailUtils) {
		this.mailUtils = mailUtils;
	}
}
