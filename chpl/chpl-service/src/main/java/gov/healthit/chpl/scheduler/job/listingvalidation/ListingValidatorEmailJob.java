package gov.healthit.chpl.scheduler.job.listingvalidation;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.mail.MessagingException;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.email.EmailBuilder;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.SchedulerManager;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "listingValidationReportEmailJobLogger")
public class ListingValidatorEmailJob  implements Job {

    @Autowired
    private ListingValidationReportDAO listingValidationReportDAO;

    @Autowired
    private CertificationBodyDAO certificationBodyDAO;

    @Autowired
    private Environment env;

    @Autowired
    private ListingValidationReportCsvCreator listingValidationReportCsvCreator;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Listing Validation Report Email job for " + context.getMergedJobDataMap().getString("email") + " *********");
        try {
            sendEmail(context, getReportData(context));
        } catch (Exception e) {
            LOGGER.catching(e);
        } finally {
            LOGGER.info("********* Completed the Listing Validation Report Email job. *********");
        }
    }

    private List<ListingValidationReport> getReportData(JobExecutionContext context) {
        List<Long> acbIds = getSelectedAcbIds(context);
        return listingValidationReportDAO.getAll().stream()
                .filter(lvr -> isListingValidForSelectedAcbs(lvr, acbIds))
                .collect(Collectors.toList());
    }

    private void sendEmail(JobExecutionContext context, List<ListingValidationReport> rows) throws MessagingException, IOException {
        LOGGER.info("Sending email to: " + context.getMergedJobDataMap().getString("email"));
        EmailBuilder emailBuilder = new EmailBuilder(env);
        emailBuilder.recipient(context.getMergedJobDataMap().getString("email"))
                .subject(env.getProperty("listingValidationReport.subject"))
                .htmlMessage(String.format(env.getProperty("listingValidationReport.body"), getAcbNamesAsCommaSeparatedList(context), rows.size()))
                .fileAttachments(Arrays.asList(listingValidationReportCsvCreator.createCsvFile(rows)))
                .sendEmail();
        LOGGER.info("Completed Sending email to: " + context.getMergedJobDataMap().getString("email"));
    }

    private String getAcbNamesAsCommaSeparatedList(JobExecutionContext jobContext) {
        if (Objects.nonNull(jobContext.getMergedJobDataMap().getString("acb"))) {
            return getSelectedAcbIds(jobContext).stream()
                    .map(acbId -> {
                        try {
                            return certificationBodyDAO.getById(acbId).getName();
                        } catch (NumberFormatException | EntityRetrievalException e) {
                            LOGGER.error("Could not retreive ACB name based on value: " + acbId, e);
                            return "";
                        }
                    })
                    .collect(Collectors.joining(", "));
        } else {
            return "";
        }
    }

    private List<Long> getSelectedAcbIds(JobExecutionContext jobContext) {
        return Arrays.asList(jobContext.getMergedJobDataMap().getString("acb").split(SchedulerManager.DATA_DELIMITER)).stream()
                .map(str -> Long.parseLong(str))
                .collect(Collectors.toList());
    }

    private boolean isListingValidForSelectedAcbs(ListingValidationReport lvr, List<Long> acbIds) {
        return acbIds.stream()
                .filter(acbId -> lvr.getCertificationBodyId().equals(acbId))
                .findAny()
                .isPresent();
    }
}
