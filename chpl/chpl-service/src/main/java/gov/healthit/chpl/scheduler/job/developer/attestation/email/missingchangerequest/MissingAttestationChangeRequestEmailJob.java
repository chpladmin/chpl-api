package gov.healthit.chpl.scheduler.job.developer.attestation.email.missingchangerequest;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.math.NumberUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.dao.auth.UserDAO;
import gov.healthit.chpl.developer.search.ActiveListingSearchOptions;
import gov.healthit.chpl.developer.search.AttestationsSearchOptions;
import gov.healthit.chpl.developer.search.DeveloperSearchRequest;
import gov.healthit.chpl.developer.search.DeveloperSearchResult;
import gov.healthit.chpl.developer.search.DeveloperSearchService;
import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.scheduler.SecurityContextCapableJob;
import gov.healthit.chpl.scheduler.job.QuartzJob;
import gov.healthit.chpl.scheduler.job.developer.attestation.email.DeveloperEmail;
import gov.healthit.chpl.scheduler.job.developer.attestation.email.StatusReportEmail;
import gov.healthit.chpl.search.domain.SearchSetOperator;
import gov.healthit.chpl.user.cognito.CognitoApiWrapper;
import gov.healthit.chpl.util.Util;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "missingAttestationChangeRequestEmailJobLogger")
public class MissingAttestationChangeRequestEmailJob extends SecurityContextCapableJob implements Job {

    @Autowired
    private DeveloperSearchService developerSearchService;

    @Autowired
    private MissingAttestationChangeRequestDeveloperEmailGenerator emailGenerator;

    @Autowired
    private MissingAttestationChangeRequestDeveloperStatusReportEmailGenerator emailStatusReportGenerator;

    @Autowired
    private ChplEmailFactory emailFactory;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private CognitoApiWrapper cognitoApiWrapper;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        LOGGER.info("********* Starting Developer Missing Attestatation Change Request Email job. *********");
        try {
            User submittedByUser = getUserFromJobData(context);
            setSecurityContext(submittedByUser);

            List<DeveloperSearchResult> developersMissingAttestations = developerSearchService.getAllPagesOfSearchResults(
                    DeveloperSearchRequest.builder()
                        .activeListingsOptions(Stream.of(ActiveListingSearchOptions.HAS_ANY_ACTIVE,
                                ActiveListingSearchOptions.HAD_ANY_ACTIVE_DURING_MOST_RECENT_PAST_ATTESTATION_PERIOD)
                                .collect(Collectors.toSet()))
                        .activeListingsOptionsOperator(SearchSetOperator.AND)
                        .attestationsOptions(Stream.of(AttestationsSearchOptions.HAS_NOT_SUBMITTED).collect(Collectors.toSet()))
                        .build(),
                        LOGGER);

            List<DeveloperEmail> developerEmails = developersMissingAttestations.stream()
                    .map(developer -> emailGenerator.getDeveloperEmail(developer, submittedByUser))
                    .toList();

            sendEmails(developerEmails);
            sendStatusReportEmail(developerEmails, submittedByUser);
        } catch (Exception e) {
            LOGGER.error(e);
        } finally {
            LOGGER.info("********* Completed Developer Missing Attestatation Change Request Email job. *********");
        }
    }

    private void sendEmails(List<DeveloperEmail> developerEmails) {
        developerEmails.forEach(email -> {
            try {
                LOGGER.info("Sending email to developer : {}", email.getDeveloper().getName());
                emailFactory.emailBuilder()
                    .recipients(email.getRecipients())
                    .subject(email.getSubject())
                    .htmlMessage(email.getMessage())
                    .sendEmail();
            } catch (Exception e) {
                LOGGER.error("Error sending emails to developer : {}", email.toString());
                LOGGER.error(e);
            }
        });
    }

    private void sendStatusReportEmail(List<DeveloperEmail> developerEmails, User submittedUser) {
        StatusReportEmail statusReportEmail = emailStatusReportGenerator.getStatusReportEmail(developerEmails, submittedUser);

        try {
            emailFactory.emailBuilder()
                .recipients(statusReportEmail.getRecipients())
                .subject(statusReportEmail.getSubject())
                .htmlMessage(statusReportEmail.getMessage())
                .sendEmail();
        } catch (Exception e) {
            LOGGER.error("Error sending status report emails to: {}", statusReportEmail.getRecipients().stream().collect(Collectors.joining("; ")));
            LOGGER.error(e);
        }
    }

    private User getUserFromJobData(JobExecutionContext context) throws UserRetrievalException {
        String id = context.getMergedJobDataMap().get(QuartzJob.JOB_DATA_KEY_SUBMITTED_BY_USER_ID).toString();

        if (NumberUtils.isParsable(id)) {
            return userDAO.getById(Long.valueOf(id)).toDomain();
        } else if (Util.isUUID(id)) {
            return cognitoApiWrapper.getUserInfo(UUID.fromString(id));
        } else {
            return null;
        }
    }
}
