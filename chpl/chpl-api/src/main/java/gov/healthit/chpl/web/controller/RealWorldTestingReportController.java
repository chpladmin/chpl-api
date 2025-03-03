package gov.healthit.chpl.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.report.ReportDataManager;
import gov.healthit.chpl.report.realworldtesting.RealWorldTestingSummaryReport;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Tag(name = "report-data/real-world-testing", description = "Allows retrieval of data used by Real World Testing report.")
@RestController
@RequestMapping("/report-data/real-world-testing")
public class RealWorldTestingReportController {

    private ReportDataManager reportDataManager;

    @Autowired
    public RealWorldTestingReportController(ReportDataManager reportDataManager) {
        this.reportDataManager = reportDataManager;
    }

    @Operation(summary = "Retrieves the data used to generate the Real World Testing Plans report.",
            description = "Retrieves the data used to generate the Real World Testing Plans report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/plans", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<RealWorldTestingSummaryReport> getRealWorldTestingPlanReports() {
        return reportDataManager.getRealWorldTestingReportDataService().getRealWorldTestingPlanSummaryReports();
    }

    @Operation(summary = "Retrieves the data used to generate the Real World Testing Results report.",
            description = "Retrieves the data used to generate the Real World Testing Results report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/results", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<RealWorldTestingSummaryReport> getRealWorldTestingResultsReports() {
        return reportDataManager.getRealWorldTestingReportDataService().getRealWorldTestingResultsSummaryReports();
    }
}
