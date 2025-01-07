package gov.healthit.chpl.report;

import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.developer.search.DeveloperSearchResult;
import gov.healthit.chpl.report.attestation.AttestationReportService;
import gov.healthit.chpl.report.criteriaattribute.StandardListingReport;
import gov.healthit.chpl.report.criteriaattribute.StandardReport;
import gov.healthit.chpl.report.criteriaattribute.StandardReportService;
import gov.healthit.chpl.report.criteriaattribute.TestToolListingReport;
import gov.healthit.chpl.report.criteriaattribute.TestToolReport;
import gov.healthit.chpl.report.criteriaattribute.TestToolReportService;
import gov.healthit.chpl.report.criteriamigrationreport.CriteriaMigrationReportDenormalized;
import gov.healthit.chpl.report.criteriamigrationreport.CriteriaMigrationReportService;
import gov.healthit.chpl.report.developer.DeveloperReportsService;
import gov.healthit.chpl.report.developer.UniqueDeveloperCount;
import gov.healthit.chpl.report.directreview.DirectReviewCounts;
import gov.healthit.chpl.report.directreview.DirectReviewReportsService;
import gov.healthit.chpl.report.listing.ListingReportsService;
import gov.healthit.chpl.report.listing.UniqueListingCount;
import gov.healthit.chpl.report.product.ProductByAcb;
import gov.healthit.chpl.report.product.ProductReportsService;
import gov.healthit.chpl.report.product.UniqueProductCount;
import gov.healthit.chpl.report.realworldtesting.RealWorldTestingReportDataService;
import gov.healthit.chpl.report.servicebaseurllistreport.ServiceBaseUrlListReportService;
import gov.healthit.chpl.report.servicebaseurllistreport.UrlUptimeMonitorEx;
import gov.healthit.chpl.report.surveillance.CapCounts;
import gov.healthit.chpl.report.surveillance.NonconformityCounts;
import gov.healthit.chpl.report.surveillance.SurveillanceActivityCounts;
import gov.healthit.chpl.report.surveillance.SurveillanceReportsService;
import gov.healthit.chpl.scheduler.job.report.attestation.AttestationReport;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.CertificationBodyStatistic;
import gov.healthit.chpl.search.domain.ListingSearchResult;
import lombok.Synchronized;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class ReportDataManager {
    private final Object lock = new Object();

    private CriteriaMigrationReportService criteriaMigrationReportService;
    private SurveillanceReportsService surveillanceReportsService;
    private DeveloperReportsService developerReportsService;
    private ProductReportsService productReportsService;
    private ListingReportsService listingReportsService;
    private TestToolReportService testToolReportService;
    private ServiceBaseUrlListReportService serviceBaseUrlListReportService;
    private StandardReportService standardReportService;
    private DirectReviewReportsService directReviewReportsService;
    private AttestationReportService attestationReportService;
    private ReportMetadataDAO reportMetadataDAO;
    private RealWorldTestingReportDataService realWorldTestingReportDataService;


    @Autowired
    public ReportDataManager(CriteriaMigrationReportService criteriaMigrationReportService,
            DeveloperReportsService developerReportsService,
            SurveillanceReportsService surveillanceReportsService,
            ProductReportsService productReportsService,
            ListingReportsService listingReportsService,
            TestToolReportService testToolReportService,
            DirectReviewReportsService directReviewReportsService,
            AttestationReportService attestationReportService,
            ReportMetadataDAO reportMetadataDAO,
            ServiceBaseUrlListReportService serviceBaseUrlListReportService,
            StandardReportService standardReportService,
            RealWorldTestingReportDataService realWorldTestingReportDataService) {

        this.criteriaMigrationReportService = criteriaMigrationReportService;
        this.developerReportsService = developerReportsService;
        this.surveillanceReportsService = surveillanceReportsService;
        this.productReportsService = productReportsService;
        this.listingReportsService = listingReportsService;
        this.testToolReportService = testToolReportService;
        this.serviceBaseUrlListReportService = serviceBaseUrlListReportService;
        this.standardReportService = standardReportService;
        this.directReviewReportsService = directReviewReportsService;
        this.attestationReportService = attestationReportService;
        this.reportMetadataDAO = reportMetadataDAO;
        this.realWorldTestingReportDataService = realWorldTestingReportDataService;
    }

    public List<ReportMetadata> getReportMetadataByReportGroup(String reportGroup) {
        return reportMetadataDAO.getReportMetadataByReportGroup(reportGroup).stream()
                .sorted(Comparator.comparing(ReportMetadata::getDisplayOrder))
                .toList();
    }

    public ReportMetadata getReportMetadata(String reportKey) {
        return reportMetadataDAO.getReportMetadata(reportKey);
    }

    @Synchronized("lock")
    public List<CriteriaMigrationReportDenormalized> getHti1CriteriaMigrationReport() {
        return criteriaMigrationReportService.getHtiReportData(CriteriaMigrationReportService.HTI1_REPORT_ID);
    }

    @Synchronized("lock")
    public SurveillanceActivityCounts getSurveillanceActivityCounts() {
        return surveillanceReportsService.getSurveiilanceActivityCounts();
    }

    @Synchronized("lock")
    public List<CertificationBodyStatistic> getOpenSurveillanceActivityCountsByAcb() {
        return surveillanceReportsService.getOpenSurveillanceActivityCountsByAcb();
    }

    @Synchronized("lock")
    public List<ListingSearchResult> getListingsWithOpenSurveillance() {
        return surveillanceReportsService.getListingsWithOpenSurveillance();
    }

    @Synchronized("lock")
    public NonconformityCounts getNonconformityCounts() {
        return surveillanceReportsService.getNonconformityCounts();
    }

    @Synchronized("lock")
    public List<CertificationBodyStatistic> getOpenNonconformityCountsByAcb() {
        return surveillanceReportsService.getOpenNonconformityCountsByAcb();
    }

    @Synchronized("lock")
    public List<ListingSearchResult> getListingsWithOpenNonconformity() {
        return surveillanceReportsService.getListingsWithOpenNonconformity();
    }

    @Synchronized("lock")
    public List<CertificationBodyStatistic> getOpenCapCountsByAcb() {
        return surveillanceReportsService.getOpenCapCountsByAcb();
    }

    @Synchronized("lock")
    public List<CertificationBodyStatistic> getClosedCapCountsByAcb() {
        return surveillanceReportsService.getClosedCapCountsByAcb();
    }

    @Synchronized("lock")
    public List<ListingSearchResult> getListingsWithOpenCap() {
        return surveillanceReportsService.getListingsWithOpenCap();
    }

    @Synchronized("lock")
    public List<ListingSearchResult> getListingsWithClosedCap() {
        return surveillanceReportsService.getListingsWithClosedCap();
    }

    @Synchronized("lock")
    public CapCounts getCapCounts() {
        return surveillanceReportsService.getCapCounts();
    }

    @Synchronized("lock")
    public UniqueDeveloperCount getUniqueDeveloperCount() {
        return developerReportsService.getUniqueDeveloperCount();
    }

    @Synchronized("lock")
    public List<CertificationBodyStatistic> getDeveloperCountsWithActiveListingsByAcb() {
        return developerReportsService.getDeveloperCountsWithActiveListingsByAcb();
    }

    @Synchronized("lock")
    public List<CertificationBodyStatistic> getDeveloperCountsWithWithdrawnListingsByAcb() {
        return developerReportsService.getDeveloperCountsWithWithdrawnListingsByAcb();
    }

    @Synchronized("lock")
    public List<DeveloperSearchResult> getDevelopersWithWithdrawnListingsByAcb() {
        return developerReportsService.getDevelopersWithWithdrawnListingsByAcb();
    }

    @Synchronized("lock")
    public List<CertificationBodyStatistic> getDeveloperCountsWithSuspendedListingsByAcb() {
        return developerReportsService.getDeveloperCountsWithSuspendedListingsByAcb();
    }

    @Synchronized("lock")
    public List<DeveloperSearchResult> getDevelopersWithSuspendedListingsByAcb() {
        return developerReportsService.getDevelopersWithSuspendedListingsByAcb();
    }

    @Synchronized("lock")
    public UniqueProductCount getUniqueProductCount() {
        return productReportsService.getUniqueProductCount();
    }

    @Synchronized("lock")
    public List<CertificationBodyStatistic> getActiveProdutCountsByAcb() {
        return productReportsService.getActiveProductCountsByAcb();
    }

    @Synchronized("lock")
    public List<CertificationBodyStatistic> getWithdrawnProdutCountsByAcb() {
        return productReportsService.getWithdrawnProductCountsByAcb();
    }

    @Synchronized("lock")
    public List<CertificationBodyStatistic> getSuspendedProdutCountsByAcb() {
        return productReportsService.getSuspendedProductCountsByAcb();
    }

    @Synchronized("lock")
    public List<ProductByAcb> getActiveProductsAndAcb() {
        return productReportsService.getActiveProductsAndAcb();
    }

    @Synchronized("lock")
    public List<ProductByAcb> getWithdrawnProductsAndAcb() {
        return productReportsService.getWithdrawnProductsAndAcb();
    }

    @Synchronized("lock")
    public List<ProductByAcb> getSuspendedProductsAndAcb() {
        return productReportsService.getSuspendedProductsAndAcb();
    }

    @Synchronized("lock")
    public UniqueListingCount getUniqueListingCount() {
        return listingReportsService.getUniqueListingCount();
    }

    @Synchronized("lock")
    public List<CertificationBodyStatistic> getActiveListingCountsByAcb() {
        return listingReportsService.getActiveListingCountsByAcb();
    }

    @Synchronized("lock")
    public List<CertificationBodyStatistic> getWithdrawnListingCountsByAcb() {
        return listingReportsService.getWithdrawnListingCountsByAcb();
    }

    @Synchronized("lock")
    public List<CertificationBodyStatistic> getSuspendedListingCountsByAcb() {
        return listingReportsService.getSuspendedListingCountsByAcb();
    }

    @Synchronized("lock")
    public List<ListingSearchResult> getActiveListings() {
        return listingReportsService.getActiveListings();
    }

    @Synchronized("lock")
    public List<ListingSearchResult> getWithdrawnListings() {
        return listingReportsService.getWithdrawnListings();
    }

    @Synchronized("lock")
    public List<ListingSearchResult> getSuspendedListings() {
        return listingReportsService.getSuspendedListings();
    }

    @Synchronized("lock")
    public List<TestToolReport> getTestToolReports() {
        return testToolReportService.getTestToolReports();
    }

    @Synchronized("lock")
    public List<TestToolListingReport> getTestToolListingReports() {
        return testToolReportService.getTestToolListingReports();
    }

    @Synchronized("lock")
    public List<UrlUptimeMonitorEx> getUrlUptimeMonitors() {
        return serviceBaseUrlListReportService.getUrlUptimeMonitors();
    }

    public List<StandardReport> getStandardReports() {
        return standardReportService.getStandardReports();
    }

    @Synchronized("lock")
    public List<StandardListingReport> getStandardListingReports() {
        return standardReportService.getStandardListingReports();
    }

    @Synchronized("lock")
    public DirectReviewCounts getDirectReviewCounts() {
        return directReviewReportsService.getDirectReviewCounts();
    }

    @Synchronized("lock")
    public RealWorldTestingReportDataService getRealWorldTestingReportDataService() {
        return realWorldTestingReportDataService;
    }

    @Synchronized("lock")
    public List<AttestationReport> getAttestationReports() {
        return attestationReportService.getAttestationReports();
    }

}
