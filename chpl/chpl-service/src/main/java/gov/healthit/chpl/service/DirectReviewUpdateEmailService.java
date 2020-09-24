package gov.healthit.chpl.service;

import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.compliance.DirectReview;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.util.EmailBuilder;
import lombok.extern.log4j.Log4j2;

@Component("directReviewUpdateEmailService")
@Log4j2
public class DirectReviewUpdateEmailService {

    @Value("${directReview.chplChanges.email}")
    private String chplChangesEmailAddress;

    @Value("${directReview.chplChanges.emailSubject}")
    private String chplChangesEmailSubject;

    @Value("${directReview.unknownChanges.email}")
    private String unknownChangesEmailAddress;

    @Value("${directReview.unknownChanges.emailSubject}")
    private String unknownChangesEmailSubject;

    private DirectReviewService directReviewService;
    private Environment env;

    @Autowired
    public DirectReviewUpdateEmailService(DirectReviewService directReviewService, Environment env) {
        this.directReviewService = directReviewService;
        this.env = env;
    }

    public void sendEmail(DeveloperDTO originalDeveloper, DeveloperDTO changedDeveloper,
            Map<Long, CertifiedProductSearchDetails> originalListings,
            Map<Long, CertifiedProductSearchDetails> changedListings) {

        List<DirectReview> originalDeveloperDrs = null;
        try {
            originalDeveloperDrs = directReviewService.getDirectReviews(originalDeveloper.getId());
        } catch (Exception ex) {
            LOGGER.error("Error querying Jira for direct reviews related to developer ID " + originalDeveloper.getId());
        }

        if (originalDeveloperDrs == null) {
            try {
                sendUnknownDirectReviewEmails(originalDeveloper, changedDeveloper, originalListings,
                        changedListings);
            } catch (Exception ex) {
                LOGGER.error("Could not send email to Jira team: " + ex.getMessage());
            }
        } else if (originalDeveloperDrs != null && originalDeveloperDrs.size() > 0) {
            try {
                sendDirectReviewEmails(originalDeveloperDrs, originalDeveloper, changedDeveloper, originalListings,
                        changedListings);
            } catch (MessagingException ex) {
                LOGGER.error("Could not send email to Jira team: " + ex.getMessage());
            }
        } else {
            LOGGER.info("No direct reviews were found for developer ID " + originalDeveloper.getId());
        }
    }

    private void sendDirectReviewEmails(List<DirectReview> drs,
            DeveloperDTO originalDeveloper, DeveloperDTO changedDeveloper,
            Map<Long, CertifiedProductSearchDetails> originalListings,
            Map<Long, CertifiedProductSearchDetails> changedListings)
        throws MessagingException {
        LOGGER.info("Sending email about direct reviews potentially needing changes.");

        String[] recipients = new String[] {};
        if (!StringUtils.isEmpty(chplChangesEmailAddress)) {
            recipients = chplChangesEmailAddress.split(",");
        }

        //TODO: how to make the message specific to the action?
        String htmlMessage = String.format("The developer %s (ID: %s) was split. "
                + "The newly created developer is %s (ID: %s). "
                + "The following direct reviews associated with the original developer may need updates: "
                + "<ul>",
                originalDeveloper.getName(),
                originalDeveloper.getId(),
                changedDeveloper.getName(),
                changedDeveloper.getId());
        for (DirectReview dr : drs) {
            htmlMessage += String.format("<li>%s</li>", dr.getJiraKey());
        }
        htmlMessage += "</ul>";

        String chplProductNumberChangedHtml = formatChplProductNumbersHtmlList(originalListings, changedListings);
        if (StringUtils.isNotEmpty(chplProductNumberChangedHtml)) {
            htmlMessage += "Any direct reviews with the following developer-associated listings "
            + "may require updates due to changes in the CHPL Product Number: "
            + "<ul>" + chplProductNumberChangedHtml + "</ul>";
        }

        EmailBuilder emailBuilder = new EmailBuilder(env);
        emailBuilder.recipients(recipients)
                .subject(env.getProperty("directReview.chplChanges.emailSubject"))
                .htmlMessage(htmlMessage)
                .acbAtlHtmlFooter()
                .sendEmail();
    }

    private void sendUnknownDirectReviewEmails(DeveloperDTO originalDeveloper, DeveloperDTO changedDeveloper,
            Map<Long, CertifiedProductSearchDetails> originalListings,
            Map<Long, CertifiedProductSearchDetails> changedListings) throws MessagingException {
        LOGGER.info("Sending email about unknown direct reviews.");

        String[] recipients = new String[] {};
        String emailAddressProperty = env.getProperty("directReview.unknownChanges.email");
        if (!StringUtils.isEmpty(emailAddressProperty)) {
            recipients = emailAddressProperty.split(",");
        }

        String htmlMessage = String.format("<p>The developer %s (ID: %s) was split. "
                + "The newly created developer is %s (ID: %s). "
                + "Any direct reviews related to '%s' may need updated.</p>",
                originalDeveloper.getName(),
                originalDeveloper.getId(),
                changedDeveloper.getName(),
                changedDeveloper.getId(),
                originalDeveloper.getName());

        String chplProductNumberChangedHtml = formatChplProductNumbersHtmlList(originalListings, changedListings);
        if (StringUtils.isNotEmpty(chplProductNumberChangedHtml)) {
            htmlMessage += "<p>Additionally, any direct reviews with the following "
                    + "developer-associated listings may require updates due to changes "
                    + "in the CHPL Product Number: "
            + "<ul>" + chplProductNumberChangedHtml + "</ul>";
        }

        EmailBuilder emailBuilder = new EmailBuilder(env);
        emailBuilder.recipients(recipients)
                .subject(env.getProperty("directReview.unknownChanges.emailSubject"))
                .htmlMessage(htmlMessage)
                .acbAtlHtmlFooter()
                .sendEmail();
    }

    private String formatChplProductNumbersHtmlList(Map<Long, CertifiedProductSearchDetails> originalListings,
            Map<Long, CertifiedProductSearchDetails> changedListings) {
        String chplProductNumberChangedHtml = "";
        for (Long id : originalListings.keySet()) {
            CertifiedProductSearchDetails originalListing = originalListings.get(id);
            CertifiedProductSearchDetails changedListing = changedListings.get(id);
            if (ObjectUtils.allNotNull(originalListing, changedListing,
                    originalListing.getChplProductNumber(), changedListing.getChplProductNumber())
                    && StringUtils.isNotEmpty(originalListing.getChplProductNumber())
                    && StringUtils.isNotEmpty(changedListing.getChplProductNumber())
                    && !originalListing.getChplProductNumber().equals(changedListing.getChplProductNumber())) {
                chplProductNumberChangedHtml += String.format("<li>%s to %s</li>",
                        originalListing.getChplProductNumber(), changedListing.getChplProductNumber());
            }
        }
        return chplProductNumberChangedHtml;
    }
}
