package gov.healthit.chpl.app;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.app.surveillance.presenter.SurveillanceOversightAllBrokenRulesCsvPresenter;
import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.domain.CertifiedProductDownloadResponse;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.SurveillanceOversightRule;
import gov.healthit.chpl.domain.concept.NotificationTypeConcept;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.notification.RecipientWithSubscriptionsDTO;

@Component("surveillanceWeeklyReportApp")
public class SurveillanceOversightReportWeeklyApp extends SurveillanceOversightReportApp{
	private SurveillanceOversightAllBrokenRulesCsvPresenter presenter;
	
    public SurveillanceOversightReportWeeklyApp() {
    	timestampFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
    }
    
	public static void main(String[] args) throws Exception {
		SurveillanceOversightReportWeeklyApp oversightApp = new SurveillanceOversightReportWeeklyApp();
		Properties props = oversightApp.getProperties();
		oversightApp.setLocalContext(props);
		AbstractApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
		oversightApp.initiateSpringBeans(context, props);
        String downloadFolderPath = oversightApp.getDownloadFolderPath(args, props);
        File downloadFolder = oversightApp.getDownloadFolder(downloadFolderPath);
		
        // Get ACBs for ONC-ACB emails
		List<CertificationBodyDTO> acbs = oversightApp.getCertificationBodyDAO().findAll(false);
		// Get all recipients with all subscriptions
		Set<GrantedPermission> permissions = new HashSet<GrantedPermission>();
		permissions.add(new GrantedPermission("ROLE_ADMIN"));
		List<RecipientWithSubscriptionsDTO> recipientSubscriptions = oversightApp.getNotificationDAO().getAllNotificationMappings(permissions, null);
		// Get email addresses for ONC subscribers
		Set<String> oncWeeklyEmails = oversightApp.getRecipientEmails(recipientSubscriptions, NotificationTypeConcept.ONC_WEEKLY_SURVEILLANCE_BROKEN_RULES);
		// Get email addresses for ONC-ACB subscribers
		Map<CertificationBodyDTO, Set<String>> acbEmailMap = oversightApp.getAcbRecipientEmails(recipientSubscriptions, acbs, NotificationTypeConcept.ONC_ACB_WEEKLY_SURVEILLANCE_BROKEN_RULES);
		if(oncWeeklyEmails.size() > 0 || acbEmailMap.size() > 0){
			// Get full set of data to send in ONC email
			List<CertifiedProductSearchDetails> allCertifiedProductDetails = oversightApp.getAllCertifiedProductSearchDetails();
			CertifiedProductDownloadResponse allCps = new CertifiedProductDownloadResponse();
			allCps.setListings(allCertifiedProductDetails);
			// Get Certification-specific set of data to send in emails
			Map<CertificationBodyDTO, CertifiedProductDownloadResponse> certificationDownloadMap = oversightApp.getCertificationDownloadResponse(allCertifiedProductDetails, acbs);
			
			// send emails
			oversightApp.sendOncWeeklyEmail(oncWeeklyEmails, props, downloadFolder, allCps);
			oversightApp.sendAcbWeeklyRecipientSubscriptionEmails(acbEmailMap, props, downloadFolder, certificationDownloadMap);
		}
        
        context.close();
	}
	
	protected void initiateSpringBeans(AbstractApplicationContext context, Properties props){
		super.initiateSpringBeans(context, props);
		this.setPresenter((SurveillanceOversightAllBrokenRulesCsvPresenter)context.getBean("surveillanceOversightAllBrokenRulesCsvPresenter"));
		this.getPresenter().setProps(props);
	}
	
	private void sendOncWeeklyEmail(Set<String> oncWeeklyEmails, Properties props, File downloadFolder, CertifiedProductDownloadResponse cpList) throws IOException, AddressException, MessagingException {
		String surveillanceReportFilename = null;
        String htmlMessage = null;
        String subject = null;
        File surveillanceReportFile = null;
		
		surveillanceReportFilename = props.getProperty("oversightEmailWeeklyFileName");
    	surveillanceReportFile = new File(downloadFolder.getAbsolutePath() + File.separator + surveillanceReportFilename);
    	this.getPresenter().presentAsFile(surveillanceReportFile, cpList);
    	subject = props.getProperty("oversightEmailWeeklySubject");
    	htmlMessage = props.getProperty("oversightEmailWeeklyHtmlMessage");
    	String[] bccEmail = oncWeeklyEmails.toArray(new String[oncWeeklyEmails.size()]);
        
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
        	htmlMessage += props.getProperty("oversightEmailWeeklyNoContent");
        } else {
        	htmlMessage += "<ul>";
        	htmlMessage += "<li>" + SurveillanceOversightRule.LONG_SUSPENSION.getTitle() + ": " + brokenRules.get(SurveillanceOversightRule.LONG_SUSPENSION) + "</li>";
        	htmlMessage += "<li>" + SurveillanceOversightRule.CAP_NOT_APPROVED.getTitle() + ": " + brokenRules.get(SurveillanceOversightRule.CAP_NOT_APPROVED) + "</li>";
        	htmlMessage += "<li>" + SurveillanceOversightRule.CAP_NOT_STARTED.getTitle() + ": " + brokenRules.get(SurveillanceOversightRule.CAP_NOT_STARTED) + "</li>";
        	htmlMessage += "<li>" + SurveillanceOversightRule.CAP_NOT_COMPLETED.getTitle() + ": " + brokenRules.get(SurveillanceOversightRule.CAP_NOT_COMPLETED) + "</li>";
        	htmlMessage += "<li>" + SurveillanceOversightRule.CAP_NOT_CLOSED.getTitle() + ": " + brokenRules.get(SurveillanceOversightRule.CAP_NOT_CLOSED) + "</li>";
        	htmlMessage += "<li>" + SurveillanceOversightRule.NONCONFORMITY_OPEN_CAP_COMPLETE.getTitle() + ": " + brokenRules.get(SurveillanceOversightRule.NONCONFORMITY_OPEN_CAP_COMPLETE) + "</li>";
        	htmlMessage += "</ul>";
        }
        
        List<File> files = new ArrayList<File>();
        files.add(surveillanceReportFile);
        this.getMailUtils().sendEmail(null, bccEmail, subject, htmlMessage, files, props);
	}
	
	private void sendAcbWeeklyRecipientSubscriptionEmails(Map<CertificationBodyDTO, Set<String>> acbEmailMap, Properties props, File downloadFolder, Map<CertificationBodyDTO, CertifiedProductDownloadResponse> acbDownloadMap) throws IOException, AddressException, MessagingException {
		String surveillanceReportFilename = null;
        String htmlMessage = null;
        String subject = null;
        File surveillanceReportFile = null;
        
        // Send one email for each ACB to all of its subscribed recipients
        for(Map.Entry<CertificationBodyDTO, Set<String>> entry : acbEmailMap.entrySet()){
        	List<File> files = new ArrayList<File>();
        	String fmtAcbName = entry.getKey().getName().replaceAll("\\W", "").toLowerCase();
        	surveillanceReportFilename = fmtAcbName + "-" + props.getProperty("oversightEmailWeeklyFileName");
        	surveillanceReportFile = new File(downloadFolder.getAbsolutePath() + File.separator + surveillanceReportFilename);
        	this.getPresenter().clear();
        	
            // Generate this ACB's download file  	
        	this.getPresenter().presentAsFile(surveillanceReportFile, acbDownloadMap.get(entry.getKey()));
    		files.add(surveillanceReportFile);	
        	
        	subject = entry.getKey().getName() + " " + props.getProperty("oversightEmailWeeklySubject");
        	htmlMessage = props.getProperty("oversightEmailAcbWeeklyHtmlMessage");
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
            	htmlMessage += props.getProperty("oversightEmailWeeklyNoContent");
            } else {
            	htmlMessage += "<ul>";
            	htmlMessage += "<li>" + SurveillanceOversightRule.LONG_SUSPENSION.getTitle() + ": " + brokenRules.get(SurveillanceOversightRule.LONG_SUSPENSION) + "</li>";
            	htmlMessage += "<li>" + SurveillanceOversightRule.CAP_NOT_APPROVED.getTitle() + ": " + brokenRules.get(SurveillanceOversightRule.CAP_NOT_APPROVED) + "</li>";
            	htmlMessage += "<li>" + SurveillanceOversightRule.CAP_NOT_STARTED.getTitle() + ": " + brokenRules.get(SurveillanceOversightRule.CAP_NOT_STARTED) + "</li>";
            	htmlMessage += "<li>" + SurveillanceOversightRule.CAP_NOT_COMPLETED.getTitle() + ": " + brokenRules.get(SurveillanceOversightRule.CAP_NOT_COMPLETED) + "</li>";
            	htmlMessage += "<li>" + SurveillanceOversightRule.CAP_NOT_CLOSED.getTitle() + ": " + brokenRules.get(SurveillanceOversightRule.CAP_NOT_CLOSED) + "</li>";
            	htmlMessage += "<li>" + SurveillanceOversightRule.NONCONFORMITY_OPEN_CAP_COMPLETE.getTitle() + ": " + brokenRules.get(SurveillanceOversightRule.NONCONFORMITY_OPEN_CAP_COMPLETE) + "</li>";
            	htmlMessage += "</ul>";
            }
            
            this.getMailUtils().sendEmail(null, bccEmail, subject, htmlMessage, files, props);
        }
	}

	private SurveillanceOversightAllBrokenRulesCsvPresenter getPresenter() {
		return presenter;
	}

	private void setPresenter(SurveillanceOversightAllBrokenRulesCsvPresenter presenter) {
		this.presenter = presenter;
	}
}
