package gov.healthit.chpl.scheduler.job.urluptime;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.util.DateUtil;
import jakarta.persistence.Query;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Repository
public class UrlUptimeMonitorTestDAO extends BaseDAOImpl {
    private static final int START_TIME_IN_HOURS = 10;

    private static final int MONITOR_ID_COL = 0;
    private static final int TOTAL_TESTS_COL = 1;
    private static final int TOTAL_TESTS_PASSED_COL = 2;
    private static final int CURR_MONTH_TESTS_COL = 3;
    private static final int CURR_MONTH_TESTS_PASSED_COL = 4;
    private static final int PAST_WEEK_TESTS_COL = 5;
    private static final int PAST_WEEK_TEST_PASSED_COL = 6;

    public UrlUptimeMonitorTest create(UrlUptimeMonitorTest urlUptimeMonitorTest) {
        UrlUptimeMonitorTestEntity entity = UrlUptimeMonitorTestEntity.builder()
                .urlUptimeMonitorId(urlUptimeMonitorTest.getUrlUptimeMonitorId())
                .datadogTestKey(urlUptimeMonitorTest.getDatadogTestKey())
                .checkTime(urlUptimeMonitorTest.getCheckTime())
                .passed(urlUptimeMonitorTest.getPassed())
                .deleted(false)
                .build();

        entityManager.persist(entity);
        entityManager.flush();
        return entity.toDomain();
    }

    public Map<Long, UrlUptimeMonitorTestCounts> getTestCountsByMonitor(LocalDateTime currentMonthStart,
            LocalDateTime pastWeekStart, LocalDateTime end) {
        LOGGER.info("Finding all uptime monitor test counts for month beginning " + currentMonthStart + " and week beginning " + pastWeekStart + " until " + end);
        List<Object[]> rows = entityManager.createQuery(
                "SELECT uumt.urlUptimeMonitorId, "
                + "COUNT(uumt), "
                + "SUM(CASE WHEN uumt.passed = true THEN 1 ELSE 0 END), "
                + "SUM(CASE WHEN uumt.checkTime > :currentMonthStart AND uumt.checkTime < :end THEN 1 ELSE 0 END), "
                + "SUM(CASE WHEN uumt.checkTime > :currentMonthStart AND uumt.checkTime < :end AND uumt.passed = true THEN 1 ELSE 0 END), "
                + "SUM(CASE WHEN uumt.checkTime > :pastWeekStart AND uumt.checkTime < :end THEN 1 ELSE 0 END), "
                + "SUM(CASE WHEN uumt.checkTime > :pastWeekStart AND uumt.checkTime < :end AND uumt.passed = true THEN 1 ELSE 0 END) "
                + "FROM UrlUptimeMonitorTestEntity uumt "
                + "WHERE uumt.deleted = false "
                + "GROUP BY uumt.urlUptimeMonitorId", Object[].class)
                .setParameter("currentMonthStart", DateUtil.fromEasternToSystem(currentMonthStart))
                .setParameter("pastWeekStart", DateUtil.fromEasternToSystem(pastWeekStart))
                .setParameter("end", DateUtil.fromEasternToSystem(end))
                .getResultList();
        LOGGER.info("Completed getting all uptime monitor test counts");

        return rows.stream()
                .map(row -> UrlUptimeMonitorTestCounts.builder()
                        .urlUptimeMonitorId(toLong(row[MONITOR_ID_COL]))
                        .totalTestCount(toLong(row[TOTAL_TESTS_COL]))
                        .totalSuccessfulTestCount(toLong(row[TOTAL_TESTS_PASSED_COL]))
                        .currentMonthTestCount(toLong(row[CURR_MONTH_TESTS_COL]))
                        .currentMonthSuccessfulTestCount(toLong(row[CURR_MONTH_TESTS_PASSED_COL]))
                        .pastWeekTestCount(toLong(row[PAST_WEEK_TESTS_COL]))
                        .pastWeekSuccessfulTestCount(toLong(row[PAST_WEEK_TEST_PASSED_COL]))
                        .build())
                .collect(Collectors.toMap(UrlUptimeMonitorTestCounts::getUrlUptimeMonitorId, Function.identity()));
    }

    private Long toLong(Object value) {
        return value == null ? 0L : ((Number) value).longValue();
    }

    public List<UrlUptimeMonitorTest> getChplUptimeMonitorTestsBetweenDates(LocalDateTime start, LocalDateTime end) {
        List<UrlUptimeMonitorTestEntity> entities = entityManager.createQuery("FROM UrlUptimeMonitorTestEntity uumt "
                + "WHERE uumt.deleted = false "
                + "AND uumt.checkTime >= :start "
                + "AND uumt.checkTime <= :end", UrlUptimeMonitorTestEntity.class)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();

        return entities.stream()
                .map(entity -> entity.toDomain())
                .toList();
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
