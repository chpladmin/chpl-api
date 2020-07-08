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

    private Long listingsForEdition2015CuresTotal;
    private Long listingsForEdition2015NonCuresTotal;
    private Long listingsForEdition2015NonCuresAndCuresTotal;
    private Long listingsForEdition2014Total;
    private Long listingsForEdition2011Total;
    private Long listingsForEditionAnyTotal;

    //////////////////////////////////////////////////////////////////////

    private Long surveillanceAllStatusTotal;
    private EmailStatistic surveillanceOpenStatus;
    private Long surveillanceClosedStatusTotal;
    private Long surveillanceAvgTimeToClose;

    //////////////////////////////////////////////////////////////////////

    private Long nonconfStatusAllTotal;
    private EmailStatistic nonconfStatusOpen;
    private Long nonconfStatusClosedTotal;
    private Long nonconfAvgTimeToAssessConformity;
    private Long nonconfAvgTimeToApproveCAP;
    private Long nonconfAvgDurationOfCAP;
    private Long nonconfAvgTimeFromCAPAprrovalToSurveillanceEnd;
    private Long nonconfAvgTimeFromCAPEndToSurveillanceEnd;
    private Long nonconfAvgTimeFromSurveillanceOpenToSurveillanceClose;
    private List<EmailCertificationBodyStatistic> nonconfCAPStatusOpen;
    private List<EmailCertificationBodyStatistic> nonconfCAPStatusClosed;


}
