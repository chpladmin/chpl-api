package gov.healthit.chpl.app.statistics;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.statistics.DeveloperStatisticsDAO;
import gov.healthit.chpl.dao.statistics.ListingStatisticsDAO;
import gov.healthit.chpl.dao.statistics.SurveillanceStatisticsDAO;
import gov.healthit.chpl.domain.DateRange;
import gov.healthit.chpl.domain.statistics.CertifiedBodyAltTestStatistics;
import gov.healthit.chpl.domain.statistics.CertifiedBodyStatistics;
import gov.healthit.chpl.entity.CertificationStatusType;

/**
 * Component that handles getting statistics data and return Futures of that data.
 * @author alarned
 *
 */
@Component
@EnableAsync
public class AsynchronousStatistics {
    @Autowired
    private DeveloperStatisticsDAO developerStatisticsDAO;
    @Autowired
    private ListingStatisticsDAO listingStatisticsDAO;
    @Autowired
    private SurveillanceStatisticsDAO surveillanceStatisticsDAO;

    /**
     * Total # of Unique Developers (Regardless of Edition).
     * @param dateRange the range of time to get statistics from
     * @return the statistic
     */
    @Transactional
    @Async
    public Future<Long> getTotalDevelopers(final DateRange dateRange) {
        return new AsyncResult<>(developerStatisticsDAO.getTotalDevelopers(dateRange));
    }

    /**
     * Total # of Developers with 2014 Listings.
     * @param dateRange the range of time to get statistics from
     * @return the statistic
     */
    @Transactional
    @Async
    public Future<Long> getTotalDevelopersWith2014Listings(final DateRange dateRange) {
        return new AsyncResult<>(developerStatisticsDAO
                .getTotalDevelopersWithListingsByEditionAndStatus(dateRange, "2014", null));
    }

    /**
     * Total # of Developers with Active 2014 Listings.
     * @param dateRange the range of time to get statistics from
     * @return the statistic
     */
    @Transactional
    @Async
    public Future<Long> getTotalDevelopersWithActive2014Listings(final DateRange dateRange) {
        List<String> activeStatuses = new ArrayList<String>();
        activeStatuses.add(CertificationStatusType.Active.getName().toUpperCase());
        activeStatuses.add(CertificationStatusType.SuspendedByAcb.getName().toUpperCase());
        activeStatuses.add(CertificationStatusType.SuspendedByOnc.getName().toUpperCase());
        return new AsyncResult<>(developerStatisticsDAO
                .getTotalDevelopersWithListingsByEditionAndStatus(dateRange, "2014", activeStatuses));
    }

    /**
     * Total # of Developers by certified body with listings for each year.
     * @param dateRange the range of time to get statistics from
     * @return the statistic
     */
    @Transactional
    @Async
    public Future<List<CertifiedBodyStatistics>> getTotalDevelopersByCertifiedBodyWithListingsEachYear(
            final DateRange dateRange) {
        return new AsyncResult<>(
                developerStatisticsDAO.getTotalDevelopersByCertifiedBodyWithListingsEachYear(dateRange));
    }

    /**
     * Total # of Developers by certified body with listings in each
     * certification status and year.
     * @param dateRange the range of time to get statistics from
     * @return the statistic
     */
    @Transactional
    @Async
    public Future<List<CertifiedBodyStatistics>>
    getTotalDevelopersByCertifiedBodyWithListingsInEachCertificationStatusAndYear(final DateRange dateRange) {
        return new AsyncResult<>(developerStatisticsDAO
                .getTotalDevelopersByCertifiedBodyWithListingsInEachCertificationStatusAndYear(dateRange));
    }

    /**
     * Total # of Developers with 2015 Listings.
     * @param dateRange the range of time to get statistics from
     * @return the statistic
     */
    @Transactional
    @Async
    public Future<Long> getTotalDevelopersWith2015Listings(final DateRange dateRange) {
        return new AsyncResult<>(developerStatisticsDAO
                .getTotalDevelopersWithListingsByEditionAndStatus(dateRange, "2015", null));
    }

    /**
     * Total # of Developers with Active 2015 Listings.
     * @param dateRange the range of time to get statistics from
     * @return the statistic
     */
    @Transactional
    @Async
    public Future<Long> getTotalDevelopersWithActive2015Listings(final DateRange dateRange) {
        List<String> activeStatuses = new ArrayList<String>();
        activeStatuses.add(CertificationStatusType.Active.getName().toUpperCase());
        activeStatuses.add(CertificationStatusType.SuspendedByAcb.getName().toUpperCase());
        activeStatuses.add(CertificationStatusType.SuspendedByOnc.getName().toUpperCase());
        return new AsyncResult<>(developerStatisticsDAO
                .getTotalDevelopersWithListingsByEditionAndStatus(dateRange, "2015", activeStatuses));    }

    /**
     * Total # of Certified Unique Products (Regardless of Status or Edition -
     * Including 2011).
     * @param dateRange the range of time to get statistics from
     * @return the statistic
     */
    @Async
    @Transactional
    public Future<Long> getTotalCertifiedProducts(final DateRange dateRange) {
        return new AsyncResult<>(listingStatisticsDAO.getTotalUniqueProductsByEditionAndStatus(dateRange, null, null));
    }

    /**
     * Total # of Certified Unique Products each year by certified body.
     * @param dateRange the range of time to get statistics from
     * @return the statistic
     */
    @Async
    @Transactional
    public Future<List<CertifiedBodyStatistics>> getTotalCPListingsEachYearByCertifiedBody(final DateRange dateRange) {
        return new AsyncResult<>(listingStatisticsDAO.getTotalCPListingsEachYearByCertifiedBody(dateRange));
    }

    /**
     * Total # of Certified Unique Products each year by certified body and
     * certification status.
     * @param dateRange the range of time to get statistics from
     * @return the statistic
     */
    @Async
    @Transactional
    public Future<List<CertifiedBodyStatistics>> getTotalCPListingsEachYearByCertifiedBodyAndCertificationStatus(
            final DateRange dateRange) {
        return new AsyncResult<>(
                listingStatisticsDAO.getTotalCPListingsEachYearByCertifiedBodyAndCertificationStatus(dateRange));
    }

    /**
     * Total # of unique Products with 2014 Listings.
     * @param dateRange the range of time to get statistics from
     * @return the statistic
     */
    @Async
    @Transactional
    public Future<Long> getTotalCPs2014Listings(final DateRange dateRange) {
        return new AsyncResult<>(listingStatisticsDAO
                .getTotalUniqueProductsByEditionAndStatus(dateRange, "2014", null));
    }

    /**
     * Total # of unique Products with Active 2014 Listings.
     * @param dateRange the range of time to get statistics from
     * @return the statistic
     */
    @Async
    @Transactional
    public Future<Long> getTotalCPsActive2014Listings(final DateRange dateRange) {
        List<String> activeStatuses = new ArrayList<String>();
        activeStatuses.add(CertificationStatusType.Active.getName().toUpperCase());
        return new AsyncResult<>(listingStatisticsDAO
                .getTotalUniqueProductsByEditionAndStatus(dateRange, "2014", activeStatuses));
    }

    /**
     * Total # of unique Products with Suspended (by ONC and ONC-ACB) 2014
     * Listings.
     * @param dateRange the range of time to get statistics from
     * @return the statistic
     */
    @Async
    @Transactional
    public Future<Long> getTotalCPsSuspended2014Listings(final DateRange dateRange) {
        List<String> suspendedStatuses = new ArrayList<String>();
        suspendedStatuses.add(CertificationStatusType.SuspendedByAcb.getName().toUpperCase());
        suspendedStatuses.add(CertificationStatusType.SuspendedByOnc.getName().toUpperCase());

        return new AsyncResult<>(listingStatisticsDAO
                .getTotalUniqueProductsByEditionAndStatus(dateRange, "2014", suspendedStatuses));
    }

    /**
     * Total # of unique Products with 2015 Listings.
     * @param dateRange the range of time to get statistics from
     * @return the statistic
     */
    @Async
    @Transactional
    public Future<Long> getTotalCPs2015Listings(final DateRange dateRange) {
        return new AsyncResult<>(listingStatisticsDAO
                .getTotalUniqueProductsByEditionAndStatus(dateRange, "2015", null));
    }

    /**
     * Total # of unique Products with Active 2015 Listings.
     * @param dateRange the range of time to get statistics from
     * @return the statistic
     */
    @Async
    @Transactional
    public Future<Long> getTotalCPsActive2015Listings(final DateRange dateRange) {
        List<String> activeStatuses = new ArrayList<String>();
        activeStatuses.add(CertificationStatusType.Active.getName().toUpperCase());
        return new AsyncResult<>(listingStatisticsDAO
                .getTotalUniqueProductsByEditionAndStatus(dateRange, "2015", activeStatuses));
    }

    /**
     * Total # of unique Products with Suspended (by ONC and ONC-ACB) 2015
     * Listings.
     * @param dateRange the range of time to get statistics from
     * @return the statistic
     */
    @Async
    @Transactional
    public Future<Long> getTotalCPsSuspended2015Listings(final DateRange dateRange) {
        List<String> suspendedStatuses = new ArrayList<String>();
        suspendedStatuses.add(CertificationStatusType.SuspendedByAcb.getName().toUpperCase());
        suspendedStatuses.add(CertificationStatusType.SuspendedByOnc.getName().toUpperCase());

        return new AsyncResult<>(listingStatisticsDAO
                .getTotalUniqueProductsByEditionAndStatus(dateRange, "2015", suspendedStatuses));      }

    /**
     * Total # of unique Products with Active Listings (Regardless of Edition).
     * @param dateRange the range of time to get statistics from
     * @return the statistic
     */
    @Async
    @Transactional
    public Future<Long> getTotalCPsActiveListings(final DateRange dateRange) {
        List<String> activeStatuses = new ArrayList<String>();
        activeStatuses.add(CertificationStatusType.Active.getName().toUpperCase());
        return new AsyncResult<>(listingStatisticsDAO
                .getTotalUniqueProductsByEditionAndStatus(dateRange, null, activeStatuses));
    }

    /**
     * Total # of Listings (Regardless of Status or Edition).
     * @param dateRange the range of time to get statistics from
     * @return the statistic
     */
    @Async
    @Transactional
    public Future<Long> getTotalListings(final DateRange dateRange) {
        return new AsyncResult<>(listingStatisticsDAO
                .getTotalListingsByEditionAndStatus(dateRange, null, null));
    }

    /**
     * Total # of Active 2014 Listings.
     * @param dateRange the range of time to get statistics from
     * @return the statistic
     */
    @Async
    @Transactional
    public Future<Long> getTotalActive2014Listings(final DateRange dateRange) {
        List<String> activeStatuses = new ArrayList<String>();
        activeStatuses.add(CertificationStatusType.Active.getName().toUpperCase());
        activeStatuses.add(CertificationStatusType.SuspendedByAcb.getName().toUpperCase());
        activeStatuses.add(CertificationStatusType.SuspendedByOnc.getName().toUpperCase());
        return new AsyncResult<>(listingStatisticsDAO
                .getTotalListingsByEditionAndStatus(dateRange, "2014", activeStatuses));
    }

    /**
     * Total # of Active 2015 Listings.
     * @param dateRange the range of time to get statistics from
     * @return the statistic
     */
    @Async
    @Transactional
    public Future<Long> getTotalActive2015Listings(final DateRange dateRange) {
        List<String> activeStatuses = new ArrayList<String>();
        activeStatuses.add(CertificationStatusType.Active.getName().toUpperCase());
        activeStatuses.add(CertificationStatusType.SuspendedByAcb.getName().toUpperCase());
        activeStatuses.add(CertificationStatusType.SuspendedByOnc.getName().toUpperCase());
        return new AsyncResult<>(listingStatisticsDAO
                .getTotalListingsByEditionAndStatus(dateRange, "2015", activeStatuses));
    }

    /**
     * Return the total number of listings that have Alternate Test Methods.
     * @return a number
     */
    @Async
    @Transactional
    public Future<Long> getTotalListingsWithAlternateTestMethods() {
        return new AsyncResult<>(listingStatisticsDAO.getTotalListingsWithAlternateTestMethods());
    }

    /**
     * Return the total number of listings with Alternate Test Methods by ACB.
     * @return a list of Statistic objects
     */
    @Async
    @Transactional
    public Future<List<CertifiedBodyAltTestStatistics>>
    getTotalListingsWithCertifiedBodyAndAlternativeTestMethods() {
        return new AsyncResult<>(listingStatisticsDAO
                .getTotalListingsWithCertifiedBodyAndAlternativeTestMethods());
    }

    /**
     * Total # of Active Listings by Certified Body.
     * @param dateRange the range of time to get statistics from
     * @return the statistic
     */
    @Async
    @Transactional
    public Future<List<CertifiedBodyStatistics>> getTotalActiveListingsByCertifiedBody(final DateRange dateRange) {
        return new AsyncResult<>(listingStatisticsDAO.getTotalActiveListingsByCertifiedBody(dateRange));
    }

    /**
     * Total # of 2014 Listings (Regardless of Status).
     * @param dateRange the range of time to get statistics from
     * @return the statistic
     */
    @Async
    @Transactional
    public Future<Long> getTotal2014Listings(final DateRange dateRange) {
        return new AsyncResult<>(listingStatisticsDAO
                .getTotalListingsByEditionAndStatus(dateRange, "2014", null));
    }

    /**
     * Total # of 2015 Listings (Regardless of Status).
     * @param dateRange the range of time to get statistics from
     * @return the statistic
     */
    @Async
    @Transactional
    public Future<Long> getTotal2015Listings(final DateRange dateRange) {
        return new AsyncResult<>(listingStatisticsDAO
                .getTotalListingsByEditionAndStatus(dateRange, "2015", null));
    }

    /**
     * Total # of 2011 Listings (Will not be active).
     * @param dateRange the range of time to get statistics from
     * @return the statistic
     */
    @Async
    @Transactional
    public Future<Long> getTotal2011Listings(final DateRange dateRange) {
        return new AsyncResult<>(listingStatisticsDAO
                .getTotalListingsByEditionAndStatus(dateRange, "2011", null));
    }

    /**
     * Total # of Surveillance Activities*.
     * @param dateRange the range of time to get statistics from
     * @return the statistic
     */
    @Async
    @Transactional
    public Future<Long> getTotalSurveillanceActivities(final DateRange dateRange) {
        return new AsyncResult<>(surveillanceStatisticsDAO.getTotalSurveillanceActivities(dateRange));
    }

    /**
     * Open Surveillance Activities.
     * @param dateRange the range of time to get statistics from
     * @return the statistic
     */
    @Async
    @Transactional
    public Future<Long> getTotalOpenSurveillanceActivities(final DateRange dateRange) {
        return new AsyncResult<>(surveillanceStatisticsDAO.getTotalOpenSurveillanceActivities(dateRange));
    }

    /**
     * Closed Surveillance Activities.
     * @param dateRange the range of time to get statistics from
     * @return the statistic
     */
    @Async
    @Transactional
    public Future<Long> getTotalClosedSurveillanceActivities(final DateRange dateRange) {
        return new AsyncResult<>(surveillanceStatisticsDAO.getTotalClosedSurveillanceActivities(dateRange));
    }

    /**
     * Total # of NCs.
     * @param dateRange the range of time to get statistics from
     * @return the statistic
     */
    @Async
    @Transactional
    public Future<Long> getTotalNonConformities(final DateRange dateRange) {
        return new AsyncResult<>(surveillanceStatisticsDAO.getTotalNonConformities(dateRange));
    }

    /**
     * Open NCs.
     * @param dateRange the range of time to get statistics from
     * @return the statistic
     */
    @Async
    @Transactional
    public Future<Long> getTotalOpenNonconformities(final DateRange dateRange) {
        return new AsyncResult<>(surveillanceStatisticsDAO.getTotalOpenNonconformities(dateRange));
    }

    /**
     * Closed NCs.
     * @param dateRange the range of time to get statistics from
     * @return the statistic
     */
    @Async
    @Transactional
    public Future<Long> getTotalClosedNonconformities(final DateRange dateRange) {
        return new AsyncResult<>(surveillanceStatisticsDAO.getTotalClosedNonconformities(dateRange));
    }
}
