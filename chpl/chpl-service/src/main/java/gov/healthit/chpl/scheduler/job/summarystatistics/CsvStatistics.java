package gov.healthit.chpl.scheduler.job.summarystatistics;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import gov.healthit.chpl.domain.DateRange;
import gov.healthit.chpl.domain.statistics.CertifiedBodyStatistics;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CsvStatistics implements Serializable{
    private static final long serialVersionUID = -5219649276730810669L;

    private DateRange dateRange;
    private Long totalDevelopers;

    private Long totalCertifiedProducts;
    private List<CertifiedBodyStatistics> totalCPListingsEachYearByCertifiedBody;
    private List<CertifiedBodyStatistics> totalCPListingsEachYearByCertifiedBodyAndCertificationStatus;
    private Long totalCPs2014Listings;
    private Long totalCPsActive2014Listings;
    private Long totalCPsSuspended2014Listings;
    private Long totalCPs2015Listings;
    private Long totalCPsActive2015Listings;
    private Long totalCPsSuspended2015Listings;
    private Long totalActive2014Listings;
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
    private List<CertifiedBodyStatistics> totalListingsWithCertifiedBodyAndAlternativeTestMethods;

    private Long totalDevelopersWith2014Listings;
    private Long totalDevelopersWith2015Listings;
    private Long totalCPsActiveListings;
    private Long totalListings;
    private Long total2011Listings;
    private Long total2014Listings;
    private Long total2015Listings;

}
