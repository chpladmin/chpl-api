package gov.healthit.chpl.scheduler.job.summarystatistics;

import java.io.File;
import java.io.IOException;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.CertificationStatusDAO;
import gov.healthit.chpl.dao.statistics.SummaryStatisticsDAO;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.entity.statistics.SummaryStatisticsEntity;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.scheduler.job.QuartzJob;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.StatisticsSnapshot;
import gov.healthit.chpl.scheduler.job.summarystatistics.email.CertificationStatusIdHelper;
import gov.healthit.chpl.scheduler.job.summarystatistics.email.DeveloperStatisticsSectionCreator;
import gov.healthit.chpl.scheduler.job.summarystatistics.email.DirectReviewStatisticsSectionCreator;
import gov.healthit.chpl.scheduler.job.summarystatistics.email.ListingStatisticsSectionCreator;
import gov.healthit.chpl.scheduler.job.summarystatistics.email.NonConformityStatisticsSectionCreator;
import gov.healthit.chpl.scheduler.job.summarystatistics.email.ProductStatisticsSectionCreator;
import gov.healthit.chpl.scheduler.job.summarystatistics.email.SurveillanceStatisticsSectionCreator;
import gov.healthit.chpl.scheduler.job.summarystatistics.pdf.SummaryStatisticsPdf;

public class SummaryStatisticsEmailJob extends QuartzJob {
    private static final Logger LOGGER = LogManager.getLogger("summaryStatisticsEmailJobLogger");

    @Autowired
    private SummaryStatisticsDAO summaryStatisticsDAO;

    @Autowired
    private CertificationBodyDAO certificationBodyDAO;

    @Autowired
    private CertificationStatusDAO certificationStatusDao;

    @Autowired
    private SummaryStatisticsPdf summaryStatisticsPdf;

    @Autowired
    private Environment env;

    @Autowired
    private ChplEmailFactory chplEmailFactory;

    private CertificationStatusIdHelper statusIdHelper;
    private List<CertificationBody> activeAcbs;

    public SummaryStatisticsEmailJob() throws Exception {
        super();
    }

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        try {
            SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
            LOGGER.info("********* Starting the Summary Statistics Email job. *********");
            LOGGER.info("Sending email to: " + jobContext.getMergedJobDataMap().getString("email"));

            statusIdHelper = new CertificationStatusIdHelper(certificationStatusDao);
            activeAcbs = certificationBodyDAO.findAllActive();

            SummaryStatisticsEntity summaryStatistics = summaryStatisticsDAO.getCurrentSummaryStatistics();
            StatisticsSnapshot stats = getStatistics(summaryStatistics);
            String message = createHtmlMessage(stats, summaryStatistics.getEndDate());
            LOGGER.info("Message to be sent: " + message);
            sendEmail(message, jobContext.getMergedJobDataMap().getString("email"));
        } catch (Exception e) {
            LOGGER.error("Caught unexpected exception: " + e.getMessage(), e);
        } finally {
            LOGGER.info("********* Completed the Summary Statistics Email job. *********");
        }
    }

    private void sendEmail(String message, String address) throws EmailNotSentException, IOException {
        String subject = env.getProperty("summaryEmailSubject").toString();

        List<String> addresses = new ArrayList<String>();
        addresses.add(address);

        chplEmailFactory.emailBuilder()
                .recipients(addresses)
                .subject(subject)
                .htmlMessage(message)
                .fileAttachments(getAttachments())
                .sendEmail();
    }

    private List<File> getAttachments() throws IOException {
        List<File> files = new ArrayList<File>();
        files.add(summaryStatisticsPdf.generate());
        return files;
    }

    private StatisticsSnapshot getStatistics(SummaryStatisticsEntity summaryStatistics)
            throws JsonParseException, JsonMappingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(summaryStatistics.getSummaryStatistics(), StatisticsSnapshot.class);
    }

    private String createHtmlMessage(StatisticsSnapshot stats, Date endDate) throws EntityRetrievalException {
        StringBuilder emailMessage = new StringBuilder();
        DeveloperStatisticsSectionCreator developerStatisticsSectionCreator = new DeveloperStatisticsSectionCreator(statusIdHelper);
        ProductStatisticsSectionCreator productStatisticsSectionCreator = new ProductStatisticsSectionCreator(statusIdHelper);
        ListingStatisticsSectionCreator listingStatisticsSectionCreator = new ListingStatisticsSectionCreator(statusIdHelper);
        SurveillanceStatisticsSectionCreator surveillanceStatisticsSectionCreator = new SurveillanceStatisticsSectionCreator();
        NonConformityStatisticsSectionCreator nonConformityStatisticsSectionCreator = new NonConformityStatisticsSectionCreator();
        DirectReviewStatisticsSectionCreator directReviewStatisticsSectionCreator = new DirectReviewStatisticsSectionCreator();

        emailMessage.append(createMessageHeader(endDate));
        emailMessage.append(developerStatisticsSectionCreator.build(stats, activeAcbs));
        emailMessage.append(productStatisticsSectionCreator.build(stats, activeAcbs));
        emailMessage.append(listingStatisticsSectionCreator.build(stats, activeAcbs));
        emailMessage.append(surveillanceStatisticsSectionCreator.build(stats, activeAcbs));
        emailMessage.append(nonConformityStatisticsSectionCreator.build(stats, activeAcbs));
        emailMessage.append(directReviewStatisticsSectionCreator.build(stats));

        return emailMessage.toString();
    }

    private String createMessageHeader(Date endDate) {
        Calendar currDateCal = Calendar.getInstance(TimeZone.getTimeZone(ZoneOffset.UTC));
        StringBuilder ret = new StringBuilder();
        ret.append("Statistics are current as of " + currDateCal.getTime());
        ret.append("<br/>");
        return ret.toString();
    }
}
