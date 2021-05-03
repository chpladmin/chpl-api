package gov.healthit.chpl.scheduler.job.surveillancereportingactivity;

import java.io.File;
import java.time.LocalDate;
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

@Log4j2()
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

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        LOGGER.info("********* Starting the Surveillance Reporting Activity job. *********");
        try {
            LocalDate startDate = (LocalDate) context.getMergedJobDataMap().get(START_DATE_KEY);
            LocalDate endDate = (LocalDate) context.getMergedJobDataMap().get(END_DATE_KEY);
            List<CSVRecord> records = dataGatherer.getData(startDate, endDate);

            LOGGER.info("Count of found Surveillance Rows: " + records.size());

            List<SurveillanceData> surveillances = records.stream()
                    .map(rec -> new SurveillanceData(rec))
                    .collect(Collectors.toList());

            SurveillanceActivityReportWorkbook workbook = new SurveillanceActivityReportWorkbook();

            File excelFile = workbook.generateWorkbook(surveillances);
            UserDTO user = getUser(context.getMergedJobDataMap().getLong(USER_KEY));

            sendEmail(user, excelFile);
        } catch (Exception e) {
            LOGGER.catching(e);
        }
        LOGGER.info("********* Completed the Surveillance Reporting Activity job. *********");
    }

    private void sendEmail(UserDTO recipient, File excelFile) throws MessagingException {
        EmailBuilder emailBuilder = new EmailBuilder(env);
        emailBuilder.recipient(recipient.getEmail())
                .fileAttachments(Arrays.asList(excelFile))
                .subject("Surveillance Activity Report")
                .htmlMessage("This text needs to be provided")
                .sendEmail();
    }

    private UserDTO getUser(Long userId) throws UserRetrievalException {
        return userDAO.getById(userId);
    }
}
