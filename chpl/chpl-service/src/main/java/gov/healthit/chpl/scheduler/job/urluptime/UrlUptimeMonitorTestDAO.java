package gov.healthit.chpl.scheduler.job.urluptime;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;

@Repository
public class UrlUptimeMonitorTestDAO extends BaseDAOImpl {
    private static final int START_TIME_IN_HOURS = 10;

    public UrlUptimeMonitorTest create(UrlUptimeMonitorTest urlUptimeMonitorTest) {
        UrlUptimeMonitorTestEntity entity = UrlUptimeMonitorTestEntity.builder()
                .urlUptimeMonitorId(urlUptimeMonitorTest.getUrlUptimeMonitorId())
                .datadogTestKey(urlUptimeMonitorTest.getDatadogTestKey())
                .checkTime(urlUptimeMonitorTest.getCheckTime())
                .passed(urlUptimeMonitorTest.getPassed())
                .lastModifiedUser(User.SYSTEM_USER_ID)
                .deleted(false)
                .build();

        entityManager.persist(entity);
        entityManager.flush();
        return entity.toDomain();
    }

    public List<UrlUptimeMonitorTest> getChplUptimeMonitorTests(Long datadogMonitorId) {
        return getDatadogMonitorTestEntities(datadogMonitorId).stream()
                .map(entity -> entity.toDomain())
                .toList();
    }

    private List<UrlUptimeMonitorTestEntity> getDatadogMonitorTestEntities(Long urlUptimeMonitorId) {
        return entityManager.createQuery("FROM UrlUptimeMonitorTestEntity uumt "
                + "WHERE uumt.deleted = false "
                + "AND uumt.urlUptimeMonitorId = :urlUptimeMonitorId ", UrlUptimeMonitorTestEntity.class)
                .setParameter("urlUptimeMonitorId", urlUptimeMonitorId)
                .getResultList();
    }

    public Long getTestCountForDate(LocalDate dateToCheck) {
        //On the servers, check_time is stored in UTC time.  Since we collect data from 8am - 8pm ET,
        //data on the servers far a particular day can roll over to the next day.  To alleviate this problem
        //we will check for existence of tests after 1000 UTC (5am or 6am ET).
        Query query = entityManager.createQuery(
                "SELECT count(*) "
                + "FROM  UrlUptimeMonitorTestEntity uumt "
                + "WHERE uumt.checkTime >= :startDateTime "
                + "AND uumt.checkTime <= :endDateTime")
                .setParameter("startDateTime", dateToCheck.atTime(START_TIME_IN_HOURS, 0))
                .setParameter("endDateTime", dateToCheck.atTime(LocalTime.MAX));
        return (Long) query.getSingleResult();
    }
}
