package gov.healthit.chpl.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.entity.ActivityConceptEntity;
import gov.healthit.chpl.entity.ActivityEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.UserMapper;
import lombok.extern.log4j.Log4j2;

@Repository("activityDAO")
@Log4j2
public class ActivityDAO extends BaseDAOImpl {

    private UserMapper userMapper;

    @Autowired
    public ActivityDAO(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public Long create(ActivityDTO dto) throws EntityCreationException, EntityRetrievalException {

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
        entity.setReason(dto.getReason());
        entity.setCreationDate(new Date());
        entity.setLastModifiedDate(new Date());
        // user may be null because when they get an API Key they do not
        // have to be logged in
        entity.setLastModifiedUser(dto.getLastModifiedUser());
        create(entity);
        return entity.getId();
    }

    public ActivityDTO getById(Long id) throws EntityRetrievalException {
        ActivityEntity entity = getEntityById(id);
        ActivityDTO dto = null;
        if (entity != null) {
            dto = mapEntityToDto(entity);
        }
        return dto;
    }

    public List<ActivityDTO> findByObjectId(Long objectId, ActivityConcept concept, Date startDate, Date endDate) {
        List<ActivityEntity> entities = this.getEntitiesByObjectId(objectId, concept, startDate, endDate);
        List<ActivityDTO> activities = entities.stream()
                .map(entity -> mapEntityToDto(entity))
                .collect(Collectors.toList());

        //Added during OCD-3759 so that curesStatisticsCreator does not run out of memory.
        //Related to the Java 17 upgrade, the ActivityEntity objects fill up available memory.
        entityManager.clear();
        return activities;
    }

    public List<ActivityDTO> findPageByConcept(ActivityConcept concept, Date startDate, Date endDate,
            Integer pageNum, Integer pageSize) {
        Query query = entityManager.createNamedQuery("getPageOfActivity", ActivityEntity.class);
        query.setParameter("conceptName", concept.name());
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        int firstRecord = (pageNum * pageSize) + 1;
        int lastRecord = firstRecord + pageSize;
        query.setParameter("firstRecord", firstRecord);
        query.setParameter("lastRecord", lastRecord);
        List<ActivityEntity> entities = query.getResultList();
        return entities.stream()
                .map(entity -> mapEntityToDto(entity))
                .collect(Collectors.toList());
    }

    public List<ActivityDTO> findPageByConceptAndObject(ActivityConcept concept,
            List<Long> objectIds, Date startDate, Date endDate, Integer pageNum, Integer pageSize) {
        Query query = entityManager.createNamedQuery("getPageOfActivityByObjectIds", ActivityEntity.class);
        query.setParameter("conceptName", concept.name());
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        query.setParameter("objectIds", objectIds);
        int firstRecord = (pageNum * pageSize) + 1;
        int lastRecord = firstRecord + pageSize;
        query.setParameter("firstRecord", firstRecord);
        query.setParameter("lastRecord", lastRecord);
        List<ActivityEntity> entities = query.getResultList();
        return entities.stream()
                .map(entity -> mapEntityToDto(entity))
                .collect(Collectors.toList());
    }

    public Long findResultSetSizeByConcept(ActivityConcept concept, Date startDate, Date endDate) {
        String queryStr = "SELECT COUNT(ae) "
                + "FROM ActivityEntity ae "
                + "JOIN ae.concept ac "
                + "WHERE ae.deleted = false "
                + "AND (ac.concept = :conceptName) ";
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

    public Long findResultSetSizeByConceptAndObject(ActivityConcept concept, List<Long> objectIds,
            Date startDate, Date endDate) {
        String queryStr = "SELECT COUNT(ae) "
                + "FROM ActivityEntity ae "
                + "JOIN ae.concept ac "
                + "WHERE ae.deleted = false "
                + "AND (ac.concept = :conceptName) ";
        if (startDate != null) {
            queryStr += "AND (ae.activityDate >= :startDate) ";
        }
        if (endDate != null) {
            queryStr += "AND (ae.activityDate <= :endDate) ";
        }
        if (objectIds != null && objectIds.size() > 0) {
            queryStr += "AND (ae.activityObjectId IN (:objectIds)) ";
        }
        Query query = entityManager.createQuery(queryStr, Long.class);
        query.setParameter("conceptName", concept.name());
        if (startDate != null) {
            query.setParameter("startDate", startDate);
        }
        if (endDate != null) {
            query.setParameter("endDate", endDate);
        }
        if (objectIds != null && objectIds.size() > 0) {
            query.setParameter("objectIds", objectIds);
        }
        return (Long) query.getSingleResult();
    }

    public List<ActivityDTO> findByConcept(ActivityConcept concept, Date startDate, Date endDate) {
        List<ActivityEntity> entities = this.getEntitiesByConcept(concept, startDate, endDate);
        return entities.stream()
                .map(entity -> mapEntityToDto(entity))
                .collect(Collectors.toList());
    }

    public List<ActivityDTO> findPublicAnnouncementActivity(Date startDate, Date endDate) {
        Query query = entityManager.createNamedQuery("getPublicAnnouncementActivityByDate",
                ActivityEntity.class);
        query.setParameter("conceptName", ActivityConcept.ANNOUNCEMENT.name());
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        List<ActivityEntity> entities = query.getResultList();
        return entities.stream()
            .map(entity -> mapEntityToDto(entity))
            .collect(Collectors.toList());
    }

    public List<ActivityDTO> findPublicAnnouncementActivityById(Long announcementId,
            Date startDate, Date endDate) {
        Query query = entityManager.createNamedQuery("getPublicAnnouncementActivityByIdAndDate",
                ActivityEntity.class);
        query.setParameter("announcementId", announcementId);
        query.setParameter("conceptName", ActivityConcept.ANNOUNCEMENT.name());
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        List<ActivityEntity> entities = query.getResultList();
        return entities.stream()
            .map(entity -> mapEntityToDto(entity))
            .collect(Collectors.toList());
    }

    public List<ActivityDTO> findAcbActivity(List<CertificationBody> acbs, Date startDate, Date endDate) {
        List<Long> acbIds = new ArrayList<Long>();
        for (CertificationBody acb : acbs) {
            acbIds.add(acb.getId());
        }

        List<ActivityEntity> entities = getEntitiesByObjectIds(acbIds,
                ActivityConcept.CERTIFICATION_BODY, startDate, endDate);

        return entities.stream()
                .map(entity -> mapEntityToDto(entity))
                .collect(Collectors.toList());
    }

    public List<ActivityDTO> findAtlActivity(List<TestingLabDTO> atls, Date startDate,
            Date endDate) {
        List<Long> atlIds = new ArrayList<Long>();
        for (TestingLabDTO atl : atls) {
            atlIds.add(atl.getId());
        }

        List<ActivityEntity> entities = getEntitiesByObjectIds(atlIds,
                ActivityConcept.TESTING_LAB, startDate, endDate);

        return entities.stream()
                .map(entity -> mapEntityToDto(entity))
                .collect(Collectors.toList());
    }

    public Map<Long, List<ActivityDTO>> findAllByUserInDateRange(Date startDate, Date endDate) {
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

    private ActivityEntity getEntityById(Long id) throws EntityRetrievalException {
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

    private List<ActivityEntity> getEntitiesByObjectIds(List<Long> objectIds,
            ActivityConcept concept, Date startDate, Date endDate) {
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

    private List<ActivityEntity> getEntitiesByObjectId(Long objectId, ActivityConcept concept,
            Date startDate, Date endDate) {
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

    private List<ActivityEntity> getEntitiesByConcept(ActivityConcept concept, Date startDate,
            Date endDate) {
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

    private List<ActivityEntity> getAllEntitiesInDateRange(Date startDate, Date endDate) {
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

    private ActivityDTO mapEntityToDto(ActivityEntity entity) {
        ActivityDTO activity = entity.toDomain();
        if (entity.getUser() != null) {
            activity.setUser(userMapper.from(entity.getUser()));
        }
        return activity;
    }
}
