package gov.healthit.chpl.scheduler.job.summarystatistics;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.CertificationResultDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.statistics.DeveloperStatisticsDAO;
import gov.healthit.chpl.dao.statistics.ListingStatisticsDAO;
import gov.healthit.chpl.dao.statistics.SurveillanceStatisticsDAO;
import gov.healthit.chpl.domain.DateRange;
import gov.healthit.chpl.domain.concept.NonconformityStatusConcept;
import gov.healthit.chpl.domain.statistics.CertifiedBodyAltTestStatistics;
import gov.healthit.chpl.domain.statistics.CertifiedBodyStatistics;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.entity.surveillance.SurveillanceEntity;
import gov.healthit.chpl.entity.surveillance.SurveillanceNonconformityEntity;
import lombok.var;

@Component
@EnableAsync
public class AsynchronousSummaryStatistics {
    private static final Long NONCONFORMITY_SURVEILLANCE_RESULT = 1L;
    private static final String ONC_TEST_METHOD = "ONC Test Method";

    private Logger logger;

    private ListingStatisticsDAO listingStatisticsDAO;
    private DeveloperStatisticsDAO developerStatisticsDAO;
    private SurveillanceStatisticsDAO surveillanceStatisticsDAO;

    @Autowired
    public AsynchronousSummaryStatistics(ListingStatisticsDAO listingStatisticsDAO, DeveloperStatisticsDAO developerStatisticsDAO, SurveillanceStatisticsDAO surveillanceStatisticsDAO) {
        this.listingStatisticsDAO = listingStatisticsDAO;
        this.developerStatisticsDAO = developerStatisticsDAO;
        this.surveillanceStatisticsDAO = surveillanceStatisticsDAO;
    }

    @Transactional
    @Async("jobAsyncDataExecutor")
    public Future<Long> getTotalDevelopers(DeveloperStatisticsDAO developerStatisticsDAO,
            DateRange dateRange) {
        Long total = developerStatisticsDAO.getTotalDevelopers(dateRange);
        return new AsyncResult<Long>(total);
    }

    //TODO: Need to fix method above
    @Transactional
    public Long getTotalDevelopers(DateRange dateRange) {
        Long total = developerStatisticsDAO.getTotalDevelopers(dateRange);
        return total;
    }

    @Transactional
    @Async("jobAsyncDataExecutor")
    public Future<Long> getTotalDevelopersWith2014Listings(DeveloperStatisticsDAO developerStatisticsDAO,
            DateRange dateRange) {
        Long total = developerStatisticsDAO.getTotalDevelopersWithListingsByEditionAndStatus(dateRange, "2014", null);
        return new AsyncResult<Long>(total);
    }

    //TODO: Need to fix method above
    @Transactional
    public Long getTotalDevelopersWith2014Listings(DateRange dateRange) {
        Long total = developerStatisticsDAO.getTotalDevelopersWithListingsByEditionAndStatus(dateRange, "2014", null);
        return total;
    }

    @Transactional
    public Long getTotalDevelopersWithActive2014Listings(DateRange dateRange) {
        logger.info("Starting #1");
        List<String> activeStatuses = new ArrayList<String>();
        activeStatuses.add(CertificationStatusType.Active.getName().toUpperCase());
        activeStatuses.add(CertificationStatusType.SuspendedByAcb.getName().toUpperCase());
        activeStatuses.add(CertificationStatusType.SuspendedByOnc.getName().toUpperCase());
        Long total = developerStatisticsDAO.getTotalDevelopersWithListingsByEditionAndStatus(
                dateRange, "2014", activeStatuses);
        logger.info("Completed #1");
        return total;
    }

    @Transactional
    @Async("jobAsyncDataExecutor")
    public Future<List<CertifiedBodyStatistics>> getTotalDevelopersByCertifiedBodyWithListingsEachYear(
            DeveloperStatisticsDAO developerStatisticsDAO, DateRange dateRange) {
        List<CertifiedBodyStatistics> total = developerStatisticsDAO
                .getTotalDevelopersByCertifiedBodyWithListingsEachYear(dateRange);
        return new AsyncResult<List<CertifiedBodyStatistics>>(total);
    }

    //TODO:  Handle the method above
    @Transactional
    public List<CertifiedBodyStatistics> getTotalDevelopersByCertifiedBodyWithListingsEachYear(DateRange dateRange) {
        List<CertifiedBodyStatistics> total = developerStatisticsDAO
                .getTotalDevelopersByCertifiedBodyWithListingsEachYear(dateRange);
        return total;
    }

    @Transactional
    @Async("jobAsyncDataExecutor")
    public Future<List<CertifiedBodyStatistics>> getTotalDevelopersByCertifiedBodyWithListingsInEachCertificationStatusAndYear(
            DeveloperStatisticsDAO developerStatisticsDAO, DateRange dateRange) {
        List<CertifiedBodyStatistics> total = developerStatisticsDAO
                .getTotalDevelopersByCertifiedBodyWithListingsInEachCertificationStatusAndYear(dateRange);
        return new AsyncResult<>(total);
    }

    // TODO: Handle the method above
    @Transactional
    public List<CertifiedBodyStatistics> getTotalDevelopersByCertifiedBodyWithListingsInEachCertificationStatusAndYear(DateRange dateRange) {
        List<CertifiedBodyStatistics> total = developerStatisticsDAO
                .getTotalDevelopersByCertifiedBodyWithListingsInEachCertificationStatusAndYear(dateRange);
        return total;
    }

    @Transactional
    @Async("jobAsyncDataExecutor")
    public Future<Long> getTotalDevelopersWith2015Listings(
            DeveloperStatisticsDAO developerStatisticsDAO, DateRange dateRange) {
        Long total = developerStatisticsDAO
                .getTotalDevelopersWithListingsByEditionAndStatus(dateRange, "2015", null);
        return new AsyncResult<>(total);
    }

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

    @Async("jobAsyncDataExecutor")
    @Transactional
    public Future<Long> getTotalCertifiedProducts(ListingStatisticsDAO listingStatisticsDAO,
            DateRange dateRange) {
        Long total = listingStatisticsDAO.getTotalUniqueProductsByEditionAndStatus(dateRange, null, null);
        return new AsyncResult<Long>(total);
    }

    //TODO: need to fix the method above
    @Transactional
    public Long getTotalCertifiedProducts(DateRange dateRange) {
        Long total = listingStatisticsDAO.getTotalUniqueProductsByEditionAndStatus(dateRange, null, null);
        return total;
    }

    @Transactional
    public List<CertifiedBodyStatistics> getTotalCPListingsEachYearByCertifiedBody(DateRange dateRange) {
        logger.info("Starting #2");
        List<CertifiedBodyStatistics> totals = listingStatisticsDAO.getTotalCPListingsEachYearByCertifiedBody(dateRange);
        logger.info("Completed #2");
        return totals;
    }

    @Transactional
    public List<CertifiedBodyStatistics> getTotalCPListingsEachYearByCertifiedBodyAndCertificationStatus(DateRange dateRange) {
        logger.info("Starting #3");
        List<CertifiedBodyStatistics> totals = listingStatisticsDAO
                .getTotalCPListingsEachYearByCertifiedBodyAndCertificationStatus(dateRange);
        logger.info("Completed #3");
        return totals;
    }

    @Async("jobAsyncDataExecutor")
    @Transactional
    public Long getTotalCPs2014Listings(DateRange dateRange) {
        Long total = listingStatisticsDAO
                .getTotalUniqueProductsByEditionAndStatus(dateRange, "2014", null);
        return total;
    }

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

    @Transactional
    public Long getTotalCPsSuspended2014Listings(DateRange dateRange) {
        logger.info("Starting #4");
        List<String> suspendedStatuses = new ArrayList<String>();
        suspendedStatuses.add(CertificationStatusType.SuspendedByAcb.getName().toUpperCase());
        suspendedStatuses.add(CertificationStatusType.SuspendedByOnc.getName().toUpperCase());
        Long total = listingStatisticsDAO
                .getTotalUniqueProductsByEditionAndStatus(dateRange, "2014", suspendedStatuses);
        logger.info("Completed #4");
        return total;
    }

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

    @Async("jobAsyncDataExecutor")
    @Transactional
    public Future<Long> getTotalListings(ListingStatisticsDAO listingStatisticsDAO,
            DateRange dateRange) {
        Long total = listingStatisticsDAO
                .getTotalListingsByEditionAndStatus(dateRange, null, null);
        return new AsyncResult<Long>(total);
    }

    @Transactional
    public Long getTotalActive2014Listings(DateRange dateRange) {
        logger.info("Starting #5");
        List<String> activeStatuses = new ArrayList<String>();
        activeStatuses.add(CertificationStatusType.Active.getName().toUpperCase());
        activeStatuses.add(CertificationStatusType.SuspendedByAcb.getName().toUpperCase());
        activeStatuses.add(CertificationStatusType.SuspendedByOnc.getName().toUpperCase());
        Long total = listingStatisticsDAO
                .getTotalListingsByEditionAndStatus(dateRange, "2014", activeStatuses);
        logger.info("Completed #5");
        return total;
    }

    @Transactional
    public Long getTotalActive2015Listings(DateRange dateRange) {
        logger.info("Starting #6");
        List<String> activeStatuses = new ArrayList<String>();
        activeStatuses.add(CertificationStatusType.Active.getName().toUpperCase());
        activeStatuses.add(CertificationStatusType.SuspendedByAcb.getName().toUpperCase());
        activeStatuses.add(CertificationStatusType.SuspendedByOnc.getName().toUpperCase());
        Long total = listingStatisticsDAO
                .getTotalListingsByEditionAndStatus(dateRange, "2015", activeStatuses);
        logger.info("Completed #6");
        return total;
    }

    @Transactional
    public Long getTotalListingsWithAlternateTestMethods() {
        logger.info("Starting #7");
        Long total = listingStatisticsDAO.getTotalListingsWithAlternateTestMethods();
        logger.info("Completed #7");
        return total;
    }

    @Transactional
    public List<CertifiedBodyAltTestStatistics> getTotalListingsWithCertifiedBodyAndAlternativeTestMethods() {
        logger.info("Starting #8");
        List<CertifiedBodyAltTestStatistics> totals = listingStatisticsDAO
                .getTotalListingsWithCertifiedBodyAndAlternativeTestMethods();
        logger.info("Completed #8");
        return totals;
    }

    @Transactional
    public List<CertifiedBodyStatistics> getTotalActiveListingsByCertifiedBody(DateRange dateRange) {
        logger.info("Starting #9");
        List<CertifiedBodyStatistics> totals = listingStatisticsDAO.getTotalActiveListingsByCertifiedBody(dateRange);
        logger.info("Completed #9");
        return totals;
    }

    @Async("jobAsyncDataExecutor")
    @Transactional
    public Future<Long> getTotal2014Listings(ListingStatisticsDAO listingStatisticsDAO,
            DateRange dateRange) {
        Long total = listingStatisticsDAO
                .getTotalListingsByEditionAndStatus(dateRange, "2014", null);
        return new AsyncResult<Long>(total);
    }

    //TODO: Need tofix the method above
    @Transactional
    public Long getTotal2014Listings(DateRange dateRange) {
        Long total = listingStatisticsDAO
                .getTotalListingsByEditionAndStatus(dateRange, "2014", null);
        return total;
    }

    @Async("jobAsyncDataExecutor")
    @Transactional
    public Future<Long> getTotal2015Listings(ListingStatisticsDAO listingStatisticsDAO,
            DateRange dateRange) {
        Long total = listingStatisticsDAO
                .getTotalListingsByEditionAndStatus(dateRange, "2015", null);
        return new AsyncResult<Long>(total);
    }

    @Async("jobAsyncDataExecutor")
    @Transactional
    public Future<Long> getTotal2011Listings(ListingStatisticsDAO listingStatisticsDAO,
            DateRange dateRange) {
        Long total = listingStatisticsDAO
                .getTotalListingsByEditionAndStatus(dateRange, "2011", null);
        return new AsyncResult<Long>(total);
    }

    @Async("jobAsyncDataExecutor")
    @Transactional
    public Future<Long> getTotalSurveillanceActivities(SurveillanceStatisticsDAO surveillanceStatisticsDAO,
            DateRange dateRange) {
        Long total = surveillanceStatisticsDAO.getTotalSurveillanceActivities(dateRange);
        return new AsyncResult<Long>(total);
    }

    @Async("jobAsyncDataExecutor")
    @Transactional
    public Future<Long> getTotalOpenSurveillanceActivities(SurveillanceStatisticsDAO surveillanceStatisticsDAO,
            DateRange dateRange) {
        Long total = surveillanceStatisticsDAO.getTotalOpenSurveillanceActivities(dateRange);
        return new AsyncResult<Long>(total);
    }

    //TODO: Need to fix the method above
    @Transactional
    public Long getTotalOpenSurveillanceActivities(DateRange dateRange) {
        Long total = surveillanceStatisticsDAO.getTotalOpenSurveillanceActivities(dateRange);
        return total;
    }

    @Async("jobAsyncDataExecutor")
    @Transactional
    public Future<Long> getTotalClosedSurveillanceActivities(SurveillanceStatisticsDAO surveillanceStatisticsDAO,
            DateRange dateRange) {
        Long total = surveillanceStatisticsDAO.getTotalClosedSurveillanceActivities(dateRange);
        return new AsyncResult<Long>(total);
    }

    @Transactional
    public Long getAverageTimeToCloseSurveillance() {
        logger.info("Starting #10");
        List<SurveillanceEntity> surveillances = surveillanceStatisticsDAO.getAllSurveillances().stream()
                .filter(surv -> surv.getStartDate() != null
                && surv.getEndDate() != null)
                .collect(Collectors.toList());

        Long totalDuration = surveillances.stream()
                .map(surv -> Math.abs(ChronoUnit.DAYS.between(surv.getStartDate().toInstant(), surv.getEndDate().toInstant())))
                .collect(Collectors.summingLong(n -> n.longValue()));
        logger.info("Completed #10");
        return totalDuration / surveillances.size();
    }

    @Async("jobAsyncDataExecutor")
    @Transactional
    public Future<Long> getTotalNonConformities(SurveillanceStatisticsDAO surveillanceStatisticsDAO,
            DateRange dateRange) {
        Long total = surveillanceStatisticsDAO.getTotalNonConformities(dateRange);
        return new AsyncResult<Long>(total);
    }

    @Async("jobAsyncDataExecutor")
    @Transactional
    public Future<Long> getTotalOpenNonconformities(SurveillanceStatisticsDAO surveillanceStatisticsDAO,
            DateRange dateRange) {
        Long total = surveillanceStatisticsDAO.getTotalOpenNonconformities(dateRange);
        return new AsyncResult<Long>(total);
    }

    //TODO: Need to fix method above
    @Transactional
    public Long getTotalOpenNonconformities(DateRange dateRange) {
        Long total = surveillanceStatisticsDAO.getTotalOpenNonconformities(dateRange);
        return total;
    }

    @Async("jobAsyncDataExecutor")
    @Transactional
    public Future<Long> getTotalClosedNonconformities(SurveillanceStatisticsDAO surveillanceStatisticsDAO,
            DateRange dateRange) {
        Long total = surveillanceStatisticsDAO.getTotalClosedNonconformities(dateRange);
        return new AsyncResult<Long>(total);
    }

    @Async("jobAsyncDataExecutor")
    @Transactional
    public Future<List<CertifiedBodyStatistics>> getTotalOpenNonconformitiesByAcb(
            SurveillanceStatisticsDAO surveillanceStatisticsDAO,
            DateRange dateRange) {
        List<CertifiedBodyStatistics> totals = surveillanceStatisticsDAO.getTotalOpenNonconformitiesByAcb(dateRange);
        return new AsyncResult<List<CertifiedBodyStatistics>>(totals);
    }

    //TODO: Fix the method above
    @Transactional
    public List<CertifiedBodyStatistics> getTotalOpenNonconformitiesByAcb(DateRange dateRange) {
        List<CertifiedBodyStatistics> totals = surveillanceStatisticsDAO.getTotalOpenNonconformitiesByAcb(dateRange);
        return totals;
    }

    @Async("jobAsyncDataExecutor")
    @Transactional
    public Future<List<CertifiedBodyStatistics>> getTotalOpenSurveillancesByAcb(
            SurveillanceStatisticsDAO surveillanceStatisticsDAO, DateRange dateRange) {

        List<CertifiedBodyStatistics> totals = surveillanceStatisticsDAO.getTotalOpenSurveillanceActivitiesByAcb(dateRange);
        return new AsyncResult<List<CertifiedBodyStatistics>>(totals);
    }

    //TODO: Need to handle the method above
    @Transactional
    public List<CertifiedBodyStatistics> getTotalOpenSurveillancesByAcb(DateRange dateRange) {
        List<CertifiedBodyStatistics> totals = surveillanceStatisticsDAO.getTotalOpenSurveillanceActivitiesByAcb(dateRange);
        return totals;
    }

    @Transactional
    public Long getAverageTimeToAssessConformity() {
        logger.info("Starting #11");
        List<SurveillanceEntity> surveillances = surveillanceStatisticsDAO.getAllSurveillancesWithNonconformities();

        List<SurveillanceNonconformityEntity> nonconformitiesWithDeterminationDate = surveillances.stream()
                .flatMap(surv -> surv.getSurveilledRequirements().stream())
                .flatMap(req -> req.getNonconformities().stream())
                .filter(nc -> nc.getDateOfDetermination() != null)
                .distinct()
                .collect(Collectors.toList());

        Long totalDuration = nonconformitiesWithDeterminationDate.stream()
                .map(nc -> getDaysToAssessNonconformtity(findSurveillanceForNonconformity(nc, surveillances), nc))
                .collect(Collectors.summingLong(n -> n.longValue()));
        logger.info("Completed #11");
        return totalDuration / nonconformitiesWithDeterminationDate.size();
    }

    @Transactional
    public Long getAverageTimeToApproveCAP() {
        logger.info("Starting #12");
        List<SurveillanceEntity> surveillances = surveillanceStatisticsDAO.getAllSurveillancesWithNonconformities();

        List<SurveillanceNonconformityEntity> nonconformities = surveillances.stream()
                .flatMap(surv -> surv.getSurveilledRequirements().stream())
                .flatMap(req -> req.getNonconformities().stream())
                .filter(nc -> nc.getDateOfDetermination() != null && nc.getCapApproval() != null)
                .distinct() // Want to figure out how to get rid of this
                .collect(Collectors.toList());

        Long totalDuration = nonconformities.stream()
                .map(nc -> Math
                        .abs(ChronoUnit.DAYS.between(nc.getDateOfDetermination().toInstant(), nc.getCapApproval().toInstant())))
                .collect(Collectors.summingLong(n -> n.longValue()));
        logger.info("Completed #12");
        return totalDuration / nonconformities.size();
    }

    @Transactional
    public Long getAverageDurationOfCAP() {
        logger.info("Starting #13");
        List<SurveillanceEntity> surveillances = surveillanceStatisticsDAO.getAllSurveillancesWithNonconformities();

        List<SurveillanceNonconformityEntity> nonconformities = surveillances.stream()
                .flatMap(surv -> surv.getSurveilledRequirements().stream())
                .flatMap(req -> req.getNonconformities().stream())
                .filter(nc -> nc.getCapApproval() != null)
                .distinct() // Want to figure out how to get rid of this
                .collect(Collectors.toList());

        Long totalDuration = nonconformities.stream()
                .map(nc -> Math
                        .abs(ChronoUnit.DAYS.between(
                                nc.getCapApproval().toInstant(),
                                nc.getCapEndDate() != null ? nc.getCapEndDate().toInstant() : Instant.now())))
                .collect(Collectors.summingLong(n -> n.longValue()));
        logger.info("Completed #13");
        return totalDuration / nonconformities.size();
    }

    @Transactional
    public Long getAverageTimeFromCAPApprovalToSurveillanceClose(SurveillanceStatisticsDAO surveillanceStatisticsDAO) {
        logger.info("Starting #14");
        List<SurveillanceEntity> surveillances = surveillanceStatisticsDAO.getAllSurveillancesWithNonconformities();

        List<SurveillanceNonconformityEntity> nonconformitiesWithDeterminationDate = surveillances.stream()
                .filter(surv -> surv.getEndDate() != null)
                .flatMap(surv -> surv.getSurveilledRequirements().stream())
                .flatMap(req -> req.getNonconformities().stream())
                .filter(nc -> nc.getCapApproval() != null)
                .distinct()
                .collect(Collectors.toList());

        Long totalDuration = nonconformitiesWithDeterminationDate.stream()
                .map(nc -> getDaysFromCAPApprovalToSurveillanceClose(findSurveillanceForNonconformity(nc, surveillances), nc))
                .collect(Collectors.summingLong(n -> n.longValue()));
        logger.info("Completed #14");
        return totalDuration / nonconformitiesWithDeterminationDate.size();
    }

    @Transactional
    public Long getAverageTimeFromCAPEndToSurveillanceClose() {
        logger.info("Starting #15");
        List<SurveillanceEntity> surveillances = surveillanceStatisticsDAO.getAllSurveillancesWithNonconformities();

        List<SurveillanceNonconformityEntity> nonconformitiesWithDeterminationDate = surveillances.stream()
                .filter(surv -> surv.getEndDate() != null)
                .flatMap(surv -> surv.getSurveilledRequirements().stream())
                .flatMap(req -> req.getNonconformities().stream())
                .filter(nc -> nc.getCapEndDate() != null
                && nc.getNonconformityStatus().getName().equals(NonconformityStatusConcept.CLOSED.getName()))
                .distinct()
                .collect(Collectors.toList());

        Long totalDuration = nonconformitiesWithDeterminationDate.stream()
                .map(nc -> getDaysFromCAPEndToSurveillanceClose(findSurveillanceForNonconformity(nc, surveillances), nc))
                .collect(Collectors.summingLong(n -> n.longValue()));

        logger.info("Completed #15");
        return totalDuration / nonconformitiesWithDeterminationDate.size();
    }

    @Transactional
    public Long getAverageTimeFromSurveillanceOpenToSurveillanceClose() {
        logger.info("Starting #16");
        List<SurveillanceEntity> surveillances = surveillanceStatisticsDAO.getAllSurveillances();

        List<SurveillanceNonconformityEntity> nonconformities = surveillances.stream()
                .filter(surv -> surv.getStartDate() != null
                && surv.getEndDate() != null)
                .flatMap(surv -> surv.getSurveilledRequirements().stream())
                .filter(req -> req.getSurveillanceResultTypeId().equals(NONCONFORMITY_SURVEILLANCE_RESULT))
                .flatMap(req -> req.getNonconformities().stream())
                .distinct()
                .collect(Collectors.toList());

        Long totalDuration = nonconformities.stream()
                .map(nc -> getDaysFromSurveillanceOpenToSurveillanceClose(findSurveillanceForNonconformity(nc, surveillances)))
                .collect(Collectors.summingLong(n -> n.longValue()));

        logger.info("Completed #16");
        return totalDuration / surveillances.size();
    }

    @Transactional
    public Map<Long, Long> getOpenCAPCountByAcb() {
        logger.info("Starting #17");
        List<SurveillanceEntity> surveillances = surveillanceStatisticsDAO.getAllSurveillancesWithNonconformities();

        Map<Long, Long> openCAPCountByAcb = surveillances.stream()
                .flatMap(surv -> surv.getSurveilledRequirements().stream())
                .flatMap(req -> req.getNonconformities().stream())
                .filter(nc -> nc.getCapApproval() != null
                && nc.getCapEndDate() == null)
                .distinct()
                .map(nc -> new NonconformanceStatistic(
                        findSurveillanceForNonconformity(nc, surveillances).getCertifiedProduct().getCertificationBodyId(), nc))
                .collect(Collectors.groupingBy(stat -> stat.getCertificationBodyId(), Collectors.counting()));
        logger.info("Completed #17");
        return openCAPCountByAcb;
    }

    @Transactional
    public Map<Long, Long> getClosedCAPCountByAcb() {
        logger.info("Starting #18");
        List<SurveillanceEntity> surveillances = surveillanceStatisticsDAO.getAllSurveillancesWithNonconformities();

        Map<Long, Long> openCAPCountByAcb = surveillances.stream()
                .flatMap(surv -> surv.getSurveilledRequirements().stream())
                .flatMap(req -> req.getNonconformities().stream())
                .filter(nc -> nc.getCapApproval() != null
                && nc.getCapEndDate() != null)
                .distinct()
                .map(nc -> new NonconformanceStatistic(
                        findSurveillanceForNonconformity(nc, surveillances).getCertifiedProduct().getCertificationBodyId(), nc))
                .collect(Collectors.groupingBy(stat -> stat.getCertificationBodyId(), Collectors.counting()));
        logger.info("Completed #18");
        return openCAPCountByAcb;
    }

    @Transactional
    public List<CertifiedBodyStatistics> getUniqueDevelopersCountFor2015ListingsByAcb(
            List<CertifiedProductDetailsDTO> certifiedProducts, Edition2015Criteria listingsToInclude) {
        logger.info("Starting #19");
        var x = certifiedProducts.stream()
                .filter(listing -> includeListing(listing, listingsToInclude))
                .collect(Collectors.groupingBy(CertifiedProductDetailsDTO::getCertificationBodyName, Collectors.toList()))
                .entrySet().stream()
                .map(entry -> {
                    CertifiedBodyStatistics stat = new CertifiedBodyStatistics();
                    stat.setName(entry.getKey());
                    stat.setTotalDevelopersWithListings(entry.getValue().stream()
                            .filter(distinctByKey(cp -> cp.getDeveloper().getId()))
                            .collect(Collectors.counting()));
                    return stat;
                })
                .collect(Collectors.toList());
        logger.info("Completed #19");
        return x;
    }

    @Transactional
    public List<CertifiedBodyStatistics> getUniqueDevelopersCountFor2015ActiveListingsByAcb(
            List<CertifiedProductDetailsDTO> certifiedProducts, Edition2015Criteria listingsToInclude) {
        logger.info("Starting #20");
        var x = certifiedProducts.stream()
                .filter(listing -> includeListing(listing, listingsToInclude)
                        && listing.getCertificationStatusName().equals(CertificationStatusType.Active.getName()))
                .collect(Collectors.groupingBy(CertifiedProductDetailsDTO::getCertificationBodyName, Collectors.toList()))
                .entrySet().stream()
                .map(entry -> {
                    CertifiedBodyStatistics stat = new CertifiedBodyStatistics();
                    stat.setName(entry.getKey());
                    stat.setTotalDevelopersWithListings(entry.getValue().stream()
                            .filter(distinctByKey(cp -> cp.getDeveloper().getId()))
                            .collect(Collectors.counting()));
                    return stat;
                })
                .collect(Collectors.toList());
        logger.info("Completed #20");
        return x;
    }

    @Transactional
    public List<CertifiedBodyStatistics> getUniqueDevelopersCountFor2015SuspendedListingsByAcb(
            List<CertifiedProductDetailsDTO> certifiedProducts, Edition2015Criteria listingsToInclude) {
        logger.info("Starting #21");
        var x = certifiedProducts.stream()
                .filter(cp -> includeListing(cp, listingsToInclude)
                        && (cp.getCertificationStatusName().equals(CertificationStatusType.SuspendedByAcb.getName())
                                || cp.getCertificationStatusName().equals(CertificationStatusType.SuspendedByOnc.getName())))
                .collect(Collectors.groupingBy(CertifiedProductDetailsDTO::getCertificationBodyName, Collectors.toList()))
                .entrySet().stream()
                .map(entry -> {
                    CertifiedBodyStatistics stat = new CertifiedBodyStatistics();
                    stat.setName(entry.getKey());
                    stat.setTotalDevelopersWithListings(entry.getValue().stream()
                            .filter(distinctByKey(cp -> cp.getDeveloper().getId()))
                            .collect(Collectors.counting()));
                    return stat;
                })
                .collect(Collectors.toList());
        logger.info("Completed #21");
        return x;
    }

    @Transactional
    public List<CertifiedBodyStatistics> getUniqueProductsCountFor2015ListingsByAcb(
            List<CertifiedProductDetailsDTO> certifiedProducts, Edition2015Criteria listingsToInclude) {
        logger.info("Starting #22");
        var x = certifiedProducts.stream()
                .filter(listing -> includeListing(listing, listingsToInclude))
                .collect(Collectors.groupingBy(CertifiedProductDetailsDTO::getCertificationBodyName, Collectors.toList()))
                .entrySet().stream()
                .map(entry -> {
                    CertifiedBodyStatistics stat = new CertifiedBodyStatistics();
                    stat.setName(entry.getKey());
                    stat.setTotalListings(entry.getValue().stream()
                            .filter(distinctByKey(cp -> cp.getProduct().getId()))
                            .collect(Collectors.counting()));
                    return stat;
                })
                .collect(Collectors.toList());
        logger.info("Completed #22");
        return x;
    }

    @Transactional
    public List<CertifiedBodyStatistics> getUniqueProductsCountWithCuresUpdatedActiveListingsByAcb(
            List<CertifiedProductDetailsDTO> certifiedProducts, Edition2015Criteria listingsToInclude) {
        logger.info("Starting #23");
        var x = certifiedProducts.stream()
                .filter(listing -> includeListing(listing, listingsToInclude)
                        && listing.getCertificationStatusName().equals(CertificationStatusType.Active.getName()))
                .collect(Collectors.groupingBy(CertifiedProductDetailsDTO::getCertificationBodyName, Collectors.toList()))
                .entrySet().stream()
                .map(entry -> {
                    CertifiedBodyStatistics stat = new CertifiedBodyStatistics();
                    stat.setName(entry.getKey());
                    stat.setTotalListings(entry.getValue().stream()
                            .filter(distinctByKey(cp -> cp.getProduct().getId()))
                            .collect(Collectors.counting()));
                    return stat;
                })
                .collect(Collectors.toList());
        logger.info("Completed #23");
        return x;
    }

    @Transactional
    public List<CertifiedBodyStatistics> getUniqueProductsCountWithCuresUpdatedSuspendedListingsByAcb(
            List<CertifiedProductDetailsDTO> certifiedProducts, Edition2015Criteria listingsToInclude) {
        logger.info("Starting #24");
        var x = certifiedProducts.stream()
                .filter(cp -> includeListing(cp, listingsToInclude)
                        && (cp.getCertificationStatusName().equals(CertificationStatusType.SuspendedByAcb.getName())
                                || cp.getCertificationStatusName().equals(CertificationStatusType.SuspendedByOnc.getName())))
                .collect(Collectors.groupingBy(CertifiedProductDetailsDTO::getCertificationBodyName, Collectors.toList()))
                .entrySet().stream()
                .map(entry -> {
                    CertifiedBodyStatistics stat = new CertifiedBodyStatistics();
                    stat.setName(entry.getKey());
                    stat.setTotalListings(entry.getValue().stream()
                            .filter(distinctByKey(cp -> cp.getProduct().getId()))
                            .collect(Collectors.counting()));
                    return stat;
                })
                .collect(Collectors.toList());
        logger.info("Completed #24");
        return x;
    }

    @Transactional
    public List<CertifiedBodyStatistics> getActiveListingCountWithCuresUpdatedByAcb(
            List<CertifiedProductDetailsDTO> certifiedProducts) {
        logger.info("Starting #25");
        var x = certifiedProducts.stream()
                .filter(cp -> cp.getCertificationStatusName().equals(CertificationStatusType.Active.getName())
                        || cp.getCertificationStatusName().equals(CertificationStatusType.SuspendedByAcb.getName())
                        || cp.getCertificationStatusName().equals(CertificationStatusType.SuspendedByOnc.getName()))
                .collect(Collectors.groupingBy(CertifiedProductDetailsDTO::getCertificationBodyName, Collectors.toList()))
                .entrySet().stream()
                .map(entry -> {
                    CertifiedBodyStatistics stat = new CertifiedBodyStatistics();
                    stat.setName(entry.getKey());
                    stat.setTotalListings(entry.getValue().stream()
                            .collect(Collectors.counting()));
                    return stat;
                })
                .collect(Collectors.toList());
        logger.info("Completed #25");
        return x;
    }

    @Transactional
    public List<CertifiedBodyStatistics> getListingCountFor2015AndAltTestMethodsByAcb(
            List<CertifiedProductDetailsDTO> certifiedProducts, CertificationResultDAO certificationResultDAO) {
        logger.info("Starting #26");
        var x = certifiedProducts.stream()
                .filter(cp -> doesListingHaveAlternativeTestMethod(cp.getId(), certificationResultDAO))
                .collect(Collectors.groupingBy(CertifiedProductDetailsDTO::getCertificationBodyName, Collectors.toList()))
                .entrySet().stream()
                .map(entry -> {
                    CertifiedBodyStatistics stat = new CertifiedBodyStatistics();
                    stat.setName(entry.getKey());
                    stat.setTotalListings(entry.getValue().stream()
                            .collect(Collectors.counting()));
                    return stat;
                })
                .collect(Collectors.toList());
        logger.info("Completed #26");
        return x;
    }

    @Transactional
    public Long getAllListingsCountWithCuresUpdated(List<CertifiedProductDetailsDTO> certifiedProducts) {
        logger.info("Starting #27");
        var x = certifiedProducts.stream()
                .filter(listing -> listing.getCuresUpdate())
                .count();

        logger.info("Completed #27");
        return x;
    }

    @Async("jobAsyncDataExecutor")
    @Transactional
    public Future<Long> getAllListingsCountWithCuresUpdatedWithAlternativeTestMethods(
            CertifiedProductDAO certifiedProductDAO, CertificationResultDAO certificationResultDAO) {
        return new AsyncResult<Long>(certifiedProductDAO.findByEdition("2015").stream()
                .filter(cp -> cp.getCuresUpdate()
                        && doesListingHaveAlternativeTestMethod(cp.getId(), certificationResultDAO))
                .count());
    }

    private boolean doesListingHaveAlternativeTestMethod(Long listingId, CertificationResultDAO certificationResultDAO) {
        return certificationResultDAO.getTestProceduresForListing(listingId).stream()
                .filter(crtp -> !crtp.getTestProcedure().getName().equals(ONC_TEST_METHOD))
                .findAny()
                .isPresent();
    }

    private class NonconformanceStatistic {
        private Long certificationBodyId;
        private SurveillanceNonconformityEntity nonconformity;

        public NonconformanceStatistic(Long certificationBodyId, SurveillanceNonconformityEntity nonconformity) {
            this.certificationBodyId = certificationBodyId;
            this.nonconformity = nonconformity;
        }

        public Long getCertificationBodyId() {
            return certificationBodyId;
        }

        public SurveillanceNonconformityEntity getNonconformity() {
            return nonconformity;
        }
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

    private Long getDaysFromCAPEndToSurveillanceClose(SurveillanceEntity surveillance,
            SurveillanceNonconformityEntity nonconformity) {
        return Math.abs(ChronoUnit.DAYS.between(
                nonconformity.getCapEndDate().toInstant(),
                surveillance.getEndDate().toInstant()));
    }

    private Long getDaysFromSurveillanceOpenToSurveillanceClose(SurveillanceEntity surveillance) {
        return Math.abs(ChronoUnit.DAYS.between(
                surveillance.getStartDate().toInstant(),
                surveillance.getEndDate().toInstant()));
    }

    private boolean includeListing(CertifiedProductDetailsDTO listing, Edition2015Criteria listinsToInclude) {
        switch (listinsToInclude) {
        case BOTH :
            return true;
        case CURES :
            return listing.getCuresUpdate();
        case NON_CURES :
            return !listing.getCuresUpdate();
        default :
            return false;
        }
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
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
