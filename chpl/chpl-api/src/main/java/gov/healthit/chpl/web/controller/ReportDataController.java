package gov.healthit.chpl.web.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.domain.statistics.CuresCriterionChartStatistic;
import gov.healthit.chpl.report.ReportDataManager;
import gov.healthit.chpl.report.criteriamigrationreport.CriteriaMigrationReport;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import gov.healthit.chpl.web.controller.results.ReportUrlResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "report-data", description = "Allows retrieval of data used by reports.")
@RestController
@RequestMapping("/report-data")
public class ReportDataController {
    private Environment environment;
    private ReportDataManager reportDataManager;
    Map<String, String> reportUrlsByReportName;

    @Autowired
    public ReportDataController(ReportDataManager reportDataManager, Environment environment,
            @Value("#{${reportUrls}}") Map<String, String> reportUrlsByReportName) {
        this.reportDataManager = reportDataManager;
        this.environment = environment;
        this.reportUrlsByReportName = reportUrlsByReportName;
    }

    @Operation(summary = "Retrieves the data used to generate the Cures Update Report.",
            description = "Retrieves the data used to generate the Cures Update Report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/cures-update-report", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<CuresCriterionChartStatistic> getCuresUpdateReportData() {
        return reportDataManager.getCuresUpdateReportData();
    }

    @Operation(summary = "Retrieves the data used to generate the HTI-1 Criteria Migration Report.",
            description = "Retrieves the data used to generate the HTI-1 Criteria Migration Report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/hti-1-criteria-migration-report", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody CriteriaMigrationReport getHti1CriteriaMigrationReport() {
        return reportDataManager.getHti1CriteriaMigrationReport();
    }

    @Operation(summary = "Retrieves the URL for a Power BI report.",
            description = "Retrieves the URL for a Power BI report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/{reportName}/url", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody ReportUrlResult getReportUrl(@PathVariable("reportName") String reportName) {

        String reportUrl = "";
        if (reportUrlsByReportName.containsKey(reportName)) {
            reportUrl = reportUrlsByReportName.get(reportName);
        }
        return ReportUrlResult.builder()
                .reportUrl(reportUrl)
                .build();
    }

}
