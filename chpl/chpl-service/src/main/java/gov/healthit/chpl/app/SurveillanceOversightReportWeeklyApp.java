package gov.healthit.chpl.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.app.surveillance.presenter.SurveillanceOversightAllBrokenRulesCsvPresenter;
import gov.healthit.chpl.auth.SendMailUtil;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CertifiedProductDownloadResponse;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.SurveillanceOversightRule;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;

@Component("surveillanceWeeklyReportApp")
public class SurveillanceOversightReportWeeklyApp {
    private static final String DEFAULT_PROPERTIES_FILE = "environment.properties";
	private static final Logger logger = LogManager.getLogger(SurveillanceOversightReportWeeklyApp.class);
	private static final String FILENAME = "surveillance-oversight-weekly-report.csv";
	
	private SimpleDateFormat timestampFormat;
	private CertifiedProductDetailsManager cpdManager;
	private CertifiedProductDAO certifiedProductDAO;
	private SurveillanceOversightAllBrokenRulesCsvPresenter presenter;
	private SendMailUtil mailUtils;
	
    public SurveillanceOversightReportWeeklyApp() {
    	timestampFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
    }
    
	public static void main( String[] args ) throws Exception {
		//read in properties - we need these to set up the data source context
		Properties props = null;
		InputStream in = SurveillanceOversightReportWeeklyApp.class.getClassLoader().getResourceAsStream(DEFAULT_PROPERTIES_FILE);
		
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
		 SurveillanceOversightReportWeeklyApp app = new SurveillanceOversightReportWeeklyApp();
		 app.setCpdManager((CertifiedProductDetailsManager)context.getBean("certifiedProductDetailsManager"));
		 app.setCertifiedProductDAO((CertifiedProductDAO)context.getBean("certifiedProductDAO"));
		 app.setPresenter((SurveillanceOversightAllBrokenRulesCsvPresenter)context.getBean("surveillanceOversightAllBrokenRulesCsvPresenter"));
		 app.getPresenter().setProps(props);
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
        String surveillanceReportFilename = downloadFolder.getAbsolutePath() + File.separator + FILENAME;
        File surveillanceReportFile = new File(surveillanceReportFilename);
        if(!surveillanceReportFile.exists()) {
        	surveillanceReportFile.createNewFile();
        } else {
        	surveillanceReportFile.delete();
        }
        
        app.getPresenter().presentAsFile(surveillanceReportFile, allCps);
        Map<SurveillanceOversightRule, Integer> brokenRules = app.getPresenter().getAllBrokenRulesCounts();
        
        String toEmailProp = props.getProperty("oversightEmailWeeklyTo");
        String[] toEmail = toEmailProp.split(";");
        String subject = props.getProperty("oversightEmailWeeklySubject");
        String htmlMessage = "<h3>Weekly Surveillance Report</h3>";
       
        //were any rules broken?
        boolean anyRulesBroken = false;
        for(SurveillanceOversightRule rule : brokenRules.keySet()) {
        	Integer brokenRuleCount = brokenRules.get(rule);
        	if(brokenRuleCount.intValue() > 0) {
        		anyRulesBroken = true;
        	}
        }
        if(!anyRulesBroken) {
        	htmlMessage += props.getProperty("oversightEmailWeeklyNoContent");
        } else {
        	htmlMessage += "<ul>";
        	for(SurveillanceOversightRule rule : brokenRules.keySet()) {
        		Integer brokenRuleCount = brokenRules.get(rule);
            	htmlMessage += "<li>" + rule.getTitle() + ": " + brokenRuleCount + "</li>";
            }
        	htmlMessage += "</ul>";
        }
        
        List<File> files = new ArrayList<File>();
        files.add(surveillanceReportFile);
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

	public SurveillanceOversightAllBrokenRulesCsvPresenter getPresenter() {
		return presenter;
	}

	public void setPresenter(SurveillanceOversightAllBrokenRulesCsvPresenter presenter) {
		this.presenter = presenter;
	}

	public SendMailUtil getMailUtils() {
		return mailUtils;
	}

	public void setMailUtils(SendMailUtil mailUtils) {
		this.mailUtils = mailUtils;
	}
}
