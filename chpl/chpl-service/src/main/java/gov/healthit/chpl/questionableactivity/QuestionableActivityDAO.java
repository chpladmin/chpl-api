package gov.healthit.chpl.questionableactivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.questionableactivity.dto.QuestionableActivityCertificationResultDTO;
import gov.healthit.chpl.questionableactivity.dto.QuestionableActivity;
import gov.healthit.chpl.questionableactivity.dto.QuestionableActivityDeveloperDTO;
import gov.healthit.chpl.questionableactivity.dto.QuestionableActivityListingDTO;
import gov.healthit.chpl.questionableactivity.dto.QuestionableActivityProductDTO;
import gov.healthit.chpl.questionableactivity.dto.QuestionableActivityTrigger;
import gov.healthit.chpl.questionableactivity.dto.QuestionableActivityVersionDTO;
import gov.healthit.chpl.questionableactivity.entity.QuestionableActivityCertificationResultEntity;
import gov.healthit.chpl.questionableactivity.entity.QuestionableActivityDeveloperEntity;
import gov.healthit.chpl.questionableactivity.entity.QuestionableActivityEntity;
import gov.healthit.chpl.questionableactivity.entity.QuestionableActivityListingEntity;
import gov.healthit.chpl.questionableactivity.entity.QuestionableActivityProductEntity;
import gov.healthit.chpl.questionableactivity.entity.QuestionableActivityTriggerEntity;
import gov.healthit.chpl.questionableactivity.entity.QuestionableActivityVersionEntity;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.UserMapper;

@Repository("questionableActivityDao")
public class QuestionableActivityDAO extends BaseDAOImpl {
    private static final Logger LOGGER = LogManager.getLogger(QuestionableActivityDAO.class);

    private UserMapper userMapper;

    @Autowired
    public QuestionableActivityDAO(@Lazy UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Transactional
    public QuestionableActivity create(QuestionableActivity qa) {
        QuestionableActivity created = null;
        QuestionableActivityEntity toCreate = null;
        if (qa instanceof QuestionableActivityVersionDTO) {
            toCreate = new QuestionableActivityVersionEntity();
            QuestionableActivityVersionEntity versionActivity = (QuestionableActivityVersionEntity) toCreate;
            versionActivity.setVersionId(((QuestionableActivityVersionDTO) qa).getVersionId());
        } else if (qa instanceof QuestionableActivityProductDTO) {
            toCreate = new QuestionableActivityProductEntity();
            QuestionableActivityProductEntity productActivity = (QuestionableActivityProductEntity) toCreate;
            productActivity.setProductId(((QuestionableActivityProductDTO) qa).getProductId());
        } else if (qa instanceof QuestionableActivityDeveloperDTO) {
            toCreate = new QuestionableActivityDeveloperEntity();
            QuestionableActivityDeveloperEntity developerActivity = (QuestionableActivityDeveloperEntity) toCreate;
            developerActivity.setDeveloperId(((QuestionableActivityDeveloperDTO) qa).getDeveloperId());
            developerActivity.setReason(((QuestionableActivityDeveloperDTO) qa).getReason());
        } else if (qa instanceof QuestionableActivityListingDTO) {
            toCreate = new QuestionableActivityListingEntity();
            QuestionableActivityListingEntity listingActivity = (QuestionableActivityListingEntity) toCreate;
            listingActivity.setListingId(((QuestionableActivityListingDTO) qa).getListingId());
            listingActivity.setCertificationStatusChangeReason(
                    ((QuestionableActivityListingDTO) qa).getCertificationStatusChangeReason());
            listingActivity.setReason(((QuestionableActivityListingDTO) qa).getReason());
        } else if (qa instanceof QuestionableActivityCertificationResultDTO) {
            toCreate = new QuestionableActivityCertificationResultEntity();
            QuestionableActivityCertificationResultEntity certResultActivity = (QuestionableActivityCertificationResultEntity) toCreate;
            certResultActivity.setCertResultId(((QuestionableActivityCertificationResultDTO) qa).getCertResultId());
            certResultActivity.setReason(((QuestionableActivityCertificationResultDTO) qa).getReason());
        } else {
            LOGGER.error("Unknown class of questionable activity passed in: " + qa.getClass().getName());
            return null;
        }

        toCreate.setActivityId(qa.getActivityId());
        toCreate.setActivityDate(qa.getActivityDate());
        toCreate.setBefore(qa.getBefore());
        toCreate.setAfter(qa.getAfter());
        toCreate.setTriggerId(qa.getTrigger().getId());
        toCreate.setUserId(qa.getUserId());
        toCreate.setDeleted(false);
        toCreate.setLastModifiedUser(AuthUtil.getAuditId());
        entityManager.persist(toCreate);
        entityManager.flush();
        entityManager.clear();

        if (toCreate instanceof QuestionableActivityVersionEntity) {
            created = mapEntityToDto((QuestionableActivityVersionEntity) toCreate);
        } else if (toCreate instanceof QuestionableActivityProductEntity) {
            created = mapEntityToDto((QuestionableActivityProductEntity) toCreate);
        } else if (toCreate instanceof QuestionableActivityDeveloperEntity) {
            created = mapEntityToDto((QuestionableActivityDeveloperEntity) toCreate);
        } else if (toCreate instanceof QuestionableActivityListingEntity) {
            created = mapEntityToDto((QuestionableActivityListingEntity) toCreate);
        } else if (toCreate instanceof QuestionableActivityCertificationResultEntity) {
            created = mapEntityToDto((QuestionableActivityCertificationResultEntity) toCreate);
        }
        return created;
    }

    @Transactional
    public List<QuestionableActivityTrigger> getAllTriggers() {
        Query query = entityManager.createQuery("SELECT trigger "
                + "FROM QuestionableActivityTriggerEntity trigger "
                + "WHERE trigger.deleted <> true",
                QuestionableActivityTriggerEntity.class);
        List<QuestionableActivityTriggerEntity> queryResults = query.getResultList();
        return queryResults.stream()
                .map(entity -> entity.toDomain())
                .collect(Collectors.toList());
    }

    @Transactional
    public List<QuestionableActivityVersionDTO> findVersionActivityBetweenDates(Date start, Date end) {
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
        List<QuestionableActivityVersionDTO> results = new ArrayList<QuestionableActivityVersionDTO>(
                queryResults.size());
        for (QuestionableActivityVersionEntity queryResult : queryResults) {
            results.add(mapEntityToDto(queryResult));
        }
        return results;
    }

    @Transactional
    public List<QuestionableActivityProductDTO> findProductActivityBetweenDates(Date start, Date end) {
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
        List<QuestionableActivityProductDTO> results = new ArrayList<QuestionableActivityProductDTO>(
                queryResults.size());
        for (QuestionableActivityProductEntity queryResult : queryResults) {
            results.add(mapEntityToDto(queryResult));
        }
        return results;
    }

    @Transactional
    public List<QuestionableActivityDeveloperDTO> findDeveloperActivityBetweenDates(Date start, Date end) {
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
        List<QuestionableActivityDeveloperDTO> results = new ArrayList<QuestionableActivityDeveloperDTO>(
                queryResults.size());
        for (QuestionableActivityDeveloperEntity queryResult : queryResults) {
            results.add(mapEntityToDto(queryResult));
        }
        return results;
    }

    @Transactional
    public List<QuestionableActivityListingDTO> findListingActivityBetweenDates(Date start, Date end) {
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
        List<QuestionableActivityListingDTO> results = new ArrayList<QuestionableActivityListingDTO>(
                queryResults.size());
        for (QuestionableActivityListingEntity queryResult : queryResults) {
            results.add(mapEntityToDto(queryResult));
        }
        return results;
    }

    @Transactional
    public List<QuestionableActivityCertificationResultDTO> findCertificationResultActivityBetweenDates(
            Date start, Date end) {
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
        List<QuestionableActivityCertificationResultDTO> results = new ArrayList<QuestionableActivityCertificationResultDTO>(
                queryResults.size());
        for (QuestionableActivityCertificationResultEntity queryResult : queryResults) {
            results.add(mapEntityToDto(queryResult));
        }
        return results;
    }

    private QuestionableActivityVersionDTO mapEntityToDto(QuestionableActivityVersionEntity entity) {
        QuestionableActivityVersionDTO dto = new QuestionableActivityVersionDTO(entity);
        dto.setUser(userMapper.from(entity.getUser()));
        return dto;
    }

    private QuestionableActivityProductDTO mapEntityToDto(QuestionableActivityProductEntity entity) {
        QuestionableActivityProductDTO dto = new QuestionableActivityProductDTO(entity);
        dto.setUser(userMapper.from(entity.getUser()));
        return dto;
    }

    private QuestionableActivityDeveloperDTO mapEntityToDto(QuestionableActivityDeveloperEntity entity) {
        QuestionableActivityDeveloperDTO dto = new QuestionableActivityDeveloperDTO(entity);
        dto.setUser(userMapper.from(entity.getUser()));
        return dto;
    }

    private QuestionableActivityListingDTO mapEntityToDto(QuestionableActivityListingEntity entity) {
        QuestionableActivityListingDTO dto = new QuestionableActivityListingDTO(entity);
        dto.setUser(userMapper.from(entity.getUser()));
        return dto;
    }

    private QuestionableActivityCertificationResultDTO mapEntityToDto(
            QuestionableActivityCertificationResultEntity entity) {
        QuestionableActivityCertificationResultDTO dto = new QuestionableActivityCertificationResultDTO(entity);
        dto.setUser(userMapper.from(entity.getUser()));
        return dto;
    }

}
