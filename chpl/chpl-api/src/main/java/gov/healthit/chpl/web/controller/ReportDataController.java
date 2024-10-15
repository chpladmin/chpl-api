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
import gov.healthit.chpl.report.criteriaattribute.TestToolListingReport;
import gov.healthit.chpl.report.criteriaattribute.TestToolReport;
import gov.healthit.chpl.report.criteriamigrationreport.CriteriaMigrationReport;
import gov.healthit.chpl.report.developer.UniqueDeveloperCount;
import gov.healthit.chpl.report.listing.UniqueListingCount;
import gov.healthit.chpl.report.product.ProductByAcb;
import gov.healthit.chpl.report.product.UniqueProductCount;
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
    @RequestMapping(value = "/closed-cap-counts-by-acb", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<CertificationBodyStatistic> getClosedCapCountsByAcb() {
        return reportDataManager.getClosedCapCountsByAcb();
    }

    @Operation(summary = "Retrieves the data used to generate the Listings with Open CAP report.",
            description = "Retrieves the data used to generate the Listings with Open CAP report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/open-cap-listings", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<ListingSearchResult> getListingsWithOpenCap() {
        return reportDataManager.getListingsWithOpenCap();
    }

    @Operation(summary = "Retrieves the data used to generate the Listings with Closed CAP report.",
            description = "Retrieves the data used to generate the Listings with Closed CAP report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/closed-cap-listings", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<ListingSearchResult> getListingsWithClosedCap() {
        return reportDataManager.getListingsWithClosedCap();
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

    @Operation(summary = "Retrieves the data used to generate the Active Products by ACB report.",
            description = "Retrieves the data used to generate the Active Products by ACB report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/active-product-counts-by-acb", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<CertificationBodyStatistic> getActiveProdutCountsByAcb() {
        return reportDataManager.getActiveProdutCountsByAcb();
    }

    @Operation(summary = "Retrieves the data used to generate the Withdrawn Products by ACB report.",
            description = "Retrieves the data used to generate the Withdrawn Products by ACB report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/withdrawn-product-counts-by-acb", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<CertificationBodyStatistic> getWithdrawnProdutCountsByAcb() {
        return reportDataManager.getWithdrawnProdutCountsByAcb();
    }

    @Operation(summary = "Retrieves the data used to generate the Suspended Products by ACB report.",
            description = "Retrieves the data used to generate the Suspended Products by ACB report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/suspended-product-counts-by-acb", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<CertificationBodyStatistic> getSuspendedProdutCountsByAcb() {
        return reportDataManager.getSuspendedProdutCountsByAcb();
    }

    @Operation(summary = "Retrieves the data used to generate the Active Products by ACB report",
            description = "Retrieves the data used to generate the Active Products by ACB report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/active-products", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<ProductByAcb> getActiveProducts() {
        return reportDataManager.getActiveProductsAndAcb();
    }

    @Operation(summary = "Retrieves the data used to generate the Suspended Products by ACB report",
            description = "Retrieves the data used to generate the Suspended Products by ACB report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/suspended-products", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<ProductByAcb> getSuspendedProducts() {
        return reportDataManager.getSuspendedProductsAndAcb();
    }

    @Operation(summary = "Retrieves the data used to generate the Withdrawn Products by ACB report",
            description = "Retrieves the data used to generate the Withdrawn Products by ACB report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/withdrawn-products", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<ProductByAcb> getWithdrawnProducts() {
        return reportDataManager.getWithdrawnProductsAndAcb();
    }

    @Operation(summary = "Retrieves the data used to generate the Product Count report.",
            description = "Retrieves the data used to generate the Product Count report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/product-count", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody UniqueProductCount getUniqueProductCount() {
        return reportDataManager.getUniqueProductCount();
    }

    @Operation(summary = "Retrieves the data used to generate the Active Listings by ACB report.",
            description = "Retrieves the data used to generate the Active Listings  by ACB report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/active-listing-counts-by-acb", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<CertificationBodyStatistic> getActiveListingsCountsByAcb() {
        return reportDataManager.getActiveListingCountsByAcb();
    }

    @Operation(summary = "Retrieves the data used to generate the Withdrawn Listings by ACB report.",
            description = "Retrieves the data used to generate the Withdrawn Listings by ACB report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/withdrawn-listing-counts-by-acb", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<CertificationBodyStatistic> getWithdrawnListingCountsByAcb() {
        return reportDataManager.getWithdrawnListingCountsByAcb();
    }

    @Operation(summary = "Retrieves the data used to generate the Suspended Listings by ACB report.",
            description = "Retrieves the data used to generate the Suspended Listings by ACB report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/suspended-listing-counts-by-acb", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<CertificationBodyStatistic> getSuspendedListingCountsByAcb() {
        return reportDataManager.getSuspendedListingCountsByAcb();
    }

    @Operation(summary = "Retrieves the data used to generate the Active Listing by ACB report",
            description = "Retrieves the data used to generate the Active Listing by ACB report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/active-listings", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<ListingSearchResult> getActiveListings() {
        return reportDataManager.getActiveListings();
    }

    @Operation(summary = "Retrieves the data used to generate the Suspended Listings by ACB report",
            description = "Retrieves the data used to generate the Suspended Listings by ACB report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/suspended-listings", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<ListingSearchResult> getSuspendedListings() {
        return reportDataManager.getSuspendedListings();
    }

    @Operation(summary = "Retrieves the data used to generate the Withdrawn Listings by ACB report",
            description = "Retrieves the data used to generate the Withdrawn Listings by ACB report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/withdrawn-listings", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<ListingSearchResult> getWithdrawnListings() {
        return reportDataManager.getWithdrawnListings();
    }

    @Operation(summary = "Retrieves the data used to generate the Listing Count report.",
            description = "Retrieves the data used to generate the Listing Count report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/listing-count", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody UniqueListingCount getUniqueListingCount() {
        return reportDataManager.getUniqueListingCount();
    }

    @Operation(summary = "Retrieves the data used to generate the Test Tool Criteria Attribute Summary report.",
            description = "Retrieves the data used to generate the Test Tool Criteria Attribute Summary report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/test-tools", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<TestToolReport> getTestToolReports() {
        return reportDataManager.getTestToolReports();
    }

    @Operation(summary = "Retrieves the data used to generate the Test Tool Criteria Attribute Listing report.",
            description = "Retrieves the data used to generate the Test Tool Criteria Attribute Listing report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/test-tools-listing", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<TestToolListingReport> getTestToolListingReports() {
        return reportDataManager.getTestToolListingReports();
    }

}
