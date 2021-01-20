package gov.healthit.chpl.scheduler.job.listingvalidation;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import javax.mail.MessagingException;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.util.EmailBuilder;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "listingValidationReportEmailJobLogger")
public class ListingValidatorEmailJob  implements Job {

    @Autowired
    private ListingValidationReportDAO listingValidationReportDAO;

    @Autowired
    private Environment env;

    @Autowired
    private ListingValidationReportCsvCreator listingValidationReportCsvCreator;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Listing Validation Report Email job for " + context.getMergedJobDataMap().getString("email") + " *********");
        try {

            sendEmail(context, getReportData());
        } catch (Exception e) {
            LOGGER.catching(e);
        } finally {
            LOGGER.info("********* Completed the Listing Validation Report Email job. *********");
        }
    }

    private List<ListingValidationReport> getReportData() {
        return listingValidationReportDAO.getAll();
    }

    private void sendEmail(JobExecutionContext context, List<ListingValidationReport> rows) throws MessagingException, IOException {
        LOGGER.info("Sending email to: " + context.getMergedJobDataMap().getString("email"));
        EmailBuilder emailBuilder = new EmailBuilder(env);
        emailBuilder.recipient(context.getMergedJobDataMap().getString("email"))
                .subject(env.getProperty("listingValidationReport.subject"))
                .htmlMessage(String.format(env.getProperty("listingValidationReport.body"), getReportDateAsString(rows)))
                .fileAttachments(Arrays.asList(listingValidationReportCsvCreator.createCsvFile(rows)))
                .sendEmail();
        LOGGER.info("Completed Sending email to: " + context.getMergedJobDataMap().getString("email"));
    }

    private String getReportDateAsString(List<ListingValidationReport> rows) {
        if (rows.size() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
            //sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            return sdf.format(rows.get(0).getReportDate());
        } else {
            return "UNKNOWN";
        }
    }
}
