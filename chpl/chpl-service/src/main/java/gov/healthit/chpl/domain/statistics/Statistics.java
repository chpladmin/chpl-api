package gov.healthit.chpl.domain.statistics;

import java.io.Serializable;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Statistics implements Serializable {
    private static final long serialVersionUID = 6977674702447513779L;

    //////////////////////////////////////////////////////////////////////
    private Stat developersForEdition2014WithAllStatuses;
    private Stat developersForEdition2014WithActiveStatuses;
    private Stat developersForEdition2014WithSuspendedStatuses;

    private Stat developersForEdition2015CuresWithAllStatuses;
    private Stat developersForEdition2015CuresWithActiveStatuses;
    private Stat developersForEdition2015CuresWithSuspendedStatuses;

    private Stat developersForEdition2015NonCuresWithAllStatuses;
    private Stat developersForEdition2015NonCuresWithActiveStatuses;
    private Stat developersForEdition2015NonCuresWithSuspendedStatuses;

    private Stat developersForEdition2015CuresAndNonCuresWithAllStatuses;
    private Stat developersForEdition2015CuresAndNonCuresWithActiveStatuses;
    private Stat developersForEdition2015CuresAndNonCuresWithSuspendedStatuses;

    private Long developersForEditionAllAndAllStatuses;

    //////////////////////////////////////////////////////////////////////
    private Stat productsForEdition2014WithAllStatuses;
    private Stat productsForEdition2014WithActiveStatuses;
    private Stat productsForEdition2014WithSuspendedStatuses;

    private Stat productsForEdition2015CuresWithAllStatuses;
    private Stat productsForEdition2015CuresWithActiveStatuses;
    private Stat productsForEdition2015CuresWithSuspendedStatuses;

    private Stat productsForEdition2015NonCuresWithAllStatuses;
    private Stat productsForEdition2015NonCuresWithActiveStatuses;
    private Stat productsForEdition2015NonCuresWithSuspendedStatuses;

    private Stat productsForEdition2015CuresAndNonCuresWithAllStatuses;
    private Stat productsForEdition2015CuresAndNonCuresWithActiveStatuses;
    private Stat productsForEdition2015CuresAndNonCuresWithSuspendedStatuses;

    private Long productsForEditionAllAndActiveStatuses;
    private Long productsForEditionAllAndAllStatuses;

    //////////////////////////////////////////////////////////////////////

    private Stat listingsForEdition2014WithActiveAndSuspendedStatuses;
    private Stat listingsForEdition2015NonCuresWithActiveAndSuspendedStatuses;
    private Stat listingsForEdition2015CuresWithActiveAndSuspendedStatuses;

    private Stat listingsForEdition2015NonCuresWithAllStatusesAndAltTestMethods;
    private Stat listingsForEdition2015CuresWithAllStatusesAndAltTestMethods;

    private Long listingsForEdition2015CuresTotal; // allListingsCountWithCuresUpdated;
    private Long listingsForEdition2015NonCuresTotal; // allListingsCountWithoutCuresUpdated;
    private Long listingsForEdition2015NonCuresAndCuresTotal; // total2015Listings;
    private Long listingsForEdition2014Total; // total2014Listings;
    private Long listingsForEdition2011Total; //total2011Listings;
    private Long listingsForEditionAnyTotal; //totalListings;

    //////////////////////////////////////////////////////////////////////

    private Long surveillanceAllStatusTotal;  //totalSurveillanceActivities;
    private Stat surveillanceOpenStatus; //totalOpenSurveillanceActivities & totalOpenSurveillanceActivitiesByAcb;
    private Long surveillanceClosedStatusTotal; //totalClosedSurveillanceActivities
    private Long surveillanceAvgTimeToClose; //averageTimeToCloseSurveillance

    //////////////////////////////////////////////////////////////////////

    private Long nonconfStatusAllTotal; //totalNonConformities
    private Stat nonconfStatusOpen; // totalOpenNonconformities; & totalOpenNonconformitiesByAcb
    private Long nonconfStatusClosedTotal; //totalClosedNonconformities
    private Long nonconfAvgTimeToAssessConformity; //averageTimeToAssessConformity
    private Long nonconfAvgTimeToApproveCAP; //averageTimeToApproveCAP
    private Long nonconfAvgDurationOfCAP; //averageDurationOfCAP
    private Long nonconfAvgTimeFromCAPAprrovalToSurveillanceEnd; //averageTimeFromCAPApprovalToSurveillanceEnd
    private Long nonconfAvgTimeFromCAPEndToSurveillanceEnd; //averageTimeFromCAPEndToSurveillanceEnd
    private Long nonconfAvgTimeFromSurveillanceOpenToSurveillanceClose; //averageTimeFromSurveillanceOpenToSurveillanceClose
    private List<AcbStat> nonconfCAPStatusOpen; //openCAPCountByAcb
    private List<AcbStat> nonconfCAPStatusClosed; //closedCAPCountByAcb


}
