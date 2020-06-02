package gov.healthit.chpl.scheduler.job;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.util.StringUtils;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.dto.ProductOwnerDTO;
import gov.healthit.chpl.util.EmailBuilder;
import gov.healthit.chpl.util.Util;

public class SplitDeveloperJob implements Job {
    private static final Logger LOGGER = LogManager.getLogger("splitDeveloperJobLogger");

    @Autowired
    private Environment env;

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        LOGGER.info("********* Starting the Split Developer job. *********");

        /**
         * Splits a developer into two. The new developer will have at least one product assigned to it that used to be
         * assigned to the original developer along with the versions and listings associated with those products. At least
         * one product along with its versions and listings will remain assigned to the original developer. Since the
         * developer code is auto-generated in the database, any listing that gets transferred to the new developer will
         * automatically have a unique ID (no other developer can have the same developer code).
         */


        //TODO: Evict all caches

        String[] recipients = jobContext.getMergedJobDataMap().getString("email").split("\u263A");

        try {
            sendEmails(jobContext, recipients);
        } catch (IOException | MessagingException e) {
            LOGGER.error(e);
        }
        LOGGER.info("********* Completed the Split Developer job. *********");
    }

    private DeveloperDTO splitDeveloper(DeveloperDTO oldDeveloper, DeveloperDTO developerToCreate,
            List<Long> productIdsToMove) {
        // create the new developer and log activity
        DeveloperDTO createdDeveloper = create(developerToCreate);

        // re-assign products to the new developer
        // log activity for all listings whose ID will have changed
        Date splitDate = new Date();
        List<CertificationBodyDTO> allowedAcbs = resourcePermissions.getAllAcbsForCurrentUser();
        for (Long productIdToMove : productIdsToMove) {
            List<CertifiedProductDetailsDTO> affectedListings = cpManager.getByProduct(productIdToMove);
            // need to get details for affected listings now before the product is re-assigned
            // so that any listings with a generated new-style CHPL ID have the old developer code
            Map<Long, CertifiedProductSearchDetails> beforeListingDetails = new HashMap<Long, CertifiedProductSearchDetails>();
            for (CertifiedProductDetailsDTO affectedListing : affectedListings) {
                CertifiedProductSearchDetails beforeListing = cpdManager
                        .getCertifiedProductDetails(affectedListing.getId());

                // make sure each listing associated with the new developer
                boolean hasAccessToAcb = false;
                for (CertificationBodyDTO allowedAcb : allowedAcbs) {
                    if (allowedAcb.getId().longValue() == affectedListing.getCertificationBodyId().longValue()) {
                        hasAccessToAcb = true;
                    }
                }
                if (!hasAccessToAcb) {
                    throw new AccessDeniedException(
                            msgUtil.getMessage("acb.accessDenied.listingUpdate", beforeListing.getChplProductNumber(),
                                    beforeListing.getCertifyingBody().get(CertifiedProductSearchDetails.ACB_NAME_KEY)));
                }

                beforeListingDetails.put(beforeListing.getId(), beforeListing);
            }

            // move the product to be owned by the new developer
            ProductDTO productToMove = productManager.getById(productIdToMove);
            productToMove.setDeveloperId(createdDeveloper.getId());
            // add owner history for old developer
            ProductOwnerDTO newOwner = new ProductOwnerDTO();
            newOwner.setProductId(productToMove.getId());
            newOwner.setDeveloper(oldDeveloper);
            newOwner.setTransferDate(splitDate.getTime());
            productToMove.getOwnerHistory().add(newOwner);
            productManager.update(productToMove);

            // get the listing details again - this time they will have the new developer code
            // so the change will show up in activity reports
            for (CertifiedProductDetailsDTO affectedListing : affectedListings) {
                CertifiedProductSearchDetails afterListing = cpdManager
                        .getCertifiedProductDetails(affectedListing.getId());
                CertifiedProductSearchDetails beforeListing = beforeListingDetails.get(afterListing.getId());
                activityManager.addActivity(ActivityConcept.CERTIFIED_PRODUCT, beforeListing.getId(),
                        "Updated certified product " + afterListing.getChplProductNumber() + ".", beforeListing,
                        afterListing);
            }
        }

        DeveloperDTO afterDeveloper = null;
        // the split is complete - log split activity
        // get the original developer object from the db to make sure it's all filled in
        DeveloperDTO origDeveloper = getById(oldDeveloper.getId());
        afterDeveloper = getById(createdDeveloper.getId());
        List<DeveloperDTO> splitDevelopers = new ArrayList<DeveloperDTO>();
        splitDevelopers.add(origDeveloper);
        splitDevelopers.add(afterDeveloper);
        activityManager.addActivity(ActivityConcept.DEVELOPER, afterDeveloper.getId(),
                "Split developer " + origDeveloper.getName() + " into " + origDeveloper.getName()
                        + " and " + afterDeveloper.getName(),
                origDeveloper, splitDevelopers);
        return afterDeveloper;
    }

    private void sendEmails(JobExecutionContext jobContext, String[] recipients)
            throws IOException, AddressException, MessagingException {

        String subject = "NEED TO REVIEW: Certification Status of listing set to \""
                + jobContext.getMergedJobDataMap().getString("status") + "\"";
        String htmlMessage = createHtmlEmailBody(jobContext);

        List<String> emailAddresses = Arrays.asList(recipients);
        for (String emailAddress : emailAddresses) {
            try {
                sendEmail(emailAddress, subject, htmlMessage);
            } catch (Exception ex) {
                LOGGER.error("Could not send message to " + emailAddress, ex);
            }
        }
    }

    private void sendEmail(String recipientEmail, String subject, String htmlMessage)
            throws MessagingException {
        LOGGER.info("Sending email to: " + recipientEmail);
        LOGGER.info("Message to be sent: " + htmlMessage);

        EmailBuilder emailBuilder = new EmailBuilder(env);
        emailBuilder.recipient(recipientEmail)
                .subject(subject)
                .htmlMessage(htmlMessage)
                .sendEmail();
    }

    private String createHtmlEmailBody(JobExecutionContext jobContext) {
        JobDataMap jdm = jobContext.getMergedJobDataMap();
        String reasonForStatusChange = jdm.getString("reason");
        if (StringUtils.isEmpty(reasonForStatusChange)) {
            reasonForStatusChange = "<strong>ONC-ACB provided reason for status change:</strong> This field is blank";
        } else {
            reasonForStatusChange = "<strong>ONC-ACB provided reason for status change:</strong> \""
                    + reasonForStatusChange + "\"";
        }
        String reasonForListingChange = jdm.getString("reasonForChange");
        if (StringUtils.isEmpty(reasonForListingChange)) {
            reasonForListingChange = "<strong>ONC-ACB provided reason for listing change:</strong> This field is blank";
        } else {
            reasonForListingChange = "<strong>ONC-ACB provided reason for listing change:</strong> \""
                    + reasonForListingChange + "\"";
        }
        int openNcs = jdm.getInt("openNcs");
        int closedNcs = jdm.getInt("closedNcs");
        String htmlMessage = String.format("<p>The CHPL Listing <a href=\"%s/#/product/%d\">%s</a>, owned by \"%s\" "
                + "and certified by \"%s\" has been set on \"%s\" by \"%s\" to a Certification Status of \"%s\" with "
                + "an effective date of \"%s\".</p>"
                + "<p>%s</p>"
                + "<p>%s</p>"
                + "<p>There %s %d Open Nonconformit%s and %d Closed Nonconformit%s.</p>"
                + "<p>ONC should review the activity and all details of the listing to determine if "
                + "this action warrants a ban on the Developer.</p>",
                env.getProperty("chplUrlBegin"), // root of URL
                jdm.getLong("dbId"), // for URL to product page
                jdm.getString("chplId"), // visible link
                jdm.getString("developer"), // developer name
                jdm.getString("acb"), // ACB name
                Util.getDateFormatter().format(new Date(jdm.getLong("changeDate"))), // date of change
                jdm.getString("fullName"), // user making change
                jdm.getString("status"), // target status
                Util.getDateFormatter().format(new Date(jdm.getLong("effectiveDate"))), // effective date of change
                reasonForStatusChange, // reason for change
                reasonForListingChange, // reason for change
                (openNcs != 1 ? "were" : "was"), openNcs, (openNcs != 1 ? "ies" : "y"), // formatted counts of open
                closedNcs, (closedNcs != 1 ? "ies" : "y")); // and closed nonconformities, with English word endings
        return htmlMessage;
    }
}
