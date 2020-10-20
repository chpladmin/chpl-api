package gov.healthit.chpl.listing.mipsMeasure;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.MipsMeasure;

@Repository("mipsMeasuresDao")
public class MipsMeasureDAO extends BaseDAOImpl {
    private static final String MIPS_MEASURE_HQL_BEGIN = "SELECT mme "
            + "FROM MipsMeasureEntity mme "
            + "JOIN FETCH mme.domain "
            + "JOIN FETCH mme.allowedCriteria ac "
            + "JOIN FETCH ac.criterion cc "
            + "JOIN FETCH cc.certificationEdition ";

    public MipsMeasure getById(Long id) {
        Query query = entityManager.createQuery(
                MIPS_MEASURE_HQL_BEGIN
                + "WHERE mme.deleted = false "
                + "AND mme.id = :id ",
                MipsMeasureEntity.class);
        query.setParameter("id", id);
        List<MipsMeasureEntity> entities = query.getResultList();

        MipsMeasure result = null;
        if (entities != null && entities.size() > 0) {
            result = entities.get(0).convert();
        }
        return result;
    }

    public MipsMeasure getByName(String name) {
        Query query = entityManager.createQuery(
                MIPS_MEASURE_HQL_BEGIN
                + "WHERE mme.deleted false "
                + "AND (UPPER(mme.value) = :name)",
                MipsMeasureEntity.class);
        query.setParameter("name", name.trim().toUpperCase());
        List<MipsMeasureEntity> entities = query.getResultList();

        MipsMeasure result = null;
        if (entities != null && entities.size() > 0) {
            result = entities.get(0).convert();
        }
        return result;
    }

    public Set<MipsMeasure> findAll() {
        Query query = entityManager.createQuery(
                MIPS_MEASURE_HQL_BEGIN
                + "WHERE mme.deleted = false",
                MipsMeasureEntity.class);
        List<MipsMeasureEntity> entities = query.getResultList();
        Set<MipsMeasure> results = new LinkedHashSet<MipsMeasure>(entities.size());
        if (entities != null && entities.size() > 0) {
            entities.stream().forEach(entity -> {
                results.add(entity.convert());
            });
        }
        return results;
    }
}
