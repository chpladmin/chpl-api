package gov.healthit.chpl.scheduler.job.urluptime;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;

@Repository
public class DatadogMonitorTestDAO extends BaseDAOImpl {

    public DatadogMonitorTest create(DatadogMonitorTest chplUptimeMonitorTest) {
        DatadogMonitorTestEntity entity = DatadogMonitorTestEntity.builder()
                .datadogMonitorId(chplUptimeMonitorTest.getDatadogMonitorId())
                .datadogTestKey(chplUptimeMonitorTest.getDatadogTestKey())
                .checkTime(chplUptimeMonitorTest.getCheckTime())
                .passed(chplUptimeMonitorTest.getPassed())
                .creationDate(new Date())
                .lastModifiedUser(User.SYSTEM_USER_ID)
                .lastModifiedDate(new Date())
                .deleted(false)
                .build();

        entityManager.persist(entity);
        entityManager.flush();
        return entity.toDomain();
    }

    public List<DatadogMonitorTest> getChplUptimeMonitorTests(Long datadogMonitorId) {
        return getDatadogMonitorTestEntities(datadogMonitorId).stream()
                .map(entity -> entity.toDomain())
                .toList();
    }

    private List<DatadogMonitorTestEntity> getDatadogMonitorTestEntities(Long datadogMonitorId) {
        return entityManager.createQuery("FROM DatadogMonitorTestEntity dmt "
                + "WHERE dmt.deleted = false "
                + "AND dmt.datadogMonitorId = :datadogMonitorId ", DatadogMonitorTestEntity.class)
                .setParameter("datadogMonitorId", datadogMonitorId)
                .getResultList();
    }
}
