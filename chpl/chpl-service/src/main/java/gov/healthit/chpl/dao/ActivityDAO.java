package gov.healthit.chpl.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.entity.ActivityConceptEntity;
import gov.healthit.chpl.entity.ActivityEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.UserMapper;

/**
 * Data access for Activity. Generally activity should only be created and
 * retrieved and not changed in any way once inserted.
 *
 * @author kekey
 *
 */
@Repository("activityDAO")
public class ActivityDAO extends BaseDAOImpl {

    private UserMapper userMapper;

    @Autowired
    public ActivityDAO(@Lazy final UserMapper userMapper) {
        this.userMapper = userMapper;
    }


    public ActivityDTO create(final ActivityDTO dto) throws EntityCreationException, EntityRetrievalException {

        // find the activity concept id for this concept
        Query conceptIdQuery = entityManager.createQuery("SELECT ac "
                + "FROM ActivityConceptEntity ac "
                + "WHERE ac.concept = :conceptName");
        conceptIdQuery.setParameter("conceptName", dto.getConcept().name());
        List<ActivityConceptEntity> conceptResults = conceptIdQuery.getResultList();
        if (conceptResults == null || conceptResults.size() == 0) {
            throw new EntityCreationException("No activity concept '" + dto.getConcept() + " was found.");
        }
        Long conceptId = conceptResults.get(0).getId();

        // insert the activity
        ActivityEntity entity = new ActivityEntity();
        entity.setId(dto.getId());
        entity.setDescription(dto.getDescription());
        entity.setOriginalData(dto.getOriginalData());
        entity.setNewData(dto.getNewData());
        entity.setActivityDate(dto.getActivityDate());
        entity.setActivityObjectConceptId(conceptId);
        entity.setActivityObjectId(dto.getActivityObjectId());
        entity.setCreationDate(new Date());
        entity.setLastModifiedDate(new Date());
        // user may be null because when they get an API Key they do not
        // have to be logged in
        entity.setLastModifiedUser(dto.getLastModifiedUser());
        entity.setDeleted(false);
        entityManager.persist(entity);
        entityManager.flush();
        entityManager.clear();

        ActivityDTO result = null;
        if (entity != null) {
            result = mapEntityToDto(entity);
        }
        return result;
    }


    public ActivityDTO getById(final Long id) throws EntityRetrievalException {

        ActivityEntity entity = getEntityById(id);
        ActivityDTO dto = null;
        if (entity != null) {
            dto = mapEntityToDto(entity);
        }
        return dto;
    }


    public List<ActivityDTO> findByObjectId(final Long objectId, final ActivityConcept concept,
            final Date startDate, final Date endDate) {

        List<ActivityEntity> entities = this.getEntitiesByObjectId(objectId, concept, startDate, endDate);
        List<ActivityDTO> activities = new ArrayList<>();

        for (ActivityEntity entity : entities) {
            ActivityDTO result = mapEntityToDto(entity);
            activities.add(result);
        }
        return activities;
    }

    public List<ActivityDTO> findPageByConcept(ActivityConcept concept, Date startDate, Date endDate,
            Integer pageNum, Integer pageSize) {
        List<ActivityEntity> entities = this.getEntityPageByConcept(concept, startDate, endDate, pageNum, pageSize);
        List<ActivityDTO> activities = new ArrayList<>();
        for (ActivityEntity entity : entities) {
            ActivityDTO result = mapEntityToDto(entity);
            activities.add(result);
        }
        return activities;
    }

    public Long findResultSetSizeByConcept(ActivityConcept concept, Date startDate, Date endDate) {
        String queryStr = "SELECT COUNT(ae) "
                + "FROM ActivityEntity ae "
                + "JOIN ae.concept ac "
                + "WHERE (ac.concept = :conceptName) ";
        if (startDate != null) {
            queryStr += "AND (ae.activityDate >= :startDate) ";
        }
        if (endDate != null) {
            queryStr += "AND (ae.activityDate <= :endDate) ";
        }
        Query query = entityManager.createQuery(queryStr, Long.class);
        query.setParameter("conceptName", concept.name());
        if (startDate != null) {
            query.setParameter("startDate", startDate);
        }
        if (endDate != null) {
            query.setParameter("endDate", endDate);
        }
        return (Long) query.getSingleResult();
    }

    public List<ActivityDTO> findByConcept(final ActivityConcept concept, final Date startDate, final Date endDate) {
        List<ActivityEntity> entities = this.getEntitiesByConcept(concept, startDate, endDate);
        List<ActivityDTO> activities = new ArrayList<>();

        for (ActivityEntity entity : entities) {
            ActivityDTO result = mapEntityToDto(entity);
            activities.add(result);
        }
        return activities;
    }


    public List<ActivityDTO> findPublicAnnouncementActivity(final Date startDate, final Date endDate) {
        Query query = entityManager.createNamedQuery("getPublicAnnouncementActivityByDate",
                ActivityEntity.class);
        query.setParameter("conceptName", ActivityConcept.ANNOUNCEMENT.name());
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);

        List<ActivityDTO> results = new ArrayList<ActivityDTO>();
        List<ActivityEntity> entities = query.getResultList();
        for (ActivityEntity entity : entities) {
            ActivityDTO result = mapEntityToDto(entity);
            results.add(result);
        }
        return results;
    }


    public List<ActivityDTO> findPublicAnnouncementActivityById(final Long announcementId,
            final Date startDate, final Date endDate) {
        Query query = entityManager.createNamedQuery("getPublicAnnouncementActivityByIdAndDate",
                ActivityEntity.class);
        query.setParameter("announcementId", announcementId);
        query.setParameter("conceptName", ActivityConcept.ANNOUNCEMENT.name());
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);

        List<ActivityDTO> results = new ArrayList<ActivityDTO>();
        List<ActivityEntity> entities = query.getResultList();
        for (ActivityEntity entity : entities) {
            ActivityDTO result = mapEntityToDto(entity);
            results.add(result);
        }
        return results;
    }


    public List<ActivityDTO> findAcbActivity(final List<CertificationBodyDTO> acbs,
            final Date startDate, final Date endDate) {
        List<Long> acbIds = new ArrayList<Long>();
        for (CertificationBodyDTO acb : acbs) {
            acbIds.add(acb.getId());
        }

        List<ActivityEntity> entities = getEntitiesByObjectIds(acbIds,
                ActivityConcept.CERTIFICATION_BODY, startDate, endDate);

        List<ActivityDTO> results = new ArrayList<ActivityDTO>();
        for (ActivityEntity entity : entities) {
            ActivityDTO result = mapEntityToDto(entity);
            results.add(result);
        }
        return results;
    }


    public List<ActivityDTO> findAtlActivity(final List<TestingLabDTO> atls, final Date startDate,
            final Date endDate) {
        List<Long> atlIds = new ArrayList<Long>();
        for (TestingLabDTO atl : atls) {
            atlIds.add(atl.getId());
        }

        List<ActivityEntity> entities = getEntitiesByObjectIds(atlIds,
                ActivityConcept.TESTING_LAB, startDate, endDate);

        List<ActivityDTO> results = new ArrayList<ActivityDTO>();
        for (ActivityEntity entity : entities) {
            ActivityDTO result = mapEntityToDto(entity);
            results.add(result);
        }
        return results;
    }


    public List<ActivityDTO> findPendingListingActivity(final List<CertificationBodyDTO> pendingListingAcbs,
            final Date startDate, final Date endDate) {
        Query query = entityManager.createNamedQuery("getPendingListingActivityByAcbIdsAndDate",
                ActivityEntity.class);
        query.setParameter("conceptName", ActivityConcept.PENDING_CERTIFIED_PRODUCT.name());
        // parameters need to be strings
        List<String> acbIdParams = new ArrayList<String>();
        for (CertificationBodyDTO acb : pendingListingAcbs) {
            acbIdParams.add(acb.getId().toString());
        }
        query.setParameter("acbIds", acbIdParams);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);

        List<ActivityDTO> results = new ArrayList<ActivityDTO>();
        List<ActivityEntity> entities = query.getResultList();
        for (ActivityEntity entity : entities) {
            ActivityDTO result = mapEntityToDto(entity);
            results.add(result);
        }
        return results;
    }


    public List<ActivityDTO> findPendingListingActivity(final Long pendingListingId,
            final Date startDate, final Date endDate) {
        List<ActivityEntity> entities = getEntitiesByObjectId(pendingListingId,
                ActivityConcept.PENDING_CERTIFIED_PRODUCT, startDate, endDate);

        List<ActivityDTO> results = new ArrayList<ActivityDTO>();
        for (ActivityEntity entity : entities) {
            ActivityDTO result = mapEntityToDto(entity);
            results.add(result);
        }
        return results;
    }


    public List<ActivityDTO> findUserActivity(final List<Long> userIds, final Date startDate, final Date endDate) {
        List<ActivityEntity> entities = getEntitiesByObjectIds(userIds,
                ActivityConcept.USER, startDate, endDate);

        List<ActivityDTO> results = new ArrayList<ActivityDTO>();
        for (ActivityEntity entity : entities) {
            ActivityDTO result = mapEntityToDto(entity);
            results.add(result);
        }
        return results;
    }


    public List<ActivityDTO> findByUserId(final Long userId, final Date startDate, final Date endDate) {

        List<ActivityEntity> entities = this.getEntitiesByUserId(userId, startDate, endDate);
        List<ActivityDTO> activities = new ArrayList<>();

        for (ActivityEntity entity : entities) {
            ActivityDTO result = mapEntityToDto(entity);
            activities.add(result);
        }
        return activities;
    }


    public Map<Long, List<ActivityDTO>> findAllByUserInDateRange(final Date startDate, final Date endDate) {

        Map<Long, List<ActivityDTO>> activityByUser = new HashMap<Long, List<ActivityDTO>>();

        List<ActivityEntity> entities = this.getAllEntitiesInDateRange(startDate, endDate);

        for (ActivityEntity entity : entities) {

            ActivityDTO result = mapEntityToDto(entity);
            Long userId = result.getLastModifiedUser();
            if (userId != null) {
                if (activityByUser.containsKey(userId)) {
                    activityByUser.get(userId).add(result);
                } else {
                    List<ActivityDTO> activity = new ArrayList<ActivityDTO>();
                    activity.add(result);
                    activityByUser.put(userId, activity);
                }
            }
        }
        return activityByUser;
    }

    private ActivityEntity getEntityById(final Long id) throws EntityRetrievalException {

        ActivityEntity entity = null;
        String queryStr = "SELECT ae "
                + "FROM ActivityEntity ae "
                + "JOIN FETCH ae.concept "
                + "LEFT OUTER JOIN FETCH ae.user "
                + "WHERE (ae.id = :entityid) ";
        Query query = entityManager.createQuery(queryStr);
        query.setParameter("entityid", id);
        List<ActivityEntity> result = query.getResultList();

        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate criterion id in database.");
        }

        if (result.size() > 0) {
            entity = result.get(0);
        }
        return entity;
    }

    private List<ActivityEntity> getEntitiesByObjectIds(final List<Long> objectIds,
            final ActivityConcept concept, final Date startDate, final Date endDate) {
        String queryStr = "SELECT ae "
                + "FROM ActivityEntity ae "
                + "JOIN FETCH ae.concept ac "
                + "LEFT OUTER JOIN FETCH ae.user "
                + "WHERE ae.activityObjectId IN (:objectIds) "
                + "AND ac.concept = :conceptName ";
        if (startDate != null) {
            queryStr += "AND (ae.activityDate >= :startDate) ";
        }
        if (endDate != null) {
            queryStr += "AND (ae.activityDate <= :endDate) ";
        }
        Query query = entityManager.createQuery(queryStr);
        query.setParameter("objectIds", objectIds);
        query.setParameter("conceptName", concept.name());
        if (startDate != null) {
            query.setParameter("startDate", startDate);
        }
        if (endDate != null) {
            query.setParameter("endDate", endDate);
        }

        List<ActivityEntity> result = query.getResultList();
        return result;
    }

    private List<ActivityEntity> getEntitiesByObjectId(final Long objectId, final ActivityConcept concept,
            final Date startDate, final Date endDate) {
        String queryStr = "SELECT ae "
                + "FROM ActivityEntity ae "
                + "JOIN FETCH ae.concept ac "
                + "LEFT OUTER JOIN FETCH ae.user "
                + "WHERE (ac.concept = :conceptName) "
                + "AND (ae.activityObjectId = :objectid) ";
        if (startDate != null) {
            queryStr += "AND (ae.activityDate >= :startDate) ";
        }
        if (endDate != null) {
            queryStr += "AND (ae.activityDate <= :endDate) ";
        }
        Query query = entityManager.createQuery(queryStr, ActivityEntity.class);
        query.setParameter("objectid", objectId);
        query.setParameter("conceptName", concept.name());
        if (startDate != null) {
            query.setParameter("startDate", startDate);
        }
        if (endDate != null) {
            query.setParameter("endDate", endDate);
        }
        List<ActivityEntity> result = query.getResultList();
        return result;
    }

    private List<ActivityEntity> getEntitiesByConcept(final ActivityConcept concept, final Date startDate,
            final Date endDate) {
        String queryStr = "SELECT ae "
                + "FROM ActivityEntity ae "
                + "JOIN FETCH ae.concept ac "
                + "LEFT OUTER JOIN FETCH ae.user "
                + "WHERE (ac.concept = :conceptName) ";
        if (startDate != null) {
            queryStr += "AND (ae.activityDate >= :startDate) ";
        }
        if (endDate != null) {
            queryStr += "AND (ae.activityDate <= :endDate) ";
        }
        Query query = entityManager.createQuery(queryStr, ActivityEntity.class);
        query.setParameter("conceptName", concept.name());
        if (startDate != null) {
            query.setParameter("startDate", startDate);
        }
        if (endDate != null) {
            query.setParameter("endDate", endDate);
        }
        List<ActivityEntity> result = query.getResultList();
        return result;
    }

    private List<ActivityEntity> getEntityPageByConcept(ActivityConcept concept, Date startDate,
            Date endDate, Integer pageNum, Integer pageSize) {
        Query query = entityManager.createNamedQuery("getPageOfActivity", ActivityEntity.class);
        query.setParameter("conceptName", concept.name());
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        int firstRecord = (pageNum * pageSize) + 1;
        int lastRecord = firstRecord + pageSize;
        query.setParameter("firstRecord", firstRecord);
        query.setParameter("lastRecord", lastRecord);
        List<ActivityEntity> result = query.getResultList();
        return result;
    }

    private List<ActivityEntity> getAllEntitiesInDateRange(final Date startDate, final Date endDate) {
        String queryStr = "SELECT ae "
                + "FROM ActivityEntity ae "
                + "JOIN FETCH ae.concept "
                + "LEFT OUTER JOIN FETCH ae.user "
                + "WHERE ";
        if (startDate != null) {
            if (!queryStr.endsWith("WHERE ")) {
                queryStr += "AND ";
            }
            queryStr += "(ae.activityDate >= :startDate) ";
        }
        if (endDate != null) {
            if (!queryStr.endsWith("WHERE ")) {
                queryStr += "AND ";
            }
            queryStr += "(ae.activityDate <= :endDate)";
        }

        Query query = entityManager.createQuery(queryStr, ActivityEntity.class);
        if (startDate != null) {
            query.setParameter("startDate", startDate);
        }
        if (endDate != null) {
            query.setParameter("endDate", endDate);
        }
        List<ActivityEntity> result = query.getResultList();
        return result;
    }

    private List<ActivityEntity> getEntitiesByUserId(final Long userId, final Date startDate, final Date endDate) {
        String queryStr = "SELECT ae "
                + "FROM ActivityEntity ae "
                + "JOIN FETCH ae.concept "
                + "LEFT OUTER JOIN FETCH ae.user "
                + "WHERE (ae.lastModifiedUser = :userid) ";
        if (startDate != null) {
            queryStr += "AND (ae.activityDate >= :startDate) ";
        }
        if (endDate != null) {
            queryStr += "AND (ae.activityDate <= :endDate) ";
        }

        Query query = entityManager.createQuery(queryStr, ActivityEntity.class);
        query.setParameter("userid", userId);
        if (startDate != null) {
            query.setParameter("startDate", startDate);
        }
        if (endDate != null) {
            query.setParameter("endDate", endDate);
        }

        List<ActivityEntity> result = query.getResultList();
        return result;
    }

    private ActivityDTO mapEntityToDto(ActivityEntity entity) {
        ActivityDTO activity = new ActivityDTO(entity);
        if (entity.getUser() != null) {
            activity.setUser(userMapper.from(entity.getUser()));
        }
        return activity;
    }
}
