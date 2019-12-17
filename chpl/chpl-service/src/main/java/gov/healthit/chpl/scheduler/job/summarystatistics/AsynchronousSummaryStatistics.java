package gov.healthit.chpl.scheduler.job.summarystatistics;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import gov.healthit.chpl.entity.surveillance.SurveillanceEntity;
import gov.healthit.chpl.entity.surveillance.SurveillanceNonconformityEntity;

/**
 * Component that handles getting statistics data and return Futures of that data.
 * 
 * @author alarned
 *
 */
@Component
@EnableAsync
public class AsynchronousSummaryStatistics {
    // private static Logger LOGGER = LogManager.getLogger(AsynchronousSummaryStatistics.class);
    private Logger logger;

    /**
     * Total # of Unique Developers (Regardless of Edition).
     * 
     * @param developerStatisticsDAO
     *            DAO that provides access to developer statistics
     * @param dateRange
     *            the range of time to get statistics from
     * @return the statistic
     */
    @Transactional
    @Async("jobAsyncDataExecutor")
    public Future<Long> getTotalDevelopers(DeveloperStatisticsDAO developerStatisticsDAO,
            DateRange dateRange) {
        Long total = developerStatisticsDAO.getTotalDevelopers(dateRange);
        return new AsyncResult<Long>(total);
    }

    /**
     * Total # of Developers with 2014 Listings.
     * 
     * @param developerStatisticsDAO
     *            DAO that provides access to developer statistics
     * @param dateRange
     *            the range of time to get statistics from
     * @return the statistic
     */
    @Transactional
    @Async("jobAsyncDataExecutor")
    public Future<Long> getTotalDevelopersWith2014Listings(DeveloperStatisticsDAO developerStatisticsDAO,
            DateRange dateRange) {
        Long total = developerStatisticsDAO.getTotalDevelopersWithListingsByEditionAndStatus(dateRange, "2014", null);
        return new AsyncResult<Long>(total);
    }

    /**
     * Total # of Developers with Active 2014 Listings.
     * 
     * @param developerStatisticsDAO
     *            DAO that provides access to developer statistics
     * @param dateRange
     *            the range of time to get statistics from
     * @return the statistic
     */
    @Transactional
    @Async("jobAsyncDataExecutor")
    public Future<Long> getTotalDevelopersWithActive2014Listings(DeveloperStatisticsDAO developerStatisticsDAO,
            DateRange dateRange) {
        List<String> activeStatuses = new ArrayList<String>();
        activeStatuses.add(CertificationStatusType.Active.getName().toUpperCase());
        activeStatuses.add(CertificationStatusType.SuspendedByAcb.getName().toUpperCase());
        activeStatuses.add(CertificationStatusType.SuspendedByOnc.getName().toUpperCase());
        Long total = developerStatisticsDAO.getTotalDevelopersWithListingsByEditionAndStatus(
                dateRange, "2014", activeStatuses);
        return new AsyncResult<Long>(total);
    }

    /**
     * Total # of Developers by certified body with listings for each year.
     * 
     * @param developerStatisticsDAO
     *            DAO that provides access to developer statistics
     * @param dateRange
     *            the range of time to get statistics from
     * @return the statistic
     */
    @Transactional
    @Async("jobAsyncDataExecutor")
    public Future<List<CertifiedBodyStatistics>> getTotalDevelopersByCertifiedBodyWithListingsEachYear(
            DeveloperStatisticsDAO developerStatisticsDAO, DateRange dateRange) {
        List<CertifiedBodyStatistics> total = developerStatisticsDAO
                .getTotalDevelopersByCertifiedBodyWithListingsEachYear(dateRange);
        return new AsyncResult<List<CertifiedBodyStatistics>>(total);
    }

    /**
     * Total # of Developers by certified body with listings in each certification status and year.
     * 
     * @param developerStatisticsDAO
     *            DAO that provides access to developer statistics
     * @param dateRange
     *            the range of time to get statistics from
     * @return the statistic
     */
    @Transactional
    @Async("jobAsyncDataExecutor")
    public Future<List<CertifiedBodyStatistics>> getTotalDevelopersByCertifiedBodyWithListingsInEachCertificationStatusAndYear(
            DeveloperStatisticsDAO developerStatisticsDAO, DateRange dateRange) {
        List<CertifiedBodyStatistics> total = developerStatisticsDAO
                .getTotalDevelopersByCertifiedBodyWithListingsInEachCertificationStatusAndYear(dateRange);
        return new AsyncResult<>(total);
    }

    /**
     * Total # of Developers with 2015 Listings.
     * 
     * @param developerStatisticsDAO
     *            DAO that provides access to developer statistics
     * @param dateRange
     *            the range of time to get statistics from
     * @return the statistic
     */
    @Transactional
    @Async("jobAsyncDataExecutor")
    public Future<Long> getTotalDevelopersWith2015Listings(
            DeveloperStatisticsDAO developerStatisticsDAO, DateRange dateRange) {
        Long total = developerStatisticsDAO
                .getTotalDevelopersWithListingsByEditionAndStatus(dateRange, "2015", null);
        return new AsyncResult<>(total);
    }

    /**
     * Total # of Developers with Active 2015 Listings.
     * 
     * @param developerStatisticsDAO
     *            DAO that provides access to developer statistics
     * @param dateRange
     *            the range of time to get statistics from
     * @return the statistic
     */
    @Transactional
    @Async("jobAsyncDataExecutor")
    public Future<Long> getTotalDevelopersWithActive2015Listings(
            DeveloperStatisticsDAO developerStatisticsDAO, DateRange dateRange) {
        List<String> activeStatuses = new ArrayList<String>();
        activeStatuses.add(CertificationStatusType.Active.getName().toUpperCase());
        activeStatuses.add(CertificationStatusType.SuspendedByAcb.getName().toUpperCase());
        activeStatuses.add(CertificationStatusType.SuspendedByOnc.getName().toUpperCase());
        Long total = developerStatisticsDAO
                .getTotalDevelopersWithListingsByEditionAndStatus(dateRange, "2015", activeStatuses);
        return new AsyncResult<Long>(total);
    }

    /**
     * Total # of Certified Unique Products (Regardless of Status or Edition - Including 2011).
     * 
     * @param listingStatisticsDAO
     *            DAO that provides access to listing statistics
     * @param dateRange
     *            the range of time to get statistics from
     * @return the statistic
     */
    @Async("jobAsyncDataExecutor")
    @Transactional
    public Future<Long> getTotalCertifiedProducts(ListingStatisticsDAO listingStatisticsDAO,
            DateRange dateRange) {
        Long total = listingStatisticsDAO.getTotalUniqueProductsByEditionAndStatus(dateRange, null, null);
        return new AsyncResult<Long>(total);
    }

    /**
     * Total # of Certified Unique Products each year by certified body.
     * 
     * @param listingStatisticsDAO
     *            DAO that provides access to listing statistics
     * @param dateRange
     *            the range of time to get statistics from
     * @return the statistic
     */
    @Async("jobAsyncDataExecutor")
    @Transactional
    public Future<List<CertifiedBodyStatistics>> getTotalCPListingsEachYearByCertifiedBody(
            ListingStatisticsDAO listingStatisticsDAO, DateRange dateRange) {
        List<CertifiedBodyStatistics> totals = listingStatisticsDAO.getTotalCPListingsEachYearByCertifiedBody(dateRange);
        return new AsyncResult<List<CertifiedBodyStatistics>>(totals);
    }

    /**
     * Total # of Certified Unique Products each year by certified body and certification status.
     * 
     * @param listingStatisticsDAO
     *            DAO that provides access to listing statistics
     * @param dateRange
     *            the range of time to get statistics from
     * @return the statistic
     */
    @Async("jobAsyncDataExecutor")
    @Transactional
    public Future<List<CertifiedBodyStatistics>> getTotalCPListingsEachYearByCertifiedBodyAndCertificationStatus(
            ListingStatisticsDAO listingStatisticsDAO, DateRange dateRange) {
        List<CertifiedBodyStatistics> totals = listingStatisticsDAO
                .getTotalCPListingsEachYearByCertifiedBodyAndCertificationStatus(dateRange);
        return new AsyncResult<List<CertifiedBodyStatistics>>(totals);
    }

    /**
     * Total # of unique Products with 2014 Listings.
     * 
     * @param listingStatisticsDAO
     *            DAO that provides access to listing statistics
     * @param dateRange
     *            the range of time to get statistics from
     * @return the statistic
     */
    @Async("jobAsyncDataExecutor")
    @Transactional
    public Future<Long> getTotalCPs2014Listings(ListingStatisticsDAO listingStatisticsDAO,
            DateRange dateRange) {
        Long total = listingStatisticsDAO
                .getTotalUniqueProductsByEditionAndStatus(dateRange, "2014", null);
        return new AsyncResult<Long>(total);
    }

    /**
     * Total # of unique Products with Active 2014 Listings.
     * 
     * @param listingStatisticsDAO
     *            DAO that provides access to listing statistics
     * @param dateRange
     *            the range of time to get statistics from
     * @return the statistic
     */
    @Async("jobAsyncDataExecutor")
    @Transactional
    public Future<Long> getTotalCPsActive2014Listings(ListingStatisticsDAO listingStatisticsDAO,
            DateRange dateRange) {
        List<String> activeStatuses = new ArrayList<String>();
        activeStatuses.add(CertificationStatusType.Active.getName().toUpperCase());
        Long total = listingStatisticsDAO
                .getTotalUniqueProductsByEditionAndStatus(dateRange, "2014", activeStatuses);
        return new AsyncResult<Long>(total);
    }

    /**
     * Total # of unique Products with Suspended (by ONC and ONC-ACB) 2014 Listings.
     * 
     * @param listingStatisticsDAO
     *            DAO that provides access to listing statistics
     * @param dateRange
     *            the range of time to get statistics from
     * @return the statistic
     */
    @Async("jobAsyncDataExecutor")
    @Transactional
    public Future<Long> getTotalCPsSuspended2014Listings(ListingStatisticsDAO listingStatisticsDAO,
            DateRange dateRange) {
        List<String> suspendedStatuses = new ArrayList<String>();
        suspendedStatuses.add(CertificationStatusType.SuspendedByAcb.getName().toUpperCase());
        suspendedStatuses.add(CertificationStatusType.SuspendedByOnc.getName().toUpperCase());
        Long total = listingStatisticsDAO
                .getTotalUniqueProductsByEditionAndStatus(dateRange, "2014", suspendedStatuses);
        return new AsyncResult<Long>(total);
    }

    /**
     * Total # of unique Products with 2015 Listings.
     * 
     * @param listingStatisticsDAO
     *            DAO that provides access to listing statistics
     * @param dateRange
     *            the range of time to get statistics from
     * @return the statistic
     */
    @Async("jobAsyncDataExecutor")
    @Transactional
    public Future<Long> getTotalCPs2015Listings(ListingStatisticsDAO listingStatisticsDAO,
            DateRange dateRange) {
        Long total = listingStatisticsDAO
                .getTotalUniqueProductsByEditionAndStatus(dateRange, "2015", null);
        return new AsyncResult<Long>(total);
    }

    /**
     * Total # of unique Products with Active 2015 Listings.
     * 
     * @param listingStatisticsDAO
     *            DAO that provides access to listing statistics
     * @param dateRange
     *            the range of time to get statistics from
     * @return the statistic
     */
    @Async("jobAsyncDataExecutor")
    @Transactional
    public Future<Long> getTotalCPsActive2015Listings(ListingStatisticsDAO listingStatisticsDAO,
            DateRange dateRange) {
        List<String> activeStatuses = new ArrayList<String>();
        activeStatuses.add(CertificationStatusType.Active.getName().toUpperCase());
        Long total = listingStatisticsDAO
                .getTotalUniqueProductsByEditionAndStatus(dateRange, "2015", activeStatuses);
        return new AsyncResult<Long>(total);
    }

    /**
     * Total # of unique Products with Suspended (by ONC and ONC-ACB) 2015 Listings.
     * 
     * @param listingStatisticsDAO
     *            DAO that provides access to listing statistics
     * @param dateRange
     *            the range of time to get statistics from
     * @return the statistic
     */
    @Async("jobAsyncDataExecutor")
    @Transactional
    public Future<Long> getTotalCPsSuspended2015Listings(ListingStatisticsDAO listingStatisticsDAO,
            DateRange dateRange) {
        List<String> suspendedStatuses = new ArrayList<String>();
        suspendedStatuses.add(CertificationStatusType.SuspendedByAcb.getName().toUpperCase());
        suspendedStatuses.add(CertificationStatusType.SuspendedByOnc.getName().toUpperCase());
        Long total = listingStatisticsDAO
                .getTotalUniqueProductsByEditionAndStatus(dateRange, "2015", suspendedStatuses);
        return new AsyncResult<Long>(total);
    }

    /**
     * Total # of unique Products with Active Listings (Regardless of Edition).
     * 
     * @param listingStatisticsDAO
     *            DAO that provides access to listing statistics
     * @param dateRange
     *            the range of time to get statistics from
     * @return the statistic
     */
    @Async("jobAsyncDataExecutor")
    @Transactional
    public Future<Long> getTotalCPsActiveListings(ListingStatisticsDAO listingStatisticsDAO,
            DateRange dateRange) {
        List<String> activeStatuses = new ArrayList<String>();
        activeStatuses.add(CertificationStatusType.Active.getName().toUpperCase());
        Long total = listingStatisticsDAO
                .getTotalUniqueProductsByEditionAndStatus(dateRange, null, activeStatuses);
        return new AsyncResult<Long>(total);
    }

    /**
     * Total # of Listings (Regardless of Status or Edition).
     * 
     * @param listingStatisticsDAO
     *            DAO that provides access to listing statistics
     * @param dateRange
     *            the range of time to get statistics from
     * @return the statistic
     */
    @Async("jobAsyncDataExecutor")
    @Transactional
    public Future<Long> getTotalListings(ListingStatisticsDAO listingStatisticsDAO,
            DateRange dateRange) {
        Long total = listingStatisticsDAO
                .getTotalListingsByEditionAndStatus(dateRange, null, null);
        return new AsyncResult<Long>(total);
    }

    /**
     * Total # of Active 2014 Listings.
     * 
     * @param listingStatisticsDAO
     *            DAO that provides access to listing statistics
     * @param dateRange
     *            the range of time to get statistics from
     * @return the statistic
     */
    @Async("jobAsyncDataExecutor")
    @Transactional
    public Future<Long> getTotalActive2014Listings(ListingStatisticsDAO listingStatisticsDAO,
            DateRange dateRange) {
        List<String> activeStatuses = new ArrayList<String>();
        activeStatuses.add(CertificationStatusType.Active.getName().toUpperCase());
        activeStatuses.add(CertificationStatusType.SuspendedByAcb.getName().toUpperCase());
        activeStatuses.add(CertificationStatusType.SuspendedByOnc.getName().toUpperCase());
        Long total = listingStatisticsDAO
                .getTotalListingsByEditionAndStatus(dateRange, "2014", activeStatuses);
        return new AsyncResult<Long>(total);
    }

    /**
     * Total # of Active 2015 Listings.
     * 
     * @param listingStatisticsDAO
     *            DAO that provides access to listing statistics
     * @param dateRange
     *            the range of time to get statistics from
     * @return the statistic
     */
    @Async("jobAsyncDataExecutor")
    @Transactional
    public Future<Long> getTotalActive2015Listings(ListingStatisticsDAO listingStatisticsDAO,
            DateRange dateRange) {
        List<String> activeStatuses = new ArrayList<String>();
        activeStatuses.add(CertificationStatusType.Active.getName().toUpperCase());
        activeStatuses.add(CertificationStatusType.SuspendedByAcb.getName().toUpperCase());
        activeStatuses.add(CertificationStatusType.SuspendedByOnc.getName().toUpperCase());
        Long total = listingStatisticsDAO
                .getTotalListingsByEditionAndStatus(dateRange, "2015", activeStatuses);
        return new AsyncResult<Long>(total);
    }

    /**
     * Return the total number of listings that have Alternate Test Methods.
     * 
     * @param listingStatisticsDAO
     *            DAO that provides access to listing statistics
     * @return a number
     */
    @Async("jobAsyncDataExecutor")
    @Transactional
    public Future<Long> getTotalListingsWithAlternateTestMethods(ListingStatisticsDAO listingStatisticsDAO) {
        Long total = listingStatisticsDAO.getTotalListingsWithAlternateTestMethods();
        return new AsyncResult<Long>(total);
    }

    /**
     * Return the total number of listings with Alternate Test Methods by ACB.
     * 
     * @param listingStatisticsDAO
     *            DAO that provides access to listing statistics
     * @return a list of Statistic objects
     */
    @Async("jobAsyncDataExecutor")
    @Transactional
    public Future<List<CertifiedBodyAltTestStatistics>> getTotalListingsWithCertifiedBodyAndAlternativeTestMethods(
            ListingStatisticsDAO listingStatisticsDAO) {
        List<CertifiedBodyAltTestStatistics> totals = listingStatisticsDAO
                .getTotalListingsWithCertifiedBodyAndAlternativeTestMethods();
        return new AsyncResult<List<CertifiedBodyAltTestStatistics>>(totals);
    }

    /**
     * Total # of Active Listings by Certified Body.
     * 
     * @param listingStatisticsDAO
     *            DAO that provides access to listing statistics
     * @param dateRange
     *            the range of time to get statistics from
     * @return the statistic
     */
    @Async("jobAsyncDataExecutor")
    @Transactional
    public Future<List<CertifiedBodyStatistics>> getTotalActiveListingsByCertifiedBody(
            ListingStatisticsDAO listingStatisticsDAO, DateRange dateRange) {
        List<CertifiedBodyStatistics> totals = listingStatisticsDAO.getTotalActiveListingsByCertifiedBody(dateRange);
        return new AsyncResult<List<CertifiedBodyStatistics>>(totals);
    }

    /**
     * Total # of 2014 Listings (Regardless of Status).
     * 
     * @param listingStatisticsDAO
     *            DAO that provides access to listing statistics
     * @param dateRange
     *            the range of time to get statistics from
     * @return the statistic
     */
    @Async("jobAsyncDataExecutor")
    @Transactional
    public Future<Long> getTotal2014Listings(ListingStatisticsDAO listingStatisticsDAO,
            DateRange dateRange) {
        Long total = listingStatisticsDAO
                .getTotalListingsByEditionAndStatus(dateRange, "2014", null);
        return new AsyncResult<Long>(total);
    }

    /**
     * Total # of 2015 Listings (Regardless of Status).
     * 
     * @param listingStatisticsDAO
     *            DAO that provides access to listing statistics
     * @param dateRange
     *            the range of time to get statistics from
     * @return the statistic
     */
    @Async("jobAsyncDataExecutor")
    @Transactional
    public Future<Long> getTotal2015Listings(ListingStatisticsDAO listingStatisticsDAO,
            DateRange dateRange) {
        Long total = listingStatisticsDAO
                .getTotalListingsByEditionAndStatus(dateRange, "2015", null);
        return new AsyncResult<Long>(total);
    }

    /**
     * Total # of 2011 Listings (Will not be active).
     * 
     * @param listingStatisticsDAO
     *            DAO that provides access to listing statistics
     * @param dateRange
     *            the range of time to get statistics from
     * @return the statistic
     */
    @Async("jobAsyncDataExecutor")
    @Transactional
    public Future<Long> getTotal2011Listings(ListingStatisticsDAO listingStatisticsDAO,
            DateRange dateRange) {
        Long total = listingStatisticsDAO
                .getTotalListingsByEditionAndStatus(dateRange, "2011", null);
        return new AsyncResult<Long>(total);
    }

    /**
     * Total # of Surveillance Activities*.
     * 
     * @param surveillanceStatisticsDAO
     *            DAO that provides access to surveillance statistics
     * @param dateRange
     *            the range of time to get statistics from
     * @return the statistic
     */
    @Async("jobAsyncDataExecutor")
    @Transactional
    public Future<Long> getTotalSurveillanceActivities(SurveillanceStatisticsDAO surveillanceStatisticsDAO,
            DateRange dateRange) {
        Long total = surveillanceStatisticsDAO.getTotalSurveillanceActivities(dateRange);
        return new AsyncResult<Long>(total);
    }

    /**
     * Open Surveillance Activities.
     * 
     * @param surveillanceStatisticsDAO
     *            DAO that provides access to surveillance statistics
     * @param dateRange
     *            the range of time to get statistics from
     * @return the statistic
     */
    @Async("jobAsyncDataExecutor")
    @Transactional
    public Future<Long> getTotalOpenSurveillanceActivities(SurveillanceStatisticsDAO surveillanceStatisticsDAO,
            DateRange dateRange) {
        Long total = surveillanceStatisticsDAO.getTotalOpenSurveillanceActivities(dateRange);
        return new AsyncResult<Long>(total);
    }

    /**
     * Closed Surveillance Activities.
     * 
     * @param surveillanceStatisticsDAO
     *            DAO that provides access to surveillance statistics
     * @param dateRange
     *            the range of time to get statistics from
     * @return the statistic
     */
    @Async("jobAsyncDataExecutor")
    @Transactional
    public Future<Long> getTotalClosedSurveillanceActivities(SurveillanceStatisticsDAO surveillanceStatisticsDAO,
            DateRange dateRange) {
        Long total = surveillanceStatisticsDAO.getTotalClosedSurveillanceActivities(dateRange);
        return new AsyncResult<Long>(total);
    }

    /**
     * Total # of NCs.
     * 
     * @param surveillanceStatisticsDAO
     *            DAO that provides access to surveillance statistics
     * @param dateRange
     *            the range of time to get statistics from
     * @return the statistic
     */
    @Async("jobAsyncDataExecutor")
    @Transactional
    public Future<Long> getTotalNonConformities(SurveillanceStatisticsDAO surveillanceStatisticsDAO,
            DateRange dateRange) {
        Long total = surveillanceStatisticsDAO.getTotalNonConformities(dateRange);
        return new AsyncResult<Long>(total);
    }

    /**
     * Open NCs.
     * 
     * @param surveillanceStatisticsDAO
     *            DAO that provides access to surveillance statistics
     * @param dateRange
     *            the range of time to get statistics from
     * @return the statistic
     */
    @Async("jobAsyncDataExecutor")
    @Transactional
    public Future<Long> getTotalOpenNonconformities(SurveillanceStatisticsDAO surveillanceStatisticsDAO,
            DateRange dateRange) {
        Long total = surveillanceStatisticsDAO.getTotalOpenNonconformities(dateRange);
        return new AsyncResult<Long>(total);
    }

    /**
     * Closed NCs.
     * 
     * @param surveillanceStatisticsDAO
     *            DAO that provides access to surveillance statistics
     * @param dateRange
     *            the range of time to get statistics from
     * @return the statistic
     */
    @Async("jobAsyncDataExecutor")
    @Transactional
    public Future<Long> getTotalClosedNonconformities(SurveillanceStatisticsDAO surveillanceStatisticsDAO,
            DateRange dateRange) {
        Long total = surveillanceStatisticsDAO.getTotalClosedNonconformities(dateRange);
        return new AsyncResult<Long>(total);
    }

    /**
     * Open NCs by ACB.
     * 
     * @param surveillanceStatisticsDAO
     *            DAO that provides access to surveillance statistics
     * @param dateRange
     *            the range of time to get statistics from
     * @return the statistic
     */
    @Async("jobAsyncDataExecutor")
    @Transactional
    public Future<List<CertifiedBodyStatistics>> getTotalOpenNonconformitiesByAcb(
            SurveillanceStatisticsDAO surveillanceStatisticsDAO,
            DateRange dateRange) {
        List<CertifiedBodyStatistics> totals = surveillanceStatisticsDAO.getTotalOpenNonconformitiesByAcb(dateRange);
        return new AsyncResult<List<CertifiedBodyStatistics>>(totals);
    }

    /**
     * Open NCs by ACB.
     * 
     * @param surveillanceStatisticsDAO
     *            DAO that provides access to surveillance statistics
     * @param dateRange
     *            the range of time to get statistics from
     * @return the statistic
     */
    @Async("jobAsyncDataExecutor")
    @Transactional
    public Future<List<CertifiedBodyStatistics>> getTotalOpenSurveillancesByAcb(
            SurveillanceStatisticsDAO surveillanceStatisticsDAO, DateRange dateRange) {

        List<CertifiedBodyStatistics> totals = surveillanceStatisticsDAO.getTotalOpenSurveillanceActivitiesByAcb(dateRange);
        return new AsyncResult<List<CertifiedBodyStatistics>>(totals);
    }

    @Async("jobAsyncDataExecutor")
    @Transactional
    public Future<Long> getAverageTimeToAssessConformity(SurveillanceStatisticsDAO surveillanceStatisticsDAO) {

        List<SurveillanceEntity> surveillances = surveillanceStatisticsDAO.getAllSurveillances();

        List<SurveillanceNonconformityEntity> nonconformitiesWithDeterminationDate = surveillances.stream()
                .flatMap(surv -> surv.getSurveilledRequirements().stream())
                .flatMap(req -> req.getNonconformities().stream())
                .filter(nc -> nc.getDateOfDetermination() != null)
                .distinct()
                .collect(Collectors.toList());

        Long totalOpenDays = nonconformitiesWithDeterminationDate.stream()
                .map(nc -> getDaysToAssessNonconformtity(findSurveillanceForNonconformity(nc, surveillances), nc))
                // .peek(System.out::println)
                .collect(Collectors.summingLong(n -> n.longValue()));

        return new AsyncResult<Long>(totalOpenDays / nonconformitiesWithDeterminationDate.size());
    }

    @Async("jobAsyncDataExecutor")
    @Transactional
    public Future<Long> getAverageTimeToApproveCAP(SurveillanceStatisticsDAO surveillanceStatisticsDAO) {
        List<SurveillanceEntity> surveillances = surveillanceStatisticsDAO.getAllSurveillances();

        List<SurveillanceNonconformityEntity> nonconformities = surveillances.stream()
                .flatMap(surv -> surv.getSurveilledRequirements().stream())
                .flatMap(req -> req.getNonconformities().stream())
                .filter(nc -> nc.getDateOfDetermination() != null && nc.getCapApproval() != null)
                .distinct() // Want to figure out how to get rid of this
                .collect(Collectors.toList());

        Long totalDaysToApproveCap = nonconformities.stream()
                .map(nc -> Math
                        .abs(ChronoUnit.DAYS.between(nc.getDateOfDetermination().toInstant(), nc.getCapApproval().toInstant())))
                // .peek(System.out::println)
                .collect(Collectors.summingLong(n -> n.longValue()));

        return new AsyncResult<Long>(totalDaysToApproveCap / nonconformities.size());
    }

    @Async("jobAsyncDataExecutor")
    @Transactional
    public Future<Long> getAverageDurationOfCAP(SurveillanceStatisticsDAO surveillanceStatisticsDAO) {
        List<SurveillanceEntity> surveillances = surveillanceStatisticsDAO.getAllSurveillances();

        List<SurveillanceNonconformityEntity> nonconformities = surveillances.stream()
                .flatMap(surv -> surv.getSurveilledRequirements().stream())
                .flatMap(req -> req.getNonconformities().stream())
                .filter(nc -> nc.getCapApproval() != null)
                .distinct() // Want to figure out how to get rid of this
                .collect(Collectors.toList());

        Long totalDaysOfCapDuration = nonconformities.stream()
                .map(nc -> Math
                        .abs(ChronoUnit.DAYS.between(
                                nc.getCapApproval().toInstant(),
                                nc.getCapEndDate() != null ? nc.getCapEndDate().toInstant() : Instant.now())))
                // .peek(System.out::println)
                .collect(Collectors.summingLong(n -> n.longValue()));

        return new AsyncResult<Long>(totalDaysOfCapDuration / nonconformities.size());
    }

    @Async("jobAsyncDataExecutor")
    @Transactional
    public Future<Long> getAverageTimeFromCAPApprovalToSurveillanceClose(SurveillanceStatisticsDAO surveillanceStatisticsDAO) {

        List<SurveillanceEntity> surveillances = surveillanceStatisticsDAO.getAllSurveillances();

        List<SurveillanceNonconformityEntity> nonconformitiesWithDeterminationDate = surveillances.stream()
                .filter(surv -> surv.getEndDate() != null)
                .flatMap(surv -> surv.getSurveilledRequirements().stream())
                .flatMap(req -> req.getNonconformities().stream())
                .filter(nc -> nc.getCapApproval() != null)
                .distinct()
                .collect(Collectors.toList());

        Long totalOpenDays = nonconformitiesWithDeterminationDate.stream()
                .map(nc -> getDaysFromCAPApprovalToSurveillanceClose(findSurveillanceForNonconformity(nc, surveillances), nc))
                // .peek(System.out::println)
                .collect(Collectors.summingLong(n -> n.longValue()));

        return new AsyncResult<Long>(totalOpenDays / nonconformitiesWithDeterminationDate.size());
    }

    private SurveillanceEntity findSurveillanceForNonconformity(SurveillanceNonconformityEntity nonconformity,
            List<SurveillanceEntity> surveillances) {

        return surveillances.stream()
                .filter(surv -> surv.getSurveilledRequirements().stream()
                        .anyMatch(req -> req.getNonconformities().stream()
                                .anyMatch(nc -> nc.getId().equals(nonconformity.getId()))))
                .findFirst()
                .orElse(null);
    }

    private Long getDaysToAssessNonconformtity(SurveillanceEntity surveillance, SurveillanceNonconformityEntity nonconformity) {
        return Math.abs(ChronoUnit.DAYS.between(surveillance.getStartDate().toInstant(),
                nonconformity.getDateOfDetermination().toInstant()));
    }

    private Long getDaysFromCAPApprovalToSurveillanceClose(SurveillanceEntity surveillance,
            SurveillanceNonconformityEntity nonconformity) {
        return Math.abs(ChronoUnit.DAYS.between(
                nonconformity.getCapApproval().toInstant(),
                surveillance.getEndDate().toInstant()));
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public Logger getLogger() {
        if (logger == null) {
            logger = LogManager.getLogger(AsynchronousSummaryStatistics.class);
        }
        return logger;
    }
}
