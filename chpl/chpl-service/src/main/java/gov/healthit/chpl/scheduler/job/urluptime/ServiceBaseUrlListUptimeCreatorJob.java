package gov.healthit.chpl.scheduler.job.urluptime;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.scheduler.job.QuartzJob;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic =  "serviceBaseUrlListUptimeCreatorJobLogger")
public class ServiceBaseUrlListUptimeCreatorJob extends QuartzJob {

    @Autowired
    private DatadogUrlUptimeSynchonizer datadogChplSynchonizer;

    @Value("${datadog.syntheticsTest.readOnly}")
    private Boolean datadogReadOnly;

    @Autowired
    private ServiceBaseUrlListUptimeCalculator serviceBaseUrlListUptimeCalculator;

    @Autowired
    private ServiceBaseUrlListUptimeXlsxWriter xslxWriter;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Service Base Url List Uptime Creator job *********");
        if (!datadogReadOnly) {
            try {
                datadogChplSynchonizer.synchronize();
            } catch (Exception e) {
                e.printStackTrace();
                LOGGER.error(e);
            }
        } else {
            LOGGER.info("Not synchronizing or gathering Service Base URL List data based on configuration");
        }

        LOGGER.info("Calculating data for downloadable Service Base URL List Uptime Report file");
        List<ServiceBaseUrlListUptimeReport> reportRows = serviceBaseUrlListUptimeCalculator.calculateRowsForReport();
        if (!CollectionUtils.isEmpty(reportRows)) {
            LOGGER.info("Writing data to file");
            xslxWriter.writeWorkbookAsFile(reportRows);
            LOGGER.info("Completed writing data to file");
        } else {
            LOGGER.error("No Service Base URL List uptime reports were found and the download file will not be written.");
        }

        LOGGER.info("********* Completed the Service Base Url List Uptime Creator job *********");
    }
}
