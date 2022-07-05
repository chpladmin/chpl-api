package gov.healthit.chpl.scheduler.job.complaints;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.dao.ComplaintDAO;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.SchedulerManager;
import gov.healthit.chpl.manager.SurveillanceManager;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "listingValidationReportEmailJobLogger")
public class ComplaintsReportEmailJob  implements Job {

    @Autowired
    private ComplaintDAO complaintDao;

    @Autowired
    private SurveillanceManager surveillanceManager;

    @Autowired
    private ChplHtmlEmailBuilder chplHtmlEmailBuilder;

    @Autowired
    private Environment env;

    @Autowired
    private ComplaintsReportCsvCreator complaintsReportCsvCreator;

    @Autowired
    private ChplEmailFactory chplEmailFactory;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Complaints Report Email job for " + context.getMergedJobDataMap().getString("email") + " *********");
        try {
            sendEmail(context, getReportData());
        } catch (Exception e) {
            LOGGER.catching(e);
        } finally {
            LOGGER.info("********* Completed the Complaints Report Email job for " + context.getMergedJobDataMap().getString("email") + " *********");
        }
    }

    private List<ComplaintsReportItem> getReportData() {
        LOGGER.info("Getting all complaints...");
        List<ComplaintsReportItem> allComplaintsReportItems = complaintDao.getAllComplaints().stream()
                .map(complaint -> ComplaintsReportItem.builder()
                        .complaint(complaint)
                        .build())
                .collect(Collectors.toList());
        LOGGER.info("Got " + allComplaintsReportItems.size() + " complaints.");

        LOGGER.info("Filling in complaint surveillance details...");
        allComplaintsReportItems.stream()
            .filter(complaintsReportItem -> !CollectionUtils.isEmpty(complaintsReportItem.getComplaint().getSurveillances()))
            .forEach(complaintsReportItem -> fillInSurveillanceDetails(complaintsReportItem));
        LOGGER.info("Completed filling in complaint surveillance details.");
        return allComplaintsReportItems;
    }

    private void fillInSurveillanceDetails(ComplaintsReportItem complaintsReportItem) {
        complaintsReportItem.getComplaint().getSurveillances().stream()
            .forEach(complaintSurveillance -> fillInSurveillanceDetails(complaintsReportItem, complaintSurveillance.getSurveillance().getId()));
    }

    private void fillInSurveillanceDetails(ComplaintsReportItem complaintsReportItem, Long surveillanceId) {
        LOGGER.info("Getting details for surveillance " + surveillanceId);
        Surveillance surv = null;
        try {
            surveillanceManager.getById(surveillanceId);
        } catch (Exception ex) {
            LOGGER.error("No surveillance found with ID " + surveillanceId);
        }
        if (surv != null) {
            LOGGER.info("Got details for surveillance " + surveillanceId);
            complaintsReportItem.getRelatedSurveillance().add(surv);
        }
    }

    private void sendEmail(JobExecutionContext context, List<ComplaintsReportItem> rows) throws EmailNotSentException, IOException {
        LOGGER.info("Sending email to: " + context.getMergedJobDataMap().getString("email"));
        chplEmailFactory.emailBuilder()
                .recipient(context.getMergedJobDataMap().getString("email"))
                .subject(env.getProperty("listingValidationReport.subject"))
                .htmlMessage(createHtmlMessage(context, rows.size()))
                .fileAttachments(Arrays.asList(complaintsReportCsvCreator.createCsvFile(rows)))
                .sendEmail();
        LOGGER.info("Completed Sending email to: " + context.getMergedJobDataMap().getString("email"));
    }

    private String createHtmlMessage(JobExecutionContext context, int errorCount) {
        return chplHtmlEmailBuilder.initialize()
                .heading(env.getProperty("listingValidationReport.subject"))
                .paragraph(
                        env.getProperty("listingValidationReport.paragraph1.heading"),
                        getAcbNamesAsBrSeparatedList(context))
                .paragraph("", String.format(env.getProperty("listingValidationReport.paragraph2.body"), errorCount))
                .footer(true)
                .build();
    }

    private String getAcbNamesAsBrSeparatedList(JobExecutionContext jobContext) {
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
                    .collect(Collectors.joining("<br />"));
        } else {
            return "";
        }
    }

    private List<Long> getSelectedAcbIds(JobExecutionContext jobContext) {
        return Arrays.asList(jobContext.getMergedJobDataMap().getString("acb").split(SchedulerManager.DATA_DELIMITER)).stream()
                .map(str -> Long.parseLong(str))
                .collect(Collectors.toList());
    }

    private boolean isListingValidForSelectedAcbs(ComplaintsReportItem lvr, List<Long> acbIds) {
        return acbIds.stream()
                .filter(acbId -> lvr.getCertificationBodyId().equals(acbId))
                .findAny()
                .isPresent();
    }
}
