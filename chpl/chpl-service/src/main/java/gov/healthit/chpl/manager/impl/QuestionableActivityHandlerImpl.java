package gov.healthit.chpl.manager.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.mail.MessagingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import gov.healthit.chpl.auth.SendMailUtil;
import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.dao.NotificationDAO;
import gov.healthit.chpl.domain.concept.NotificationTypeConcept;
import gov.healthit.chpl.dto.notification.RecipientWithSubscriptionsDTO;
import gov.healthit.chpl.manager.QuestionableActivityHandler;

public abstract class QuestionableActivityHandlerImpl implements QuestionableActivityHandler {
	private static final Logger logger = LogManager.getLogger(QuestionableActivityHandlerImpl.class);

	@Autowired protected NotificationDAO notificationDao;
	@Autowired protected SendMailUtil sendMailService;
	
	public abstract boolean isQuestionableActivity(Object src, Object dest);
	public abstract String getQuestionableActivityHtmlMessage(Object src, Object dest);

	public String getQuestionableActivitySubject(Object src, Object dest) {
		return "CHPL Questionable Activity";
	}
	
	public void handleActivity(Object src, Object dest) {
		if(isQuestionableActivity(src, dest)) {
			sendQuestionableActivityEmail(getQuestionableActivitySubject(src, dest), 
					getQuestionableActivityHtmlMessage(src, dest));
		}
	}
	
	private void sendQuestionableActivityEmail(String subject, String htmlMessage) {
		Set<GrantedPermission> permissions = new HashSet<GrantedPermission>();
		permissions.add(new GrantedPermission("ROLE_ADMIN"));
		List<RecipientWithSubscriptionsDTO> questionableActivityRecipients = 
				notificationDao.getAllNotificationMappingsForType(permissions, NotificationTypeConcept.QUESTIONABLE_ACTIVITY, null);
		if(questionableActivityRecipients != null && questionableActivityRecipients.size() > 0) {
			String[] emailAddrs = new String[questionableActivityRecipients.size()];
			for(int i = 0; i < questionableActivityRecipients.size(); i++) {
				RecipientWithSubscriptionsDTO recip = questionableActivityRecipients.get(i);
				emailAddrs[i] = recip.getEmail();
			}
			
			try {
				sendMailService.sendEmail(null, emailAddrs, subject, htmlMessage);
			} catch(MessagingException me) {
				logger.error("Could not send questionable activity email", me);
			}
		} else {
			logger.warn("No recipients were found for notification type " + NotificationTypeConcept.QUESTIONABLE_ACTIVITY.getName());
		}
	}
}
