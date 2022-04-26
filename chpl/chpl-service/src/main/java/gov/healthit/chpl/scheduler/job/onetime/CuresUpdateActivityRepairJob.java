package gov.healthit.chpl.scheduler.job.onetime;

import java.util.List;

import javax.persistence.Query;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.activity.history.ListingActivityUtil;
import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.entity.ActivityEntity;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.util.JSONUtils;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "curesUpdateActivityRepairJobLogger")
public class CuresUpdateActivityRepairJob implements Job {

    @Autowired
    private ActivityManager activityManager;

    @Autowired
    private UpdatableActivityDAO updatableActivityDao;

    @Autowired
    private ListingActivityUtil activityUtil;

    @SuppressWarnings("checkstyle:linelength")
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Cures Update Activity Repair job *********");
        setSecurityContext();

        //Listing 10838

        //Listing 9582

        //********************************************************
        //Listing 10869 should have been uploaded as cures = true
        //********************************************************/
        try {
            //activity ID 84346 "new data" should have cures update = true
            Long activityId = 84346L;
            ActivityEntity activity = updatableActivityDao.getEntityById(activityId);
            CertifiedProductSearchDetails data = activityUtil.getListing(activity.getNewData());
            data.setCuresUpdate(true);
            updatableActivityDao.updateActivity(activityId, null, JSONUtils.toJSON(data));
        } catch (Exception ex) {
            LOGGER.error("Could not get/update activity with ID 84346", ex);
        }
        try {
            //activity ID 84412 should not exist (Jim's edit to make the listing appear as cures update)
            updatableActivityDao.deleteActivity(84412L);
        } catch (Exception ex) {
            LOGGER.error("Could not delete activity with ID 84412", ex);
        }
        //TODO: check cures_update_event table (db PR)

        LOGGER.info("********* Completed the Cures Update Activity Repair job *********");

    }

    private void setSecurityContext() {
        JWTAuthenticatedUser adminUser = new JWTAuthenticatedUser();
        adminUser.setFullName("Administrator");
        adminUser.setId(User.ADMIN_USER_ID);
        adminUser.setFriendlyName("Admin");
        adminUser.setSubjectName("admin");
        adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));

        SecurityContextHolder.getContext().setAuthentication(adminUser);
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }

    @Component("updatableActivityDao")
    @NoArgsConstructor
    private static class UpdatableActivityDAO extends BaseDAOImpl {

        public void updateActivity(Long activityId, String updatedOriginalJson,
                String updatedNewJson) {
            ActivityEntity entity = getEntityById(activityId);
            entity.setOriginalData(updatedOriginalJson);
            entity.setNewData(updatedNewJson);
            //TODO: do we want to set the last modified user as the system? Or pretend it never happened?
            update(entity);
        }

        public void deleteActivity(Long activityId) {
            ActivityEntity entity = getEntityById(activityId);
            entity.setDeleted(true);
            //TODO: do we want to set the last modified user as the system? Or pretend it never happened?
            update(entity);
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
