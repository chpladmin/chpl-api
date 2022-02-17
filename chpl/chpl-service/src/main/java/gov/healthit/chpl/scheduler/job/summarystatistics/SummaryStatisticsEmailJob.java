package gov.healthit.chpl.scheduler.job.summarystatistics;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.mail.internet.AddressException;

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
import gov.healthit.chpl.dao.statistics.SummaryStatisticsDAO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.entity.statistics.SummaryStatisticsEntity;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.scheduler.job.QuartzJob;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.EmailStatistics;
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
    private SummaryStatisticsPdf summaryStatisticsPdf;

    @Autowired
    private Environment env;

    @Autowired
    private ChplEmailFactory chplEmailFactory;

    private List<CertificationBodyDTO> activeAcbs;

    public SummaryStatisticsEmailJob() throws Exception {
        super();
    }

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        try {
            SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
            LOGGER.info("********* Starting the Summary Statistics Email job. *********");
            LOGGER.info("Sending email to: " + jobContext.getMergedJobDataMap().getString("email"));

            activeAcbs = certificationBodyDAO.findAllActive();

            SummaryStatisticsEntity summaryStatistics = summaryStatisticsDAO.getCurrentSummaryStatistics();
            EmailStatistics stats = getStatistics(summaryStatistics);
            String message = createHtmlMessage(stats, summaryStatistics.getEndDate());
            LOGGER.info("Message to be sent: " + message);
            sendEmail(message, jobContext.getMergedJobDataMap().getString("email"));
        } catch (Exception e) {
            LOGGER.error("Caught unexpected exception: " + e.getMessage(), e);
        } finally {
            LOGGER.info("********* Completed the Summary Statistics Email job. *********");
        }
    }

    private void sendEmail(String message, String address) throws AddressException, EmailNotSentException, IOException {
        String subject = env.getProperty("summaryEmailSubject").toString();

        List<String> addresses = new ArrayList<String>();
        addresses.add(address);

        chplEmailFactory.emailBuilder()
                .recipients(addresses)
                .subject(subject).htmlMessage(message)
                .fileAttachments(getAttachments())
                .sendEmail();
    }

    private List<File> getAttachments() throws IOException {
        List<File> files = new ArrayList<File>();
        files.add(getCopyOfSummaryStatisticsCsvFile());
        files.add(summaryStatisticsPdf.generate(getSummaryStatisticsFile()));
        return files;
    }

    private File getCopyOfSummaryStatisticsCsvFile() throws IOException {
        File origCsvFile = getSummaryStatisticsFile();
        Path newPath = Files.createTempFile("SummaryStatistics_", ".csv");
        Path origPath = origCsvFile.toPath();
        try {
            Files.createDirectories(newPath.getParent());
            Files.copy(origPath, newPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            LOGGER.info("Could not copy original file: " + origPath.toString());
            LOGGER.catching(e);
        }
        LOGGER.info("Copied " + origPath.toString() + " to " + newPath.toString());
        return newPath.toFile();
    }

    private File getSummaryStatisticsFile() {
        return new File(env.getProperty("downloadFolderPath") + File.separator
                + env.getProperty("summaryEmailName", "summaryStatistics.csv"));
    }

    private EmailStatistics getStatistics(SummaryStatisticsEntity summaryStatistics)
            throws JsonParseException, JsonMappingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(summaryStatistics.getSummaryStatistics(), EmailStatistics.class);
    }

    private String createHtmlMessage(EmailStatistics stats, Date endDate) throws EntityRetrievalException {
        StringBuilder emailMessage = new StringBuilder();
        DeveloperStatisticsSectionCreator developerStatisticsSectionCreator = new DeveloperStatisticsSectionCreator();
        ProductStatisticsSectionCreator productStatisticsSectionCreator = new ProductStatisticsSectionCreator();
        ListingStatisticsSectionCreator listingStatisticsSectionCreator = new ListingStatisticsSectionCreator();
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
        Calendar endDateCal = Calendar.getInstance(TimeZone.getTimeZone(ZoneOffset.UTC));
        endDateCal.setTime(endDate);
        StringBuilder ret = new StringBuilder();
        ret.append("Email body has current statistics as of " + currDateCal.getTime());
        ret.append("<br/>");
        ret.append("Email attachment has weekly statistics ending " + endDateCal.getTime());
        ret.append("<br/>");
        return ret.toString();
    }
}
