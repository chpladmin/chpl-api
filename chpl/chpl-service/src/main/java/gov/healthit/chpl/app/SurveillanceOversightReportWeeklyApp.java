package gov.healthit.chpl.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.app.surveillance.presenter.SurveillanceOversightAllBrokenRulesCsvPresenter;
import gov.healthit.chpl.auth.SendMailUtil;
import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.NotificationDAO;
import gov.healthit.chpl.domain.CertifiedProductDownloadResponse;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.NotificationTypeConcept;
import gov.healthit.chpl.domain.SurveillanceOversightRule;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.notification.NotificationTypeDTO;
import gov.healthit.chpl.dto.notification.RecipientWithSubscriptionsDTO;
import gov.healthit.chpl.dto.notification.SubscriptionDTO;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;

@Component("surveillanceWeeklyReportApp")
public class SurveillanceOversightReportWeeklyApp {
    private static final String DEFAULT_PROPERTIES_FILE = "environment.properties";
	private static final Logger logger = LogManager.getLogger(SurveillanceOversightReportWeeklyApp.class);
	
	private SimpleDateFormat timestampFormat;
	private CertifiedProductDetailsManager cpdManager;
	private CertifiedProductDAO certifiedProductDAO;
	private SurveillanceOversightAllBrokenRulesCsvPresenter presenter;
	private SendMailUtil mailUtils;
	private NotificationDAO notificationDAO;
	private CertificationBodyDAO certificationBodyDAO;
	
    public SurveillanceOversightReportWeeklyApp() {
    	timestampFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
    }
    
	public static void main( String[] args ) throws Exception {
		//read in properties - we need these to set up the data source context
		Properties props = null;
		InputStream in = SurveillanceOversightReportWeeklyApp.class.getClassLoader().getResourceAsStream(DEFAULT_PROPERTIES_FILE);
		SurveillanceOversightReportWeeklyApp oversightApp = new SurveillanceOversightReportWeeklyApp();
		
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
		 
		JWTAuthenticatedUser adminUser = new JWTAuthenticatedUser();
		adminUser.setFirstName("Administrator");
		adminUser.setId(-2L);
		adminUser.setLastName("Administrator");
		adminUser.setSubjectName("admin");
		adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));
		 
		//init spring classes
		AbstractApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
		SurveillanceOversightReportWeeklyApp app = new SurveillanceOversightReportWeeklyApp();
		app.setCpdManager((CertifiedProductDetailsManager)context.getBean("certifiedProductDetailsManager"));
		app.setCertifiedProductDAO((CertifiedProductDAO)context.getBean("certifiedProductDAO"));
		app.setNotificationDAO((NotificationDAO)context.getBean("notificationDAO"));
		app.setCertificationBodyDAO((CertificationBodyDAO)context.getBean("certificationBodyDAO"));
		app.setPresenter((SurveillanceOversightAllBrokenRulesCsvPresenter)context.getBean("surveillanceOversightAllBrokenRulesCsvPresenter"));
		app.getPresenter().setProps(props);
		app.setMailUtils((SendMailUtil)context.getBean("SendMailUtil"));
		 
		//specify where to store files
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
		
		List<CertificationBodyDTO> acbs = app.getCertificationBodyDAO().findAll(false);
		List<RecipientWithSubscriptionsDTO> recipientSubscriptions = app.getNotificationDAO().getAllNotificationMappings(adminUser.getPermissions(), acbs);
		
		oversightApp.sendWeeklyRecipientSubscriptionEmails(recipientSubscriptions, props, downloadFolder, app);
        
        context.close();
	}
	
	private void sendWeeklyRecipientSubscriptionEmails(List<RecipientWithSubscriptionsDTO> recipientSubscriptions, Properties props, File downloadFolder, SurveillanceOversightReportWeeklyApp app) throws IOException, AddressException, MessagingException {
		String surveillanceReportFilename = null;
        String htmlMessage = null;
        String subject = null;
        File surveillanceReportFile = null;
        CertifiedProductDownloadResponse acbSpecificCpsDownloadResponse = new CertifiedProductDownloadResponse();
        
        List<CertifiedProductSearchDetails> allCertifiedProductDetails = app.getAllCertifiedProductSearchDetails(app);
		CertifiedProductDownloadResponse allCps = new CertifiedProductDownloadResponse();
		allCps.setProducts(allCertifiedProductDetails);
        
		// send email for each recipient's subscriptions
        for(RecipientWithSubscriptionsDTO rwsDTO : recipientSubscriptions){
        	for(SubscriptionDTO subscription : rwsDTO.getSubscriptions()){
        		NotificationTypeDTO notificationType = subscription.getNotificationType();
        		if(notificationType.getName().equalsIgnoreCase(NotificationTypeConcept.ONC_ACB_WEEKLY_SURVEILLANCE_BROKEN_RULES.getName()) 
        				|| notificationType.getName().equalsIgnoreCase(NotificationTypeConcept.ONC_WEEKLY_SURVEILLANCE_BROKEN_RULES.getName())
        				&& !notificationType.getName().equalsIgnoreCase(NotificationTypeConcept.ONC_ACB_DAILY_SURVEILLANCE_BROKEN_RULES.getName())
        						&& !notificationType.getName().equalsIgnoreCase(NotificationTypeConcept.ONC_DAILY_SURVEILLANCE_BROKEN_RULES.getName())){
                    if(notificationType.getName().equalsIgnoreCase(NotificationTypeConcept.ONC_ACB_WEEKLY_SURVEILLANCE_BROKEN_RULES.getName())){
                    	List<CertifiedProductSearchDetails> acbSpecificCps = new ArrayList<CertifiedProductSearchDetails>();
                    	for(CertifiedProductSearchDetails cpDetails : allCertifiedProductDetails){
                    		if(subscription.getAcb().getAcbCode().equalsIgnoreCase(cpDetails.getAcbCertificationId())){
                    			acbSpecificCps.add(cpDetails);
                    		}
                    	}
                    	surveillanceReportFilename = props.getProperty("oversightEmailAcbWeeklyFileName");
                    	surveillanceReportFile = new File(downloadFolder.getAbsolutePath() + File.separator + surveillanceReportFilename);
                        if(!surveillanceReportFile.exists()) {
                        	surveillanceReportFile.createNewFile();
                        } else {
                        	surveillanceReportFile.delete();
                        }
                    	acbSpecificCpsDownloadResponse.setProducts(acbSpecificCps);
                    	app.getPresenter().presentAsFile(surveillanceReportFile, acbSpecificCpsDownloadResponse);
                    	subject = subscription.getAcb().getName() + " " + props.getProperty("oversightEmailAcbWeeklySubject");
                    	htmlMessage = props.getProperty("oversightEmailAcbWeeklyHtmlMessage");
                    } else if(notificationType.getName().equalsIgnoreCase(NotificationTypeConcept.ONC_WEEKLY_SURVEILLANCE_BROKEN_RULES.getName())){
                    	surveillanceReportFilename = props.getProperty("oversightEmailWeeklyFileName");
                    	surveillanceReportFile = new File(downloadFolder.getAbsolutePath() + File.separator + surveillanceReportFilename);
                        if(!surveillanceReportFile.exists()) {
                        	surveillanceReportFile.createNewFile();
                        } else {
                        	surveillanceReportFile.delete();
                        }
                    	app.getPresenter().presentAsFile(surveillanceReportFile, allCps);
                    	subject = props.getProperty("oversightEmailWeeklySubject");
                    	htmlMessage = props.getProperty("oversightEmailWeeklyHtmlMessage");
                    }
                    
                    String[] toEmail = {rwsDTO.getEmail()};
                    
                    Map<SurveillanceOversightRule, Integer> brokenRules = app.getPresenter().getAllBrokenRulesCounts();
                   
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
                    	htmlMessage += "<li>" + SurveillanceOversightRule.LONG_SUSPENSION.getTitle() + ": " + brokenRules.get(SurveillanceOversightRule.LONG_SUSPENSION) + "</li>";
                    	htmlMessage += "<li>" + SurveillanceOversightRule.CAP_NOT_APPROVED.getTitle() + ": " + brokenRules.get(SurveillanceOversightRule.CAP_NOT_APPROVED) + "</li>";
                    	htmlMessage += "<li>" + SurveillanceOversightRule.CAP_NOT_STARTED.getTitle() + ": " + brokenRules.get(SurveillanceOversightRule.CAP_NOT_STARTED) + "</li>";
                    	htmlMessage += "<li>" + SurveillanceOversightRule.CAP_NOT_COMPLETED.getTitle() + ": " + brokenRules.get(SurveillanceOversightRule.CAP_NOT_COMPLETED) + "</li>";
                    	htmlMessage += "<li>" + SurveillanceOversightRule.CAP_NOT_CLOSED.getTitle() + ": " + brokenRules.get(SurveillanceOversightRule.CAP_NOT_CLOSED) + "</li>";
                    	htmlMessage += "</ul>";
                    }
                    
                    List<File> files = new ArrayList<File>();
                    files.add(surveillanceReportFile);
                    app.getMailUtils().sendEmail(toEmail, subject, htmlMessage, files, props);
        		}	
        	}
        }
	}
	
	private List<CertifiedProductSearchDetails> getAllCertifiedProductSearchDetails(SurveillanceOversightReportWeeklyApp app){
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
		return allCertifiedProductDetails;
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

	public NotificationDAO getNotificationDAO() {
		return notificationDAO;
	}

	public void setNotificationDAO(NotificationDAO notificationDAO) {
		this.notificationDAO = notificationDAO;
	}

	public CertificationBodyDAO getCertificationBodyDAO() {
		return certificationBodyDAO;
	}

	public void setCertificationBodyDAO(CertificationBodyDAO certificationBodyDAO) {
		this.certificationBodyDAO = certificationBodyDAO;
	}
}
