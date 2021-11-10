package gov.healthit.chpl.scheduler.job.summarystatistics.data;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.statistics.SurveillanceStatisticsDAO;
import gov.healthit.chpl.entity.surveillance.SurveillanceEntity;

@Component
public class SurveillanceDataCreator {

    private SurveillanceStatisticsDAO surveillanceStatisticsDAO;

    @Autowired
    public SurveillanceDataCreator(SurveillanceStatisticsDAO surveillanceStatisticsDAO) {
        this.surveillanceStatisticsDAO = surveillanceStatisticsDAO;
    }

    public Long getTotalSurveillanceActivities() {
        return surveillanceStatisticsDAO.getTotalSurveillanceActivities(null);
    }

    public EmailStatistic getTotalOpenSurveillanceActivities() {
        EmailStatistic openSurvs = new EmailStatistic();
        openSurvs.setCount(surveillanceStatisticsDAO.getTotalOpenSurveillanceActivities(null));
        openSurvs.setAcbStatistics(getTotalOpenSurveillancesByAcb());
        return openSurvs;
    }

    public Long getTotalClosedSurveillanceActivities() {
        return surveillanceStatisticsDAO.getTotalClosedSurveillanceActivities(null);
    }

    public Long getAverageTimeToCloseSurveillance() {
        List<SurveillanceEntity> surveillances = surveillanceStatisticsDAO.getAllSurveillances().stream()
                .filter(surv -> surv.getStartDate() != null
                && surv.getEndDate() != null)
                .collect(Collectors.toList());

        Long totalDuration = surveillances.stream()
                .map(surv -> Math.abs(ChronoUnit.DAYS.between(surv.getStartDate(), surv.getEndDate())))
                .collect(Collectors.summingLong(n -> n.longValue()));
        return totalDuration / surveillances.size();
    }

    private List<EmailCertificationBodyStatistic> getTotalOpenSurveillancesByAcb() {
        return surveillanceStatisticsDAO.getTotalOpenSurveillanceActivitiesByAcb(null);
    }


}
