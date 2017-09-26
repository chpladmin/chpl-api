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
		oversightApp.setLocalContext();
		AbstractApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
		oversightApp.initiateSpringBeans(context);
        File downloadFolder = oversightApp.getDownloadFolder();

        // Get ACBs for ONC-ACB emails
		List<CertificationBodyDTO> acbs = oversightApp.getCertificationBodyDAO().findAll(false);
		// Get all recipients with all subscriptions
		Set<GrantedPermission> permissions = new HashSet<GrantedPermission>();
		permissions.add(new GrantedPermission("ROLE_ADMIN"));
		List<RecipientWithSubscriptionsDTO> oncRecipientSubscriptions = oversightApp.getNotificationDAO().getAllNotificationMappingsForType(permissions, NotificationTypeConcept.ONC_WEEKLY_SURVEILLANCE_BROKEN_RULES, null);
 		List<RecipientWithSubscriptionsDTO> allAcbRecipientSubscriptions = oversightApp.getNotificationDAO().getAllNotificationMappingsForType(permissions, NotificationTypeConcept.ONC_ACB_WEEKLY_SURVEILLANCE_BROKEN_RULES, acbs);
 		if(oncRecipientSubscriptions.size() > 0 || allAcbRecipientSubscriptions.size() > 0){
 			// Get full set of data to send in ONC email
			List<CertifiedProductSearchDetails> allCertifiedProductDetails = oversightApp.getAllCertifiedProductSearchDetails();
			CertifiedProductDownloadResponse allCps = new CertifiedProductDownloadResponse();
			allCps.setListings(allCertifiedProductDetails);
			// Get Certification-specific set of data to send in emails
			Map<CertificationBodyDTO, CertifiedProductDownloadResponse> certificationDownloadMap = oversightApp.getCertificationDownloadResponse(allCertifiedProductDetails, acbs);
			
			// send emails
			oversightApp.sendOncWeeklyEmail(oncRecipientSubscriptions, downloadFolder, allCps);
			for(CertificationBodyDTO acb : acbs) {
				List<CertificationBodyDTO> currAcbList = new ArrayList<CertificationBodyDTO>();
				currAcbList.add(acb);
			 	List<RecipientWithSubscriptionsDTO> acbRecipientSubscriptions = oversightApp.getNotificationDAO().getAllNotificationMappingsForType(permissions, NotificationTypeConcept.ONC_ACB_WEEKLY_SURVEILLANCE_BROKEN_RULES, currAcbList);
				oversightApp.sendAcbWeeklyEmail(acb, acbRecipientSubscriptions, downloadFolder, certificationDownloadMap);
			}
		}
        
        context.close();
	}
	
	@Override
	protected void initiateSpringBeans(AbstractApplicationContext context) throws IOException {
		super.initiateSpringBeans(context);
		this.setPresenter((SurveillanceOversightAllBrokenRulesCsvPresenter)context.getBean("surveillanceOversightAllBrokenRulesCsvPresenter"));
		this.getPresenter().setProps(getProperties());
	}
	
	private void sendOncWeeklyEmail(List<RecipientWithSubscriptionsDTO> oncRecipientSubscriptions, File downloadFolder, CertifiedProductDownloadResponse cpList) throws IOException, AddressException, MessagingException {
		Properties props = getProperties();
		
        String surveillanceReportFilename = props.getProperty("oversightEmailWeeklyFileName");
        File surveillanceReportFile = new File(downloadFolder.getAbsolutePath() + File.separator + surveillanceReportFilename);
    	String subject = props.getProperty("oversightEmailWeeklySubject");
    	String htmlMessage = props.getProperty("oversightEmailWeeklyHtmlMessage");
    	this.getPresenter().presentAsFile(surveillanceReportFile, cpList);

    	//get emails
    	Set<String> oncEmails = new HashSet<String>();
    	for(RecipientWithSubscriptionsDTO recip : oncRecipientSubscriptions) {
    		oncEmails.add(recip.getEmail());
    	}
    	String[] bccEmail = oncEmails.toArray(new String[oncEmails.size()]);

    	if(bccEmail.length > 0) {
	        Map<SurveillanceOversightRule, Integer> brokenRules = this.getPresenter().getAllBrokenRulesCounts();
	        htmlMessage += createHtmlEmailBody(brokenRules, props.getProperty("oversightEmailWeeklyNoContent"));
	        List<File> files = new ArrayList<File>();
	        files.add(surveillanceReportFile);
	        this.getMailUtils().sendEmail(null, bccEmail, subject, htmlMessage, files, props);
    	}
	}
	
	private void sendAcbWeeklyEmail(CertificationBodyDTO acb, List<RecipientWithSubscriptionsDTO> acbRecipientSubscriptions, File downloadFolder, Map<CertificationBodyDTO, CertifiedProductDownloadResponse> acbDownloadMap) throws IOException, AddressException, MessagingException {
		Properties props = getProperties();
		
		//get emails
    	Set<String> acbEmails = new HashSet<String>();
    	for(RecipientWithSubscriptionsDTO recip : acbRecipientSubscriptions) {
    		acbEmails.add(recip.getEmail());
    	}
    	
    	List<File> files = new ArrayList<File>();
    	String fmtAcbName = acb.getName().replaceAll("\\W", "").toLowerCase();
    	String surveillanceReportFilename = fmtAcbName + "-" + props.getProperty("oversightEmailWeeklyFileName");
    	File surveillanceReportFile = new File(downloadFolder.getAbsolutePath() + File.separator + surveillanceReportFilename);
    	this.getPresenter().clear();
    	
    	// Generate this ACB's download file  	
        this.getPresenter().presentAsFile(surveillanceReportFile, acbDownloadMap.get(acb));
		files.add(surveillanceReportFile);	

    	String subject = acb.getName() + " " + props.getProperty("oversightEmailWeeklySubject");
    	String[] bccEmail = acbEmails.toArray(new String[acbEmails.size()]);
    	
    	// Get broken rules for email body
    	if(bccEmail.length > 0) {
	        Map<SurveillanceOversightRule, Integer> brokenRules = this.getPresenter().getAllBrokenRulesCounts();
	    	String htmlMessage = props.getProperty("oversightEmailAcbWeeklyHtmlMessage");
	        htmlMessage += createHtmlEmailBody(brokenRules, props.getProperty("oversightEmailWeeklyNoContent"));
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
