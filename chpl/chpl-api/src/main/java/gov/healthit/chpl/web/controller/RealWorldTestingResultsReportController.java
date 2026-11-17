package gov.healthit.chpl.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.report.ReportDataManager;
import gov.healthit.chpl.report.realworldtesting.RealWorldTestingSummaryByAcbReport;
import gov.healthit.chpl.report.realworldtesting.RealWorldTestingSummaryByDeveloperReport;
import gov.healthit.chpl.util.LogMethodUsage;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Tag(name = "report-data/real-world-testing-results", description = "Allows retrieval of data used by Real World Testing Results reports.")
@RestController
@RequestMapping("/report-data/real-world-testing-results")
public class RealWorldTestingResultsReportController {

    private ReportDataManager reportDataManager;

    @Autowired
    public RealWorldTestingResultsReportController(ReportDataManager reportDataManager) {
        this.reportDataManager = reportDataManager;
    }

    @Operation(summary = "Retrieves the data used to generate the Real World Testing Results summary by ONC-ACB report.",
            description = "Retrieves the data used to generate the Real World Testing Results summary by ONC-ACB report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @LogMethodUsage
    @RequestMapping(value = "/summaries-by-acb", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<RealWorldTestingSummaryByAcbReport> getRealWorldTestingResultsSummariesByAcbReports() {
        return reportDataManager.getRealWorldTestingReportDataService().getRealWorldTestingResultsSummariesByAcbReports();
    }

    @Operation(summary = "Retrieves the data used to generate the Real World Testing Results summary by Developer report.",
            description = "Retrieves the data used to generate the Real World Testing Results summary by Developer report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @LogMethodUsage
    @RequestMapping(value = "/summaries-by-developer", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<RealWorldTestingSummaryByDeveloperReport> getRealWorldTestingResultsSummariesByDeveloperReports() {
        return reportDataManager.getRealWorldTestingReportDataService().getRealWorldTestingResultsSummariesByDeveloperReports();
    }
}
