package gov.healthit.chpl.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.report.ReportDataManager;
import gov.healthit.chpl.report.realworldtesting.RealWorldTestingSummaryByAcbReportPreHti5;
import gov.healthit.chpl.report.realworldtesting.RealWorldTestingSummaryByDeveloperReportPreHti5;
import gov.healthit.chpl.util.LogMethodUsage;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import gov.healthit.chpl.web.controller.annotation.DeprecatedApi;
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

    @Deprecated
    @DeprecatedApi(friendlyUrl = "/report-data/real-world-testing/plans", httpMethod = "GET",
        message = "This endpoint will be removed, and there is no replacement.",
        removalDate = "2027-01-01")
    @Operation(summary = "Retrieves the data used to generate the Real World Testing Plans summary by ONC-ACB report.",
            description = "Retrieves the data used to generate the Real World Testing Plans summary by ONC-ACB report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @LogMethodUsage
    @RequestMapping(value = "/plans", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<Object> getRealWorldTestingPlanSummaryByAcbReports() {
        return List.of();
    }

    @Deprecated
    @DeprecatedApi(friendlyUrl = "/report-data/real-world-testing/results", httpMethod = "GET",
        message = "This endpoint will be removed. Please use /report-data/real-world-testing-results/summary-by-acb",
        removalDate = "2027-01-01")
    @Operation(summary = "Retrieves the data used to generate the Real World Testing Results summary by ONC-ACB report.",
            description = "Retrieves the data used to generate the Real World Testing Results summary by ONC-ACB report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @LogMethodUsage
    @RequestMapping(value = "/results", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<RealWorldTestingSummaryByAcbReportPreHti5> getRealWorldTestingResultsSummaryByAcbReports() {
        return reportDataManager.getRealWorldTestingReportDataService().getRealWorldTestingResultsSummaryByAcbReportsPreHti5();
    }

    @Deprecated
    @DeprecatedApi(friendlyUrl = "/report-data/real-world-testing/results-summary-by-developer", httpMethod = "GET",
        message = "This endpoint will be removed. Please use /report-data/real-world-testing-results/summary-by-developer",
        removalDate = "2027-01-01")
    @Operation(summary = "Retrieves the data used to generate the Real World Testing Results summary by Developer report.",
            description = "Retrieves the data used to generate the Real World Testing Results summary by Developer report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @LogMethodUsage
    @RequestMapping(value = "/results-summary-by-developer", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<RealWorldTestingSummaryByDeveloperReportPreHti5> getRealWorldTestingResultsSummaryByDeveloperReports() {
        return reportDataManager.getRealWorldTestingReportDataService().getRealWorldTestingResultsSummaryByDeveloperReportsPreHti5();
    }
}
