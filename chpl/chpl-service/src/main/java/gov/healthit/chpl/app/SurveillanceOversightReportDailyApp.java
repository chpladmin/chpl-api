package gov.healthit.chpl.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.app.surveillance.presenter.SurveillanceOversightNewBrokenRulesCsvPresenter;
import gov.healthit.chpl.auth.SendMailUtil;
import gov.healthit.chpl.auth.permission.GrantedPermission;
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
import gov.healthit.chpl.dto.notification.RecipientWithSubscriptionsDTO;
import gov.healthit.chpl.dto.notification.SubscriptionDTO;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;

@Component("surveillanceDailyReportApp")
public class SurveillanceOversightReportDailyApp {
    private static final String DEFAULT_PROPERTIES_FILE = "environment.properties";
	private static final Logger logger = LogManager.getLogger(SurveillanceOversightReportDailyApp.class);
	
	private SimpleDateFormat timestampFormat;
	private CertifiedProductDetailsManager cpdManager;
	private CertifiedProductDAO certifiedProductDAO;
	private SurveillanceOversightNewBrokenRulesCsvPresenter presenter;
	private SendMailUtil mailUtils;
	private NotificationDAO notificationDAO;
	private CertificationBodyDAO certificationBodyDAO;
	
    public SurveillanceOversightReportDailyApp() {
    	timestampFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
    }
    
	public static void main( String[] args ) throws Exception {		
		//read in properties - we need these to set up the data source context
		Properties props = null;
		InputStream in = SurveillanceOversightReportDailyApp.class.getClassLoader().getResourceAsStream(DEFAULT_PROPERTIES_FILE);
		SurveillanceOversightReportDailyApp oversightApp = new SurveillanceOversightReportDailyApp();
		
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
		 oversightApp.setCpdManager((CertifiedProductDetailsManager)context.getBean("certifiedProductDetailsManager"));
		 oversightApp.setCertifiedProductDAO((CertifiedProductDAO)context.getBean("certifiedProductDAO"));
		 oversightApp.setNotificationDAO((NotificationDAO)context.getBean("notificationDAO"));
		 oversightApp.setCertificationBodyDAO((CertificationBodyDAO)context.getBean("certificationBodyDAO"));
		 oversightApp.setPresenter((SurveillanceOversightNewBrokenRulesCsvPresenter)context.getBean("surveillanceOversightNewBrokenRulesCsvPresenter"));
		 oversightApp.getPresenter().setProps(props);
		 oversightApp.setMailUtils((SendMailUtil)context.getBean("SendMailUtil"));
		 
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
        
        // Get ACBs for ONC-ACB emails
     	List<CertificationBodyDTO> acbs = oversightApp.getCertificationBodyDAO().findAll(false);
     	// Get all recipients with all subscriptions
 		Set<GrantedPermission> permissions = new HashSet<GrantedPermission>();
 		permissions.add(new GrantedPermission("ROLE_ADMIN"));
 		List<RecipientWithSubscriptionsDTO> recipientSubscriptions = oversightApp.getNotificationDAO().getAllNotificationMappings(permissions, null);
 		// Get email addresses for ONC subscribers
		Set<String> oncDailyEmails = oversightApp.getDailyRecipientEmails(recipientSubscriptions);
		// Get email addresses for ONC-ACB subscribers
		Map<CertificationBodyDTO, Set<String>> acbEmailMap = oversightApp.getDailyAcbRecipientEmails(recipientSubscriptions, acbs);
		// Get full set of data to send in ONC email
		List<CertifiedProductSearchDetails> allCertifiedProductDetails = oversightApp.getAllCertifiedProductSearchDetails();
		CertifiedProductDownloadResponse allCps = new CertifiedProductDownloadResponse();
		allCps.setProducts(allCertifiedProductDetails);
		// Get Certification-specific set of data to send in emails
		Map<CertificationBodyDTO, CertifiedProductDownloadResponse> certificationDownloadMap = oversightApp.getCertificationDownloadResponse(allCertifiedProductDetails, acbs);	
		
		// send emails
		oversightApp.sendOncDailyEmail(oncDailyEmails, props, downloadFolder, allCps);
		oversightApp.sendAcbDailyRecipientSubscriptionEmails(acbEmailMap, props, downloadFolder, certificationDownloadMap);
        
        context.close();
	}
	
	private Set<String> getDailyRecipientEmails(List<RecipientWithSubscriptionsDTO> recipientSubscriptions){
		Set<String> oncDailyRecipients = new HashSet<String>();
		for(RecipientWithSubscriptionsDTO rDto : recipientSubscriptions){
			for(SubscriptionDTO sDto : rDto.getSubscriptions()){
				if(sDto.getNotificationType().getName().equalsIgnoreCase(NotificationTypeConcept.ONC_DAILY_SURVEILLANCE_BROKEN_RULES.getName())){
					oncDailyRecipients.add(rDto.getEmail());
				}
			}
		}
		return oncDailyRecipients;
	}
	
	private Map<CertificationBodyDTO, Set<String>> getDailyAcbRecipientEmails(List<RecipientWithSubscriptionsDTO> recipientSubscriptions, List<CertificationBodyDTO> acbs){
		Map<CertificationBodyDTO, Set<String>> acbDailyRecipients = new HashMap<CertificationBodyDTO, Set<String>>();
		for(CertificationBodyDTO acb : acbs){
			Set<String> emails = new HashSet<String>();
			for(RecipientWithSubscriptionsDTO rDto : recipientSubscriptions){
				for(SubscriptionDTO sDto : rDto.getSubscriptions()){
					if(sDto.getNotificationType().getName().equalsIgnoreCase(NotificationTypeConcept.ONC_ACB_DAILY_SURVEILLANCE_BROKEN_RULES.getName())){
						if(sDto.getAcb().getAcbCode().equalsIgnoreCase(acb.getAcbCode())){
							emails.add(rDto.getEmail());
						}
					}
				}
			}
			if(emails.size() > 0){
				acbDailyRecipients.put(acb, emails);
			}
		}
		return acbDailyRecipients;
	}
	
	private List<CertifiedProductSearchDetails> getAllCertifiedProductSearchDetails(){
	      List<CertifiedProductDetailsDTO> allCertifiedProducts = this.getCertifiedProductDAO().findWithSurveillance();
	      List<CertifiedProductSearchDetails> allCertifiedProductDetails = new ArrayList<CertifiedProductSearchDetails>(allCertifiedProducts.size());
			for(CertifiedProductDetailsDTO currProduct : allCertifiedProducts) {
				try {
					CertifiedProductSearchDetails product = this.getCpdManager().getCertifiedProductDetails(currProduct.getId());
					allCertifiedProductDetails.add(product);
				} catch(EntityRetrievalException ex) {
					logger.error("Could not certified product details for certified product " + currProduct.getId());
				}
			}
			return allCertifiedProductDetails;
	}
	
	private Map<CertificationBodyDTO, CertifiedProductDownloadResponse> getCertificationDownloadResponse(List<CertifiedProductSearchDetails> allCertifiedProductDetails, List<CertificationBodyDTO> acbs){
		Map<CertificationBodyDTO, CertifiedProductDownloadResponse> certificationDownloadResponse = new HashMap<CertificationBodyDTO, CertifiedProductDownloadResponse>();
		
		for(CertificationBodyDTO cbDTO : acbs){
			CertifiedProductDownloadResponse cpDlResponse = new CertifiedProductDownloadResponse();
			List<CertifiedProductSearchDetails> acbCpSearchDetails = new ArrayList<CertifiedProductSearchDetails>();
			for(CertifiedProductSearchDetails cpDetail : allCertifiedProductDetails){
				if(cpDetail.getCertifyingBody().get("code").toString().equalsIgnoreCase(cbDTO.getAcbCode())){
					acbCpSearchDetails.add(cpDetail);
				}
			}
			cpDlResponse.setProducts(acbCpSearchDetails);
			certificationDownloadResponse.put(cbDTO, cpDlResponse);
		}
		return certificationDownloadResponse;
	}
	
	private void sendOncDailyEmail(Set<String> oncDailyEmails, Properties props, File downloadFolder, CertifiedProductDownloadResponse cpList) throws IOException, AddressException, MessagingException {
		String surveillanceReportFilename = null;
        String htmlMessage = null;
        String subject = null;
        File surveillanceReportFile = null;
		
		surveillanceReportFilename = props.getProperty("oversightEmailDailyFileName");
    	surveillanceReportFile = new File(downloadFolder.getAbsolutePath() + File.separator + surveillanceReportFilename);
        if(!surveillanceReportFile.exists()) {
        	surveillanceReportFile.createNewFile();
        } else {
        	surveillanceReportFile.delete();
        }
    	this.getPresenter().presentAsFile(surveillanceReportFile, cpList);
    	subject = props.getProperty("oversightEmailDailySubject");
    	htmlMessage = props.getProperty("oversightEmailDailyHtmlMessage");
    	String[] bccEmail = oncDailyEmails.toArray(new String[oncDailyEmails.size()]);
        
        Map<SurveillanceOversightRule, Integer> brokenRules = this.getPresenter().getAllBrokenRulesCounts();
       
        //were any rules broken?
        boolean anyRulesBroken = false;
        for(SurveillanceOversightRule rule : brokenRules.keySet()) {
        	Integer brokenRuleCount = brokenRules.get(rule);
        	if(brokenRuleCount.intValue() > 0) {
        		anyRulesBroken = true;
        	}
        }
        if(!anyRulesBroken) {
        	htmlMessage += props.getProperty("oversightEmailDailyNoContent");
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
        this.getMailUtils().sendEmail(null, bccEmail, subject, htmlMessage, files, props);
	}
	
	private void sendAcbDailyRecipientSubscriptionEmails(Map<CertificationBodyDTO, Set<String>> acbEmailMap, Properties props, File downloadFolder, Map<CertificationBodyDTO, CertifiedProductDownloadResponse> acbDownloadMap) throws IOException, AddressException, MessagingException {
		String surveillanceReportFilename = null;
        String htmlMessage = null;
        String subject = null;
        File surveillanceReportFile = null;
        
        // Send one email for each ACB to all of its subscribed recipients
        for(Map.Entry<CertificationBodyDTO, Set<String>> entry : acbEmailMap.entrySet()){
        	List<File> files = new ArrayList<File>();
        	String fmtAcbName = entry.getKey().getName().replaceAll("\\W", "").toLowerCase();
        	surveillanceReportFilename = fmtAcbName + "-" + props.getProperty("oversightEmailDailyFileName");
        	surveillanceReportFile = new File(downloadFolder.getAbsolutePath() + File.separator + surveillanceReportFilename);
            if(!surveillanceReportFile.exists()) {
            	surveillanceReportFile.createNewFile();
            } else {
            	surveillanceReportFile.delete();
            }
            // Generate this ACB's download file  	
            this.getPresenter().presentAsFile(surveillanceReportFile, acbDownloadMap.get(entry.getKey()));
    		files.add(surveillanceReportFile);	
        	
        	subject = entry.getKey().getName() + " " + props.getProperty("oversightEmailDailySubject");
        	htmlMessage = props.getProperty("oversightEmailAcbDailyHtmlMessage");
        	String[] bccEmail = entry.getValue().toArray(new String[entry.getValue().size()]);
            
        	// Get broken rules for email body
            Map<SurveillanceOversightRule, Integer> brokenRules = this.getPresenter().getAllBrokenRulesCounts();
           
            //were any rules broken?
            boolean anyRulesBroken = false;
            for(SurveillanceOversightRule rule : brokenRules.keySet()) {
            	Integer brokenRuleCount = brokenRules.get(rule);
            	if(brokenRuleCount.intValue() > 0) {
            		anyRulesBroken = true;
            	}
            }
            if(!anyRulesBroken) {
            	htmlMessage += props.getProperty("oversightEmailDailyNoContent");
            } else {
            	htmlMessage += "<ul>";
            	htmlMessage += "<li>" + SurveillanceOversightRule.LONG_SUSPENSION.getTitle() + ": " + brokenRules.get(SurveillanceOversightRule.LONG_SUSPENSION) + "</li>";
            	htmlMessage += "<li>" + SurveillanceOversightRule.CAP_NOT_APPROVED.getTitle() + ": " + brokenRules.get(SurveillanceOversightRule.CAP_NOT_APPROVED) + "</li>";
            	htmlMessage += "<li>" + SurveillanceOversightRule.CAP_NOT_STARTED.getTitle() + ": " + brokenRules.get(SurveillanceOversightRule.CAP_NOT_STARTED) + "</li>";
            	htmlMessage += "<li>" + SurveillanceOversightRule.CAP_NOT_COMPLETED.getTitle() + ": " + brokenRules.get(SurveillanceOversightRule.CAP_NOT_COMPLETED) + "</li>";
            	htmlMessage += "<li>" + SurveillanceOversightRule.CAP_NOT_CLOSED.getTitle() + ": " + brokenRules.get(SurveillanceOversightRule.CAP_NOT_CLOSED) + "</li>";
            	htmlMessage += "</ul>";
            }
            
            this.getMailUtils().sendEmail(null, bccEmail, subject, htmlMessage, files, props);
        }
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

	public SurveillanceOversightNewBrokenRulesCsvPresenter getPresenter() {
		return presenter;
	}

	public void setPresenter(SurveillanceOversightNewBrokenRulesCsvPresenter presenter) {
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
