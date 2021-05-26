package gov.healthit.chpl.scheduler.job.versionActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.entity.ActivityEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.UserMapper;

@Component("updateableActivityDao")
public class UpdateableActivityDao extends BaseDAOImpl {
    private UserMapper userMapper;

    @Autowired
    public UpdateableActivityDao(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Transactional
    public void update(ActivityDTO dto) {
        ActivityEntity entity = getEntityManager().find(ActivityEntity.class, dto.getId());
        if (entity != null) {
            entity.setNewData(dto.getNewData());
            entity.setOriginalData(dto.getOriginalData());
        }
        update(entity);
    }

    @Transactional
    public ActivityDTO getById(Long id) throws EntityRetrievalException {
        ActivityEntity entity = null;
        String queryStr = "SELECT a "
                + "FROM ActivityEntity a "
                + "JOIN FETCH a.concept concept "
                + "LEFT JOIN FETCH a.user u "
                + "LEFT JOIN FETCH u.permission "
                + "LEFT JOIN FETCH u.contact "
                + "WHERE (a.id = :entityid) ";
        Query query = entityManager.createQuery(queryStr, ActivityEntity.class);
        query.setParameter("entityid", id);
        List<ActivityEntity> result = query.getResultList();
        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate activity id in database.");
        }
        if (result.size() > 0) {
            entity = result.get(0);
        }
        return mapEntityToDto(entity);
    }

    @Transactional
    public List<ActivityDTO> getAllVersionActivityMetadata() {
        String hql = "SELECT a "
                + "FROM ActivityEntity a "
                + "JOIN FETCH a.concept concept "
                + "LEFT JOIN FETCH a.user u "
                + "LEFT JOIN FETCH u.permission "
                + "LEFT JOIN FETCH u.contact "
                + "WHERE concept.concept = :conceptName "
                + "AND a.deleted = false";
        Query query = entityManager.createQuery(hql, ActivityEntity.class);
        query.setParameter("conceptName", ActivityConcept.VERSION.name());
        return ((List<ActivityEntity>) query.getResultList()).stream()
                .map(entity -> mapEntityToDto(entity))
                .collect(Collectors.toList());
    }

    @Transactional
    public List<ActivityDTO> findByObjectId(Long objectId, ActivityConcept concept, Date startDate, Date endDate) {
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
        List<ActivityEntity> entities = query.getResultList();

        List<ActivityDTO> activities = new ArrayList<>();
        for (ActivityEntity entity : entities) {
            ActivityDTO result = mapEntityToDto(entity);
            activities.add(result);
        }
        return activities;
    }

    private ActivityDTO mapEntityToDto(ActivityEntity entity) {
        ActivityDTO activity = new ActivityDTO(entity);
        if (entity.getUser() != null) {
            activity.setUser(userMapper.from(entity.getUser()));
        }
        return activity;
    }
}
