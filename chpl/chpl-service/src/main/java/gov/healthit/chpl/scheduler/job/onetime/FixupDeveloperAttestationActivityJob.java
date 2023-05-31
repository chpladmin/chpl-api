package gov.healthit.chpl.scheduler.job.onetime;

import java.util.List;

import javax.persistence.Query;
import javax.transaction.Transactional;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.PublicAttestation;
import gov.healthit.chpl.entity.ActivityEntity;
import gov.healthit.chpl.scheduler.job.QuartzJob;
import gov.healthit.chpl.util.JSONUtils;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "fixupDeveloperAttestationActivityJobLogger")
public class FixupDeveloperAttestationActivityJob extends QuartzJob {
    @Qualifier("fixupDeveloperAttestationActivityDAO")
    @Autowired
    private UpdatableActivityDAO activityDao;

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Fixup Developer Attestation Activity job. *********");

        //Rewrite activity 99251 to use attestation ID 670 in originalData/newData
            //new data is incorrect and uses ID 671
            //original data uses ID 670
        LOGGER.info("Rewriting activity with ID 99251");
        try {
            activityDao.updateToUseCorrectPeriod2Attestation(99251L);
        } catch (Exception ex) {
            LOGGER.error("Error updating activity 99251", ex);
        }

        //Rewrite activity 94115 to use attestation ID 670 in originalData/newData
            //original data is incorrect and uses ID 671
            //new data uses ID 670
        LOGGER.info("Rewriting activity with ID 94115");
        try {
            activityDao.updateToUseCorrectPeriod2Attestation(94115L);
        } catch (Exception ex) {
            LOGGER.error("Error updating activity 94115L", ex);
        }

        //Delete activity ID 91715 (the double-click submission)
        LOGGER.info("Deleting activity 91715");
        try {
            activityDao.delete(91715L);
        } catch (Exception ex) {
            LOGGER.error("Error deleting activity 91715L", ex);
        }

        LOGGER.info("********* Completed the Fixup Developer Attestation Activity job. *********");
    }

    @Component("fixupDeveloperAttestationActivityDAO")
    private static class UpdatableActivityDAO extends BaseDAOImpl {
        private static final Long PERIOD_2_ID = 3L;
        private static final Long CORRECT_ATTESTATION_ID = 670L;
        private ObjectMapper jsonMapper = new ObjectMapper();

        UpdatableActivityDAO() {
            super();
        }

        @Transactional
        public void updateToUseCorrectPeriod2Attestation(Long activityId) throws JsonProcessingException {
            ActivityEntity activity = getSession().find(ActivityEntity.class, activityId);
            Developer originalDeveloper = null, newDeveloper = null;
            try {
                originalDeveloper =
                    jsonMapper.readValue(activity.getOriginalData(), Developer.class);
            } catch (final Exception ignore) {
                LOGGER.error("Unable to deserialize original value of activity ID " + activityId
                        + " as a Developer object.");
            }

            try {
                newDeveloper =
                    jsonMapper.readValue(activity.getNewData(), Developer.class);
            } catch (final Exception ignore) {
                LOGGER.error("Unable to deserialize original value of activity ID " + activityId
                        + " as a Developer object.");
            }

            updateAttestationIdForPeriod2(originalDeveloper);
            updateAttestationIdForPeriod2(newDeveloper);

            activity.setOriginalData(JSONUtils.toJSON(originalDeveloper));
            activity.setNewData(JSONUtils.toJSON(newDeveloper));
            entityManager.merge(activity);
            entityManager.flush();
        }

        private void updateAttestationIdForPeriod2(Developer developer) {
            PublicAttestation period2Attestation = getAttestationWithId(developer.getAttestations(), PERIOD_2_ID);
            period2Attestation.setId(CORRECT_ATTESTATION_ID);
        }

        private PublicAttestation getAttestationWithId(List<PublicAttestation> attestations, Long periodId) {
            return attestations.stream()
                    .filter(attestation -> attestation.getAttestationPeriod().getId().equals(periodId))
                    .findAny().get();
        }

        @Transactional
        public void delete(Long activityId) {
            Query query = entityManager.createQuery("DELETE FROM ActivityEntity WHERE id = :activityId");
            query.setParameter("activityId", activityId);
            query.executeUpdate();
            entityManager.flush();
        }
    }
}
