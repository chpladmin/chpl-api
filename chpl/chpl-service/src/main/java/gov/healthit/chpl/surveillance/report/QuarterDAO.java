package gov.healthit.chpl.surveillance.report;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.surveillance.report.domain.Quarter;
import gov.healthit.chpl.surveillance.report.entity.QuarterEntity;

@Repository("quarterDao")
public class QuarterDAO extends BaseDAOImpl {

    public List<Quarter> getAll() {
        List<QuarterEntity> resultEntities = entityManager.createQuery(
                "SELECT q "
                + " FROM QuarterEntity q "
                + " WHERE q.deleted = false").getResultList();
        return resultEntities.stream()
                .map(entity -> entity.toDomain())
                .collect(Collectors.toList());
    }

    public Quarter getById(Long id) throws EntityRetrievalException {
        QuarterEntity entity = entityManager.find(QuarterEntity.class, id);
        if (entity == null) {
            throw new EntityRetrievalException("No quarter exists with database ID " + id);
        }
        return entity.toDomain();
    }

    public Quarter getByName(String name) {
        Query query = entityManager.createQuery(
                "SELECT q "
                + " FROM QuarterEntity q "
                + " WHERE q.name = :name "
                + " AND q.deleted = false");
        query.setParameter("name", name);
        List<QuarterEntity> resultEntities = query.getResultList();
        Quarter result = null;
        if (resultEntities != null && resultEntities.size() > 0) {
            result = resultEntities.get(0).toDomain();
        }
        return result;
    }
}
