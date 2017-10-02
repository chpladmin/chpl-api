package gov.healthit.chpl.app;

import java.io.File;
import java.io.IOException;
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

import gov.healthit.chpl.app.surveillance.presenter.SurveillanceOversightNewBrokenRulesCsvPresenter;
import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.domain.CertifiedProductDownloadResponse;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.SurveillanceOversightRule;
import gov.healthit.chpl.domain.concept.NotificationTypeConcept;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.notification.RecipientWithSubscriptionsDTO;

@Component("surveillanceDailyReportApp")
public class SurveillanceOversightReportDailyApp extends SurveillanceOversightReportApp {
	private SurveillanceOversightNewBrokenRulesCsvPresenter presenter;

	public static void main(String[] args) throws Exception {
		SurveillanceOversightReportDailyApp oversightApp = new SurveillanceOversightReportDailyApp();
		oversightApp.setLocalContext();
		AbstractApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
		oversightApp.initiateSpringBeans(context);
        File downloadFolder = oversightApp.getDownloadFolder();

        // Get ACBs for ONC-ACB emails
     	List<CertificationBodyDTO> acbs = oversightApp.getCertificationBodyDAO().findAll(false);
     	// Get all recipients with all subscriptions
 		Set<GrantedPermission> permissions = new HashSet<GrantedPermission>();
 		permissions.add(new GrantedPermission("ROLE_ADMIN"));
 		List<RecipientWithSubscriptionsDTO> oncRecipientSubscriptions = oversightApp.getNotificationDAO().getAllNotificationMappingsForType(permissions, NotificationTypeConcept.ONC_DAILY_SURVEILLANCE_BROKEN_RULES, null);
 		List<RecipientWithSubscriptionsDTO> allAcbRecipientSubscriptions = oversightApp.getNotificationDAO().getAllNotificationMappingsForType(permissions, NotificationTypeConcept.ONC_ACB_DAILY_SURVEILLANCE_BROKEN_RULES, acbs);
		if(oncRecipientSubscriptions.size() > 0 || allAcbRecipientSubscriptions.size() > 0) {
			// Get full set of data to send in ONC email
			List<CertifiedProductSearchDetails> allCertifiedProductDetails = oversightApp.getAllCertifiedProductSearchDetails();
			CertifiedProductDownloadResponse allCps = new CertifiedProductDownloadResponse();
			allCps.setListings(allCertifiedProductDetails);
			// Get Certification-specific set of data to send in emails
			Map<CertificationBodyDTO, CertifiedProductDownloadResponse> certificationDownloadMap = oversightApp.getCertificationDownloadResponse(allCertifiedProductDetails, acbs);

			// send emails
			oversightApp.sendOncDailyEmail(oncRecipientSubscriptions, downloadFolder, allCps);
			for(CertificationBodyDTO acb : acbs) {
				List<CertificationBodyDTO> currAcbList = new ArrayList<CertificationBodyDTO>();
				currAcbList.add(acb);
		 		List<RecipientWithSubscriptionsDTO> acbRecipientSubscriptions = oversightApp.getNotificationDAO().getAllNotificationMappingsForType(permissions, NotificationTypeConcept.ONC_ACB_DAILY_SURVEILLANCE_BROKEN_RULES, currAcbList);
				oversightApp.sendAcbDailyEmail(acb, acbRecipientSubscriptions, downloadFolder, certificationDownloadMap);
			}
		}

        context.close();
	}

	protected void initiateSpringBeans(AbstractApplicationContext context) throws IOException {
		super.initiateSpringBeans(context);
		this.setPresenter((SurveillanceOversightNewBrokenRulesCsvPresenter)context.getBean("surveillanceOversightNewBrokenRulesCsvPresenter"));
		this.getPresenter().setProps(getProperties());
	}

	/**
	 * Send an email to appropriate recipients with broken surveillance rules
	 * across all listings
	 * @param oncRecipientSubscriptions
	 * @param downloadFolder
	 * @param cpList
	 * @throws IOException
	 * @throws AddressException
	 * @throws MessagingException
	 */
	private void sendOncDailyEmail(List<RecipientWithSubscriptionsDTO> oncRecipientSubscriptions, File downloadFolder, CertifiedProductDownloadResponse cpList) throws IOException, AddressException, MessagingException {
		Properties props = getProperties();
		String surveillanceReportFilename = props.getProperty("oversightEmailDailyFileName");
        String subject = props.getProperty("oversightEmailDailySubject");
        File surveillanceReportFile = new File(downloadFolder.getAbsolutePath() + File.separator + surveillanceReportFilename);

    	this.getPresenter().presentAsFile(surveillanceReportFile, cpList);

    	//get emails
    	Set<String> oncEmails = new HashSet<String>();
    	for(RecipientWithSubscriptionsDTO recip : oncRecipientSubscriptions) {
    		oncEmails.add(recip.getEmail());
    	}
    	String[] bccEmail = oncEmails.toArray(new String[oncEmails.size()]);
    	if(bccEmail.length > 0) {
	        Map<SurveillanceOversightRule, Integer> brokenRules = this.getPresenter().getNewBrokenRulesCounts();
	        String htmlMessage = props.getProperty("oversightEmailDailyHtmlMessage");
	        htmlMessage += createHtmlEmailBody(brokenRules, props.getProperty("oversightEmailDailyNoContent"));

	        List<File> files = new ArrayList<File>();
	        files.add(surveillanceReportFile);
	        this.getMailUtils().sendEmail(null, bccEmail, subject, htmlMessage, files, props);
    	}
	}

	/**
	 * send an email to the appropriate recipients with broken surveillance rules
	 * for listings within a given ACB
	 * @param acb
	 * @param acbRecipientSubscriptions
	 * @param downloadFolder
	 * @param acbDownloadMap
	 * @throws IOException
	 * @throws AddressException
	 * @throws MessagingException
	 */
	private void sendAcbDailyEmail(CertificationBodyDTO acb, List<RecipientWithSubscriptionsDTO> acbRecipientSubscriptions, File downloadFolder, Map<CertificationBodyDTO, CertifiedProductDownloadResponse> acbDownloadMap)
			throws IOException, AddressException, MessagingException {
		Properties props = getProperties();

		//get emails
    	Set<String> acbEmails = new HashSet<String>();
    	for(RecipientWithSubscriptionsDTO recip : acbRecipientSubscriptions) {
    		acbEmails.add(recip.getEmail());
    	}

    	List<File> files = new ArrayList<File>();
    	String fmtAcbName = acb.getName().replaceAll("\\W", "").toLowerCase();
    	String surveillanceReportFilename = fmtAcbName + "-" + props.getProperty("oversightEmailDailyFileName");
    	File surveillanceReportFile = new File(downloadFolder.getAbsolutePath() + File.separator + surveillanceReportFilename);
    	this.getPresenter().clear();

    	// Generate this ACB's download file
        this.getPresenter().presentAsFile(surveillanceReportFile, acbDownloadMap.get(acb));
		files.add(surveillanceReportFile);

    	String subject = acb.getName() + " " + props.getProperty("oversightEmailDailySubject");
    	String[] bccEmail = acbEmails.toArray(new String[acbEmails.size()]);

    	if(bccEmail.length > 0) {
	    	// Get broken rules for email body
	        Map<SurveillanceOversightRule, Integer> brokenRules = this.getPresenter().getNewBrokenRulesCounts();
	    	String htmlMessage = props.getProperty("oversightEmailAcbDailyHtmlMessage");
	        htmlMessage += createHtmlEmailBody(brokenRules, props.getProperty("oversightEmailDailyNoContent"));
	        this.getMailUtils().sendEmail(null, bccEmail, subject, htmlMessage, files, props);
    	}
	}

    private SurveillanceOversightNewBrokenRulesCsvPresenter getPresenter() {
        return presenter;
    }

    private void setPresenter(SurveillanceOversightNewBrokenRulesCsvPresenter presenter) {
        this.presenter = presenter;
    }
}
