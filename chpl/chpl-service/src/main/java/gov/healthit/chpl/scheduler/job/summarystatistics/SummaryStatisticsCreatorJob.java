package gov.healthit.chpl.scheduler.job.summarystatistics;

import java.io.File;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

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

import gov.healthit.chpl.dao.CertificationStatusEventDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.statistics.SummaryStatisticsDAO;
import gov.healthit.chpl.dto.CertificationStatusEventDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.entity.statistics.SummaryStatisticsEntity;
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
    private CertificationStatusEventDAO certificationStatusEventDAO;

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

            EmailStatistics emailBodyStats = emailStatisticsCreator.getStatistics(allListings);
            saveSummaryStatistics(emailBodyStats);

            createSummaryStatisticsFile(allListings, jobContext);

        } catch (Exception e) {
            LOGGER.error("Caught unexpected exception: " + e.getMessage(), e);
        }
        LOGGER.info("********* Completed the Summary Statistics Creation job. *********");
    }


    @SuppressWarnings("checkstyle:linelength")
    private void createSummaryStatisticsFile(List<CertifiedProductDetailsDTO> allListings, JobExecutionContext jobContext)
            throws InterruptedException, ExecutionException {


        if (!isGenerateStatisticsFlagOn(jobContext)) {
            return;
        }

        Date startDate = getStartDate();
        if (startDate == null) {
            throw new RuntimeException("Could not obtain the startDate.");
        }
        Date endDate = new Date();
        Integer numDaysInPeriod = Integer.valueOf(env.getProperty("summaryEmailPeriodInDays").toString());

        List<CsvStatistics> csvStats = new ArrayList<CsvStatistics>();
        Calendar endDateCal = Calendar.getInstance(TimeZone.getTimeZone(ZoneOffset.UTC));
        endDateCal.setTime(startDate);

        Map<Long, List<CertificationStatusEventDTO>> statusesForAllListings = getAllStatusesForAllListings();

        while (endDate.compareTo(endDateCal.getTime()) >= 0) {
            CsvStatistics historyStat = new CsvStatistics();
            historyStat.setEndDate(endDateCal.getTime());
            historyStat = historicalStatisticsCreator.getStatistics(allListings, statusesForAllListings, endDateCal.getTime());
            csvStats.add(historyStat);
            endDateCal.add(Calendar.DATE, numDaysInPeriod);
        }

        StatsCsvFileWriter csvFileWriter = new StatsCsvFileWriter();
        csvFileWriter.writeCsvFile(env.getProperty("downloadFolderPath") + File.separator
                + env.getProperty("summaryEmailName", "summaryStatistics.csv"), csvStats);

        new File(env.getProperty("downloadFolderPath") + File.separator
                + env.getProperty("summaryEmailName", "summaryStatistics.csv"));
    }

    private String getJson(EmailStatistics statistics) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(statistics);
    }

    public void saveSummaryStatistics(EmailStatistics statistics)
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
                    SummaryStatisticsEntity entity = new SummaryStatisticsEntity();
                    entity.setEndDate(new Date());
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
            startDateCalendar.add(Calendar.DATE, 1);
            if (startDateCalendar.get(Calendar.DAY_OF_WEEK) == dow) {
                return startDateCalendar.getTime();
            }
        }
        return null;
    }

    private Map<Long, List<CertificationStatusEventDTO>> getAllStatusesForAllListings() {
        Map<Long, List<CertificationStatusEventDTO>> map = certificationStatusEventDAO.findAll().stream()
                .collect(Collectors.groupingBy(CertificationStatusEventDTO::getCertifiedProductId));

        Map<Long, List<CertificationStatusEventDTO>> syncdMap = new Hashtable<Long, List<CertificationStatusEventDTO>>();
        syncdMap.putAll(map);
        return syncdMap;
    }

    private Boolean isGenerateStatisticsFlagOn(JobExecutionContext jobContext) {
        return Boolean.valueOf(jobContext.getMergedJobDataMap().getString("generateCsvFile"));
    }

}
