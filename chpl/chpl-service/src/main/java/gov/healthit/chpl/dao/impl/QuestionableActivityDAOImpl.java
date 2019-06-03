package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.QuestionableActivityDAO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityCertificationResultDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityDeveloperDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityListingDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityProductDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityTriggerDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityVersionDTO;
import gov.healthit.chpl.entity.questionableActivity.QuestionableActivityCertificationResultEntity;
import gov.healthit.chpl.entity.questionableActivity.QuestionableActivityDeveloperEntity;
import gov.healthit.chpl.entity.questionableActivity.QuestionableActivityEntity;
import gov.healthit.chpl.entity.questionableActivity.QuestionableActivityListingEntity;
import gov.healthit.chpl.entity.questionableActivity.QuestionableActivityProductEntity;
import gov.healthit.chpl.entity.questionableActivity.QuestionableActivityTriggerEntity;
import gov.healthit.chpl.entity.questionableActivity.QuestionableActivityVersionEntity;
import gov.healthit.chpl.util.AuthUtil;

@Repository("questionableActivityDao")
public class QuestionableActivityDAOImpl extends BaseDAOImpl implements QuestionableActivityDAO {
    private static final Logger LOGGER = LogManager.getLogger(QuestionableActivityDAOImpl.class);

    @Override
    @Transactional
    public QuestionableActivityDTO create(QuestionableActivityDTO dto) {
        QuestionableActivityDTO created = null;
        QuestionableActivityEntity toCreate = null;
        if (dto instanceof QuestionableActivityVersionDTO) {
            toCreate = new QuestionableActivityVersionEntity();
            QuestionableActivityVersionEntity versionActivity = (QuestionableActivityVersionEntity) toCreate;
            versionActivity.setVersionId(((QuestionableActivityVersionDTO) dto).getVersionId());
        } else if (dto instanceof QuestionableActivityProductDTO) {
            toCreate = new QuestionableActivityProductEntity();
            QuestionableActivityProductEntity productActivity = (QuestionableActivityProductEntity) toCreate;
            productActivity.setProductId(((QuestionableActivityProductDTO) dto).getProductId());
        } else if (dto instanceof QuestionableActivityDeveloperDTO) {
            toCreate = new QuestionableActivityDeveloperEntity();
            QuestionableActivityDeveloperEntity developerActivity = (QuestionableActivityDeveloperEntity) toCreate;
            developerActivity.setDeveloperId(((QuestionableActivityDeveloperDTO) dto).getDeveloperId());
            developerActivity.setReason(((QuestionableActivityDeveloperDTO) dto).getReason());
        } else if (dto instanceof QuestionableActivityListingDTO) {
            toCreate = new QuestionableActivityListingEntity();
            QuestionableActivityListingEntity listingActivity = (QuestionableActivityListingEntity) toCreate;
            listingActivity.setListingId(((QuestionableActivityListingDTO) dto).getListingId());
            listingActivity.setCertificationStatusChangeReason(
                    ((QuestionableActivityListingDTO) dto).getCertificationStatusChangeReason());
            listingActivity.setReason(((QuestionableActivityListingDTO) dto).getReason());
        } else if (dto instanceof QuestionableActivityCertificationResultDTO) {
            toCreate = new QuestionableActivityCertificationResultEntity();
            QuestionableActivityCertificationResultEntity certResultActivity =
                    (QuestionableActivityCertificationResultEntity) toCreate;
            certResultActivity.setCertResultId(((QuestionableActivityCertificationResultDTO) dto).getCertResultId());
            certResultActivity.setReason(((QuestionableActivityCertificationResultDTO) dto).getReason());
        } else {
            LOGGER.error("Unknown class of questionable activity passed in: " + dto.getClass().getName());
            return null;
        }

        toCreate.setActivityDate(dto.getActivityDate());
        toCreate.setBefore(dto.getBefore());
        toCreate.setAfter(dto.getAfter());
        toCreate.setTriggerId(dto.getTriggerId());
        toCreate.setUserId(dto.getUserId());
        toCreate.setDeleted(false);
        toCreate.setLastModifiedUser(AuthUtil.getAuditId());
        entityManager.persist(toCreate);
        entityManager.flush();
        entityManager.clear();

        if (toCreate instanceof QuestionableActivityVersionEntity) {
            created = new QuestionableActivityVersionDTO((QuestionableActivityVersionEntity) toCreate);
        } else if (toCreate instanceof QuestionableActivityProductEntity) {
            created = new QuestionableActivityProductDTO((QuestionableActivityProductEntity) toCreate);
        } else if (toCreate instanceof QuestionableActivityDeveloperEntity) {
            created = new QuestionableActivityDeveloperDTO((QuestionableActivityDeveloperEntity) toCreate);
        } else if (toCreate instanceof QuestionableActivityListingEntity) {
            created = new QuestionableActivityListingDTO((QuestionableActivityListingEntity) toCreate);
        } else if (toCreate instanceof QuestionableActivityCertificationResultEntity) {
            created = new QuestionableActivityCertificationResultDTO((QuestionableActivityCertificationResultEntity) toCreate);
        }
        return created;
    }

    @Override
    @Transactional
    public List<QuestionableActivityTriggerDTO> getAllTriggers() {
        Query query = entityManager.createQuery("SELECT trigger "
                + "FROM QuestionableActivityTriggerEntity trigger "
                + "WHERE trigger.deleted <> true",
                QuestionableActivityTriggerEntity.class);
        List<QuestionableActivityTriggerEntity> queryResults = query.getResultList();
        List<QuestionableActivityTriggerDTO> results = new ArrayList<QuestionableActivityTriggerDTO>(queryResults.size());
        for (QuestionableActivityTriggerEntity queryResult : queryResults) {
            results.add(new QuestionableActivityTriggerDTO(queryResult));
        }
        return results;
    }

    @Override
    @Transactional
    public List<QuestionableActivityVersionDTO> findVersionActivityBetweenDates(final Date start, final Date end) {
        Query query = entityManager.createQuery("SELECT activity "
                + "FROM QuestionableActivityVersionEntity activity "
                + "LEFT OUTER JOIN FETCH activity.version "
                + "LEFT OUTER JOIN FETCH activity.trigger "
                + "LEFT OUTER JOIN FETCH activity.user activityUser "
                + "LEFT OUTER JOIN FETCH activityUser.contact "
                + "WHERE activity.deleted <> true "
                + "AND activity.activityDate >= :startDate "
                + "AND activity.activityDate <= :endDate",
                QuestionableActivityVersionEntity.class);
        query.setParameter("startDate", start);
        query.setParameter("endDate", end);
        List<QuestionableActivityVersionEntity> queryResults = query.getResultList();
        List<QuestionableActivityVersionDTO> results = new ArrayList<QuestionableActivityVersionDTO>(queryResults.size());
        for(QuestionableActivityVersionEntity queryResult : queryResults) {
            results.add(new QuestionableActivityVersionDTO(queryResult));
        }
        return results;
    }

    @Override
    @Transactional
    public List<QuestionableActivityProductDTO> findProductActivityBetweenDates(final Date start, final Date end) {
        Query query = entityManager.createQuery("SELECT activity "
                + "FROM QuestionableActivityProductEntity activity "
                + "LEFT OUTER JOIN FETCH activity.product "
                + "LEFT OUTER JOIN FETCH activity.trigger "
                + "LEFT OUTER JOIN FETCH activity.user activityUser "
                + "LEFT OUTER JOIN FETCH activityUser.contact "
                + "WHERE activity.deleted <> true "
                + "AND activity.activityDate >= :startDate "
                + "AND activity.activityDate <= :endDate",
                QuestionableActivityProductEntity.class);
        query.setParameter("startDate", start);
        query.setParameter("endDate", end);
        List<QuestionableActivityProductEntity> queryResults = query.getResultList();
        List<QuestionableActivityProductDTO> results = new ArrayList<QuestionableActivityProductDTO>(queryResults.size());
        for (QuestionableActivityProductEntity queryResult : queryResults) {
            results.add(new QuestionableActivityProductDTO(queryResult));
        }
        return results;
    }

    @Override
    @Transactional
    public List<QuestionableActivityDeveloperDTO> findDeveloperActivityBetweenDates(final Date start, final Date end) {
        Query query = entityManager.createQuery("SELECT activity "
                + "FROM QuestionableActivityDeveloperEntity activity "
                + "LEFT OUTER JOIN FETCH activity.developer "
                + "LEFT OUTER JOIN FETCH activity.trigger "
                + "LEFT OUTER JOIN FETCH activity.user activityUser "
                + "LEFT OUTER JOIN FETCH activityUser.contact "
                + "WHERE activity.deleted <> true "
                + "AND activity.activityDate >= :startDate "
                + "AND activity.activityDate <= :endDate",
                QuestionableActivityDeveloperEntity.class);
        query.setParameter("startDate", start);
        query.setParameter("endDate", end);
        List<QuestionableActivityDeveloperEntity> queryResults = query.getResultList();
        List<QuestionableActivityDeveloperDTO> results = new ArrayList<QuestionableActivityDeveloperDTO>(queryResults.size());
        for (QuestionableActivityDeveloperEntity queryResult : queryResults) {
            results.add(new QuestionableActivityDeveloperDTO(queryResult));
        }
        return results;
    }

    @Override
    @Transactional
    public List<QuestionableActivityListingDTO> findListingActivityBetweenDates(final Date start, final Date end) {
        Query query = entityManager.createQuery("SELECT activity "
                + "FROM QuestionableActivityListingEntity activity "
                + "LEFT OUTER JOIN FETCH activity.listing "
                + "LEFT OUTER JOIN FETCH activity.trigger "
                + "LEFT OUTER JOIN FETCH activity.user activityUser "
                + "LEFT OUTER JOIN FETCH activityUser.contact "
                + "WHERE activity.deleted <> true "
                + "AND activity.activityDate >= :startDate "
                + "AND activity.activityDate <= :endDate",
                QuestionableActivityListingEntity.class);
        query.setParameter("startDate", start);
        query.setParameter("endDate", end);
        List<QuestionableActivityListingEntity> queryResults = query.getResultList();
        List<QuestionableActivityListingDTO> results = new ArrayList<QuestionableActivityListingDTO>(queryResults.size());
        for (QuestionableActivityListingEntity queryResult : queryResults) {
            results.add(new QuestionableActivityListingDTO(queryResult));
        }
        return results;
    }

    @Override
    @Transactional
    public List<QuestionableActivityCertificationResultDTO> findCertificationResultActivityBetweenDates(
            final Date start, final Date end) {
        Query query = entityManager.createQuery("SELECT activity "
                + "FROM QuestionableActivityCertificationResultEntity activity "
                + "LEFT OUTER JOIN FETCH activity.certResult certResult "
                + "LEFT OUTER JOIN FETCH certResult.listing "
                + "LEFT OUTER JOIN FETCH activity.trigger "
                + "LEFT OUTER JOIN FETCH activity.user activityUser "
                + "LEFT OUTER JOIN FETCH activityUser.contact "
                + "WHERE activity.deleted <> true "
                + "AND activity.activityDate >= :startDate "
                + "AND activity.activityDate <= :endDate",
                QuestionableActivityCertificationResultEntity.class);
        query.setParameter("startDate", start);
        query.setParameter("endDate", end);
        List<QuestionableActivityCertificationResultEntity> queryResults = query.getResultList();
        List<QuestionableActivityCertificationResultDTO> results
            = new ArrayList<QuestionableActivityCertificationResultDTO>(queryResults.size());
        for (QuestionableActivityCertificationResultEntity queryResult : queryResults) {
            results.add(new QuestionableActivityCertificationResultDTO(queryResult));
        }
        return results;
    }
}
