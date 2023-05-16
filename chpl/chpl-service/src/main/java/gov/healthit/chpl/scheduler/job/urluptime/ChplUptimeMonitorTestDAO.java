package gov.healthit.chpl.scheduler.job.urluptime;

import java.util.Date;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;

@Repository
public class ChplUptimeMonitorTestDAO extends BaseDAOImpl {

    public ChplUptimeMonitorTest create(ChplUptimeMonitorTest chplUptimeMonitorTest) {
        ChplUptimeMonitorTestEntity entity = ChplUptimeMonitorTestEntity.builder()
                .chplUptimeMonitorId(chplUptimeMonitorTest.getChplUptimeMonitorId())
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

}
