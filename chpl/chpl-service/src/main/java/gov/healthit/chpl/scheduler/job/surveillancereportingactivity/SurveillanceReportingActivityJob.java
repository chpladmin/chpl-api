package gov.healthit.chpl.scheduler.job.surveillancereportingactivity;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVRecord;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.scheduler.job.surveillancereportingactivity.excel.SurveillanceActivityReportWorkbook;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "surveillanceActivityReportJobLogger")
public class SurveillanceReportingActivityJob implements Job {
    public static final String JOB_NAME = "surveillanceReportingActivityJob";
    public static final String START_DATE_KEY = "startDate";
    public static final String END_DATE_KEY = "endDate";
    public static final String USER_EMAIL = "email";
    public static final DateTimeFormatter JOB_DATA_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private SurveillanceActivityReportDataGatherer dataGatherer;

    @Autowired
    private Environment env;

    @Autowired
    private CertificationBodyDAO certificationBodyDAO;

    @Autowired
    private ChplHtmlEmailBuilder htmlEmailBuilder;

    @Autowired
    private ChplEmailFactory chplEmailFactory;

    @Value("${surveillanceActivityReport.subject}")
    private String surveillanceActvityReportTitle;

    @Value("${surveillanceActivityReport.htmlBody}")
    private String surveillanceActvityReportBody;

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

            LocalDate startDate = getStartDate(context);
            LOGGER.info("Getting ACBs that were not retired before  " + startDate);
            List<CertificationBodyDTO> allAcbs = certificationBodyDAO.findAllActiveBefore(startDate.atStartOfDay());
            LOGGER.info(String.format("The following ACBs were not retired before " + startDate + ": \n%s",
                    allAcbs.stream().map(acb -> acb.getName()).collect(Collectors.joining(", "))));

            allAcbs.sort(Comparator.comparing(CertificationBodyDTO::getName));
            File excelFile = workbook.generateWorkbook(surveillances, allAcbs);

            sendSuccessEmail(excelFile, context);
        } catch (Exception e) {
            LOGGER.catching(e);
            sendErrorEmail(context);
        }
        LOGGER.info("********* Completed the Surveillance Reporting Activity job. *********");
    }

    private void sendSuccessEmail(File excelFile, JobExecutionContext context) throws EmailNotSentException, UserRetrievalException {
        String bodyHtml = htmlEmailBuilder.initialize()
            .heading(surveillanceActvityReportTitle)
            .paragraph(null, String.format(surveillanceActvityReportBody, emailDateFormatter.format(getStartDate(context)),
                        emailDateFormatter.format(getEndDate(context))))
            .footer(false)
            .build();

        chplEmailFactory.emailBuilder()
                .recipient(getUserEmail(context))
                .fileAttachments(Arrays.asList(excelFile))
                .subject(surveillanceActvityReportTitle)
                .htmlMessage(bodyHtml)
                .sendEmail();
    }

    private void sendErrorEmail(JobExecutionContext context) {
        try {
            chplEmailFactory.emailBuilder()
                    .recipient(getUserEmail(context))
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

    private String getUserEmail(JobExecutionContext context) throws UserRetrievalException {
        return context.getMergedJobDataMap().getString(USER_EMAIL);
    }

    private LocalDate getStartDate(JobExecutionContext context) {
        String startDateStr = context.getMergedJobDataMap().get(START_DATE_KEY).toString();
        return LocalDate.parse(startDateStr, JOB_DATA_DATE_FORMATTER);
    }

    private LocalDate getEndDate(JobExecutionContext context) {
        String endDateStr = context.getMergedJobDataMap().get(END_DATE_KEY).toString();
        return LocalDate.parse(endDateStr, JOB_DATA_DATE_FORMATTER);
    }
}
