package gov.healthit.chpl.app;

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
	
	protected void initiateSpringBeans(AbstractApplicationContext context, Properties props){
		this.setCpdManager((CertifiedProductDetailsManager)context.getBean("certifiedProductDetailsManager"));
		this.setCertifiedProductDAO((CertifiedProductDAO)context.getBean("certifiedProductDAO"));
		this.setNotificationDAO((NotificationDAO)context.getBean("notificationDAO"));
		this.setCertificationBodyDAO((CertificationBodyDAO)context.getBean("certificationBodyDAO"));
		this.setMailUtils((SendMailUtil)context.getBean("SendMailUtil"));
	}
	
	public List<CertifiedProductSearchDetails> getAllCertifiedProductSearchDetails(){
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
	
	protected Set<String> getRecipientEmails(List<RecipientWithSubscriptionsDTO> recipientSubscriptions, NotificationTypeConcept notificationTypeConcept){
		Set<String> oncDailyRecipients = new HashSet<String>();
		for(RecipientWithSubscriptionsDTO rDto : recipientSubscriptions){
			for(SubscriptionDTO sDto : rDto.getSubscriptions()){
				if(sDto.getNotificationType().getName().equalsIgnoreCase(notificationTypeConcept.getName())){
					oncDailyRecipients.add(rDto.getEmail());
				}
			}
		}
		return oncDailyRecipients;
	}
	
	protected Map<CertificationBodyDTO, Set<String>> getAcbRecipientEmails(List<RecipientWithSubscriptionsDTO> recipientSubscriptions, List<CertificationBodyDTO> acbs, NotificationTypeConcept notificationTypeConcept){
		Map<CertificationBodyDTO, Set<String>> acbDailyRecipients = new HashMap<CertificationBodyDTO, Set<String>>();
		for(CertificationBodyDTO acb : acbs){
			Set<String> emails = new HashSet<String>();
			for(RecipientWithSubscriptionsDTO rDto : recipientSubscriptions){
				for(SubscriptionDTO sDto : rDto.getSubscriptions()){
					if(sDto.getNotificationType().getName().equalsIgnoreCase(notificationTypeConcept.getName())){
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
