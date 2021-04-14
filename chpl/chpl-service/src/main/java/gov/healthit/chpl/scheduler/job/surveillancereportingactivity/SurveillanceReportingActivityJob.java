package gov.healthit.chpl.scheduler.job.surveillancereportingactivity;

import java.time.LocalDate;
import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import lombok.extern.log4j.Log4j2;

@Log4j2()
public class SurveillanceReportingActivityJob implements Job {
    @Autowired
    private SurveillanceActivityReportDataGatherer dataGatherer;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        LOGGER.info("********* Starting the Surveillance Reporting Activity job. *********");
        try {
            LocalDate startDate = LocalDate.of(2020, 1, 1);
            LocalDate endDate = LocalDate.of(2020, 3, 31);
            List<CSVRecord> records = dataGatherer.getData(startDate, endDate);

            LOGGER.info("Count of found Surveillance Rows: " + records.size());
            //Temp output
            records.stream()
                    .forEach(rec -> LOGGER.info(rec.toString()));

            SurveillanceActivityReportWorkbook workbook = new SurveillanceActivityReportWorkbook();
            workbook.generateWorkbook(records);
        } catch (Exception e) {
            LOGGER.catching(e);
        }
        LOGGER.info("********* Completed the Surveillance Reporting Activity job. *********");

    }
}
