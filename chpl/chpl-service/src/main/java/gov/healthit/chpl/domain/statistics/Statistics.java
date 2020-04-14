package gov.healthit.chpl.domain.statistics;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import gov.healthit.chpl.domain.DateRange;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Statistics implements Serializable {
    private static final long serialVersionUID = 6977674702447513779L;
    private DateRange dateRange;
    private Long totalDevelopers;
    private Long totalDevelopersWith2014Listings;
    private Long totalDevelopersWithActive2014Listings;
    private List<CertifiedBodyStatistics> totalDevelopersByCertifiedBodyWithListingsEachYear;
    private List<CertifiedBodyStatistics> totalDevelopersByCertifiedBodyWithListingsInEachCertificationStatusAndYear;
    private Long totalSuspendedDevelopersWith2014Listings;
    private Long totalDevelopersWith2015Listings;
    private Long totalDevelopersWithActive2015Listings;
    private Long totalCertifiedProducts;
    private List<CertifiedBodyStatistics> totalCPListingsEachYearByCertifiedBody;
    private List<CertifiedBodyStatistics> totalCPListingsEachYearByCertifiedBodyAndCertificationStatus;
    private Long totalCPs2014Listings;
    private Long totalCPsActive2014Listings;
    private Long totalCPsSuspended2014Listings;
    private Long totalCPs2015Listings;
    private Long totalCPsActive2015Listings;
    private Long totalCPsSuspended2015Listings;
    private Long totalCPsActiveListings;
    private Long totalListings;
    private Long totalActive2014Listings;
    private Long totalActive2015Listings;
    private List<CertifiedBodyStatistics> totalActiveListingsByCertifiedBody;
    private Long total2014Listings;
    private Long total2015Listings;
    private Long total2011Listings;
    private Long totalSurveillanceActivities;
    private Long totalOpenSurveillanceActivities;
    private List<CertifiedBodyStatistics> totalOpenSurveillanceActivitiesByAcb;
    private Long totalClosedSurveillanceActivities;
    private Long totalNonConformities;
    private Long totalOpenNonconformities;

    private Long averageTimeToAssessConformity;
    private Long averageTimeToApproveCAP;
    private Long averageDurationOfCAP;
    private Long averageTimeFromCAPApprovalToSurveillanceEnd;
    private Long averageTimeFromCAPEndToSurveillanceEnd;
    private Long averageTimeFromSurveillanceOpenToSurveillanceClose;
    private Map<Long, Long> openCAPCountByAcb;
    private Map<Long, Long> closedCAPCountByAcb;
    private Long averageTimeToCloseSurveillance;

    private List<CertifiedBodyStatistics> totalOpenNonconformitiesByAcb;
    private Long totalClosedNonconformities;
    private Long totalListingsWithAlternativeTestMethods;
    private List<CertifiedBodyAltTestStatistics> totalListingsWithCertifiedBodyAndAlternativeTestMethods;

    private List<CertifiedBodyStatistics> uniqueDevelopersCountWithCuresUpdatedListingsByAcb;
    private List<CertifiedBodyStatistics> uniqueDevelopersCountWithCuresUpdatedActiveListingsByAcb;
    private Long uniqueDevelopersCountWithCuresUpdatedSuspendedListings;
    private Long uniqueProductsCountWithCuresUpdatedListings;

    public List<CertifiedBodyStatistics> getTotalDevsByCertifiedBodyWithListingsInEachCertificationStatusAndYear() {
        return totalDevelopersByCertifiedBodyWithListingsInEachCertificationStatusAndYear;
    }

    public void setTotalDevsByCertifiedBodyWithListingsInEachCertificationStatusAndYear(
            List<CertifiedBodyStatistics> totalDevelopersByCertifiedBodyWithListingsInEachCertificationStatusAndYear) {
        this.totalDevelopersByCertifiedBodyWithListingsInEachCertificationStatusAndYear = totalDevelopersByCertifiedBodyWithListingsInEachCertificationStatusAndYear;
    }

}
