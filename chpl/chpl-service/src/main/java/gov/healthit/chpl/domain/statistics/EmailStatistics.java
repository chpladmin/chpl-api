package gov.healthit.chpl.domain.statistics;

import java.io.Serializable;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EmailStatistics implements Serializable {
    private static final long serialVersionUID = 6977674702447513779L;

    //////////////////////////////////////////////////////////////////////
    private EmailStatistic developersForEdition2014WithAllStatuses;
    private EmailStatistic developersForEdition2014WithActiveStatuses;
    private EmailStatistic developersForEdition2014WithSuspendedStatuses;

    private EmailStatistic developersForEdition2015CuresWithAllStatuses;
    private EmailStatistic developersForEdition2015CuresWithActiveStatuses;
    private EmailStatistic developersForEdition2015CuresWithSuspendedStatuses;

    private EmailStatistic developersForEdition2015NonCuresWithAllStatuses;
    private EmailStatistic developersForEdition2015NonCuresWithActiveStatuses;
    private EmailStatistic developersForEdition2015NonCuresWithSuspendedStatuses;

    private EmailStatistic developersForEdition2015CuresAndNonCuresWithAllStatuses;
    private EmailStatistic developersForEdition2015CuresAndNonCuresWithActiveStatuses;
    private EmailStatistic developersForEdition2015CuresAndNonCuresWithSuspendedStatuses;

    private Long developersForEditionAllAndAllStatuses;

    //////////////////////////////////////////////////////////////////////
    private EmailStatistic productsForEdition2014WithAllStatuses;
    private EmailStatistic productsForEdition2014WithActiveStatuses;
    private EmailStatistic productsForEdition2014WithSuspendedStatuses;

    private EmailStatistic productsForEdition2015CuresWithAllStatuses;
    private EmailStatistic productsForEdition2015CuresWithActiveStatuses;
    private EmailStatistic productsForEdition2015CuresWithSuspendedStatuses;

    private EmailStatistic productsForEdition2015NonCuresWithAllStatuses;
    private EmailStatistic productsForEdition2015NonCuresWithActiveStatuses;
    private EmailStatistic productsForEdition2015NonCuresWithSuspendedStatuses;

    private EmailStatistic productsForEdition2015CuresAndNonCuresWithAllStatuses;
    private EmailStatistic productsForEdition2015CuresAndNonCuresWithActiveStatuses;
    private EmailStatistic productsForEdition2015CuresAndNonCuresWithSuspendedStatuses;

    private Long productsForEditionAllAndActiveStatuses;
    private Long productsForEditionAllAndAllStatuses;

    //////////////////////////////////////////////////////////////////////

    private EmailStatistic listingsForEdition2014WithActiveAndSuspendedStatuses;
    private EmailStatistic listingsForEdition2015NonCuresWithActiveAndSuspendedStatuses;
    private EmailStatistic listingsForEdition2015CuresWithActiveAndSuspendedStatuses;

    private EmailStatistic listingsForEdition2015NonCuresWithAllStatusesAndAltTestMethods;
    private EmailStatistic listingsForEdition2015CuresWithAllStatusesAndAltTestMethods;

    private Long listingsForEdition2015CuresTotal; // allListingsCountWithCuresUpdated;
    private Long listingsForEdition2015NonCuresTotal; // allListingsCountWithoutCuresUpdated;
    private Long listingsForEdition2015NonCuresAndCuresTotal; // total2015Listings;
    private Long listingsForEdition2014Total; // total2014Listings;
    private Long listingsForEdition2011Total; //total2011Listings;
    private Long listingsForEditionAnyTotal; //totalListings;

    //////////////////////////////////////////////////////////////////////

    private Long surveillanceAllStatusTotal;  //totalSurveillanceActivities;
    private EmailStatistic surveillanceOpenStatus; //totalOpenSurveillanceActivities & totalOpenSurveillanceActivitiesByAcb;
    private Long surveillanceClosedStatusTotal; //totalClosedSurveillanceActivities
    private Long surveillanceAvgTimeToClose; //averageTimeToCloseSurveillance

    //////////////////////////////////////////////////////////////////////

    private Long nonconfStatusAllTotal; //totalNonConformities
    private EmailStatistic nonconfStatusOpen; // totalOpenNonconformities; & totalOpenNonconformitiesByAcb
    private Long nonconfStatusClosedTotal; //totalClosedNonconformities
    private Long nonconfAvgTimeToAssessConformity; //averageTimeToAssessConformity
    private Long nonconfAvgTimeToApproveCAP; //averageTimeToApproveCAP
    private Long nonconfAvgDurationOfCAP; //averageDurationOfCAP
    private Long nonconfAvgTimeFromCAPAprrovalToSurveillanceEnd; //averageTimeFromCAPApprovalToSurveillanceEnd
    private Long nonconfAvgTimeFromCAPEndToSurveillanceEnd; //averageTimeFromCAPEndToSurveillanceEnd
    private Long nonconfAvgTimeFromSurveillanceOpenToSurveillanceClose; //averageTimeFromSurveillanceOpenToSurveillanceClose
    private List<EmailCertificationBodyStatistic> nonconfCAPStatusOpen; //openCAPCountByAcb
    private List<EmailCertificationBodyStatistic> nonconfCAPStatusClosed; //closedCAPCountByAcb


}
