package gov.healthit.chpl.scheduler.job.summarystatistics;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
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
    private CertifiedProductDAO certifiedProductDAO;

    @Autowired
    public AsynchronousSummaryStatistics(ListingStatisticsDAO listingStatisticsDAO, DeveloperStatisticsDAO developerStatisticsDAO,
            SurveillanceStatisticsDAO surveillanceStatisticsDAO, CertifiedProductDAO certifiedProductDAO) {
        this.listingStatisticsDAO = listingStatisticsDAO;
        this.developerStatisticsDAO = developerStatisticsDAO;
        this.surveillanceStatisticsDAO = surveillanceStatisticsDAO;
        this.certifiedProductDAO = certifiedProductDAO;
    }

    @Transactional
    public Long getTotalDevelopers(DateRange dateRange) {
        Long total = developerStatisticsDAO.getTotalDevelopers(dateRange);
        return total;
    }

    @Transactional
    public Long getTotalDevelopersWith2014Listings(DateRange dateRange) {
        Long total = developerStatisticsDAO.getTotalDevelopersWithListingsByEditionAndStatus(dateRange, "2014", null);
        return total;
    }

    @Transactional
    public Long getTotalDevelopersWith2015Listings(DateRange dateRange) {
        Long total = developerStatisticsDAO.getTotalDevelopersWithListingsByEditionAndStatus(dateRange, "2015", null);
        return total;
    }

    @Transactional
    public Long getTotalDevelopersWithActive2014Listings(DateRange dateRange) {
        List<String> activeStatuses = new ArrayList<String>();
        activeStatuses.add(CertificationStatusType.Active.getName().toUpperCase());
        activeStatuses.add(CertificationStatusType.SuspendedByAcb.getName().toUpperCase());
        activeStatuses.add(CertificationStatusType.SuspendedByOnc.getName().toUpperCase());
        Long total = developerStatisticsDAO.getTotalDevelopersWithListingsByEditionAndStatus(
                dateRange, "2014", activeStatuses);
        return total;
    }

    @Transactional
    public List<CertifiedBodyStatistics> getTotalDevelopersByCertifiedBodyWithListingsEachYear(DateRange dateRange) {
        List<CertifiedBodyStatistics> total = developerStatisticsDAO
                .getTotalDevelopersByCertifiedBodyWithListingsEachYear(dateRange);
        return total;
    }

    @Transactional
    public List<CertifiedBodyStatistics> getTotalDevelopersByCertifiedBodyWithListingsInEachCertificationStatusAndYear(DateRange dateRange) {
        List<CertifiedBodyStatistics> total = developerStatisticsDAO
                .getTotalDevelopersByCertifiedBodyWithListingsInEachCertificationStatusAndYear(dateRange);
        return total;
    }

    @Transactional
    public Long getTotalCertifiedProducts(DateRange dateRange) {
        Long total = listingStatisticsDAO.getTotalUniqueProductsByEditionAndStatus(dateRange, null, null);
        return total;
    }

    @Transactional
    public List<CertifiedBodyStatistics> getTotalCPListingsEachYearByCertifiedBody(DateRange dateRange) {
        List<CertifiedBodyStatistics> totals = listingStatisticsDAO.getTotalCPListingsEachYearByCertifiedBody(dateRange);
        return totals;
    }

    @Transactional
    public List<CertifiedBodyStatistics> getTotalCPListingsEachYearByCertifiedBodyAndCertificationStatus(DateRange dateRange) {
        List<CertifiedBodyStatistics> totals = listingStatisticsDAO
                .getTotalCPListingsEachYearByCertifiedBodyAndCertificationStatus(dateRange);
        return totals;
    }

    @Transactional
    public Long getTotalCPs2014Listings(DateRange dateRange) {
        Long total = listingStatisticsDAO
                .getTotalUniqueProductsByEditionAndStatus(dateRange, "2014", null);
        return total;
    }

    @Transactional
    public Long getTotalCPsActive2014Listings(DateRange dateRange) {
        List<String> activeStatuses = new ArrayList<String>();
        activeStatuses.add(CertificationStatusType.Active.getName().toUpperCase());
        Long total = listingStatisticsDAO
                .getTotalUniqueProductsByEditionAndStatus(dateRange, "2014", activeStatuses);
        return total;
    }

    @Transactional
    public Long getTotalCPsSuspended2014Listings(DateRange dateRange) {
        List<String> suspendedStatuses = new ArrayList<String>();
        suspendedStatuses.add(CertificationStatusType.SuspendedByAcb.getName().toUpperCase());
        suspendedStatuses.add(CertificationStatusType.SuspendedByOnc.getName().toUpperCase());
        Long total = listingStatisticsDAO
                .getTotalUniqueProductsByEditionAndStatus(dateRange, "2014", suspendedStatuses);
        return total;
    }

    @Transactional
    public Long getTotalCPsActive2015Listings(DateRange dateRange) {
        List<String> activeStatuses = new ArrayList<String>();
        activeStatuses.add(CertificationStatusType.Active.getName().toUpperCase());
        Long total = listingStatisticsDAO
                .getTotalUniqueProductsByEditionAndStatus(dateRange, "2015", activeStatuses);
        return total;
    }

    @Transactional
    public Long getTotalCPsActiveListings(DateRange dateRange) {
        List<String> activeStatuses = new ArrayList<String>();
        activeStatuses.add(CertificationStatusType.Active.getName().toUpperCase());
        Long total = listingStatisticsDAO
                .getTotalUniqueProductsByEditionAndStatus(dateRange, null, activeStatuses);
        return total;
    }

    @Transactional
    public Long getTotalListings(DateRange dateRange) {
        Long total = listingStatisticsDAO
                .getTotalListingsByEditionAndStatus(dateRange, null, null);
        return total;
    }

    @Transactional
    public Long getTotalActive2014Listings(DateRange dateRange) {
        List<String> activeStatuses = new ArrayList<String>();
        activeStatuses.add(CertificationStatusType.Active.getName().toUpperCase());
        activeStatuses.add(CertificationStatusType.SuspendedByAcb.getName().toUpperCase());
        activeStatuses.add(CertificationStatusType.SuspendedByOnc.getName().toUpperCase());
        Long total = listingStatisticsDAO
                .getTotalListingsByEditionAndStatus(dateRange, "2014", activeStatuses);
        return total;
    }

    @Transactional
    public Long getTotalActive2015Listings(DateRange dateRange) {
        List<String> activeStatuses = new ArrayList<String>();
        activeStatuses.add(CertificationStatusType.Active.getName().toUpperCase());
        activeStatuses.add(CertificationStatusType.SuspendedByAcb.getName().toUpperCase());
        activeStatuses.add(CertificationStatusType.SuspendedByOnc.getName().toUpperCase());
        Long total = listingStatisticsDAO
                .getTotalListingsByEditionAndStatus(dateRange, "2015", activeStatuses);
        return total;
    }

    @Transactional
    public Long getTotalListingsWithAlternateTestMethods() {
        Long total = listingStatisticsDAO.getTotalListingsWithAlternateTestMethods();
        return total;
    }

    @Transactional
    public List<CertifiedBodyAltTestStatistics> getTotalListingsWithCertifiedBodyAndAlternativeTestMethods() {
        List<CertifiedBodyAltTestStatistics> totals = listingStatisticsDAO
                .getTotalListingsWithCertifiedBodyAndAlternativeTestMethods();
        return totals;
    }

    @Transactional
    public List<CertifiedBodyStatistics> getTotalActiveListingsByCertifiedBody(DateRange dateRange) {
        List<CertifiedBodyStatistics> totals = listingStatisticsDAO.getTotalActiveListingsByCertifiedBody(dateRange);
        return totals;
    }

    @Transactional
    public Long getTotal2014Listings(DateRange dateRange) {
        Long total = listingStatisticsDAO.getTotalListingsByEditionAndStatus(dateRange, "2014", null);
        return total;
    }

    @Transactional
    public Long getTotal2015Listings(DateRange dateRange) {
        Long total = listingStatisticsDAO.getTotalListingsByEditionAndStatus(dateRange, "2015", null);
        return total;
    }

    @Transactional
    public Long getTotal2011Listings(DateRange dateRange) {
        Long total = listingStatisticsDAO.getTotalListingsByEditionAndStatus(dateRange, "2011", null);
        return total;
    }

    @Transactional
    public Long getTotalSurveillanceActivities(DateRange dateRange) {
        Long total = surveillanceStatisticsDAO.getTotalSurveillanceActivities(dateRange);
        return total;
    }

    @Transactional
    public Long getTotalOpenSurveillanceActivities(DateRange dateRange) {
        Long total = surveillanceStatisticsDAO.getTotalOpenSurveillanceActivities(dateRange);
        return total;
    }

    @Transactional
    public Long getTotalClosedSurveillanceActivities(DateRange dateRange) {
        Long total = surveillanceStatisticsDAO.getTotalClosedSurveillanceActivities(dateRange);
        return total;
    }

    @Transactional
    public Long getAverageTimeToCloseSurveillance() {
        List<SurveillanceEntity> surveillances = surveillanceStatisticsDAO.getAllSurveillances().stream()
                .filter(surv -> surv.getStartDate() != null
                && surv.getEndDate() != null)
                .collect(Collectors.toList());

        Long totalDuration = surveillances.stream()
                .map(surv -> Math.abs(ChronoUnit.DAYS.between(surv.getStartDate().toInstant(), surv.getEndDate().toInstant())))
                .collect(Collectors.summingLong(n -> n.longValue()));
        return totalDuration / surveillances.size();
    }

    @Transactional
    public Long getTotalNonConformities(DateRange dateRange) {
        Long total = surveillanceStatisticsDAO.getTotalNonConformities(dateRange);
        return total;
    }

    @Transactional
    public Long getTotalOpenNonconformities(DateRange dateRange) {
        Long total = surveillanceStatisticsDAO.getTotalOpenNonconformities(dateRange);
        return total;
    }

    @Transactional
    public Long getTotalClosedNonconformities(DateRange dateRange) {
        Long total = surveillanceStatisticsDAO.getTotalClosedNonconformities(dateRange);
        return total;
    }

    @Transactional
    public List<CertifiedBodyStatistics> getTotalOpenNonconformitiesByAcb(DateRange dateRange) {
        List<CertifiedBodyStatistics> totals = surveillanceStatisticsDAO.getTotalOpenNonconformitiesByAcb(dateRange);
        return totals;
    }

    @Transactional
    public List<CertifiedBodyStatistics> getTotalOpenSurveillancesByAcb(DateRange dateRange) {
        List<CertifiedBodyStatistics> totals = surveillanceStatisticsDAO.getTotalOpenSurveillanceActivitiesByAcb(dateRange);
        return totals;
    }

    @Transactional
    public Long getAverageTimeToAssessConformity() {
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
        return totalDuration / nonconformitiesWithDeterminationDate.size();
    }

    @Transactional
    public Long getAverageTimeToApproveCAP() {
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
        return totalDuration / nonconformities.size();
    }

    @Transactional
    public Long getAverageDurationOfCAP() {
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
        return totalDuration / nonconformities.size();
    }

    @Transactional
    public Long getAverageTimeFromCAPApprovalToSurveillanceClose(SurveillanceStatisticsDAO surveillanceStatisticsDAO) {
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
        return totalDuration / nonconformitiesWithDeterminationDate.size();
    }

    @Transactional
    public Long getAverageTimeFromCAPEndToSurveillanceClose() {
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

        return totalDuration / nonconformitiesWithDeterminationDate.size();
    }

    @Transactional
    public Long getAverageTimeFromSurveillanceOpenToSurveillanceClose() {
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

        return totalDuration / surveillances.size();
    }

    @Transactional
    public Map<Long, Long> getOpenCAPCountByAcb() {
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
        return openCAPCountByAcb;
    }

    @Transactional
    public Map<Long, Long> getClosedCAPCountByAcb() {
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
        return openCAPCountByAcb;
    }

    @Transactional
    public List<CertifiedBodyStatistics> getUniqueDevelopersCountFor2015ListingsByAcb(
            List<CertifiedProductDetailsDTO> certifiedProducts, Edition2015Criteria listingsToInclude) {
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
        return x;
    }

    @Transactional
    public List<CertifiedBodyStatistics> getUniqueDevelopersCountFor2015ActiveListingsByAcb(
            List<CertifiedProductDetailsDTO> certifiedProducts, Edition2015Criteria listingsToInclude) {
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
        return x;
    }

    @Transactional
    public List<CertifiedBodyStatistics> getUniqueDevelopersCountFor2015SuspendedListingsByAcb(
            List<CertifiedProductDetailsDTO> certifiedProducts, Edition2015Criteria listingsToInclude) {
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
        return x;
    }

    @Transactional
    public List<CertifiedBodyStatistics> getUniqueProductsCountFor2015ListingsByAcb(
            List<CertifiedProductDetailsDTO> certifiedProducts, Edition2015Criteria listingsToInclude) {
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
        return x;
    }

    @Transactional
    public List<CertifiedBodyStatistics> getUniqueProductsCountWithCuresUpdatedActiveListingsByAcb(
            List<CertifiedProductDetailsDTO> certifiedProducts, Edition2015Criteria listingsToInclude) {
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
        return x;
    }

    @Transactional
    public List<CertifiedBodyStatistics> getUniqueProductsCountWithCuresUpdatedSuspendedListingsByAcb(
            List<CertifiedProductDetailsDTO> certifiedProducts, Edition2015Criteria listingsToInclude) {
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
        return x;
    }

    @Transactional
    public List<CertifiedBodyStatistics> getActiveListingCountWithCuresUpdatedByAcb(
            List<CertifiedProductDetailsDTO> certifiedProducts) {
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
        return x;
    }

    @Transactional
    public List<CertifiedBodyStatistics> getListingCountFor2015AndAltTestMethodsByAcb(
            List<CertifiedProductDetailsDTO> certifiedProducts, CertificationResultDAO certificationResultDAO) {
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
        return x;
    }

    @Transactional
    public Long getAllListingsCountWithCuresUpdated(List<CertifiedProductDetailsDTO> certifiedProducts) {
        var x = certifiedProducts.stream()
                .filter(listing -> listing.getCuresUpdate())
                .count();

        return x;
    }

    @Transactional
    public Long getTotalDevelopersCountFor2015Listings(List<CertifiedProductDetailsDTO> listings, DateRange dateRange, Edition2015Criteria listingsToInclude) {
        //hql += "AND ((s.deleted = false AND s.creationDate <= :endDate) " + " OR "
        //        + "(s.deleted = true AND s.creationDate <= :endDate AND s.lastModifiedDate > :endDate)) ";


        Long x = listings.stream()
                .filter(listing -> includeListing(listing, listingsToInclude))
                .filter(listing -> isDateBeforeOrEqual(listing.getCreationDate(), dateRange.getEndDate()))
                .filter(distinctByKey(listing -> listing.getDeveloper().getId()))
                .collect(Collectors.counting());


        return x;
    }

    private boolean isDateBeforeOrEqual(Date date1, Date date2) {
        return date1.before(date2) || date1.equals(date2);
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
