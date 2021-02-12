package gov.healthit.chpl.scheduler.job.summarystatistics;

import java.io.File;
import java.io.IOException;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.DocumentException;
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
import gov.healthit.chpl.entity.SummaryStatisticsEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.scheduler.job.QuartzJob;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.EmailStatistics;
import gov.healthit.chpl.scheduler.job.summarystatistics.pdf.SummaryStatisticsPdf;
import gov.healthit.chpl.util.EmailBuilder;

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

    private void sendEmail(String message, String address) throws AddressException, MessagingException, IOException, DocumentException {
        String subject = env.getProperty("summaryEmailSubject").toString();

        List<String> addresses = new ArrayList<String>();
        addresses.add(address);

        EmailBuilder emailBuilder = new EmailBuilder(env);
        emailBuilder.recipients(addresses)
                .subject(subject).htmlMessage(message)
                .fileAttachments(getSummaryStatisticsFiles())
                .sendEmail();
    }

    private List<File> getSummaryStatisticsFiles() throws IOException, DocumentException {
        List<File> files = new ArrayList<File>();
        File file = new File(env.getProperty("downloadFolderPath") + File.separator
                + env.getProperty("summaryEmailName", "summaryStatistics.csv"));
        files.add(file);

        files.add(summaryStatisticsPdf.generate(file));
        return files;
    }

    private EmailStatistics getStatistics(SummaryStatisticsEntity summaryStatistics)
            throws JsonParseException, JsonMappingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(summaryStatistics.getSummaryStatistics(), EmailStatistics.class);
    }

    private String createHtmlMessage(EmailStatistics stats, Date endDate) throws EntityRetrievalException {
        StringBuilder emailMessage = new StringBuilder();
        emailMessage.append(createMessageHeader(endDate));
        return emailMessage.toString();
    }

    private String createMessageHeader(Date endDate) {
        Calendar endDateCal = Calendar.getInstance(TimeZone.getTimeZone(ZoneOffset.UTC));
        endDateCal.setTime(endDate);
        StringBuilder ret = new StringBuilder();
        ret.append("Current statistics are in the attached PDF attachment.");
        ret.append("<br/>");
        ret.append("Historical statistics has weekly statistics ending " + endDateCal.getTime());
        ret.append("<br/>");
        ret.append("In the attached CSV file: <br/>");
        ret.append("<ul>");
        ret.append("<li>Total Closed Non-Conformities - Some Non-Conformities may be closed that are not counted in these statistics</li>");
        ret.append("</ul>");
        return ret.toString();
    }
}
