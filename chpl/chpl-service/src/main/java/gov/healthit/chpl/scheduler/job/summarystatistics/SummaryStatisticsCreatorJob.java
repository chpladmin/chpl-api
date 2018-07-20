package gov.healthit.chpl.scheduler.job.summarystatistics;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.app.statistics.StatsCsvFileWriter;
import gov.healthit.chpl.dao.statistics.SummaryStatisticsDAO;
import gov.healthit.chpl.domain.DateRange;
import gov.healthit.chpl.domain.statistics.Statistics;
import gov.healthit.chpl.entity.SummaryStatisticsEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.scheduler.JobConfig;
import gov.healthit.chpl.scheduler.job.QuartzJob;

@DisallowConcurrentExecution
public class SummaryStatisticsCreatorJob extends QuartzJob {
    private static final Logger LOGGER = LogManager.getLogger(SummaryStatisticsCreatorJob.class);
    private static final String DEFAULT_PROPERTIES_FILE = "environment.properties";
    private AsynchronousSummaryStatisticsInitializor asynchronousStatisticsInitializor;
    private SummaryStatisticsDAO summaryStatisticsDAO;
    private Properties props;
    private AbstractApplicationContext context;

    public SummaryStatisticsCreatorJob() throws Exception{
        super();
        setLocalContext();
        context = new AnnotationConfigApplicationContext(JobConfig.class);
        initiateSpringBeans(context);
        loadProperties();
    }
    
    @Override
    protected void initiateSpringBeans(AbstractApplicationContext context) throws IOException {
        setAsynchronousStatisticsInitializor(
                (AsynchronousSummaryStatisticsInitializor) context.getBean("asynchronousStatisticsSummaryInitializor"));
        setSummaryStatisticsDAO((SummaryStatisticsDAO) context.getBean("summaryStatisticsDAO"));
    }
    
    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        try {
            Boolean generateCsv = Boolean.valueOf(jobContext.getMergedJobDataMap().getString("generateCsvFile"));
            Calendar cal = Calendar.getInstance();
            cal.set(2016, Calendar.MARCH, 31);
            Date startDate = getStartDate();
            if (startDate == null) {
                throw new RuntimeException("Could not obtain the startDate.");
            }
            Date endDate = new Date();
            Integer numDaysInPeriod = Integer.valueOf(props.getProperty("summaryEmailSubject").toString());
            
            Future<Statistics> futureEmailBodyStats = asynchronousStatisticsInitializor.getStatistics(null);
            Statistics emailBodyStats = futureEmailBodyStats.get();
            
            if (generateCsv) {
                createSummaryStatisticsFile(startDate, endDate, numDaysInPeriod);
            }
            saveSummaryStatistics(emailBodyStats, endDate);

        } catch (Exception e) {
            LOGGER.error(e);
        } finally {
            context.close();
        }
    }

    private void createSummaryStatisticsFile(Date startDate, Date endDate, Integer numDaysInPeriod)
            throws InterruptedException, ExecutionException {
        List<Statistics> csvStats = new ArrayList<Statistics>();
        Calendar startDateCal = Calendar.getInstance(TimeZone.getTimeZone(ZoneOffset.UTC));
        startDateCal.setTime(startDate);
        Calendar endDateCal = Calendar.getInstance(TimeZone.getTimeZone(ZoneOffset.UTC));
        endDateCal.setTime(startDate);
        endDateCal.add(Calendar.DATE, numDaysInPeriod);

        while (endDate.compareTo(endDateCal.getTime()) >= 0) {
            LOGGER.info("Getting csvRecord for start date " + startDateCal.getTime().toString() + " end date "
                    + endDateCal.getTime().toString());
            System.out.println("Getting csvRecord for start date " + startDateCal.getTime().toString() + " end date "
                    + endDateCal.getTime().toString());
            DateRange csvRange = new DateRange(startDateCal.getTime(), new Date(endDateCal.getTimeInMillis()));
            Statistics historyStat = new Statistics();
            historyStat.setDateRange(csvRange);
            Future<Statistics> futureEmailCsvStats = asynchronousStatisticsInitializor.getStatistics(csvRange);
            historyStat = futureEmailCsvStats.get();
            csvStats.add(historyStat);
            LOGGER.info("Finished getting csvRecord for start date "
                    + startDateCal.getTime().toString() + " end date "
                    + endDateCal.getTime().toString());
            System.out.println("Finished getting csvRecord for start date "
                    + startDateCal.getTime().toString() + " end date "
                    + endDateCal.getTime().toString());
            startDateCal.add(Calendar.DATE, numDaysInPeriod);
            endDateCal.setTime(startDateCal.getTime());
            endDateCal.add(Calendar.DATE, numDaysInPeriod);
        }
        LOGGER.info("Finished getting statistics");
        System.out.println("Finished getting statistics");
        StatsCsvFileWriter.writeCsvFile(props.getProperty("downloadFolderPath") + File.separator
                + props.getProperty("summaryEmailName", "summaryStatistics.csv"), csvStats);

        new File(props.getProperty("downloadFolderPath") + File.separator
                + props.getProperty("summaryEmailName", "summaryStatistics.csv"));
    }

    private String getJson(Statistics statistics) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(statistics);
    }
    
    private void saveSummaryStatistics(Statistics statistics, Date endDate) throws JsonProcessingException, EntityCreationException, EntityRetrievalException {
        SummaryStatisticsEntity entity = new SummaryStatisticsEntity();
        entity.setEndDate(endDate);
        entity.setSummaryStatistics(getJson(statistics));
        getSummaryStatisticsDAO().create(entity);
    }
    
    
    private Properties loadProperties() throws IOException {
        InputStream in = SummaryStatisticsCreatorJob.class.getClassLoader().getResourceAsStream(DEFAULT_PROPERTIES_FILE);
        if (in == null) {
            props = null;
            throw new FileNotFoundException("Environment Properties File not found in class path.");
        } else {
            props = new Properties();
            props.load(in);
            in.close();
        }
        return props;
    }
    
    private Date getStartDate() {
        Calendar startDateCalendar = Calendar.getInstance();
        startDateCalendar.set(2016, 3, 1);
        
        //What DOW is today?
        Calendar now = Calendar.getInstance();
        Integer dow = now.get(Calendar.DAY_OF_WEEK);
        if (startDateCalendar.get(Calendar.DAY_OF_WEEK) == dow) {
            return startDateCalendar.getTime();
        }
        for (int i = 0; i <= 6; i++ ) {
            startDateCalendar.add(Calendar.DATE, (-1));
            if (startDateCalendar.get(Calendar.DAY_OF_WEEK) == dow) {
                return startDateCalendar.getTime();
            }
        }
        return null;
    }

    public AsynchronousSummaryStatisticsInitializor getAsynchronousStatisticsInitializor() {
        return asynchronousStatisticsInitializor;
    }

    public void setAsynchronousStatisticsInitializor(AsynchronousSummaryStatisticsInitializor asynchronousStatisticsInitializor) {
        this.asynchronousStatisticsInitializor = asynchronousStatisticsInitializor;
    }

    public SummaryStatisticsDAO getSummaryStatisticsDAO() {
        return summaryStatisticsDAO;
    }

    public void setSummaryStatisticsDAO(SummaryStatisticsDAO summaryStatisticsDAO) {
        this.summaryStatisticsDAO = summaryStatisticsDAO;
    }
   
}
