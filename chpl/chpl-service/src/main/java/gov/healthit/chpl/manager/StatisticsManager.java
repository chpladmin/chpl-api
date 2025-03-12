package gov.healthit.chpl.manager;

import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.stereotype.Service;

import gov.healthit.chpl.certificationCriteria.CertificationCriterionComparator;
import gov.healthit.chpl.dao.CriterionProductStatisticsDAO;
import gov.healthit.chpl.domain.CriterionProductStatistics;

@Service
public class StatisticsManager extends ApplicationObjectSupport {

    private CriterionProductStatisticsDAO criterionProductStatisticsDAO;
    private CertificationCriterionComparator certificationCriterionComparator;

    @Autowired
    public StatisticsManager(CriterionProductStatisticsDAO criterionProductStatisticsDAO,
            CertificationCriterionComparator certificationCriterionComparator) {

        this.criterionProductStatisticsDAO = criterionProductStatisticsDAO;
        this.certificationCriterionComparator = certificationCriterionComparator;
    }

    public List<CriterionProductStatistics> getCriterionProductStatisticsResult() {
        List<CriterionProductStatistics> criterionProductStatistics = criterionProductStatisticsDAO.findAll().stream()
                .sorted(Comparator.comparing(CriterionProductStatistics::getCriterion, certificationCriterionComparator))
                .toList();

        for (int i = 0; i < criterionProductStatistics.size(); i++) {
            criterionProductStatistics.get(i).setSortOrder(i);
        }
        return criterionProductStatistics;
    }
}