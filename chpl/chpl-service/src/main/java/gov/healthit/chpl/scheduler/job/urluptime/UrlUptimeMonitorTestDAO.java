package gov.healthit.chpl.scheduler.job.urluptime;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;

@Repository
public class UrlUptimeMonitorTestDAO extends BaseDAOImpl {

    public UrlUptimeMonitorTest create(UrlUptimeMonitorTest urlUptimeMonitorTest) {
        UrlUptimeMonitorTestEntity entity = UrlUptimeMonitorTestEntity.builder()
                .urlUptimeMonitorId(urlUptimeMonitorTest.getUrlUptimeMonitorId())
                .checkTime(urlUptimeMonitorTest.getCheckTime())
                .passed(urlUptimeMonitorTest.getPassed())
                .creationDate(new Date())
                .lastModifiedUser(User.SYSTEM_USER_ID)
                .lastModifiedDate(new Date())
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
        Query query = entityManager.createQuery(
                "SELECT count(*) "
                + "FROM  UrlUptimeMonitorTestEntity uumt "
                + "WHERE uumt.checkTime >= :startDateTime "
                + "AND uumt.checkTime <= :endDateTime")
                .setParameter("startDateTime", dateToCheck.atStartOfDay())
                .setParameter("endDateTime", dateToCheck.atTime(LocalTime.MAX));
        return (Long) query.getSingleResult();
    }
}
