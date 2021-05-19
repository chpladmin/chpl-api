package gov.healthit.chpl.scheduler.job.curesStatistics;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.SpecialProperties;
import gov.healthit.chpl.dao.ActivityDAO;
import gov.healthit.chpl.dao.statistics.CuresCriterionUpgradedWithoutOriginalListingStatisticsDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.statistics.CuresCriterionUpgradedWithoutOriginalListingStatisticDTO;
import gov.healthit.chpl.service.CertificationCriterionService;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2(topic = "curesStatisticsCreatorJobLogger")
public class CuresCriterionActivityStatisticsCalculator {
    private CertificationCriterionService criteriaService;
    private ActivityDAO activityDao;
    private CuresCriterionUpgradedWithoutOriginalListingStatisticsDAO curesCriterionUpgradedWithoutOriginalStatisticDao;
    private ObjectMapper jsonMapper;
    private Date curesEffectiveDate;
    private Date currentDate;

    @Autowired
    public CuresCriterionActivityStatisticsCalculator(CertificationCriterionService criteriaService,
            ActivityDAO activityDao,
            CuresCriterionUpgradedWithoutOriginalListingStatisticsDAO curesCriterionUpgradedWithoutOriginalStatisticDao,
            SpecialProperties specialProperties) {
        this.criteriaService = criteriaService;
        this.activityDao = activityDao;
        this.curesCriterionUpgradedWithoutOriginalStatisticDao = curesCriterionUpgradedWithoutOriginalStatisticDao;
        jsonMapper = new ObjectMapper();
        curesEffectiveDate = specialProperties.getEffectiveRuleTimestamp();
        currentDate = new Date();
    }

    @Transactional
    public boolean hasStatisticsForDate(LocalDate statisticDate) {
        List<CuresCriterionUpgradedWithoutOriginalListingStatisticDTO> statisticsForDate
            = curesCriterionUpgradedWithoutOriginalStatisticDao.getStatisticsForDate(statisticDate);
        return statisticsForDate != null && statisticsForDate.size() > 0;
    }

    @Transactional
    public List<CuresCriterionUpgradedWithoutOriginalListingStatisticDTO> calculateCurrentStatistics(LocalDate statisticDate) {
        LOGGER.info("Calculating cures criterion upgrade without original statistics for " + statisticDate);
        List<CuresCriterionUpgradedWithoutOriginalListingStatisticDTO> results
            = new ArrayList<CuresCriterionUpgradedWithoutOriginalListingStatisticDTO>();

        Map<CertificationCriterion, CertificationCriterion> originalToCuresCriteriaMap = criteriaService.getOriginalToCuresCriteriaMap();
        for (CertificationCriterion originalCriterion : originalToCuresCriteriaMap.keySet()) {
            long listingCount = 0;
            CertificationCriterion curesCriterion = originalToCuresCriteriaMap.get(originalCriterion);
            List<Long> listingIdsAttestingToCriterion = curesCriterionUpgradedWithoutOriginalStatisticDao.getListingIdsAttestingToCriterion(curesCriterion.getId());
            for (Long listingId : listingIdsAttestingToCriterion) {
                if (!didListingRemoveAttestationToCriterionDuringTimeInterval(listingId, originalCriterion, curesEffectiveDate, currentDate)) {
                    listingCount++;
                }
            }
            results.add(buildStatistic(curesCriterion, listingCount, statisticDate));
        }
        return results;
    }

    private boolean didListingRemoveAttestationToCriterionDuringTimeInterval(Long listingId, CertificationCriterion criterion, Date startDate, Date endDate) {
        LOGGER.info("Determining if listing ID " + listingId + " removed attestation to " + criterion.getId() + " between " + startDate + " and " + endDate);
        List<ActivityDTO> listingActivities = activityDao.findByObjectId(listingId, ActivityConcept.CERTIFIED_PRODUCT, startDate, endDate);
        for (ActivityDTO listingActivity : listingActivities) {
            CertifiedProductSearchDetails originalListingInActivity = getListing(listingActivity.getOriginalData());
            CertifiedProductSearchDetails updatedListingInActivity = getListing(listingActivity.getNewData());
            CertificationResult originalListingCertResultForCriterion
                = originalListingInActivity.getCertificationResults().stream()
                    .filter(certResult -> certResult.getCriterion() != null && certResult.getCriterion().getId().equals(criterion.getId()))
                    .findAny().get();
            CertificationResult updatedListingCertResultForCriterion
            = updatedListingInActivity.getCertificationResults().stream()
                .filter(certResult -> certResult.getCriterion() != null && certResult.getCriterion().getId().equals(criterion.getId()))
                .findAny().get();
            if (originalListingCertResultForCriterion.isSuccess() && !updatedListingCertResultForCriterion.isSuccess()) {
                LOGGER.info("Listing ID " + listingId + " unattested to criterion " + criterion.getId() +  " on " + listingActivity.getActivityDate());
                return true;
            }
        }
        return false;
    }

    private CertifiedProductSearchDetails getListing(String listingJson) {
        CertifiedProductSearchDetails listing = null;
        if (!StringUtils.isEmpty(listingJson)) {
            try {
                listing =
                    jsonMapper.readValue(listingJson, CertifiedProductSearchDetails.class);
            } catch (Exception ex) {
                LOGGER.error("Could not parse activity JSON " + listingJson, ex);
            }
        }
        return listing;
    }

    private CuresCriterionUpgradedWithoutOriginalListingStatisticDTO buildStatistic(
            CertificationCriterion criterion, long listingCount, LocalDate statisticDate) {
        return CuresCriterionUpgradedWithoutOriginalListingStatisticDTO.builder()
                .listingsUpgradedWithoutAttestingToOriginalCount(listingCount)
                .curesCriterion(CertificationCriterionDTO.builder()
                        .id(criterion.getId())
                        .number(criterion.getNumber())
                        .title(criterion.getTitle())
                        .build())
                .statisticDate(statisticDate)
                .build();
    }

    @Transactional
    public void save(List<CuresCriterionUpgradedWithoutOriginalListingStatisticDTO> statistics) {
        for (CuresCriterionUpgradedWithoutOriginalListingStatisticDTO statistic : statistics) {
            try {
                curesCriterionUpgradedWithoutOriginalStatisticDao.create(statistic);
            } catch (Exception ex) {
                LOGGER.error("Could not save statistic for date: " + statistic.getStatisticDate()
                        + ", criterionId: " + statistic.getCuresCriterion().getId()
                        + ", listingCount: " + statistic.getListingsUpgradedWithoutAttestingToOriginalCount());
            }
        }
    }

    @Transactional
    public void deleteStatisticsForDate(LocalDate statisticDate) {
        List<CuresCriterionUpgradedWithoutOriginalListingStatisticDTO> statisticsForDate
            = curesCriterionUpgradedWithoutOriginalStatisticDao.getStatisticsForDate(statisticDate);
        for (CuresCriterionUpgradedWithoutOriginalListingStatisticDTO statistic : statisticsForDate) {
            try {
                curesCriterionUpgradedWithoutOriginalStatisticDao.delete(statistic.getId());
            } catch (Exception ex) {
                LOGGER.error("Could not delete statistic with ID " + statistic.getId());
            }
        }
    }
}
