package gov.healthit.chpl.compliance.directreview;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.compliance.DirectReview;
import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.exception.EmailNotSentException;

@Component("directReviewUpdateEmailService")
public class DirectReviewUpdateEmailService {

    @Value("${directReview.chplChanges.email}")
    private String chplChangesEmailAddress;

    @Value("${directReview.chplChanges.emailSubject}")
    private String chplChangesEmailSubject;

    @Value("${directReview.unknownChanges.email}")
    private String unknownChangesEmailAddress;

    @Value("${directReview.unknownChanges.emailSubject}")
    private String unknownChangesEmailSubject;

    private DirectReviewCachingService directReviewService;
    private ChplEmailFactory chplEmailFactory;
    private ChplHtmlEmailBuilder chplHtmlEmailBuilder;

    @Autowired
    public DirectReviewUpdateEmailService(DirectReviewCachingService directReviewService,
            ChplEmailFactory chplEmailFactory, ChplHtmlEmailBuilder chplHtmlEmailBuilder) {
        this.directReviewService = directReviewService;
        this.chplEmailFactory = chplEmailFactory;
        this.chplHtmlEmailBuilder = chplHtmlEmailBuilder;
    }

    /**
     * A developer and possibly listings under that developer have changed in some way.
    */
    public void sendEmail(List<Developer> originalDevelopers, List<Developer> changedDevelopers,
            Map<Long, CertifiedProductSearchDetails> originalListings,
            Map<Long, CertifiedProductSearchDetails> changedListings,
            Logger logger) {
        List<DirectReview> originalDeveloperDrs = new ArrayList<DirectReview>();
        for (Developer originalDeveloper : originalDevelopers) {
            try {
                originalDeveloperDrs.addAll(directReviewService.getDirectReviews(originalDeveloper.getId(), logger).getDirectReviews());
            } catch (Exception ex) {
                logger.error("Error querying Jira for direct reviews related to developer ID " + originalDeveloper.getId());
                originalDeveloperDrs = null;
            }
        }

        if (originalDeveloperDrs == null) {
            try {
                sendUnknownDirectReviewEmails(originalDevelopers, changedDevelopers, originalListings,
                        changedListings, logger);
            } catch (Exception ex) {
                logger.error("Could not send email to Jira team: " + ex.getMessage());
            }
        } else if (originalDeveloperDrs != null && originalDeveloperDrs.size() > 0) {
            try {
                sendDirectReviewEmails(originalDeveloperDrs, originalDevelopers, changedDevelopers, originalListings,
                        changedListings, logger);
            } catch (Exception ex) {
                logger.error("Could not send email to Jira team: " + ex.getMessage());
            }
        } else {
            logger.info("No direct reviews were found for the affected developer(s). Not notifying the Jira team.");
        }
    }

    private void sendDirectReviewEmails(List<DirectReview> drs,
            List<Developer> originalDevelopers, List<Developer> changedDevelopers,
            Map<Long, CertifiedProductSearchDetails> originalListings,
            Map<Long, CertifiedProductSearchDetails> changedListings,
            Logger logger)
        throws EmailNotSentException {
        logger.info("Sending email about direct reviews potentially needing changes.");

        String[] recipients = new String[] {};
        if (!StringUtils.isEmpty(chplChangesEmailAddress)) {
            recipients = chplChangesEmailAddress.split(",");
        }

        String developerList = "The following direct reviews associated with the original developer"
                + (originalDevelopers.size() == 1 ? " " : "s ")
                + "may need updates: "
                + "<ul>";
        for (DirectReview dr : drs) {
            developerList += String.format("<li>%s</li>", dr.getJiraKey());
        }
        developerList += "</ul>";

        String chplProductNumbersChangedHtml = "";
        String chplProductNumbersChangedList = formatChplProductNumbersChangedHtml(originalListings, changedListings);
        if (StringUtils.isNotEmpty(chplProductNumbersChangedList)) {
            chplProductNumbersChangedHtml = "Additionally, any direct reviews with the following "
                    + "developer-associated listings may require updates due to changes "
                    + "in the CHPL Product Number: "
            + "<ul>" + chplProductNumbersChangedList + "</ul>";
        }

        String htmlMessage = chplHtmlEmailBuilder.initialize()
                .heading(chplChangesEmailSubject)
                .paragraph(null, formatDeveloperActionSummary(originalDevelopers, changedDevelopers))
                .paragraph(null, developerList)
                .paragraph(null, chplProductNumbersChangedHtml)
                .footer(false)
                .build();

        chplEmailFactory.emailBuilder().recipients(recipients)
                .subject(chplChangesEmailSubject)
                .htmlMessage(htmlMessage)
                .sendEmail();
    }

    private void sendUnknownDirectReviewEmails(List<Developer> originalDevelopers,
            List<Developer> changedDevelopers,
            Map<Long, CertifiedProductSearchDetails> originalListings,
            Map<Long, CertifiedProductSearchDetails> changedListings,
            Logger logger) throws EmailNotSentException {
        logger.info("Sending email about unknown direct reviews.");

        String[] recipients = new String[] {};
        String emailAddressProperty = unknownChangesEmailAddress;
        if (!StringUtils.isEmpty(emailAddressProperty)) {
            recipients = emailAddressProperty.split(",");
        }

        String chplProductNumbersChangedHtml = "";
        String chplProductNumbersChangedList = formatChplProductNumbersChangedHtml(originalListings, changedListings);
        if (StringUtils.isNotEmpty(chplProductNumbersChangedList)) {
            chplProductNumbersChangedHtml = "Additionally, any direct reviews with the following "
                    + "developer-associated listings may require updates due to changes "
                    + "in the CHPL Product Number: "
            + "<ul>" + chplProductNumbersChangedList + "</ul>";
        }

        String htmlMessage = chplHtmlEmailBuilder.initialize()
                .heading(unknownChangesEmailSubject)
                .paragraph(null, formatDeveloperActionSummary(originalDevelopers, changedDevelopers))
                .paragraph(null, "Any direct reviews related to the original developers may need updated.")
                .paragraph(null, chplProductNumbersChangedHtml)
                .footer(false)
                .build();

        chplEmailFactory.emailBuilder().recipients(recipients)
                .subject(unknownChangesEmailSubject)
                .htmlMessage(htmlMessage)
                .sendEmail();
    }

    private String formatDeveloperActionSummary(List<Developer> originalDevelopers,
            List<Developer> changedDevelopers) {
        String html = "";
        if (!ObjectUtils.allNotNull(originalDevelopers, changedDevelopers)) {
            return html;
        } else if (originalDevelopers.size() == 1 && changedDevelopers.size() > 1) {
            Developer newDeveloper = null;
            for (Developer changedDeveloper : changedDevelopers) {
                if (!(changedDeveloper.getId().equals(originalDevelopers.get(0).getId()))) {
                    newDeveloper = changedDeveloper;
                }
            }
            html = String.format("The developer %s (ID: %s) was split. "
                    + "The new developer is %s (ID: %s).",
                    originalDevelopers.get(0).getName(),
                    originalDevelopers.get(0).getId(),
                    newDeveloper.getName(),
                    newDeveloper.getId());
        }  else if (originalDevelopers.size() > 1 && changedDevelopers.size() == 1) {
            List<String> originalDeveloperNames = originalDevelopers.stream()
                    .map(dev -> dev.getName())
                    .collect(Collectors.toList());
            List<String> originalDeveloperIds = originalDevelopers.stream()
                    .map(dev -> dev.getId().toString())
                    .collect(Collectors.toList());
            html = String.format("The developers %s (IDs: %s) were merged into a single new developer. "
                    + "The newly created developer is %s (ID: %s).",
                    String.join(",", originalDeveloperNames),
                    String.join(",", originalDeveloperIds),
                    changedDevelopers.get(0).getName(),
                    changedDevelopers.get(0).getId());
        } else if (originalDevelopers.size() == 1 && changedDevelopers.size() == 1) {
            html = String.format("A product changed ownership from developer %s (IDs: %s) "
                    + "to developer %s (ID: %s).",
                    originalDevelopers.get(0).getName(),
                    originalDevelopers.get(0).getId(),
                    changedDevelopers.get(0).getName(),
                    changedDevelopers.get(0).getId());
        } else {
            html += "A change to developers occurred.";
        }
        return html;
    }

    private String formatChplProductNumbersChangedHtml(Map<Long, CertifiedProductSearchDetails> originalListings,
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
