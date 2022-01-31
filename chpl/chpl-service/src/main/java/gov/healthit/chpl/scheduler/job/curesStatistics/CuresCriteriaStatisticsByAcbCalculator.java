package gov.healthit.chpl.scheduler.job.curesStatistics;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.SpecialProperties;
import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.statistics.CuresCriteriaStatisticsByAcbDAO;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.statistics.CuresCriteriaStatisticsByAcb;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.service.CertificationCriterionService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "curesStatisticsCreatorJobLogger")
@Component
public class CuresCriteriaStatisticsByAcbCalculator {
    private CertifiedProductDAO certifiedProductDAO;
    private CertificationResultActivityHistoryHelper activityStatisticsHelper;
    private CertificationBodyDAO certificationBodyDAO;
    private CuresCriteriaStatisticsByAcbDAO curesStatisticsByAcbDAO;

    private List<CuresCriteriaUpdate> curesCriteriaUpdates = new ArrayList<CuresCriteriaStatisticsByAcbCalculator.CuresCriteriaUpdate>();
    private List<CertificationStatusType> activeStatuses;
    private Date curesEffectiveDate;


    @Autowired
    public CuresCriteriaStatisticsByAcbCalculator(CertificationCriterionService certificationCriterionService,
            CertifiedProductDAO certifiedProductDAO,
            CertificationBodyDAO certificationBodyDAO,
            CertificationResultActivityHistoryHelper activityStatisticsHelper,
            CuresCriteriaStatisticsByAcbDAO curesStatisticsByAcbDAO,
            SpecialProperties specialProperties) {

        this.certifiedProductDAO = certifiedProductDAO;
        this.activityStatisticsHelper = activityStatisticsHelper;
        this.certificationBodyDAO = certificationBodyDAO;
        this.curesStatisticsByAcbDAO = curesStatisticsByAcbDAO;

        curesEffectiveDate = specialProperties.getEffectiveRuleDate();

        activeStatuses = Stream.of(CertificationStatusType.Active,
                CertificationStatusType.SuspendedByAcb,
                CertificationStatusType.SuspendedByOnc)
                .collect(Collectors.toList());

        curesCriteriaUpdates = certificationCriterionService.getOriginalToCuresCriteriaMap().entrySet().stream()
                .map(entry -> new CuresCriteriaUpdate(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void setCuresStatisticsByAcbForDate(LocalDate statisticDate) {
        if (hasStatisticsForDate(statisticDate)) {
            deleteStatisticsForDate(statisticDate);
        }
        List<CuresCriteriaStatisticsByAcb> stats = calculate(statisticDate);
        save(stats);
    }

    private List<CuresCriteriaStatisticsByAcb> calculate(LocalDate statisticsDate) {
        List<CuresCriteriaStatisticsByAcb> curesCriteriaUpdateByAcbs = new ArrayList<CuresCriteriaStatisticsByAcb>();
        for (CuresCriteriaUpdate curesCriteriaUpdate : curesCriteriaUpdates) {

            List<CertifiedProductDetailsDTO> listingsAttestingToCuresCriterion =
                    certifiedProductDAO.getListingsAttestingToCriterion(curesCriteriaUpdate.getCuresCriterion().getId(), activeStatuses);

            List<CertifiedProductDetailsDTO> listingsAttestingToOriginalCriterion =
                            certifiedProductDAO.getListingsAttestingToCriterion(curesCriteriaUpdate.getOriginalCriterion().getId(), activeStatuses);

            List<Long> acbIds = certificationBodyDAO.findAllActive().stream()
                    .map(dto -> dto.getId())
                    .collect(Collectors.toList());

            for (Long acbId : acbIds) {
                CuresCriteriaStatisticsByAcb statistic = CuresCriteriaStatisticsByAcb.builder()
                        .certificationBody(getCertificationBody(acbId))
                        .originalCriterion(curesCriteriaUpdate.originalCriterion)
                        .curesCriterion(curesCriteriaUpdate.curesCriterion)
                        .originalCriterionUpgradedCount(calculateUpgradeCriterionByAcb(acbId, listingsAttestingToCuresCriterion, curesCriteriaUpdate.getOriginalCriterion()))
                        .curesCriterionCreatedCount(calculateNewCriterionByAcb(acbId, listingsAttestingToCuresCriterion, curesCriteriaUpdate.getOriginalCriterion()))
                        .criteriaNeedingUpgradeCount(calculateCriteriaNeedingUpgradeCountByAcb(acbId, listingsAttestingToOriginalCriterion))
                        .statisticDate(statisticsDate)
                        .build();

                curesCriteriaUpdateByAcbs.add(statistic);
            }
        }

        curesCriteriaUpdateByAcbs.stream()
                .forEach(item -> LOGGER.info(String.format("%s -- %s -- %s -- %s", item.getCertificationBody().getName(), item.getOriginalCriterion().getNumber(), item.getOriginalCriterionUpgradedCount(), item.getCuresCriterionCreatedCount())));
        return curesCriteriaUpdateByAcbs;
    }

    private boolean hasStatisticsForDate(LocalDate statisticDate) {
        List<CuresCriteriaStatisticsByAcb> statisticsForDate = curesStatisticsByAcbDAO.getStatisticsForDate(statisticDate);
        return statisticsForDate != null && statisticsForDate.size() > 0;
    }

    private void deleteStatisticsForDate(LocalDate statisticDate) {
        List<CuresCriteriaStatisticsByAcb> statisticsForDate = curesStatisticsByAcbDAO.getStatisticsForDate(statisticDate);
        for (CuresCriteriaStatisticsByAcb statistic : statisticsForDate) {
            try {
                curesStatisticsByAcbDAO.delete(statistic.getId());
            } catch (Exception ex) {
                LOGGER.error("Could not delete statistic with ID " + statistic.getId());
            }
        }
    }

    private void save(List<CuresCriteriaStatisticsByAcb> statistics) {
        statistics.stream()
            .forEach(stat -> stat.setLastModifiedUser(User.SYSTEM_USER_ID));

        curesStatisticsByAcbDAO.create(statistics);
    }

    private Long calculateUpgradeCriterionByAcb(Long acbId, List<CertifiedProductDetailsDTO> certifiedProductDetails, CertificationCriterion criterion) {
        return certifiedProductDetails.stream()
                .filter(listing -> listing.getCertificationBodyId().equals(acbId))
                .filter(listing -> activityStatisticsHelper.didListingRemoveAttestationToCriterionDuringTimeInterval(listing.getId(), criterion, curesEffectiveDate, new Date()))
                .collect(Collectors.counting());
    }

    private Long calculateNewCriterionByAcb(Long acbId, List<CertifiedProductDetailsDTO> certifiedProductDetails, CertificationCriterion criterion) {
        return certifiedProductDetails.stream()
                .filter(listing -> listing.getCertificationBodyId().equals(acbId))
                .filter(listing -> !activityStatisticsHelper.didListingRemoveAttestationToCriterionDuringTimeInterval(listing.getId(), criterion, curesEffectiveDate, new Date()))
                .collect(Collectors.counting());
    }

    private Long calculateCriteriaNeedingUpgradeCountByAcb(Long acbId, List<CertifiedProductDetailsDTO> listingsAttestingToOriginalCriterion) {
        return listingsAttestingToOriginalCriterion.stream()
                .filter(listing -> listing.getCertificationBodyId().equals(acbId))
                .collect(Collectors.counting());
    }

    private CertificationBody getCertificationBody(Long acbId) {
        try {
            return new CertificationBody(certificationBodyDAO.getById(acbId));
        } catch (EntityRetrievalException e) {
            return null;
        }
    }

    @Data
    @AllArgsConstructor
    class CuresCriteriaUpdate {
        private CertificationCriterion originalCriterion;
        private CertificationCriterion curesCriterion;
    }
}
