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

import gov.healthit.chpl.developer.search.DeveloperSearchRequest;
import gov.healthit.chpl.developer.search.DeveloperSearchResult;
import gov.healthit.chpl.developer.search.DeveloperSearchService;
import gov.healthit.chpl.report.ReportDataManager;
import gov.healthit.chpl.report.criteriamigrationreport.CriteriaMigrationReport;
import gov.healthit.chpl.report.developer.UniqueDeveloperCount;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.CertificationBodyStatistic;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import gov.healthit.chpl.web.controller.results.ReportUrlResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Tag(name = "report-data", description = "Allows retrieval of data used by reports.")
@RestController
@RequestMapping("/report-data")
public class ReportDataController {
    private ReportDataManager reportDataManager;
    private DeveloperSearchService developerSearchService;
    private Map<String, String> reportUrlsByReportName;

    @Autowired
    public ReportDataController(ReportDataManager reportDataManager, DeveloperSearchService developerSearchService,
            @Value("#{${reportUrls}}") Map<String, String> reportUrlsByReportName) {
        this.reportDataManager = reportDataManager;
        this.developerSearchService = developerSearchService;
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

    @Operation(summary = "Retrieves the data used to generate the Unique Developer Count report.",
            description = "Retrieves the data used to generate the Unique Developer Count report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/unique-developer-count", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody UniqueDeveloperCount getUniqueDeveloperCount() {
        return reportDataManager.getUniqueDeveloperCount();
    }

    @Operation(summary = "Retrieves the data used to generate the Developer Counts with Active Listings by ACB report.",
            description = "Retrieves the data used to generate the Developer Counts with Active Listings by ACB report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/developer-count-with-active-listings-by-acb", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<CertificationBodyStatistic> getDeveloperCountsWithActiveListingsByAcb() {
        return reportDataManager.getDeveloperCountsWithActiveListingsByAcb();
    }

    @Operation(summary = "Retrieves the data used to generate the Developers with Active Listings by ACB report.",
            description = "Retrieves the data used to generate the Developers with Active Listings by ACB report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/developers-with-active-listings-by-acb", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<DeveloperSearchResult> getDevelopersWithActiveListingsByAcb() {
        return developerSearchService.getAllPagesOfSearchResults(
                DeveloperSearchRequest.builder().build(),
                LOGGER);
    }

    @Operation(summary = "Retrieves the data used to generate the Developer Counts with Withdrawn Listings by ACB report.",
            description = "Retrieves the data used to generate the Developer Counts with Withdrawn Listings by ACB report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/developer-count-with-withdrawn-listings-by-acb", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<CertificationBodyStatistic> getDeveloperCountsWithWithdrawnListingsByAcb() {
        return reportDataManager.getDeveloperCountsWithWithdrawnListingsByAcb();
    }

    @Operation(summary = "Retrieves the data used to generate the Developers with Withdrawn Listings by ACB report.",
            description = "Retrieves the data used to generate the Developers with Withdrawn Listings by ACB report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/developers-with-withdrawn-listings-by-acb", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<DeveloperSearchResult> getDevelopersWithWithdrawnListingsByAcb() {
        return reportDataManager.getDevelopersWithWithdrawnListingsByAcb();
    }

    @Operation(summary = "Retrieves the data used to generate the Developer Counts with Suspended Listings by ACB report.",
            description = "Retrieves the data used to generate the Developer Counts with Suspended Listings by ACB report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/developer-count-with-suspended-listings-by-acb", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<CertificationBodyStatistic> getDeveloperCountsWithSuspendedListingsByAcb() {
        return reportDataManager.getDeveloperCountsWithSuspendedListingsByAcb();
    }

    @Operation(summary = "Retrieves the data used to generate the Developers with Suspended Listings by ACB report.",
            description = "Retrieves the data used to generate the Developers with Suspended Listings by ACB report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/developers-with-suspended-listings-by-acb", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<DeveloperSearchResult> getDevelopersWithSuspendedListingsByAcb() {
        return reportDataManager.getDevelopersWithSuspendedListingsByAcb();
    }

}
