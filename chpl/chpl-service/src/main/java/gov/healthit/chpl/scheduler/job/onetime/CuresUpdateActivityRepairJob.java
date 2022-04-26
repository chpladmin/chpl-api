package gov.healthit.chpl.scheduler.job.onetime;

import java.util.List;

import javax.persistence.Query;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.activity.history.ListingActivityUtil;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.entity.ActivityEntity;
import gov.healthit.chpl.util.JSONUtils;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "curesUpdateActivityRepairJobLogger")
public class CuresUpdateActivityRepairJob implements Job {

    @Autowired
    private UpdatableActivityDAO updatableActivityDao;

    @Autowired
    private ListingActivityUtil activityUtil;

    @SuppressWarnings("checkstyle:linelength")
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Cures Update Activity Repair job *********");

        //********************************************************
        //Listing 10850 should had an update that removed it's Cures Update
        //designation. The update should have left cures update = true.
        //********************************************************/
        try {
            //activity ID 84258 "new data" should have cures update = true and original data unchanged
            Long activityId = 84258L;
            ActivityEntity activity = updatableActivityDao.getEntityById(activityId);
            CertifiedProductSearchDetails newData = activityUtil.getListing(activity.getNewData());
            newData.setCuresUpdate(true);
            updatableActivityDao.updateActivity(activityId, activity.getOriginalData(),
                    JSONUtils.toJSON(newData));
        } catch (Exception ex) {
            LOGGER.error("Could not get/update activity with ID 84258", ex);
        }

        try {
            //activity ID 84398 should not exist (Jim's edit to make the listing appear as cures update)
            updatableActivityDao.deleteActivity(84398L);
        } catch (Exception ex) {
            LOGGER.error("Could not delete activity with ID 84398", ex);
        }

        //********************************************************
        //Listing 10861 should have been uploaded as cures = true
        //********************************************************/
        try {
            //activity ID 84263 "new data" should have cures update = true
            Long activityId = 84263L;
            ActivityEntity activity = updatableActivityDao.getEntityById(activityId);
            CertifiedProductSearchDetails newData = activityUtil.getListing(activity.getNewData());
            newData.setCuresUpdate(true);
            updatableActivityDao.updateActivity(activityId, null, JSONUtils.toJSON(newData));
        } catch (Exception ex) {
            LOGGER.error("Could not get/update activity with ID 84263", ex);
        }

        try {
            //activity ID 84399 should not exist (Jim's edit to make the listing appear as cures update)
            updatableActivityDao.deleteActivity(84399L);
        } catch (Exception ex) {
            LOGGER.error("Could not delete activity with ID 84399", ex);
        }

        //********************************************************
        //Listing 10869 should have been uploaded as cures = true
        //********************************************************/
        try {
            //activity ID 84346 "new data" should have cures update = true
            Long activityId = 84346L;
            ActivityEntity activity = updatableActivityDao.getEntityById(activityId);
            CertifiedProductSearchDetails newData = activityUtil.getListing(activity.getNewData());
            newData.setCuresUpdate(true);
            updatableActivityDao.updateActivity(activityId, null, JSONUtils.toJSON(newData));
        } catch (Exception ex) {
            LOGGER.error("Could not get/update activity with ID 84346", ex);
        }

        try {
            //activity ID 84412 should not exist (Jim's edit to make the listing appear as cures update)
            updatableActivityDao.deleteActivity(84412L);
        } catch (Exception ex) {
            LOGGER.error("Could not delete activity with ID 84412", ex);
        }

        LOGGER.info("********* Completed the Cures Update Activity Repair job *********");

    }

    @Component("updatableActivityDao")
    @NoArgsConstructor
    private static class UpdatableActivityDAO extends BaseDAOImpl {

        public void updateActivity(Long activityId, String updatedOriginalJson,
                String updatedNewJson) {
            ActivityEntity entity = getEntityById(activityId);
            entity.setOriginalData(updatedOriginalJson);
            entity.setNewData(updatedNewJson);
            update(entity);
        }

        public void deleteActivity(Long activityId) {
            ActivityEntity entity = getEntityById(activityId);
            entityManager.remove(entity);
            entityManager.flush();
            entityManager.clear();
        }

        public ActivityEntity getEntityById(Long id) {
            ActivityEntity entity = null;
            String queryStr = "SELECT ae "
                    + "FROM ActivityEntity ae "
                    + "JOIN FETCH ae.concept "
                    + "LEFT OUTER JOIN FETCH ae.user "
                    + "WHERE (ae.id = :entityid) ";
            Query query = entityManager.createQuery(queryStr);
            query.setParameter("entityid", id);
            List<ActivityEntity> result = query.getResultList();

            if (result != null && result.size() > 0) {
                entity = result.get(0);
            }
            return entity;
        }
    }
}
