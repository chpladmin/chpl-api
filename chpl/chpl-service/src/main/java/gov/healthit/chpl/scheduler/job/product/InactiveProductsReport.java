package gov.healthit.chpl.scheduler.job.product;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.email.footer.AdminFooter;
import gov.healthit.chpl.exception.EmailNotSentException;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "inactiveProductsReportJobLogger")
public class InactiveProductsReport implements Job {

    @Autowired
    private InactiveProductDAO inactiveProductDao;

    @Autowired
    private InactiveProductsReportCsvCreator inactiveProductsReportCsvCreator;

    @Autowired
    private ChplHtmlEmailBuilder chplHtmlEmailBuilder;

    @Autowired
    private ChplEmailFactory chplEmailFactory;

    @Autowired
    private Environment env;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Inactive Products Report job for " + context.getMergedJobDataMap().getString("email") + " *********");
        try {
            sendEmail(context, getReportData(context));
        } catch (Exception e) {
            LOGGER.catching(e);
        } finally {
            LOGGER.info("********* Completed the Inactive Products Report job. *********");
        }
    }

    private List<InactiveProduct> getReportData(JobExecutionContext context) {
        return inactiveProductDao.getAll();
    }

    private void sendEmail(JobExecutionContext context, List<InactiveProduct> inactiveProducts) throws EmailNotSentException, IOException {
        LOGGER.info("Sending email to: " + context.getMergedJobDataMap().getString("email"));
        chplEmailFactory.emailBuilder()
                .recipient(context.getMergedJobDataMap().getString("email"))
                .subject(env.getProperty("inactiveProductsReport.subject"))
                .htmlMessage(createHtmlMessage(context, inactiveProducts.size()))
                .fileAttachments(Arrays.asList(inactiveProductsReportCsvCreator.createCsvFile(inactiveProducts)))
                .sendEmail();
        LOGGER.info("Completed Sending email to: " + context.getMergedJobDataMap().getString("email"));
    }

    private String createHtmlMessage(JobExecutionContext context, int inactiveProductCount) {
        return chplHtmlEmailBuilder.initialize()
                .heading(env.getProperty("inactiveProductsReport.subject"))
                .paragraph("", String.format(env.getProperty("inactiveProductsReport.body"), inactiveProductCount))
                .footer(AdminFooter.class)
                .build();
    }
}
