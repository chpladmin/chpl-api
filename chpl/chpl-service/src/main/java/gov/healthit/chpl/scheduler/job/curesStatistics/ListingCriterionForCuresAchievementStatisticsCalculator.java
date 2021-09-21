package gov.healthit.chpl.scheduler.job.curesStatistics;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.dao.statistics.ListingToCriterionForCuresAchievementStatisticsDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.statistics.ListingToCriterionForCuresAchievementStatistic;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.service.CertificationCriterionService.Criteria2015;
import gov.healthit.chpl.service.CuresUpdateService;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2(topic = "curesStatisticsCreatorJobLogger")
public class ListingCriterionForCuresAchievementStatisticsCalculator {
    private CertificationCriterionService certService;
    private CuresUpdateService curesUpdateService;
    private ListingToCriterionForCuresAchievementStatisticsDAO listingToCuresAchievementDao;
    private CertifiedProductDetailsManager cpdManager;
    private Long b6Id;
    private List<Long> privacyAndSecurityCriteriaIds;
    private List<Long> privacyAndSecurityRequiredCriteriaIds;

    @Autowired
    public ListingCriterionForCuresAchievementStatisticsCalculator(CertificationCriterionService certService,
            CuresUpdateService curesUpdateService,
            ListingToCriterionForCuresAchievementStatisticsDAO listingToCuresAchievementDao,
            CertifiedProductDetailsManager cpdManager,
            @Value("${privacyAndSecurityCriteria}") String privacyAndSecurityCriteriaIdList,
            @Value("${privacyAndSecurityRequiredCriteria}") String privacyAndSecurityRequiredCriteriaIdList) {
        this.certService = certService;
        this.curesUpdateService = curesUpdateService;
        this.listingToCuresAchievementDao = listingToCuresAchievementDao;
        this.cpdManager = cpdManager;

        this.b6Id = certService.get(Criteria2015.B_6).getId();
        if (!StringUtils.isEmpty(privacyAndSecurityCriteriaIdList)) {
            privacyAndSecurityCriteriaIds = Stream.of(privacyAndSecurityCriteriaIdList.split(","))
                .map(criterionId -> new Long(criterionId))
                .collect(Collectors.toList());
        } else {
            LOGGER.error("No value found for privacyAndSecurityCriteria property");
        }
        if (!StringUtils.isEmpty(privacyAndSecurityRequiredCriteriaIdList)) {
            privacyAndSecurityRequiredCriteriaIds = Stream.of(privacyAndSecurityRequiredCriteriaIdList.split(","))
                    .map(criterionId -> new Long(criterionId))
                    .collect(Collectors.toList());
        } else {
            LOGGER.error("No value found for privacyAndSecurityRequiredCriteria property");
        }
    }

    @Transactional
    public boolean hasStatisticsForDate(LocalDate statisticDate) {
        List<ListingToCriterionForCuresAchievementStatistic> statisticsForDate
            = listingToCuresAchievementDao.getStatisticsForDate(statisticDate);
        return statisticsForDate != null && statisticsForDate.size() > 0;
    }

    @Transactional
    public List<ListingToCriterionForCuresAchievementStatistic> calculateCurrentStatistics(LocalDate statisticDate) {
        List<Long> listingIdsWithoutCuresUpdate = listingToCuresAchievementDao.getListingIdsWithoutCuresUpdateStatus();
        LOGGER.info("There are " + listingIdsWithoutCuresUpdate.size() + " Active listings without cures update status.");
        List<ListingToCriterionForCuresAchievementStatistic> statistics
            = listingIdsWithoutCuresUpdate.stream()
                .map(listingId -> getListingDetails(listingId))
                .filter(listing -> listing != null)
                .map(listing -> calculateCurrentStatistic(statisticDate, listing))
                .flatMap(List::stream)
                .collect(Collectors.toList());
        return statistics;
    }

    private CertifiedProductSearchDetails getListingDetails(Long listingId) {
        CertifiedProductSearchDetails details = null;
        try {
            details = cpdManager.getCertifiedProductDetailsUsingCache(listingId);
        } catch (EntityRetrievalException ex) {
            LOGGER.error("Could not find listing with ID " + listingId, ex);
        }
        LOGGER.info("Got listing details for " + listingId);
        return details;
    }

    private List<ListingToCriterionForCuresAchievementStatistic> calculateCurrentStatistic(LocalDate statisticDate, CertifiedProductSearchDetails listing) {
        List<ListingToCriterionForCuresAchievementStatistic> statisticsWithNeededCuresCriterion
            = new ArrayList<ListingToCriterionForCuresAchievementStatistic>();
        LOGGER.info("Getting criterion needed for Cures status for listing " + listing.getId());
        List<CertificationCriterionDTO> neededCriteria = getCriterionNeededForCures(listing);
        if (neededCriteria != null && neededCriteria.size() > 0) {
            for (CertificationCriterionDTO criterion : neededCriteria) {
                statisticsWithNeededCuresCriterion.add(ListingToCriterionForCuresAchievementStatistic.builder()
                        .statisticDate(statisticDate)
                        .listingId(listing.getId())
                        .criterion(criterion)
                    .build());
            }
        }
        LOGGER.info("Listing " + listing.getId() + " needs updates to " + statisticsWithNeededCuresCriterion.size() + " criteria.");
        LOGGER.info("\t" + statisticsWithNeededCuresCriterion.stream().map(stat -> stat.getCriterion().getNumber()).collect(Collectors.joining(",")));
        return statisticsWithNeededCuresCriterion;
    }

    private List<CertificationCriterionDTO> getCriterionNeededForCures(CertifiedProductSearchDetails listing) {
        List<CertificationCriterionDTO> neededCriterion = new ArrayList<CertificationCriterionDTO>();
        neededCriterion.addAll(getCriteriaNeedingUpdates(listing));
        if (hasCriteriaRequiringPrivacyAndSecurity(listing)) {
            neededCriterion.addAll(getPrivacyAndSecurityRequiredCriteria(listing));
        }
        return neededCriterion;
    }

    private List<CertificationCriterionDTO> getCriteriaNeedingUpdates(CertifiedProductSearchDetails listing) {
        List<Long> criteriaIdsNeedingUpdate = curesUpdateService.getNeedsToBeUpdatedCriteriaIds();
        //in addition to whatever criteria need updated, b6 will have to be removed.
        criteriaIdsNeedingUpdate.add(b6Id);
        List<Long> attestedCriterionIdsNeedingUpdate = listing.getCertificationResults().stream()
            .filter(certResult -> certResult.isSuccess())
            .map(attestedCertResult -> attestedCertResult.getCriterion().getId())
            .filter(attestedCriterionId -> criteriaIdsNeedingUpdate.contains(attestedCriterionId))
            .collect(Collectors.toList());
        return attestedCriterionIdsNeedingUpdate.stream()
            .map(criterionId -> certService.get(criterionId))
            .map(criterion -> CertificationCriterionDTO.builder()
                    .id(criterion.getId())
                    .number(criterion.getNumber())
                    .title(criterion.getTitle())
                    .build())
            .collect(Collectors.toList());
    }

    private boolean hasCriteriaRequiringPrivacyAndSecurity(CertifiedProductSearchDetails listing) {
        return listing.getCertificationResults().stream()
                .filter(certResult -> certResult.isSuccess())
                .map(attestedCertResult -> attestedCertResult.getCriterion().getId())
                .filter(attestedCertResult -> privacyAndSecurityCriteriaIds.contains(attestedCertResult))
                .findAny().isPresent();
    }

    private List<CertificationCriterionDTO> getPrivacyAndSecurityRequiredCriteria(CertifiedProductSearchDetails listing) {
        List<Long> unattestedRequiredCriteria = listing.getCertificationResults().stream()
                .filter(certResult -> !certResult.isSuccess())
                .map(unattestedCertResult -> unattestedCertResult.getCriterion().getId())
                .filter(unattestedCriterionId -> privacyAndSecurityRequiredCriteriaIds.contains(unattestedCriterionId))
                .collect(Collectors.toList());
            return unattestedRequiredCriteria.stream()
                .map(criterionId -> certService.get(criterionId))
                .map(criterion -> CertificationCriterionDTO.builder()
                        .id(criterion.getId())
                        .number(criterion.getNumber())
                        .title(criterion.getTitle())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void save(List<ListingToCriterionForCuresAchievementStatistic> statistics) {
        for (ListingToCriterionForCuresAchievementStatistic statistic : statistics) {
            try {
                listingToCuresAchievementDao.create(statistic);
            } catch (Exception ex) {
                LOGGER.error("Could not save statistic for date: " + statistic.getStatisticDate()
                    + ", criterion: " + statistic.getCriterion().getId()
                    + ", listingId: " + statistic.getListingId());
            }
        }
    }

    @Transactional
    public void deleteStatisticsForDate(LocalDate statisticDate) {
        List<ListingToCriterionForCuresAchievementStatistic> statisticsForDate = listingToCuresAchievementDao.getStatisticsForDate(statisticDate);
        for (ListingToCriterionForCuresAchievementStatistic statistic : statisticsForDate) {
            try {
                listingToCuresAchievementDao.delete(statistic.getId());
            } catch (Exception ex) {
                LOGGER.error("Could not delete statistic with ID " + statistic.getId());
            }
        }
    }
}
