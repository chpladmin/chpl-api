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
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.statistics.ListingToCriterionForCuresAchievementStatisticDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.service.CuresUpdateService;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2(topic = "curesStatisticsCreatorJobLogger")
public class ListingCriterionForCuresAchievementStatisticsCalculator {
    private CertificationCriterionService certService;
    private CuresUpdateService curesUpdateService;
    private ListingToCriterionForCuresAchievementStatisticsDAO listingToCuresAchievementDao;
    private CertifiedProductDetailsManager cpdManager;
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
        List<ListingToCriterionForCuresAchievementStatisticDTO> statisticsForDate
            = listingToCuresAchievementDao.getStatisticsForDate(statisticDate);
        return statisticsForDate != null && statisticsForDate.size() > 0;
    }

    @Transactional
    public List<ListingToCriterionForCuresAchievementStatisticDTO> calculateCurrentStatistics(LocalDate statisticDate) {
        List<Long> listingIdsWithoutCuresUpdate = listingToCuresAchievementDao.getListingIdsWithoutCuresUpdateStatus();
        LOGGER.info("There are " + listingIdsWithoutCuresUpdate + " Active listings without cures update status.");
        List<ListingToCriterionForCuresAchievementStatisticDTO> statistics
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
            details = cpdManager.getCertifiedProductDetails(listingId);
        } catch (EntityRetrievalException ex) {
            LOGGER.error("Could not find listing with ID " + listingId, ex);
        }
        LOGGER.info("Got listing details for " + listingId);
        return details;
    }

    private List<ListingToCriterionForCuresAchievementStatisticDTO> calculateCurrentStatistic(LocalDate statisticDate, CertifiedProductSearchDetails listing) {
        List<ListingToCriterionForCuresAchievementStatisticDTO> statisticsWithNeededCuresCriterion
            = new ArrayList<ListingToCriterionForCuresAchievementStatisticDTO>();
        LOGGER.info("Getting criterion needed for Cures status for listing " + listing.getId());
        //TODO: what about a two-stage update that would be needed?
        //like if they have b3 original they need to upgrade to b3 cures but then after that upgrade
        //they might require d12/d13 when they previously would not have
        List<CertificationCriterionDTO> neededCriteria = getCriterionNeededForCures(listing);
        if (neededCriteria != null && neededCriteria.size() > 0) {
            for (CertificationCriterionDTO criterion : neededCriteria) {
                statisticsWithNeededCuresCriterion.add(ListingToCriterionForCuresAchievementStatisticDTO.builder()
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
    public void save(List<ListingToCriterionForCuresAchievementStatisticDTO> statistics) {
        for (ListingToCriterionForCuresAchievementStatisticDTO statistic : statistics) {
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
        List<ListingToCriterionForCuresAchievementStatisticDTO> statisticsForDate = listingToCuresAchievementDao.getStatisticsForDate(statisticDate);
        for (ListingToCriterionForCuresAchievementStatisticDTO statistic : statisticsForDate) {
            try {
                listingToCuresAchievementDao.delete(statistic.getId());
            } catch (Exception ex) {
                LOGGER.error("Could not delete statistic with ID " + statistic.getId());
            }
        }
    }
}
