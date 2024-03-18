package gov.healthit.chpl.dao;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.entity.TestTaskEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@Repository("testTaskDao")
@Log4j2
public class TestTaskDAO extends BaseDAOImpl {
    private ErrorMessageUtil msgUtil;

    @Autowired
    public TestTaskDAO(ErrorMessageUtil msgUtil) {
        this.msgUtil = msgUtil;
    }

    public Long create(TestTask testTask) throws EntityCreationException {
        TestTaskEntity entity = new TestTaskEntity();
        try {
            entity.setDescription(testTask.getDescription());
            entity.setTaskErrors(testTask.getTaskErrors());
            entity.setTaskErrorsStddev(testTask.getTaskErrorsStddev());
            entity.setTaskPathDeviationObserved(testTask.getTaskPathDeviationObserved());
            entity.setTaskPathDeviationOptimal(testTask.getTaskPathDeviationOptimal());
            entity.setTaskRating(testTask.getTaskRating());
            entity.setTaskRatingScale(testTask.getTaskRatingScale());
            entity.setTaskRatingStddev(testTask.getTaskRatingStddev());
            entity.setTaskSuccessAverage(testTask.getTaskSuccessAverage());
            entity.setTaskSuccessStddev(testTask.getTaskSuccessStddev());
            entity.setTaskTimeAvg(testTask.getTaskTimeAvg());
            entity.setTaskTimeDeviationObservedAvg(testTask.getTaskTimeDeviationObservedAvg());
            entity.setTaskTimeDeviationOptimalAvg(testTask.getTaskTimeDeviationOptimalAvg());
            entity.setTaskTimeStddev(testTask.getTaskTimeStddev());
            create(entity);
        } catch (Exception ex) {
            String msg = msgUtil.getMessage("listing.criteria.badTestTask", testTask.getDescription());
            LOGGER.error(msg, ex);
            throw new EntityCreationException(msg);
        }
        return entity.getId();
    }

    public void update(TestTask task) throws EntityRetrievalException {
        TestTaskEntity entity = this.getEntityById(task.getId());

        if (entity == null) {
            throw new EntityRetrievalException("Entity with id " + task.getId() + " does not exist");
        }

        entity.setDescription(task.getDescription());
        entity.setTaskErrors(task.getTaskErrors());
        entity.setTaskErrorsStddev(task.getTaskErrorsStddev());
        entity.setTaskPathDeviationObserved(task.getTaskPathDeviationObserved());
        entity.setTaskPathDeviationOptimal(task.getTaskPathDeviationOptimal());
        entity.setTaskRating(task.getTaskRating());
        entity.setTaskRatingScale(task.getTaskRatingScale());
        entity.setTaskRatingStddev(task.getTaskRatingStddev());
        entity.setTaskSuccessAverage(task.getTaskSuccessAverage());
        entity.setTaskSuccessStddev(task.getTaskSuccessStddev());
        entity.setTaskTimeAvg(task.getTaskTimeAvg());
        entity.setTaskTimeDeviationObservedAvg(task.getTaskTimeDeviationObservedAvg());
        entity.setTaskTimeDeviationOptimalAvg(task.getTaskTimeDeviationOptimalAvg());
        entity.setTaskTimeStddev(task.getTaskTimeStddev());

        update(entity);
    }

    public void delete(Long id) {
        TestTaskEntity toDelete = getEntityById(id);

        if (toDelete != null) {
            toDelete.setDeleted(true);
            entityManager.merge(toDelete);
            entityManager.flush();
        }
    }

    public TestTask getById(Long id) {
        TestTaskEntity entity = getEntityById(id);
        if (entity != null) {
            return entity.toDomain();
        }
        return null;
    }

    public List<TestTask> findAll() {
        List<TestTaskEntity> entities = getAllEntities();
        return entities.stream()
                .map(entity -> entity.toDomain())
                .collect(Collectors.toList());
    }

    private List<TestTaskEntity> getAllEntities() {
        return entityManager.createQuery("from TestTaskEntity where (NOT deleted = true) ", TestTaskEntity.class)
                .getResultList();
    }

    private TestTaskEntity getEntityById(Long id) {
        TestTaskEntity entity = null;

        Query query = entityManager.createQuery("SELECT tt "
                + "FROM TestTaskEntity tt "
                + "LEFT OUTER JOIN FETCH tt.testParticipants participantMappings "
                + "LEFT OUTER JOIN FETCH participantMappings.testParticipant participant "
                + "LEFT JOIN FETCH participant.education "
                + "LEFT JOIN FETCH participant.ageRange "
                + "WHERE (NOT tt.deleted = true) "
                + "AND (tt.id = :entityid) ", TestTaskEntity.class);
        query.setParameter("entityid", id);
        List<TestTaskEntity> result = query.getResultList();

        if (result.size() > 0) {
            entity = result.get(0);
        }

        return entity;
    }
}
