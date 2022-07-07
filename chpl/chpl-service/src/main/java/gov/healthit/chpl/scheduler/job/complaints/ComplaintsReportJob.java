package gov.healthit.chpl.scheduler.job.complaints;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
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
import gov.healthit.chpl.manager.SurveillanceManager;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "complaintsReportJobLogger")
public class ComplaintsReportJob  implements Job {
    public static final String JOB_NAME = "Complaints Report Email";
    public static final String EMAIL_KEY = "email";

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
        String email = context.getMergedJobDataMap().getString(EMAIL_KEY);
        LOGGER.info("********* Starting the Complaints Report Email job for " + email + " *********");
        try {
            sendEmail(context, getReportData());
        } catch (Exception e) {
            LOGGER.catching(e);
        } finally {
            LOGGER.info("********* Completed the Complaints Report Email job for " + email + " *********");
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
            surv = surveillanceManager.getById(surveillanceId);
        } catch (Exception ex) {
            LOGGER.error("No surveillance found with ID " + surveillanceId);
        }
        if (surv != null) {
            LOGGER.info("Got details for surveillance " + surveillanceId);
            complaintsReportItem.getRelatedSurveillance().add(surv);
        }
    }

    private void sendEmail(JobExecutionContext context, List<ComplaintsReportItem> rows) throws EmailNotSentException, IOException {
        String email = context.getMergedJobDataMap().getString(EMAIL_KEY);
        LOGGER.info("Sending email to: " + email);
        chplEmailFactory.emailBuilder()
                .recipient(email)
                .subject(env.getProperty("complaintsReport.subject"))
                .htmlMessage(createHtmlMessage(context, rows.size()))
                .fileAttachments(Arrays.asList(complaintsReportCsvCreator.createCsvFile(rows)))
                .sendEmail();
        LOGGER.info("Completed Sending email to: " + email);
    }

    private String createHtmlMessage(JobExecutionContext context, int errorCount) {
        return chplHtmlEmailBuilder.initialize()
                .heading(env.getProperty("complaintsReport.heading"))
                .paragraph(
                        "",
                        env.getProperty("complaintsReport.paragraph1.body"))
                .footer(true)
                .build();
    }
}
