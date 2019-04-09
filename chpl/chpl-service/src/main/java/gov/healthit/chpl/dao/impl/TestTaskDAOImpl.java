package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.TestTaskDAO;
import gov.healthit.chpl.dto.TestTaskDTO;
import gov.healthit.chpl.entity.TestTaskEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;

@Repository("testTaskDao")
public class TestTaskDAOImpl extends BaseDAOImpl implements TestTaskDAO {
    private static final Logger LOGGER = LogManager.getLogger(TestTaskDAOImpl.class);
    @Autowired
    MessageSource messageSource;

    @Override
    public TestTaskDTO create(final TestTaskDTO dto) throws EntityCreationException {

        TestTaskEntity entity = null;
        if (dto.getId() != null) {
            entity = this.getEntityById(dto.getId());
        }

        if (entity == null) {
            entity = new TestTaskEntity();
            entity.setCreationDate(new Date());
            entity.setDeleted(false);
            entity.setLastModifiedDate(new Date());
            entity.setLastModifiedUser(AuthUtil.getAuditId());
            entity.setDescription(dto.getDescription());
            entity.setTaskErrors(dto.getTaskErrors());
            entity.setTaskErrorsStddev(dto.getTaskErrorsStddev());
            entity.setTaskPathDeviationObserved(dto.getTaskPathDeviationObserved());
            entity.setTaskPathDeviationOptimal(dto.getTaskPathDeviationOptimal());
            entity.setTaskRating(dto.getTaskRating());
            entity.setTaskRatingScale(dto.getTaskRatingScale());
            entity.setTaskRatingStddev(dto.getTaskRatingStddev());
            entity.setTaskSuccessAverage(dto.getTaskSuccessAverage());
            entity.setTaskSuccessStddev(dto.getTaskSuccessStddev());
            entity.setTaskTimeAvg(dto.getTaskTimeAvg());
            entity.setTaskTimeDeviationObservedAvg(dto.getTaskTimeDeviationObservedAvg());
            entity.setTaskTimeDeviationOptimalAvg(dto.getTaskTimeDeviationOptimalAvg());
            entity.setTaskTimeStddev(dto.getTaskTimeStddev());

            try {
                create(entity);
            } catch (Exception ex) {
                String msg = String.format(
                        messageSource.getMessage(new DefaultMessageSourceResolvable("listing.criteria.badTestTask"),
                                LocaleContextHolder.getLocale()),
                        dto.getDescription());
                LOGGER.error(msg, ex);
                throw new EntityCreationException(msg);
            }
        }
        return new TestTaskDTO(entity);
    }

    @Override
    public TestTaskDTO update(TestTaskDTO dto) throws EntityRetrievalException {
        TestTaskEntity entity = this.getEntityById(dto.getId());

        if (entity == null) {
            throw new EntityRetrievalException("Entity with id " + dto.getId() + " does not exist");
        }

        entity.setDescription(dto.getDescription());
        entity.setTaskErrors(dto.getTaskErrors());
        entity.setTaskErrorsStddev(dto.getTaskErrorsStddev());
        entity.setTaskPathDeviationObserved(dto.getTaskPathDeviationObserved());
        entity.setTaskPathDeviationOptimal(dto.getTaskPathDeviationOptimal());
        entity.setTaskRating(dto.getTaskRating());
        entity.setTaskRatingScale(dto.getTaskRatingScale());
        entity.setTaskRatingStddev(dto.getTaskRatingStddev());
        entity.setTaskSuccessAverage(dto.getTaskSuccessAverage());
        entity.setTaskSuccessStddev(dto.getTaskSuccessStddev());
        entity.setTaskTimeAvg(dto.getTaskTimeAvg());
        entity.setTaskTimeDeviationObservedAvg(dto.getTaskTimeDeviationObservedAvg());
        entity.setTaskTimeDeviationOptimalAvg(dto.getTaskTimeDeviationOptimalAvg());
        entity.setTaskTimeStddev(dto.getTaskTimeStddev());
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        entity.setLastModifiedDate(new Date());

        update(entity);
        return new TestTaskDTO(entity);
    }

    @Override
    public void delete(Long id) {

        TestTaskEntity toDelete = getEntityById(id);

        if (toDelete != null) {
            toDelete.setDeleted(true);
            toDelete.setLastModifiedDate(new Date());
            toDelete.setLastModifiedUser(AuthUtil.getAuditId());
            entityManager.merge(toDelete);
            entityManager.flush();
        }
    }

    @Override
    public TestTaskDTO getById(Long id) {

        TestTaskDTO dto = null;
        TestTaskEntity entity = getEntityById(id);

        if (entity != null) {
            dto = new TestTaskDTO(entity);
        }
        return dto;
    }

    @Override
    public List<TestTaskDTO> findAll() {

        List<TestTaskEntity> entities = getAllEntities();
        List<TestTaskDTO> dtos = new ArrayList<TestTaskDTO>();

        for (TestTaskEntity entity : entities) {
            TestTaskDTO dto = new TestTaskDTO(entity);
            dtos.add(dto);
        }
        return dtos;

    }

    private void create(TestTaskEntity entity) {

        entityManager.persist(entity);
        entityManager.flush();

    }

    private void update(TestTaskEntity entity) {

        entityManager.merge(entity);
        entityManager.flush();
    }

    private List<TestTaskEntity> getAllEntities() {
        return entityManager.createQuery("from TestTaskEntity where (NOT deleted = true) ", TestTaskEntity.class)
                .getResultList();
    }

    private TestTaskEntity getEntityById(Long id) {

        TestTaskEntity entity = null;

        Query query = entityManager.createQuery("SELECT tt " + "FROM TestTaskEntity tt "
                + "LEFT OUTER JOIN FETCH tt.testParticipants participantMappings "
                + "LEFT OUTER JOIN FETCH participantMappings.testParticipant participant "
                + "WHERE (NOT tt.deleted = true) " + "AND (tt.id = :entityid) ", TestTaskEntity.class);
        query.setParameter("entityid", id);
        List<TestTaskEntity> result = query.getResultList();

        if (result.size() > 0) {
            entity = result.get(0);
        }

        return entity;
    }
}
