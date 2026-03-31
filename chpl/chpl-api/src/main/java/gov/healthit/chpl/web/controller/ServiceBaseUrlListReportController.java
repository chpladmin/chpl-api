package gov.healthit.chpl.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.report.ReportDataManager;
import gov.healthit.chpl.report.servicebaseurllistreport.UrlUptimeMonitorEx;
import gov.healthit.chpl.report.servicebaseurllistreport.UrlUptimeMonitorSummary;
import gov.healthit.chpl.util.LogMethodUsage;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Tag(name = "report-data/service-base-url-list", description = "Allows retrieval of data used by SBUL reports.")
@RestController
@RequestMapping("/report-data/service-base-url-list")
public class ServiceBaseUrlListReportController {
    private ReportDataManager reportDataManager;

    @Autowired
    public ServiceBaseUrlListReportController(ReportDataManager reportDataManager) {
        this.reportDataManager = reportDataManager;
    }

    @Operation(summary = "Retrieves the data used to generate the Service Base Url List report.",
            description = "Retrieves the data used to generate the Service Base Url List report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
    })
    @LogMethodUsage
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<UrlUptimeMonitorEx> getUrlUptimeMonitors() {
        return reportDataManager.getUrlUptimeMonitors();
    }

    @Operation(summary = "Retrieves the data used to generate the Service Base Url List uptime summaries by Developer and URL.",
            description = "Retrieves the data used to generate the Service Base Url List uptime summaries by Developer and URL.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
    })
    @LogMethodUsage
    @RequestMapping(value = "/summary", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<UrlUptimeMonitorSummary> getUrlUptimeMonitorsSummaries(
            @RequestParam(name = "numDaysAgo", required = false, defaultValue = "30") Integer numDaysAgo) {
        return reportDataManager.getUrlUptimeMonitorsSummaries(numDaysAgo);
    }

}
