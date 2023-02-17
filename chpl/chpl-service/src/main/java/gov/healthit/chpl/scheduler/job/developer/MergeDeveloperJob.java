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
import org.springframework.core.env.Environment;
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
@Log4j2(topic = "mergeDeveloperJobLogger")
public class MergeDeveloperJob implements Job {
    public static final String JOB_NAME = "mergeDeveloperJob";
    public static final String OLD_DEVELOPERS_KEY = "oldDevelopers";
    public static final String NEW_DEVELOPER_KEY = "newDeveloper";
    public static final String PRODUCT_IDS_TO_MOVE_KEY = "productIdsToMove";
    public static final String USER_KEY = "user";

    @Autowired
    private Environment env;

    @Autowired
    @Qualifier("transactionalDeveloperMergeManager")
    private TransactionalDeveloperMergeManager mergeManager;

    @Autowired
    private DeveloperDAO devDao;

    @Value("${internalErrorEmailRecipients}")
    private String internalErrorEmailRecipients;

    @Autowired
    private ChplEmailFactory chplEmailFactory;

    @Autowired
    private ChplHtmlEmailBuilder emailBuilder;

    private List<Developer> preMergeDevelopers;
    private Developer postMergeDeveloper;

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        LOGGER.info("********* Starting the Merge Developer job. *********");

        JobDataMap jobDataMap = jobContext.getMergedJobDataMap();
        UserDTO user = (UserDTO) jobDataMap.get(USER_KEY);
        if (user == null) {
            LOGGER.fatal("No user could be found in the job data.");
        } else {
            setSecurityContext(user);

            preMergeDevelopers = (List<Developer>) jobDataMap.get(OLD_DEVELOPERS_KEY);
            Developer newDeveloper = (Developer) jobDataMap.get(NEW_DEVELOPER_KEY);
            Exception mergeException = null;
            try {
                confirmDevelopersExistBeforeMerge();
                //merge within transaction so changes will be rolled back
                postMergeDeveloper = mergeManager.merge(preMergeDevelopers, newDeveloper);
            } catch  (Exception e) {
                LOGGER.error("Error completing merge of developers '"
                        + StringUtils.join(preMergeDevelopers.stream()
                            .map(Developer::getName)
                            .collect(Collectors.toList()), ",")
                        + "' to new developer '"
                        + newDeveloper.getName() + "'.", e);
                mergeException = e;
            }

            if (postMergeDeveloper != null) {
                clearCachesRelatedToDevelopers();
            }

            //send email about success/failure of job
            if (!StringUtils.isEmpty(user.getEmail())) {
                List<String> recipients = new ArrayList<String>();
                recipients.add(user.getEmail());
                try {
                    sendJobCompletionEmails(postMergeDeveloper != null ? postMergeDeveloper : newDeveloper,
                            preMergeDevelopers, mergeException, recipients);
                } catch (IOException e) {
                    LOGGER.error(e);
                }
            } else {
                LOGGER.warn("The user " + user.getUsername()
                    + " does not have a configured email address so no email will be sent.");
            }
        }
        LOGGER.info("********* Completed the Merge Developer job. *********");
    }

    private void setSecurityContext(UserDTO user) {
        JWTAuthenticatedUser mergeUser = new JWTAuthenticatedUser();
        mergeUser.setFullName(user.getFullName());
        mergeUser.setId(user.getId());
        mergeUser.setFriendlyName(user.getFriendlyName());
        mergeUser.setSubjectName(user.getUsername());
        mergeUser.getPermissions().add(user.getPermission().getGrantedPermission());

        SecurityContextHolder.getContext().setAuthentication(mergeUser);
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }

    private void confirmDevelopersExistBeforeMerge() throws EntityRetrievalException {
        List<Long> preMergeDeveloperIds = preMergeDevelopers.stream().map(dev -> dev.getId()).collect(Collectors.toList());
        for (Long developerId : preMergeDeveloperIds) {
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

    private void sendJobCompletionEmails(Developer newDeveloper, List<Developer> oldDevelopers,
            Exception mergeException, List<String> recipients) throws IOException {

        String subject = getSubject(mergeException == null);
        String htmlMessage = "";
        if (mergeException == null) {
            htmlMessage = createHtmlEmailBodySuccess(subject, newDeveloper, oldDevelopers);
        } else {
            String[] errorEmailRecipients = internalErrorEmailRecipients.split(",");
            for (int i = 0; i < errorEmailRecipients.length; i++) {
                recipients.add(errorEmailRecipients[i].trim());
            }
            htmlMessage = createHtmlEmailBodyFailure(subject, oldDevelopers, mergeException);
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
            return env.getProperty("mergeDeveloper.success.emailSubject");
        } else {
            return env.getProperty("mergeDeveloper.failed.emailSubject");
        }
    }

    private String createHtmlEmailBodySuccess(String title, Developer createdDeveloper, List<Developer> developers) {
        String summaryText = String
                .format("The Developer <a href=\"%s/#/organizations/developers/%d\">%s</a> has been "
                        + "created. It was merged from the following developers: ",
                env.getProperty("chplUrlBegin"), // root of URL
                createdDeveloper.getId(),
                createdDeveloper.getName());

        String developerList = "<ul>";
        for (Developer dev : developers) {
            developerList += String.format("<li><a href=\"%s/#/organizations/developers/%d\">%s</a></li>",
                    env.getProperty("chplUrlBegin"),
                    dev.getId(),
                    dev.getName());
        }
        developerList += "</ul>";

        String htmlMessage = emailBuilder.initialize()
                .heading(title)
                .paragraph(null, summaryText)
                .paragraph(null, developerList)
                .footer(false)
                .build();
        return htmlMessage;
    }

    private String createHtmlEmailBodyFailure(String title, List<Developer> developers, Exception ex) {
        String developerList = "<ul>";
        for (Developer dev : developers) {
            developerList += String.format("<li><a href=\"%s/#/organizations/developers/%d\">%s</a></li>",
                    env.getProperty("chplUrlBegin"),
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
                .paragraph(null, "The below developers could not be merged into a new developer: ")
                .paragraph(null, developerList)
                .paragraph(null, String.format("The error was: %s", exceptionMessage))
                .footer(false)
                .build();

        return htmlMessage;
    }
}
