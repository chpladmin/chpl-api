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
import gov.healthit.chpl.domain.statistics.CertifiedBodyStatistics;
import gov.healthit.chpl.entity.CertificationStatusType;

@Component
@EnableAsync
public class AsynchronousStatistics {
    @Autowired
    DeveloperStatisticsDAO developerStatisticsDAO;
    @Autowired
    ListingStatisticsDAO listingStatisticsDAO;
    @Autowired
    SurveillanceStatisticsDAO surveillanceStatisticsDAO;

    /**
     * Total # of Unique Developers (Regardless of Edition)
     */
    @Transactional
    @Async
    public Future<Long> getTotalDevelopers(DateRange dateRange) {
        return new AsyncResult<>(developerStatisticsDAO.getTotalDevelopers(dateRange));
    }

    /**
     * Total # of Developers with 2014 Listings
     */
    @Transactional
    @Async
    public Future<Long> getTotalDevelopersWith2014Listings(DateRange dateRange) {
        return new AsyncResult<>(developerStatisticsDAO
                .getTotalDevelopersWithListingsByEditionAndStatus(dateRange, "2014", null));
    }

    /**
     * Total # of Developers with Active 2014 Listings
     */
    @Transactional
    @Async
    public Future<Long> getTotalDevelopersWithActive2014Listings(DateRange dateRange) {
        List<String> activeStatuses = new ArrayList<String>();
        activeStatuses.add(CertificationStatusType.Active.getName().toUpperCase());
        activeStatuses.add(CertificationStatusType.SuspendedByAcb.getName().toUpperCase());
        activeStatuses.add(CertificationStatusType.SuspendedByOnc.getName().toUpperCase());
        return new AsyncResult<>(developerStatisticsDAO
                .getTotalDevelopersWithListingsByEditionAndStatus(dateRange, "2014", activeStatuses));
    }

    /**
     * Total # of Developers by certified body with listings for each year
     */
    @Transactional
    @Async
    public Future<List<CertifiedBodyStatistics>> getTotalDevelopersByCertifiedBodyWithListingsEachYear(
            DateRange dateRange) {
        return new AsyncResult<>(
                developerStatisticsDAO.getTotalDevelopersByCertifiedBodyWithListingsEachYear(dateRange));
    }

    /**
     * Total # of Developers by certified body with listings in each
     * certification status and year
     */
    @Transactional
    @Async
    public Future<List<CertifiedBodyStatistics>> getTotalDevelopersByCertifiedBodyWithListingsInEachCertificationStatusAndYear(
            DateRange dateRange) {
        return new AsyncResult<>(developerStatisticsDAO
                .getTotalDevelopersByCertifiedBodyWithListingsInEachCertificationStatusAndYear(dateRange));
    }

    /**
     * Total # of Developers with 2015 Listings
     */
    @Transactional
    @Async
    public Future<Long> getTotalDevelopersWith2015Listings(DateRange dateRange) {
        return new AsyncResult<>(developerStatisticsDAO
                .getTotalDevelopersWithListingsByEditionAndStatus(dateRange, "2015", null));
    }

    /**
     * Total # of Developers with Active 2015 Listings
     */
    @Transactional
    @Async
    public Future<Long> getTotalDevelopersWithActive2015Listings(DateRange dateRange) {
        List<String> activeStatuses = new ArrayList<String>();
        activeStatuses.add(CertificationStatusType.Active.getName().toUpperCase());
        activeStatuses.add(CertificationStatusType.SuspendedByAcb.getName().toUpperCase());
        activeStatuses.add(CertificationStatusType.SuspendedByOnc.getName().toUpperCase());
        return new AsyncResult<>(developerStatisticsDAO
                .getTotalDevelopersWithListingsByEditionAndStatus(dateRange, "2015", activeStatuses));    }

    /**
     * Total # of Certified Unique Products (Regardless of Status or Edition -
     * Including 2011)
     */
    @Async
    @Transactional
    public Future<Long> getTotalCertifiedProducts(DateRange dateRange) {
        return new AsyncResult<>(listingStatisticsDAO.getTotalUniqueProductsByEditionAndStatus(dateRange, null, null));
    }

    /**
     * Total # of Certified Unique Products each year by certified body
     */
    @Async
    @Transactional
    public Future<List<CertifiedBodyStatistics>> getTotalCPListingsEachYearByCertifiedBody(DateRange dateRange) {
        return new AsyncResult<>(listingStatisticsDAO.getTotalCPListingsEachYearByCertifiedBody(dateRange));
    }

    /**
     * Total # of Certified Unique Products each year by certified body and
     * certification status
     */
    @Async
    @Transactional
    public Future<List<CertifiedBodyStatistics>> getTotalCPListingsEachYearByCertifiedBodyAndCertificationStatus(
            DateRange dateRange) {
        return new AsyncResult<>(
                listingStatisticsDAO.getTotalCPListingsEachYearByCertifiedBodyAndCertificationStatus(dateRange));
    }

    /**
     * Total # of unique Products with 2014 Listings
     */
    @Async
    @Transactional
    public Future<Long> getTotalCPs2014Listings(DateRange dateRange) {
        return new AsyncResult<>(listingStatisticsDAO
                .getTotalUniqueProductsByEditionAndStatus(dateRange, "2014", null));
    }

    /**
     * Total # of unique Products with Active 2014 Listings
     */
    @Async
    @Transactional
    public Future<Long> getTotalCPsActive2014Listings(DateRange dateRange) {
        List<String> activeStatuses = new ArrayList<String>();
        activeStatuses.add(CertificationStatusType.Active.getName().toUpperCase());
        return new AsyncResult<>(listingStatisticsDAO
                .getTotalUniqueProductsByEditionAndStatus(dateRange, "2014", activeStatuses));
    }

    /**
     * Total # of unique Products with Suspended (by ONC and ONC-ACB) 2014
     * Listings
     */
    @Async
    @Transactional
    public Future<Long> getTotalCPsSuspended2014Listings(DateRange dateRange) {
        List<String> suspendedStatuses = new ArrayList<String>();
        suspendedStatuses.add(CertificationStatusType.SuspendedByAcb.getName().toUpperCase());
        suspendedStatuses.add(CertificationStatusType.SuspendedByOnc.getName().toUpperCase());

        return new AsyncResult<>(listingStatisticsDAO
                .getTotalUniqueProductsByEditionAndStatus(dateRange, "2014", suspendedStatuses));        
    }

    /**
     * Total # of unique Products with 2015 Listings
     */
    @Async
    @Transactional
    public Future<Long> getTotalCPs2015Listings(DateRange dateRange) {
        return new AsyncResult<>(listingStatisticsDAO
                .getTotalUniqueProductsByEditionAndStatus(dateRange, "2015", null));
    }

    /**
     * Total # of unique Products with Active 2015 Listings
     */
    @Async
    @Transactional
    public Future<Long> getTotalCPsActive2015Listings(DateRange dateRange) {
        List<String> activeStatuses = new ArrayList<String>();
        activeStatuses.add(CertificationStatusType.Active.getName().toUpperCase());
        return new AsyncResult<>(listingStatisticsDAO
                .getTotalUniqueProductsByEditionAndStatus(dateRange, "2015", activeStatuses));
    }

    /**
     * Total # of unique Products with Suspended (by ONC and ONC-ACB) 2015
     * Listings
     */
    @Async
    @Transactional
    public Future<Long> getTotalCPsSuspended2015Listings(DateRange dateRange) {
        List<String> suspendedStatuses = new ArrayList<String>();
        suspendedStatuses.add(CertificationStatusType.SuspendedByAcb.getName().toUpperCase());
        suspendedStatuses.add(CertificationStatusType.SuspendedByOnc.getName().toUpperCase());

        return new AsyncResult<>(listingStatisticsDAO
                .getTotalUniqueProductsByEditionAndStatus(dateRange, "2015", suspendedStatuses));      }

    /**
     * Total # of unique Products with Active Listings (Regardless of Edition)
     */
    @Async
    @Transactional
    public Future<Long> getTotalCPsActiveListings(DateRange dateRange) {
        List<String> activeStatuses = new ArrayList<String>();
        activeStatuses.add(CertificationStatusType.Active.getName().toUpperCase());
        return new AsyncResult<>(listingStatisticsDAO
                .getTotalUniqueProductsByEditionAndStatus(dateRange, null, activeStatuses));
    }

    /**
     * Total # of Listings (Regardless of Status or Edition)
     */
    @Async
    @Transactional
    public Future<Long> getTotalListings(DateRange dateRange) {
        return new AsyncResult<>(listingStatisticsDAO
                .getTotalListingsByEditionAndStatus(dateRange, null, null));
    }

    /**
     * Total # of Active 2014 Listings
     */
    @Async
    @Transactional
    public Future<Long> getTotalActive2014Listings(DateRange dateRange) {
        List<String> activeStatuses = new ArrayList<String>();
        activeStatuses.add(CertificationStatusType.Active.getName().toUpperCase());
        activeStatuses.add(CertificationStatusType.SuspendedByAcb.getName().toUpperCase());
        activeStatuses.add(CertificationStatusType.SuspendedByOnc.getName().toUpperCase());
        return new AsyncResult<>(listingStatisticsDAO
                .getTotalListingsByEditionAndStatus(dateRange, "2014", activeStatuses));
    }

    /**
     * Total # of Active 2015 Listings
     */
    @Async
    @Transactional
    public Future<Long> getTotalActive2015Listings(DateRange dateRange) {
        List<String> activeStatuses = new ArrayList<String>();
        activeStatuses.add(CertificationStatusType.Active.getName().toUpperCase());
        activeStatuses.add(CertificationStatusType.SuspendedByAcb.getName().toUpperCase());
        activeStatuses.add(CertificationStatusType.SuspendedByOnc.getName().toUpperCase());
        return new AsyncResult<>(listingStatisticsDAO
                .getTotalListingsByEditionAndStatus(dateRange, "2015", activeStatuses));
    }

    /**
     * Total # of Active Listings by Certified Body
     */
    @Async
    @Transactional
    public Future<List<CertifiedBodyStatistics>> getTotalActiveListingsByCertifiedBody(DateRange dateRange) {
        return new AsyncResult<>(listingStatisticsDAO.getTotalActiveListingsByCertifiedBody(dateRange));
    }

    /**
     * Total # of 2014 Listings (Regardless of Status)
     */
    @Async
    @Transactional
    public Future<Long> getTotal2014Listings(DateRange dateRange) {
        return new AsyncResult<>(listingStatisticsDAO
                .getTotalListingsByEditionAndStatus(dateRange, "2014", null));
    }

    /**
     * Total # of 2015 Listings (Regardless of Status)
     */
    @Async
    @Transactional
    public Future<Long> getTotal2015Listings(DateRange dateRange) {
        return new AsyncResult<>(listingStatisticsDAO
                .getTotalListingsByEditionAndStatus(dateRange, "2015", null));
    }

    /**
     * Total # of 2011 Listings (Will not be active)
     */
    @Async
    @Transactional
    public Future<Long> getTotal2011Listings(DateRange dateRange) {
        return new AsyncResult<>(listingStatisticsDAO
                .getTotalListingsByEditionAndStatus(dateRange, "2011", null));
    }

    /**
     * Total # of Surveillance Activities*
     */
    @Async
    @Transactional
    public Future<Long> getTotalSurveillanceActivities(DateRange dateRange) {
        return new AsyncResult<>(surveillanceStatisticsDAO.getTotalSurveillanceActivities(dateRange));
    }

    /**
     * Open Surveillance Activities
     */
    @Async
    @Transactional
    public Future<Long> getTotalOpenSurveillanceActivities(DateRange dateRange) {
        return new AsyncResult<>(surveillanceStatisticsDAO.getTotalOpenSurveillanceActivities(dateRange));
    }

    /**
     * Closed Surveillance Activities
     */
    @Async
    @Transactional
    public Future<Long> getTotalClosedSurveillanceActivities(DateRange dateRange) {
        return new AsyncResult<>(surveillanceStatisticsDAO.getTotalClosedSurveillanceActivities(dateRange));
    }

    /**
     * Total # of NCs
     */
    @Async
    @Transactional
    public Future<Long> getTotalNonConformities(DateRange dateRange) {
        return new AsyncResult<>(surveillanceStatisticsDAO.getTotalNonConformities(dateRange));
    }

    /**
     * Open NCs
     */
    @Async
    @Transactional
    public Future<Long> getTotalOpenNonconformities(DateRange dateRange) {
        return new AsyncResult<>(surveillanceStatisticsDAO.getTotalOpenNonconformities(dateRange));
    }

    /**
     * Closed NCs
     */
    @Async
    @Transactional
    public Future<Long> getTotalClosedNonconformities(DateRange dateRange) {
        return new AsyncResult<>(surveillanceStatisticsDAO.getTotalClosedNonconformities(dateRange));
    }

}
