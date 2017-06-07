package gov.healthit.chpl.app;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.support.AbstractApplicationContext;

import gov.healthit.chpl.app.surveillance.presenter.SurveillanceOversightNewBrokenRulesCsvPresenter;
import gov.healthit.chpl.auth.SendMailUtil;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.NotificationDAO;
import gov.healthit.chpl.domain.CertifiedProductDownloadResponse;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.SurveillanceOversightRule;
import gov.healthit.chpl.domain.concept.NotificationTypeConcept;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.notification.RecipientWithSubscriptionsDTO;
import gov.healthit.chpl.dto.notification.SubscriptionDTO;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;

public abstract class SurveillanceOversightReportApp extends App {
	protected SimpleDateFormat timestampFormat;
	protected CertifiedProductDetailsManager cpdManager;
	protected CertifiedProductDAO certifiedProductDAO;
	protected SendMailUtil mailUtils;
	protected NotificationDAO notificationDAO;
	protected CertificationBodyDAO certificationBodyDAO;
	
	private static final Logger logger = LogManager.getLogger(SurveillanceOversightReportApp.class);
	
	public SurveillanceOversightReportApp(){
		timestampFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
	}
	
	public Map<CertificationBodyDTO, CertifiedProductDownloadResponse> getCertificationDownloadResponse(List<CertifiedProductSearchDetails> allCertifiedProductDetails, List<CertificationBodyDTO> acbs){
		Map<CertificationBodyDTO, CertifiedProductDownloadResponse> certificationDownloadResponse = new HashMap<CertificationBodyDTO, CertifiedProductDownloadResponse>();
		
		for(CertificationBodyDTO cbDTO : acbs){
			CertifiedProductDownloadResponse cpDlResponse = new CertifiedProductDownloadResponse();
			List<CertifiedProductSearchDetails> acbCpSearchDetails = new ArrayList<CertifiedProductSearchDetails>();
			for(CertifiedProductSearchDetails cpDetail : allCertifiedProductDetails){
				if(cpDetail.getCertifyingBody().get("code").toString().equalsIgnoreCase(cbDTO.getAcbCode())){
					acbCpSearchDetails.add(cpDetail);
				}
			}
			cpDlResponse.setListings(acbCpSearchDetails);
			certificationDownloadResponse.put(cbDTO, cpDlResponse);
		}
		return certificationDownloadResponse;
	}
	
	@Override
	protected void initiateSpringBeans(AbstractApplicationContext context)  throws IOException {
		this.setCpdManager((CertifiedProductDetailsManager)context.getBean("certifiedProductDetailsManager"));
		this.setCertifiedProductDAO((CertifiedProductDAO)context.getBean("certifiedProductDAO"));
		this.setNotificationDAO((NotificationDAO)context.getBean("notificationDAO"));
		this.setCertificationBodyDAO((CertificationBodyDAO)context.getBean("certificationBodyDAO"));
		this.setMailUtils((SendMailUtil)context.getBean("SendMailUtil"));
	}
	
	public List<CertifiedProductSearchDetails> getAllCertifiedProductSearchDetails() {
		List<CertifiedProductDetailsDTO> allCertifiedProducts = this.getCertifiedProductDAO().findWithSurveillance();
	    List<CertifiedProductSearchDetails> allCertifiedProductDetails = new ArrayList<CertifiedProductSearchDetails>(allCertifiedProducts.size());
		for(CertifiedProductDetailsDTO currProduct : allCertifiedProducts) {
			try {
				CertifiedProductSearchDetails product = this.getCpdManager().getCertifiedProductDetails(currProduct.getId());
				allCertifiedProductDetails.add(product);
			} catch(EntityRetrievalException ex) {
				logger.error("Could not find certified product details for certified product with id = " + currProduct.getId());
			}
		}
		return allCertifiedProductDetails;
	}
	
	protected String createHtmlEmailBody(Map<SurveillanceOversightRule, Integer> brokenRules, String noContentMsg) throws IOException {
		//were any rules broken?
		boolean anyRulesBroken = hasBrokenRules(brokenRules);
        String htmlMessage = "";
        if(!anyRulesBroken) {
        	htmlMessage = noContentMsg;
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
        return htmlMessage;
	}

	protected Boolean hasBrokenRules(Map<SurveillanceOversightRule, Integer> brokenRules){
		Boolean anyRulesBroken = false;
		for(SurveillanceOversightRule rule : brokenRules.keySet()) {
        	Integer brokenRuleCount = brokenRules.get(rule);
        	if(brokenRuleCount.intValue() > 0) {
        		anyRulesBroken = true;
        	}
        }
		return anyRulesBroken;
	}

	public CertifiedProductDetailsManager getCpdManager() {
		return cpdManager;
	}

	public void setCpdManager(CertifiedProductDetailsManager cpdManager) {
		this.cpdManager = cpdManager;
	}

	public CertifiedProductDAO getCertifiedProductDAO() {
		return certifiedProductDAO;
	}

	public void setCertifiedProductDAO(CertifiedProductDAO certifiedProductDAO) {
		this.certifiedProductDAO = certifiedProductDAO;
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
