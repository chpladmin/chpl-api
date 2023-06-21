package gov.healthit.chpl.scheduler.job.urluptime;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.scheduler.job.QuartzJob;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "urlUptimeEmailJobLogger")
public class ServiceBasedUrlUptimeEmailJob extends QuartzJob {

    @Autowired
    private ChplHtmlEmailBuilder chplHtmlEmailBuilder;

    @Autowired
    private ChplEmailFactory chplEmailFactory;

    @Autowired
    private ServiceBasedUrlUptimeCalculator urlUptimeCalculator;

    @Autowired
    private ServiceBasedUrlUptimeCsvWriter urlUptimeCsvWriter;

    @Autowired
    private Environment env;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
//        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
//        LOGGER.info("********* Starting the Url Uptime Email job *********");
//        try {
//            sendEmail(context, getReportRows());
//        } catch (Exception e) {
//            LOGGER.error(e);
//        }
//        LOGGER.info("********* Completed the Url Uptime Email job *********");
    }

//    private List<UrlUptimeReport> getReportRows() {
//        return urlUptimeCalculator.calculateRowsForReport();
//    }
//
//    private void sendEmail(JobExecutionContext context, List<UrlUptimeReport> rows) throws EmailNotSentException, IOException {
//        LOGGER.info("Sending email to: " + context.getMergedJobDataMap().getString("email"));
//        chplEmailFactory.emailBuilder()
//                .recipient(context.getMergedJobDataMap().getString("email"))
//                .subject(env.getProperty("urlUptime.report.subject"))
//                .htmlMessage(createHtmlMessage(context, rows.size()))
//                .fileAttachments(Arrays.asList(urlUptimeCsvWriter.generateFile(rows)))
//                .sendEmail();
//        LOGGER.info("Completed Sending email to: " + context.getMergedJobDataMap().getString("email"));
//    }
//
//    private String createHtmlMessage(JobExecutionContext context, int errorCount) {
//        return chplHtmlEmailBuilder.initialize()
//                .heading(env.getProperty("urlUptime.report.subject"))
//                .paragraph("", env.getProperty("urlUptime.report.paragraph1.body"))
//                .footer(true)
//                .build();
//    }
}
