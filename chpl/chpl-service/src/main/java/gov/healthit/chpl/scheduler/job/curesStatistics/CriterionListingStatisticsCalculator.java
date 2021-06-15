package gov.healthit.chpl.scheduler.job.curesStatistics;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.statistics.CriterionListingStatisticsDAO;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.domain.statistics.CriterionListingCountStatistic;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2(topic = "curesStatisticsCreatorJobLogger")
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
        List<CriterionListingCountStatistic> statisticsForDate
            = criterionListingStatisticsDao.getStatisticsForDate(statisticDate);
        return statisticsForDate != null && statisticsForDate.size() > 0;
    }

    @Transactional
    public List<CriterionListingCountStatistic> calculateCurrentStatistics(LocalDate statisticDate) {
        List<CertificationCriterionDTO> allCriteria = criteriaDao.findByCertificationEditionYear(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear());
        return allCriteria.stream()
            .map(criterion -> getStatisticForCriterion(criterion, statisticDate))
            .collect(Collectors.toList());
    }

    private CriterionListingCountStatistic getStatisticForCriterion(CertificationCriterionDTO criterion, LocalDate statisticDate) {
        LOGGER.info("Getting listing count for criterion id: " + criterion.getId() + ", number: " + criterion.getNumber());
        Long listingCount = criterionListingStatisticsDao.getListingCountForCriterion(criterion.getId());
        return CriterionListingCountStatistic.builder()
                .criterion(criterion)
                .listingsCertifyingToCriterionCount(listingCount)
                .statisticDate(statisticDate)
                .build();
    }

    @Transactional
    public void save(List<CriterionListingCountStatistic> statistics) {
        for (CriterionListingCountStatistic statistic : statistics) {
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
        List<CriterionListingCountStatistic> statisticsForDate = criterionListingStatisticsDao.getStatisticsForDate(statisticDate);
        for (CriterionListingCountStatistic statistic : statisticsForDate) {
            try {
                criterionListingStatisticsDao.delete(statistic.getId());
            } catch (Exception ex) {
                LOGGER.error("Could not delete statistic with ID " + statistic.getId());
            }
        }
    }
}
