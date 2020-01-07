package gov.healthit.chpl.scheduler.job.summarystatistics;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import gov.healthit.chpl.domain.concept.NonconformityStatusConcept;
import gov.healthit.chpl.domain.statistics.CertifiedBodyAltTestStatistics;
import gov.healthit.chpl.domain.statistics.CertifiedBodyStatistics;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.entity.surveillance.SurveillanceEntity;
import gov.healthit.chpl.entity.surveillance.SurveillanceNonconformityEntity;

@Component
@EnableAsync
public class AsynchronousSummaryStatistics {
    private Logger logger;

    @Transactional
    @Async("jobAsyncDataExecutor")
    public Future<Long> getTotalDevelopers(DeveloperStatisticsDAO developerStatisticsDAO,
            DateRange dateRange) {
        Long total = developerStatisticsDAO.getTotalDevelopers(dateRange);
        return new AsyncResult<Long>(total);
    }

    @Transactional
    @Async("jobAsyncDataExecutor")
    public Future<Long> getTotalDevelopersWith2014Listings(DeveloperStatisticsDAO developerStatisticsDAO,
            DateRange dateRange) {
        Long total = developerStatisticsDAO.getTotalDevelopersWithListingsByEditionAndStatus(dateRange, "2014", null);
        return new AsyncResult<Long>(total);
    }

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

    @Transactional
    @Async("jobAsyncDataExecutor")
    public Future<List<CertifiedBodyStatistics>> getTotalDevelopersByCertifiedBodyWithListingsEachYear(
            DeveloperStatisticsDAO developerStatisticsDAO, DateRange dateRange) {
        List<CertifiedBodyStatistics> total = developerStatisticsDAO
                .getTotalDevelopersByCertifiedBodyWithListingsEachYear(dateRange);
        return new AsyncResult<List<CertifiedBodyStatistics>>(total);
    }

    @Transactional
    @Async("jobAsyncDataExecutor")
    public Future<List<CertifiedBodyStatistics>> getTotalDevelopersByCertifiedBodyWithListingsInEachCertificationStatusAndYear(
            DeveloperStatisticsDAO developerStatisticsDAO, DateRange dateRange) {
        List<CertifiedBodyStatistics> total = developerStatisticsDAO
                .getTotalDevelopersByCertifiedBodyWithListingsInEachCertificationStatusAndYear(dateRange);
        return new AsyncResult<>(total);
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

    @Async("jobAsyncDataExecutor")
    @Transactional
    public Future<List<CertifiedBodyStatistics>> getTotalCPListingsEachYearByCertifiedBody(
            ListingStatisticsDAO listingStatisticsDAO, DateRange dateRange) {
        List<CertifiedBodyStatistics> totals = listingStatisticsDAO.getTotalCPListingsEachYearByCertifiedBody(dateRange);
        return new AsyncResult<List<CertifiedBodyStatistics>>(totals);
    }

    @Async("jobAsyncDataExecutor")
    @Transactional
    public Future<List<CertifiedBodyStatistics>> getTotalCPListingsEachYearByCertifiedBodyAndCertificationStatus(
            ListingStatisticsDAO listingStatisticsDAO, DateRange dateRange) {
        List<CertifiedBodyStatistics> totals = listingStatisticsDAO
                .getTotalCPListingsEachYearByCertifiedBodyAndCertificationStatus(dateRange);
        return new AsyncResult<List<CertifiedBodyStatistics>>(totals);
    }

    @Async("jobAsyncDataExecutor")
    @Transactional
    public Future<Long> getTotalCPs2014Listings(ListingStatisticsDAO listingStatisticsDAO,
            DateRange dateRange) {
        Long total = listingStatisticsDAO
                .getTotalUniqueProductsByEditionAndStatus(dateRange, "2014", null);
        return new AsyncResult<Long>(total);
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

    @Async("jobAsyncDataExecutor")
    @Transactional
    public Future<Long> getTotalCPs2015Listings(ListingStatisticsDAO listingStatisticsDAO,
            DateRange dateRange) {
        Long total = listingStatisticsDAO
                .getTotalUniqueProductsByEditionAndStatus(dateRange, "2015", null);
        return new AsyncResult<Long>(total);
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
    public Future<Long> getTotalCPsSuspended2015Listings(ListingStatisticsDAO listingStatisticsDAO,
            DateRange dateRange) {
        List<String> suspendedStatuses = new ArrayList<String>();
        suspendedStatuses.add(CertificationStatusType.SuspendedByAcb.getName().toUpperCase());
        suspendedStatuses.add(CertificationStatusType.SuspendedByOnc.getName().toUpperCase());
        Long total = listingStatisticsDAO
                .getTotalUniqueProductsByEditionAndStatus(dateRange, "2015", suspendedStatuses);
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

    @Async("jobAsyncDataExecutor")
    @Transactional
    public Future<Long> getTotalListingsWithAlternateTestMethods(ListingStatisticsDAO listingStatisticsDAO) {
        Long total = listingStatisticsDAO.getTotalListingsWithAlternateTestMethods();
        return new AsyncResult<Long>(total);
    }

    @Async("jobAsyncDataExecutor")
    @Transactional
    public Future<List<CertifiedBodyAltTestStatistics>> getTotalListingsWithCertifiedBodyAndAlternativeTestMethods(
            ListingStatisticsDAO listingStatisticsDAO) {
        List<CertifiedBodyAltTestStatistics> totals = listingStatisticsDAO
                .getTotalListingsWithCertifiedBodyAndAlternativeTestMethods();
        return new AsyncResult<List<CertifiedBodyAltTestStatistics>>(totals);
    }

    @Async("jobAsyncDataExecutor")
    @Transactional
    public Future<List<CertifiedBodyStatistics>> getTotalActiveListingsByCertifiedBody(
            ListingStatisticsDAO listingStatisticsDAO, DateRange dateRange) {
        List<CertifiedBodyStatistics> totals = listingStatisticsDAO.getTotalActiveListingsByCertifiedBody(dateRange);
        return new AsyncResult<List<CertifiedBodyStatistics>>(totals);
    }

    @Async("jobAsyncDataExecutor")
    @Transactional
    public Future<Long> getTotal2014Listings(ListingStatisticsDAO listingStatisticsDAO,
            DateRange dateRange) {
        Long total = listingStatisticsDAO
                .getTotalListingsByEditionAndStatus(dateRange, "2014", null);
        return new AsyncResult<Long>(total);
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

    @Async("jobAsyncDataExecutor")
    @Transactional
    public Future<Long> getTotalClosedSurveillanceActivities(SurveillanceStatisticsDAO surveillanceStatisticsDAO,
            DateRange dateRange) {
        Long total = surveillanceStatisticsDAO.getTotalClosedSurveillanceActivities(dateRange);
        return new AsyncResult<Long>(total);
    }

    @Async("jobAsyncDataExecutor")
    @Transactional
    public Future<Long> getAverageTimeToCloseSurveillance(
            SurveillanceStatisticsDAO surveillanceStatisticsDAO) {

        List<SurveillanceEntity> surveillances = surveillanceStatisticsDAO.getAllSurveillancesWithNonconformities().stream()
                .filter(surv -> surv.getStartDate() != null
                        && surv.getEndDate() != null)
                .collect(Collectors.toList());

        Long totalDuration = surveillances.stream()
                .map(surv -> Math.abs(ChronoUnit.DAYS.between(surv.getStartDate().toInstant(), surv.getEndDate().toInstant())))
                .collect(Collectors.summingLong(n -> n.longValue()));

        return new AsyncResult<Long>(totalDuration / surveillances.size());
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

        return new AsyncResult<Long>(totalDuration / nonconformitiesWithDeterminationDate.size());
    }

    @Async("jobAsyncDataExecutor")
    @Transactional
    public Future<Long> getAverageTimeToApproveCAP(SurveillanceStatisticsDAO surveillanceStatisticsDAO) {
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

        return new AsyncResult<Long>(totalDuration / nonconformities.size());
    }

    @Async("jobAsyncDataExecutor")
    @Transactional
    public Future<Long> getAverageDurationOfCAP(SurveillanceStatisticsDAO surveillanceStatisticsDAO) {
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

        return new AsyncResult<Long>(totalDuration / nonconformities.size());
    }

    @Async("jobAsyncDataExecutor")
    @Transactional
    public Future<Long> getAverageTimeFromCAPApprovalToSurveillanceClose(SurveillanceStatisticsDAO surveillanceStatisticsDAO) {

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

        return new AsyncResult<Long>(totalDuration / nonconformitiesWithDeterminationDate.size());
    }

    @Async("jobAsyncDataExecutor")
    @Transactional
    public Future<Long> getAverageTimeFromCAPEndToSurveillanceClose(SurveillanceStatisticsDAO surveillanceStatisticsDAO) {

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

        return new AsyncResult<Long>(totalDuration / nonconformitiesWithDeterminationDate.size());
    }

    @Async("jobAsyncDataExecutor")
    @Transactional
    public Future<Long> getAverageTimeFromSurveillanceOpenToSurveillanceClose(
            SurveillanceStatisticsDAO surveillanceStatisticsDAO) {

        List<SurveillanceEntity> surveillances = surveillanceStatisticsDAO.getAllSurveillances().stream()
                .filter(surv -> surv.getStartDate() != null
                        && surv.getEndDate() != null)
                .collect(Collectors.toList());

        Long totalDuration = surveillances.stream()
                .map(surv -> Math.abs(ChronoUnit.DAYS.between(surv.getStartDate().toInstant(),
                        surv.getEndDate().toInstant())))
                .collect(Collectors.summingLong(n -> n.longValue()));

        return new AsyncResult<Long>(totalDuration / surveillances.size());
    }

    @Async("jobAsyncDataExecutor")
    @Transactional
    public Future<Map<Long, Long>> getOpenCAPCountByAcb(
            SurveillanceStatisticsDAO surveillanceStatisticsDAO) {

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

        return new AsyncResult<Map<Long, Long>>(openCAPCountByAcb);
    }

    @Async("jobAsyncDataExecutor")
    @Transactional
    public Future<Map<Long, Long>> getClosedCAPCountByAcb(
            SurveillanceStatisticsDAO surveillanceStatisticsDAO) {

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

        return new AsyncResult<Map<Long, Long>>(openCAPCountByAcb);
    }

    class NonconformanceStatistic {
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
