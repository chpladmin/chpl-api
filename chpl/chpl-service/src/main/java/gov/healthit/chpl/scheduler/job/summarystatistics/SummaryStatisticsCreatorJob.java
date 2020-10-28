package gov.healthit.chpl.scheduler.job.summarystatistics;

import java.io.File;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.statistics.SummaryStatisticsDAO;
import gov.healthit.chpl.domain.DateRange;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.entity.SummaryStatisticsEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.scheduler.job.QuartzJob;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.CsvStatistics;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.EmailStatistics;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.EmailStatisticsCreator;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.HistoricalStatisticsCreator;

@DisallowConcurrentExecution
public class SummaryStatisticsCreatorJob extends QuartzJob {
    private static final Logger LOGGER = LogManager.getLogger("summaryStatisticsCreatorJobLogger");

    @Autowired
    private HistoricalStatisticsCreator historicalStatisticsCreator;

    @Autowired
    private EmailStatisticsCreator emailStatisticsCreator;

    @Autowired
    private SummaryStatisticsDAO summaryStatisticsDAO;

    @Autowired
    private CertifiedProductDAO certifiedProductDAO;

    @Autowired
    private JpaTransactionManager txManager;

    @Autowired
    private Environment env;

    public SummaryStatisticsCreatorJob() throws Exception {
        super();
    }

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        LOGGER.info("********* Starting the Summary Statistics Creation job. *********");
        try {
            SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

            LOGGER.info("Getting all listings.");
            List<CertifiedProductDetailsDTO> allListings = certifiedProductDAO.findAll();
            LOGGER.info("Completing getting all listings.");

            Boolean generateCsv = Boolean.valueOf(jobContext.getMergedJobDataMap().getString("generateCsvFile"));
            Date startDate = getStartDate();
            if (startDate == null) {
                throw new RuntimeException("Could not obtain the startDate.");
            }
            Date endDate = new Date();
            Integer numDaysInPeriod = Integer.valueOf(env.getProperty("summaryEmailPeriodInDays").toString());

            //EmailStatistics emailBodyStats = emailStatisticsCreator.getStatistics(allListings);

            if (generateCsv) {
                createSummaryStatisticsFile(allListings, startDate, endDate, numDaysInPeriod);
            }
            //saveSummaryStatistics(emailBodyStats, endDate);

        } catch (Exception e) {
            LOGGER.error("Caught unexpected exception: " + e.getMessage(), e);
        }
        LOGGER.info("********* Completed the Summary Statistics Creation job. *********");
    }


    @SuppressWarnings("checkstyle:linelength")
    private void createSummaryStatisticsFile(List<CertifiedProductDetailsDTO> allListings, Date startDate, Date endDate, Integer numDaysInPeriod)
            throws InterruptedException, ExecutionException {
        List<CsvStatistics> csvStats = new ArrayList<CsvStatistics>();
        Calendar startDateCal = Calendar.getInstance(TimeZone.getTimeZone(ZoneOffset.UTC));
        startDateCal.setTime(startDate);
        Calendar endDateCal = Calendar.getInstance(TimeZone.getTimeZone(ZoneOffset.UTC));
        endDateCal.setTime(startDate);
        endDateCal.add(Calendar.DATE, numDaysInPeriod);

        while (endDate.compareTo(endDateCal.getTime()) >= 0) {
            LOGGER.info("Getting csvRecord for start date " + startDateCal.getTime().toString() + " end date "
                    + endDateCal.getTime().toString());

            DateRange csvRange = new DateRange(startDateCal.getTime(), new Date(endDateCal.getTimeInMillis()));
            CsvStatistics historyStat = new CsvStatistics();
            historyStat.setDateRange(csvRange);
            historyStat = historicalStatisticsCreator.getStatistics(allListings, csvRange);
            csvStats.add(historyStat);
            LOGGER.info("Finished getting csvRecord for start date " + startDateCal.getTime().toString() + " end date "
                    + endDateCal.getTime().toString());
            startDateCal.add(Calendar.DATE, numDaysInPeriod);
            endDateCal.setTime(startDateCal.getTime());
            endDateCal.add(Calendar.DATE, numDaysInPeriod);
        }
        LOGGER.info("Finished getting statistics");

        LOGGER.info("Writing statistics CSV");
        StatsCsvFileWriter csvFileWriter = new StatsCsvFileWriter();
        csvFileWriter.writeCsvFile(env.getProperty("downloadFolderPath") + File.separator
                + env.getProperty("summaryEmailName", "summaryStatistics.csv"), csvStats);

        new File(env.getProperty("downloadFolderPath") + File.separator
                + env.getProperty("summaryEmailName", "summaryStatistics.csv"));
        LOGGER.info("Completed statistics CSV");
    }

    private String getJson(EmailStatistics statistics) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(statistics);
    }

    public void saveSummaryStatistics(EmailStatistics statistics, Date endDate)
            throws JsonProcessingException, EntityCreationException, EntityRetrievalException {

        // We need to manually create a transaction in this case because of how AOP works. When a method is
        // annotated with @Transactional, the transaction wrapper is only added if the object's proxy is called.
        // The object's proxy is not called when the method is called from within this class. The object's proxy
        // is called when the method is public and is called from a different object.
        // https://stackoverflow.com/questions/3037006/starting-new-transaction-in-spring-bean
        TransactionTemplate txTemplate = new TransactionTemplate(txManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        txTemplate.execute(new TransactionCallbackWithoutResult() {

            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                    summaryStatisticsDAO.deleteAll();

                    SummaryStatisticsEntity entity = new SummaryStatisticsEntity();
                    entity.setEndDate(endDate);
                    entity.setSummaryStatistics(getJson(statistics));
                    summaryStatisticsDAO.create(entity);
                } catch (Exception e) {
                    status.setRollbackOnly();
                }
            }
        });
    }

    @SuppressWarnings({"checkstyle:magicnumber"})
    private Date getStartDate() {
        Calendar startDateCalendar = Calendar.getInstance();
        // This is a constant date, which marks the beginning of time for
        // retrieving statistics;
        startDateCalendar.set(2016, 3, 1, 0, 0, 0);

        // What DOW is today?
        Calendar now = Calendar.getInstance();
        Integer dow = now.get(Calendar.DAY_OF_WEEK);
        if (startDateCalendar.get(Calendar.DAY_OF_WEEK) == dow) {
            return startDateCalendar.getTime();
        }
        for (int i = 0; i <= 6; i++) {
            startDateCalendar.add(Calendar.DATE, (-1));
            if (startDateCalendar.get(Calendar.DAY_OF_WEEK) == dow) {
                return startDateCalendar.getTime();
            }
        }
        return null;
    }

}
