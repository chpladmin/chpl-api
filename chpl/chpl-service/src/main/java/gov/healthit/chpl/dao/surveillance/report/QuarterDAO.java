package gov.healthit.chpl.dao.surveillance.report;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.dto.surveillance.report.QuarterDTO;
import gov.healthit.chpl.entity.surveillance.report.QuarterEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;

@Repository("quarterDao")
public class QuarterDAO extends BaseDAOImpl {

    public List<QuarterDTO> getAll() {
        List<QuarterEntity> resultEntities = entityManager.createQuery(
                "SELECT q "
                + " FROM QuarterEntity q "
                + " WHERE q.deleted = false").getResultList();
        List<QuarterDTO> results = new ArrayList<QuarterDTO>();
        if (resultEntities != null && resultEntities.size() > 0) {
            for (QuarterEntity resultEntity : resultEntities) {
                results.add(new QuarterDTO(resultEntity));
            }
        }
        return results;
    }

    public QuarterDTO getById(Long id) throws EntityRetrievalException {
        QuarterEntity entity = entityManager.find(QuarterEntity.class, id);
        if (entity == null) {
            throw new EntityRetrievalException("No quarter exists with database ID " + id);
        }
        return new QuarterDTO(entity);
    }

    public QuarterDTO getByName(String name) {
        Query query = entityManager.createQuery(
                "SELECT q "
                + " FROM QuarterEntity q "
                + " WHERE q.name = :name "
                + " AND q.deleted = false");
        query.setParameter("name", name);
        List<QuarterEntity> resultEntities = query.getResultList();
        QuarterDTO result = null;
        if (resultEntities != null && resultEntities.size() > 0) {
            result = new QuarterDTO(resultEntities.get(0));
        }
        return result;
    }
}
