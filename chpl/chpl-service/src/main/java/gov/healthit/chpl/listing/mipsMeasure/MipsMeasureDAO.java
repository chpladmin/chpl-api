package gov.healthit.chpl.listing.mipsMeasure;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.MipsMeasure;

@Repository("mipsMeasuresDao")
public class MipsMeasureDAO extends BaseDAOImpl {

    public MipsMeasure getById(Long id) {
        Query query = entityManager.createQuery(
                "SELECT mme "
                + "FROM MipsMeasureEntity mme "
                + "WHERE mme.deleted = false "
                + "AND mme.id = :id ",
                MipsMeasureEntity.class);
        query.setParameter("id", id);
        List<MipsMeasureEntity> entities = query.getResultList();

        MipsMeasure result = null;
        if (entities != null && entities.size() > 0) {
            result = convert(entities.get(0));
        }
        return result;
    }

    public MipsMeasure getByName(String name) {
        Query query = entityManager.createQuery(
                "SELECT mme "
                + "FROM MipsMeasureEntity mme "
                + "WHERE (NOT mme.deleted = true) "
                + "AND (UPPER(mme.value) = :name)",
                MipsMeasureEntity.class);
        query.setParameter("name", name.trim().toUpperCase());
        List<MipsMeasureEntity> entities = query.getResultList();

        MipsMeasure result = null;
        if (entities != null && entities.size() > 0) {
            result = convert(entities.get(0));
        }
        return result;
    }

    public List<MipsMeasure> findAll() {
        Query query = entityManager.createQuery(
                "SELECT mme "
                + "FROM MipsMeasureEntity mme "
                + "WHERE mme.deleted = false",
                MipsMeasureEntity.class);
        List<MipsMeasureEntity> entities = query.getResultList();
        List<MipsMeasure> results = new ArrayList<MipsMeasure>(entities.size());
        if (entities != null && entities.size() > 0) {
            entities.stream().forEach(entity -> {
                results.add(convert(entity));
            });
        }
        return results;
    }

    private MipsMeasure convert(MipsMeasureEntity entity) {
        MipsMeasure measure = new MipsMeasure();
        measure.setId(entity.getId());
        measure.setAbbreviation(entity.getValue());
        measure.setName(entity.getName());
        measure.setDescription(entity.getDescription());
        measure.setRemoved(entity.getRemoved());
        return measure;
    }
}
