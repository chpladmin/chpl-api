package gov.healthit.chpl.scheduler.job.surveillancereportingactivity;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.mail.MessagingException;

import org.apache.commons.csv.CSVRecord;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.dao.auth.UserDAO;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.scheduler.job.surveillancereportingactivity.excel.SurveillanceActivityReportWorkbook;
import gov.healthit.chpl.util.EmailBuilder;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "surveillanceActivityReportJobLogger")
public class SurveillanceReportingActivityJob implements Job {
    public static final String JOB_NAME = "surveillanceReportingActivityJob";
    public static final String START_DATE_KEY = "start-date";
    public static final String END_DATE_KEY = "end-date";
    public static final String USER_KEY = "user-id";

    @Autowired
    private SurveillanceActivityReportDataGatherer dataGatherer;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private Environment env;

    private DateTimeFormatter emailDateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        LOGGER.info("********* Starting the Surveillance Reporting Activity job. *********");
        try {
            List<CSVRecord> records = dataGatherer.getData(getStartDate(context), getEndDate(context));
            List<SurveillanceData> surveillances = records.stream()
                    .map(rec -> new SurveillanceData(rec))
                    .collect(Collectors.toList());

            SurveillanceActivityReportWorkbook workbook = new SurveillanceActivityReportWorkbook();

            File excelFile = workbook.generateWorkbook(surveillances);

            sendSuccessEmail(excelFile, context);
        } catch (Exception e) {
            LOGGER.catching(e);
            sendErrorEmail(context);
        }
        LOGGER.info("********* Completed the Surveillance Reporting Activity job. *********");
    }

    private void sendSuccessEmail(File excelFile, JobExecutionContext context) throws MessagingException, UserRetrievalException {
        UserDTO recipient = getUser(context);
        EmailBuilder emailBuilder = new EmailBuilder(env);
        emailBuilder.recipient(recipient.getEmail())
                .fileAttachments(Arrays.asList(excelFile))
                .subject(env.getProperty("surveillanceActivityReport.subject"))
                .htmlMessage(String.format(env.getProperty("surveillanceActivityReport.htmlBody"),
                        emailDateFormatter.format(getStartDate(context)),
                        emailDateFormatter.format(getEndDate(context))))
                .sendEmail();
    }

    private void sendErrorEmail(JobExecutionContext context) {
        try {
            UserDTO recipient = getUser(context);
            EmailBuilder emailBuilder = new EmailBuilder(env);
            emailBuilder.recipient(recipient.getEmail())
                    .subject(env.getProperty("surveillanceActivityReport.subject"))
                    .htmlMessage(String.format(env.getProperty("surveillanceActivityReport.htmlBody.error"),
                            emailDateFormatter.format(getStartDate(context)),
                            emailDateFormatter.format(getEndDate(context))))
                    .sendEmail();
        } catch (Exception e) {
            LOGGER.error("CHPL was unable to send the error email!");
            LOGGER.catching(e);
        }
    }

    private UserDTO getUser(JobExecutionContext context) throws UserRetrievalException {
        return userDAO.getById(context.getMergedJobDataMap().getLong(USER_KEY));
    }

    private LocalDate getStartDate(JobExecutionContext context) {
        return (LocalDate) context.getMergedJobDataMap().get(START_DATE_KEY);
    }

    private LocalDate getEndDate(JobExecutionContext context) {
        return (LocalDate) context.getMergedJobDataMap().get(END_DATE_KEY);
    }
}
