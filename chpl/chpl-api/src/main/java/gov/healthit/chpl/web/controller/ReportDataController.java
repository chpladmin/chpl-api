package gov.healthit.chpl.web.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.report.ReportDataManager;
import gov.healthit.chpl.report.criteriamigrationreport.CriteriaMigrationReport;
import gov.healthit.chpl.report.surveillance.CapCounts;
import gov.healthit.chpl.report.surveillance.NonconformityCounts;
import gov.healthit.chpl.report.surveillance.SurveillanceActivityCounts;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.CertificationBodyStatistic;
import gov.healthit.chpl.search.domain.ListingSearchResult;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import gov.healthit.chpl.web.controller.results.ReportUrlResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "report-data", description = "Allows retrieval of data used by reports.")
@RestController
@RequestMapping("/report-data")
public class ReportDataController {
    private ReportDataManager reportDataManager;
    private Map<String, String> reportUrlsByReportName;

    @Autowired
    public ReportDataController(ReportDataManager reportDataManager, @Value("#{${reportUrls}}") Map<String, String> reportUrlsByReportName) {
        this.reportDataManager = reportDataManager;
        this.reportUrlsByReportName = reportUrlsByReportName;
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

    @Operation(summary = "Retrieves the data used to generate the Surveillance Activity Counts report.",
            description = "Retrieves the data used to generate the Surveillance Activity Counts report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/surveillance-activity-counts", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody SurveillanceActivityCounts getSurveillanceActivityCounts() {
        return reportDataManager.getSurveillanceActivityCounts();
    }

    @Operation(summary = "Retrieves the data used to generate the Open Surveillance Activity Counts by ACB report.",
            description = "Retrieves the data used to generate the Open Surveillance Activity Counts by ACB report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/open-surveillance-activity-counts-by-acb", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<CertificationBodyStatistic> getOpenSurveillanceActivityCountsByAcb() {
        return reportDataManager.getOpenSurveillanceActivityCountsByAcb();
    }

    @Operation(summary = "Retrieves the data used to generate the Listings with Open Surveillance report.",
            description = "Retrieves the data used to generate the Listings with Open Surveillance report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/open-surveillance-listings", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<ListingSearchResult> getListingsWithOpenSurveillance() {
        return reportDataManager.getListingsWithOpenSurveillance();
    }

    @Operation(summary = "Retrieves the data used to generate the Non-conformity Counts report.",
            description = "Retrieves the data used to generate the Non-conformity Counts report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/non-conformity-counts", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody NonconformityCounts getNonconformityCounts() {
        return reportDataManager.getNonconformityCounts();
    }

    @Operation(summary = "Retrieves the data used to generate the Open Non-conformity Counts by ACB report.",
            description = "Retrieves the data used to generate the Open Non-conformity Counts by ACB report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/open-non-conformity-counts-by-acb", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<CertificationBodyStatistic> getOpenNonconformityCountsByAcb() {
        return reportDataManager.getOpenNonconformityCountsByAcb();
    }

    @Operation(summary = "Retrieves the data used to generate the Listings with Open Non-conformity report.",
            description = "Retrieves the data used to generate the Listings with Open Non-conformity report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/open-non-conformity-listings", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<ListingSearchResult> getListingsWithOpenNonconformity() {
        return reportDataManager.getListingsWithOpenNonconformity();
    }

    @Operation(summary = "Retrieves the data used to generate the CAP Counts report.",
            description = "Retrieves the data used to generate the CAP Counts report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/cap-counts", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody CapCounts getCapCounts() {
        return reportDataManager.getCapCounts();
    }

    @Operation(summary = "Retrieves the data used to generate the Open CAP Counts by ACB report.",
            description = "Retrieves the data used to generate the Open CAP Counts by ACB report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/open-cap-counts-by-acb", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<CertificationBodyStatistic> getOpenCapCountsByAcb() {
        return reportDataManager.getOpenCapCountsByAcb();
    }

    @Operation(summary = "Retrieves the data used to generate the Closed CAP Counts by ACB report.",
            description = "Retrieves the data used to generate the Closed CAP Counts by ACB report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/Closed-cap-counts-by-acb", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<CertificationBodyStatistic> getClosedCapCountsByAcb() {
        return reportDataManager.getClosedCapCountsByAcb();
    }

}
