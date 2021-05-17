package gov.healthit.chpl.scheduler.job.curesReporting;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.statistics.CriterionListingStatisticsDAO;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.statistics.CriterionListingCountStatisticDTO;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2(topic = "curesReportingJobLogger")
public class CriterionListingStatisticsCalculator {
    private CertificationCriterionDAO criteriaDao;
    private CriterionListingStatisticsDAO criterionListingStatisticsDao;

    @Autowired
    public CriterionListingStatisticsCalculator(CertificationCriterionDAO criteriaDao,
            CriterionListingStatisticsDAO criterionListingStatisticsDao) {
        this.criteriaDao = criteriaDao;
        this.criterionListingStatisticsDao = criterionListingStatisticsDao;
    }

    @Transactional
    public boolean hasStatisticsForDate(LocalDate statisticDate) {
        List<CriterionListingCountStatisticDTO> statisticsForDate
            = criterionListingStatisticsDao.getStatisticsForDate(statisticDate);
        return statisticsForDate != null && statisticsForDate.size() > 0;
    }

    @Transactional
    public List<CriterionListingCountStatisticDTO> calculateCurrentStatistics(LocalDate statisticDate) {
        List<CertificationCriterionDTO> allCriteria = criteriaDao.findAll();
        return allCriteria.stream()
            .map(criterion -> getStatisticForCriterion(criterion, statisticDate))
            .collect(Collectors.toList());
    }

    private CriterionListingCountStatisticDTO getStatisticForCriterion(CertificationCriterionDTO criterion, LocalDate statisticDate) {
        Integer listingCount = criterionListingStatisticsDao.getListingCountForCriterion(criterion.getId());
        return CriterionListingCountStatisticDTO.builder()
                .criterion(criterion)
                .listingsCertifyingToCriterionCount(listingCount)
                .statisticDate(statisticDate)
                .build();
    }

    @Transactional
    public void save(List<CriterionListingCountStatisticDTO> statistics) {
        for (CriterionListingCountStatisticDTO statistic : statistics) {
            try {
                criterionListingStatisticsDao.create(statistic);
            } catch (Exception ex) {
                LOGGER.error("Could not save statistic for date: " + statistic.getStatisticDate() + ", criterion: "
                        + statistic.getCriterion().getNumber() + ", and count: "
                        + statistic.getListingsCertifyingToCriterionCount());
            }
        }
    }

    @Transactional
    public void deleteStatisticsForDate(LocalDate statisticDate) {
        List<CriterionListingCountStatisticDTO> statisticsForDate = criterionListingStatisticsDao.getStatisticsForDate(statisticDate);
        for (CriterionListingCountStatisticDTO statistic : statisticsForDate) {
            try {
                criterionListingStatisticsDao.delete(statistic.getId());
            } catch (Exception ex) {
                LOGGER.error("Could not delete statistic with ID " + statistic.getId());
            }
        }
    }
}
