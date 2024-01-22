package gov.healthit.chpl.scheduler.job.summarystatistics.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StatisticsSnapshot implements Serializable {
    private static final long serialVersionUID = 6977674702447509179L;

    private List<CertificationBodyStatusStatistic> developerCountsByStatus;
    private List<CertificationBodyStatusStatistic> productCountsByStatus;
    private List<CertificationBodyStatusStatistic> listingCountsByStatus;

    //////////////////////////////////////////////////////////////////////

    private Long surveillanceAllStatusTotal;
    private Statistic surveillanceOpenStatus;
    private Long surveillanceClosedStatusTotal;
    private Long surveillanceAvgTimeToClose;

    //////////////////////////////////////////////////////////////////////

    private Long nonConfStatusAllTotal;
    private Statistic nonConfStatusOpen;
    private Long nonConfStatusClosedTotal;
    private Long nonConfAvgTimeToAssessConformity;
    private Long nonConfAvgTimeToApproveCAP;
    private Long nonConfAvgDurationOfCAP;
    private Long nonConfAvgTimeFromCAPAprrovalToSurveillanceEnd;
    private Long nonConfAvgTimeFromCAPEndToSurveillanceEnd;
    private Long nonConfAvgTimeFromSurveillanceOpenToSurveillanceClose;
    private List<CertificationBodyStatistic> nonConfCAPStatusOpen;
    private List<CertificationBodyStatistic> nonConfCAPStatusClosed;

    ////////////////////////////////////////////////////////////////////

    private Long totalDirectReviews;
    private Long openDirectReviews;
    private Long closedDirectReviews;
    private Long averageDaysOpenDirectReviews;
    private Long totalNonConformities;
    private Long openNonConformities;
    private Long closedNonConformities;
    private Long openCaps;
    private Long closedCaps;

    public Long getDeveloperCountForStatuses(List<Long> statusIds) {
        return getUniqueItemCountForStatuses(developerCountsByStatus, statusIds);
    }

    public List<CertificationBodyStatistic> getDeveloperCountForStatusesByAcb(List<Long> statusIds) {
        return getUniqueItemCountByStatusesByAcb(developerCountsByStatus, statusIds);
    }

    public Long getProductCountForStatuses(List<Long> statusIds) {
        return getUniqueItemCountForStatuses(productCountsByStatus, statusIds);
    }

    public List<CertificationBodyStatistic> getProductCountForStatusesByAcb(List<Long> statusIds) {
        return getUniqueItemCountByStatusesByAcb(productCountsByStatus, statusIds);
    }

    public Long getListingCountForStatuses(List<Long> statusIds) {
        return getUniqueItemCountForStatuses(listingCountsByStatus, statusIds);
    }

    public List<CertificationBodyStatistic> getListingCountForStatusesByAcb(List<Long> statusIds) {
        return getUniqueItemCountByStatusesByAcb(listingCountsByStatus, statusIds);
    }

    private Long getUniqueItemCountForStatuses(List<CertificationBodyStatusStatistic> statistics, List<Long> statusIds) {
        return statistics.stream()
            .filter(itemStatistic -> statusIds.contains(itemStatistic.getStatusId()))
            .flatMap(itemStatisticForStatus -> itemStatisticForStatus.getIds().stream())
            .distinct().count();
    }

    private List<CertificationBodyStatistic> getUniqueItemCountByStatusesByAcb(
            List<CertificationBodyStatusStatistic> statistics, List<Long> statusIds) {
        List<CertificationBodyStatusStatistic> itemCountsForStatuses = statistics.stream()
                .filter(statistic -> statusIds.contains(statistic.getStatusId()))
                .toList();
        Map<Long, List<CertificationBodyStatusStatistic>> itemStatisticsByAcb = itemCountsForStatuses.stream()
            .collect(Collectors.groupingBy(CertificationBodyStatusStatistic::getAcbId));

        List<CertificationBodyStatistic> result = new ArrayList<CertificationBodyStatistic>();
        itemStatisticsByAcb.keySet()
           .forEach(acbId -> {
               List<CertificationBodyStatusStatistic> stats = itemStatisticsByAcb.get(acbId);
               Long uniqueItemCount = stats.stream()
                   .flatMap(stat -> stat.getIds().stream())
                   .distinct().count();
               result.add(CertificationBodyStatistic.builder()
                       .acbId(acbId)
                       .acbName(itemStatisticsByAcb.get(acbId).get(0).getAcbName())
                       .count(uniqueItemCount).build());

           });
        return result;
    }
}
