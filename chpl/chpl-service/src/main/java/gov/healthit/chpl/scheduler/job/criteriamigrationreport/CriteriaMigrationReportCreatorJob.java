package gov.healthit.chpl.scheduler.job.criteriamigrationreport;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.report.criteriamigrationreport.CriteriaMigrationReportService;
import gov.healthit.chpl.scheduler.job.QuartzJob;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "criteriaMigrationReportCreatorJobLogger")
public class CriteriaMigrationReportCreatorJob extends QuartzJob {

    @Autowired
    private CriteriaMigrationReportService criteriaMigrationReportService;

    @Override
    public void execute(JobExecutionContext arg0) throws JobExecutionException {
        LOGGER.info("*****Criteria Migration Report Creator Job is starting.*****");
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        try {
            criteriaMigrationReportService.gatherDataForReport(LOGGER);
        } catch (Exception e) {
            LOGGER.error("Error generating data for Criteria Migration Report.", e);
        }
        LOGGER.info("*****Criteria Migration Report Creator Job is complete.*****");
    }

}
