package gov.healthit.chpl.scheduler.job.summarystatistics.data;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.compliance.surveillance.entity.SurveillanceEntity;

@Component
public class SurveillanceDataCalculator {

    private SurveillanceStatisticsDAO surveillanceStatisticsDao;

    @Autowired
    public SurveillanceDataCalculator(SurveillanceStatisticsDAO surveillanceStatisticsDao) {
        this.surveillanceStatisticsDao = surveillanceStatisticsDao;
    }

    public Long getTotalSurveillanceActivities() {
        return surveillanceStatisticsDao.getTotalSurveillanceActivities(null);
    }

    public Statistic getTotalOpenSurveillanceActivities() {
        Statistic openSurvs = new Statistic();
        openSurvs.setCount(surveillanceStatisticsDao.getTotalOpenSurveillanceActivities(null));
        openSurvs.setAcbStatistics(getTotalOpenSurveillancesByAcb());
        return openSurvs;
    }

    public Long getTotalClosedSurveillanceActivities() {
        return surveillanceStatisticsDao.getTotalClosedSurveillanceActivities(null);
    }

    public Long getAverageTimeToCloseSurveillance() {
        List<SurveillanceEntity> surveillances = surveillanceStatisticsDao.getAllSurveillances().stream()
                .filter(surv -> surv.getStartDate() != null
                && surv.getEndDate() != null)
                .collect(Collectors.toList());

        Long totalDuration = surveillances.stream()
                .map(surv -> Math.abs(ChronoUnit.DAYS.between(surv.getStartDate(), surv.getEndDate())))
                .collect(Collectors.summingLong(n -> n.longValue()));
        return totalDuration / surveillances.size();
    }

    private List<CertificationBodyStatistic> getTotalOpenSurveillancesByAcb() {
        return surveillanceStatisticsDao.getTotalOpenSurveillanceActivitiesByAcb(null);
    }
}
