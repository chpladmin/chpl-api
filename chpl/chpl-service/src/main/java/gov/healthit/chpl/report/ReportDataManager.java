package gov.healthit.chpl.report;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.developer.search.DeveloperSearchResult;
import gov.healthit.chpl.report.criteriamigrationreport.CriteriaMigrationReport;
import gov.healthit.chpl.report.criteriamigrationreport.CriteriaMigrationReportService;
import gov.healthit.chpl.report.developer.DeveloperReportsService;
import gov.healthit.chpl.report.developer.UniqueDeveloperCount;
import gov.healthit.chpl.report.listing.ListingReportsService;
import gov.healthit.chpl.report.listing.UniqueListingCount;
import gov.healthit.chpl.report.product.ProductByAcb;
import gov.healthit.chpl.report.product.ProductReportsService;
import gov.healthit.chpl.report.product.UniqueProductCount;
import gov.healthit.chpl.report.surveillance.CapCounts;
import gov.healthit.chpl.report.surveillance.NonconformityCounts;
import gov.healthit.chpl.report.surveillance.SurveillanceActivityCounts;
import gov.healthit.chpl.report.surveillance.SurveillanceReportsService;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.CertificationBodyStatistic;
import gov.healthit.chpl.search.domain.ListingSearchResult;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class ReportDataManager {

    private CriteriaMigrationReportService criteriaMigrationReportService;
    private SurveillanceReportsService surveillanceReportsService;
    private DeveloperReportsService developerReportsService;
    private ProductReportsService productReportsService;
    private ListingReportsService listingReportsService;

    @Autowired
    public ReportDataManager(CriteriaMigrationReportService criteriaMigrationReportService, DeveloperReportsService developerReportsService,
            SurveillanceReportsService surveillanceReportsService, ProductReportsService productReportsService, ListingReportsService listingReportsService) {
        this.criteriaMigrationReportService = criteriaMigrationReportService;
        this.developerReportsService = developerReportsService;
        this.surveillanceReportsService = surveillanceReportsService;
        this.productReportsService = productReportsService;
        this.listingReportsService = listingReportsService;
    }

    public CriteriaMigrationReport getHti1CriteriaMigrationReport() {
        return criteriaMigrationReportService.getReport(CriteriaMigrationReportService.HTI1_REPORT_ID);
    }

    public SurveillanceActivityCounts getSurveillanceActivityCounts() {
        return surveillanceReportsService.getSurveiilanceActivityCounts();
    }

    public List<CertificationBodyStatistic> getOpenSurveillanceActivityCountsByAcb() {
        return surveillanceReportsService.getOpenSurveillanceActivityCountsByAcb();
    }

    public List<ListingSearchResult> getListingsWithOpenSurveillance() {
        return surveillanceReportsService.getListingsWithOpenSurveillance();
    }

    public NonconformityCounts getNonconformityCounts() {
        return surveillanceReportsService.getNonconformityCounts();
    }

    public List<CertificationBodyStatistic> getOpenNonconformityCountsByAcb() {
        return surveillanceReportsService.getOpenNonconformityCountsByAcb();
    }

    public List<ListingSearchResult> getListingsWithOpenNonconformity() {
        return surveillanceReportsService.getListingsWithOpenNonconformity();
    }

    public List<CertificationBodyStatistic> getOpenCapCountsByAcb() {
        return surveillanceReportsService.getOpenCapCountsByAcb();
    }

    public List<CertificationBodyStatistic> getClosedCapCountsByAcb() {
        return surveillanceReportsService.getClosedCapCountsByAcb();
    }

    public List<ListingSearchResult> getListingsWithOpenCap() {
        return surveillanceReportsService.getListingsWithOpenCap();
    }

    public List<ListingSearchResult> getListingsWithClosedCap() {
        return surveillanceReportsService.getListingsWithClosedCap();
    }

    public CapCounts getCapCounts() {
        return surveillanceReportsService.getCapCounts();
    }

    public UniqueDeveloperCount getUniqueDeveloperCount() {
        return developerReportsService.getUniqueDeveloperCount();
    }

    public List<CertificationBodyStatistic> getDeveloperCountsWithActiveListingsByAcb() {
        return developerReportsService.getDeveloperCountsWithActiveListingsByAcb();
    }

    public List<CertificationBodyStatistic> getDeveloperCountsWithWithdrawnListingsByAcb() {
        return developerReportsService.getDeveloperCountsWithWithdrawnListingsByAcb();
    }

    public List<DeveloperSearchResult> getDevelopersWithWithdrawnListingsByAcb() {
        return developerReportsService.getDevelopersWithWithdrawnListingsByAcb();
    }

    public List<CertificationBodyStatistic> getDeveloperCountsWithSuspendedListingsByAcb() {
        return developerReportsService.getDeveloperCountsWithSuspendedListingsByAcb();
    }

    public List<DeveloperSearchResult> getDevelopersWithSuspendedListingsByAcb() {
        return developerReportsService.getDevelopersWithSuspendedListingsByAcb();
    }

    public UniqueProductCount getUniqueProductCount() {
        return productReportsService.getUniqueProductCount();
    }

    public List<CertificationBodyStatistic> getActiveProdutCountsByAcb() {
        return productReportsService.getActiveProductCountsByAcb();
    }

    public List<CertificationBodyStatistic> getWithdrawnProdutCountsByAcb() {
        return productReportsService.getWithdrawnProductCountsByAcb();
    }

    public List<CertificationBodyStatistic> getSuspendedProdutCountsByAcb() {
        return productReportsService.getSuspendedProductCountsByAcb();
    }

    public List<ProductByAcb> getActiveProductsAndAcb() {
        return productReportsService.getActiveProductsAndAcb();
    }

    public List<ProductByAcb> getWithdrawnProductsAndAcb() {
        return productReportsService.getWithdrawnProductsAndAcb();
    }

    public List<ProductByAcb> getSuspendedProductsAndAcb() {
        return productReportsService.getSuspendedProductsAndAcb();
    }

    public UniqueListingCount getUniqueListingCount() {
        return listingReportsService.getUniqueListingCount();
    }

    public List<CertificationBodyStatistic> getActiveListingCountsByAcb() {
        return listingReportsService.getActiveListingCountsByAcb();
    }

    public List<CertificationBodyStatistic> getWithdrawnListingCountsByAcb() {
        return listingReportsService.getWithdrawnListingCountsByAcb();
    }

    public List<CertificationBodyStatistic> getSuspendedListingCountsByAcb() {
        return listingReportsService.getSuspendedListingCountsByAcb();
    }

    public List<ListingSearchResult> getActiveListings() {
        return listingReportsService.getActiveListings();
    }

    public List<ListingSearchResult> getWithdrawnListings() {
        return listingReportsService.getWithdrawnListings();
    }

    public List<ListingSearchResult> getSuspendedListings() {
        return listingReportsService.getSuspendedListings();
    }

}
