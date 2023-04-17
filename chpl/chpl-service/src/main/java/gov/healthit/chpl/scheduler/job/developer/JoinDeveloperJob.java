package gov.healthit.chpl.scheduler.job.developer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import lombok.extern.log4j.Log4j2;
import net.sf.ehcache.CacheManager;

@DisallowConcurrentExecution
@Log4j2(topic = "joinDeveloperJobLogger")
public class JoinDeveloperJob implements Job {
    public static final String JOB_NAME = "joinDeveloperJob";
    public static final String DEVELOPER_TO_JOIN = "toJoinDeveloper";
    public static final String JOINING_DEVELOPERS = "joiningDevelopers";
    public static final String USER_KEY = "user";

    @Autowired
    @Qualifier("transactionalJoinDeveloperManager")
    private TransactionalJoinDeveloperManager joinManager;

    @Autowired
    private DeveloperDAO devDao;

    @Autowired
    private ChplEmailFactory chplEmailFactory;

    @Autowired
    private ChplHtmlEmailBuilder emailBuilder;

    @Value("${internalErrorEmailRecipients}")
    private String internalErrorEmailRecipients;

    @Value("${joinDeveloper.success.emailSubject}")
    private String emailSubjectSuccess;

    @Value("${joinDeveloper.failed.emailSubject}")
    private String emailSubjectFailed;

    @Value("${chplUrlBegin}")
    private String chplUrlBegin;

    private List<Developer> preJoinDevelopers;

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        LOGGER.info("********* Starting the Join Developer job. *********");

        JobDataMap jobDataMap = jobContext.getMergedJobDataMap();
        UserDTO user = (UserDTO) jobDataMap.get(USER_KEY);
        if (user == null) {
            LOGGER.fatal("No user could be found in the job data.");
        } else {
            setSecurityContext(user);

            preJoinDevelopers = (List<Developer>) jobDataMap.get(JOINING_DEVELOPERS);
            Developer developerToJoin = (Developer) jobDataMap.get(DEVELOPER_TO_JOIN);
            Exception joinException = null;
            try {
                confirmDevelopersExistBeforeJoin();
                //join within transaction so changes will be rolled back
                joinManager.join(preJoinDevelopers, developerToJoin);
                clearCachesRelatedToDevelopers();
            } catch  (Exception e) {
                LOGGER.error("Error joining developers '"
                        + StringUtils.join(preJoinDevelopers.stream()
                            .map(Developer::getName)
                            .collect(Collectors.toList()), ",")
                        + "' to developer '"
                        + developerToJoin.getName() + "'.", e);
                joinException = e;
            }

            //send email about success/failure of job
            if (!StringUtils.isEmpty(user.getEmail())) {
                List<String> recipients = new ArrayList<String>();
                recipients.add(user.getEmail());
                try {
                    sendJobCompletionEmails(developerToJoin, preJoinDevelopers, joinException, recipients);
                } catch (IOException e) {
                    LOGGER.error(e);
                }
            } else {
                LOGGER.warn("The user " + user.getUsername()
                    + " does not have a configured email address so no email will be sent.");
            }
        }
        LOGGER.info("********* Completed the Join Developer job. *********");
    }

    private void setSecurityContext(UserDTO user) {
        JWTAuthenticatedUser joinUser = new JWTAuthenticatedUser();
        joinUser.setFullName(user.getFullName());
        joinUser.setId(user.getId());
        joinUser.setFriendlyName(user.getFriendlyName());
        joinUser.setSubjectName(user.getUsername());
        joinUser.getPermissions().add(user.getPermission().getGrantedPermission());

        SecurityContextHolder.getContext().setAuthentication(joinUser);
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }

    private void confirmDevelopersExistBeforeJoin() throws EntityRetrievalException {
        List<Long> preJoinDeveloperIds = preJoinDevelopers.stream().map(dev -> dev.getId()).collect(Collectors.toList());
        for (Long developerId : preJoinDeveloperIds) {
            devDao.getById(developerId);
        }
    }

    private void clearCachesRelatedToDevelopers() {
        CacheManager.getInstance().getCache(CacheNames.DEVELOPER_NAMES).removeAll();
        CacheManager.getInstance().getCache(CacheNames.ALL_DEVELOPERS).removeAll();
        CacheManager.getInstance().getCache(CacheNames.ALL_DEVELOPERS_INCLUDING_DELETED).removeAll();
        CacheManager.getInstance().getCache(CacheNames.COLLECTIONS_DEVELOPERS).removeAll();
        CacheManager.getInstance().getCache(CacheNames.COLLECTIONS_LISTINGS).removeAll();
        CacheManager.getInstance().getCache(CacheNames.COLLECTIONS_SEARCH).removeAll();
        CacheManager.getInstance().getCache(CacheNames.GET_DECERTIFIED_DEVELOPERS).removeAll();
    }

    private void sendJobCompletionEmails(Developer developerJoined, List<Developer> developersJoining,
            Exception joinException, List<String> recipients) throws IOException {

        String subject = getSubject(joinException == null);
        String htmlMessage = "";
        if (joinException == null) {
            htmlMessage = createHtmlEmailBodySuccess(subject, developerJoined, developersJoining);
        } else {
            String[] errorEmailRecipients = internalErrorEmailRecipients.split(",");
            for (int i = 0; i < errorEmailRecipients.length; i++) {
                recipients.add(errorEmailRecipients[i].trim());
            }
            htmlMessage = createHtmlEmailBodyFailure(subject, developerJoined, developersJoining, joinException);
        }

        for (String emailAddress : recipients) {
            try {
                sendEmail(emailAddress, subject, htmlMessage);
            } catch (Exception ex) {
                LOGGER.error("Could not send message to " + emailAddress, ex);
            }
        }
    }

    private void sendEmail(String recipientEmail, String subject, String htmlMessage)
            throws EmailNotSentException {
        LOGGER.info("Sending email to: " + recipientEmail);
        LOGGER.info("Message to be sent: " + htmlMessage);

        chplEmailFactory.emailBuilder().recipient(recipientEmail)
                .subject(subject)
                .htmlMessage(htmlMessage)
                .sendEmail();
    }

    private String getSubject(boolean success) {
        if (success) {
            return emailSubjectSuccess;
        } else {
            return emailSubjectFailed;
        }
    }

    private String createHtmlEmailBodySuccess(String title, Developer developerJoined, List<Developer> developersJoining) {
        String summaryText = "The Developers <ul>";
        for (Developer dev : developersJoining) {
            summaryText += String.format("<li>%s</li>", dev.getName());
        }
        summaryText += "</ul> have joined ";
        summaryText += String.format("<a href=\"%s/#/organizations/developers/%d\">%s</a>",
                chplUrlBegin, // root of URL
                developerJoined.getId(),
                developerJoined.getName());

        String htmlMessage = emailBuilder.initialize()
                .heading(title)
                .paragraph(null, summaryText)
                .footer(false)
                .build();
        return htmlMessage;
    }

    private String createHtmlEmailBodyFailure(String title, Developer developerJoined,
            List<Developer> developersJoining, Exception ex) {
        String summaryText = String.format("The below developers could not join developer "
                + "<a href=\"%s/#/organizations/developers/%d\">%s</a>",
                chplUrlBegin, // root of URL
                developerJoined.getId(),
                developerJoined.getName());

        String developerList = "<ul>";
        for (Developer dev : developersJoining) {
            developerList += String.format("<li><a href=\"%s/#/organizations/developers/%d\">%s</a></li>",
                    chplUrlBegin,
                    dev.getId(),
                    dev.getName());
        }
        developerList += "</ul>";

        String exceptionMessage = "";
        if (ex instanceof ValidationException) {
            ValidationException validationEx = (ValidationException) ex;
            exceptionMessage = validationEx.getErrorMessages().stream()
                    .collect(Collectors.joining("<br />"));
        } else {
            exceptionMessage = ex.getMessage();
        }

        String htmlMessage = emailBuilder.initialize()
                .heading(title)
                .paragraph(null, summaryText)
                .paragraph(null, developerList)
                .paragraph(null, String.format("Reason for failure: %s", exceptionMessage))
                .footer(false)
                .build();

        return htmlMessage;
    }
}
