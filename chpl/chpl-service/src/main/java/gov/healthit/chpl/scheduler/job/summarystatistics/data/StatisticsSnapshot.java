package gov.healthit.chpl.scheduler.job.summarystatistics.data;

import java.io.Serializable;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StatisticsSnapshot implements Serializable {
    private static final long serialVersionUID = 6977674702447509179L;

    private List<Listing> listings;

    //////////////////////////////////////////////////////////////////////

    private Long surveillanceAllStatusTotal;
    private Statistic surveillanceOpenStatus;
    private Long surveillanceClosedStatusTotal;
    private Long surveillanceAvgTimeToClose;

    //////////////////////////////////////////////////////////////////////

    private Long nonconfStatusAllTotal;
    private Statistic nonconfStatusOpen;
    private Long nonconfStatusClosedTotal;
    private Long nonconfAvgTimeToAssessConformity;
    private Long nonconfAvgTimeToApproveCAP;
    private Long nonconfAvgDurationOfCAP;
    private Long nonconfAvgTimeFromCAPAprrovalToSurveillanceEnd;
    private Long nonconfAvgTimeFromCAPEndToSurveillanceEnd;
    private Long nonconfAvgTimeFromSurveillanceOpenToSurveillanceClose;
    private List<CertificationBodyStatistic> nonconfCAPStatusOpen;
    private List<CertificationBodyStatistic> nonconfCAPStatusClosed;

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
}
