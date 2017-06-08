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

import gov.healthit.chpl.app.presenter.InvalidInheritanceCsvPresenter;
import gov.healthit.chpl.app.surveillance.presenter.SurveillanceOversightAllBrokenRulesCsvPresenter;
import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.ListingGraphDAO;
import gov.healthit.chpl.domain.CertifiedProductDownloadResponse;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.SurveillanceOversightRule;
import gov.healthit.chpl.domain.concept.NotificationTypeConcept;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.notification.RecipientWithSubscriptionsDTO;

@Component("inheritanceReportWeeklyApp")
public class InheritanceReportWeeklyApp extends SurveillanceOversightReportApp{
	private InvalidInheritanceCsvPresenter presenter;
	
    public InheritanceReportWeeklyApp() {
    	timestampFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
    }
    
	public static void main(String[] args) throws Exception {
		InheritanceReportWeeklyApp app = new InheritanceReportWeeklyApp();
		app.setLocalContext();
		AbstractApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
		app.initiateSpringBeans(context);
		
		List<CertifiedProductSearchDetails> allCertifiedProductDetails = app.getAllCertifiedProductSearchDetails();

        
        context.close();
	}
	
	@Override
	protected void initiateSpringBeans(AbstractApplicationContext context) throws IOException {
		super.initiateSpringBeans(context);
		this.setPresenter((InvalidInheritanceCsvPresenter)context.getBean("invalidInheritanceCsvPresenter"));
		this.getPresenter().setProps(getProperties());
		this.getPresenter().setInheritanceDao((ListingGraphDAO)context.getBean("listingGraphDao"));
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

	private InvalidInheritanceCsvPresenter getPresenter() {
		return presenter;
	}

	private void setPresenter(InvalidInheritanceCsvPresenter presenter) {
		this.presenter = presenter;
	}
}
