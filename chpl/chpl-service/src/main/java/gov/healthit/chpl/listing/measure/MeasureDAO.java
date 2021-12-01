package gov.healthit.chpl.listing.measure;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.Measure;

@Repository("measuresDao")
public class MeasureDAO extends BaseDAOImpl {
    private static final String MEASURE_HQL_BEGIN = "SELECT DISTINCT measure "
            + "FROM MeasureEntity measure "
            + "LEFT JOIN FETCH measure.domain "
            + "LEFT JOIN FETCH measure.allowedCriteria ac "
            + "LEFT JOIN FETCH ac.criterion cc "
            + "LEFT JOIN FETCH cc.certificationEdition ";

    public Measure getById(Long id) {
        Query query = entityManager.createQuery(
                MEASURE_HQL_BEGIN
                + "WHERE measure.deleted = false "
                + "AND measure.id = :id ",
                MeasureEntity.class);
        query.setParameter("id", id);
        List<MeasureEntity> entities = query.getResultList();

        Measure result = null;
        if (entities != null && entities.size() > 0) {
            result = entities.get(0).convert();
        }
        return result;
    }

    public Measure getMeasureByMacraMeasureId(Long macraMeasureId) {
        MeasureEntity entity = getEntityByMacraMeasureId(macraMeasureId);
        return entity.convert();
    }

    public MeasureEntity getEntityByMacraMeasureId(Long macraMeasureId) {
        Query query = entityManager.createQuery(
                MEASURE_HQL_BEGIN
                + "WHERE measure.deleted = false "
                + "AND measure.id = "
                + " (SELECT DISTINCT mcm.measureId "
                +   "FROM LegacyMacraMeasureCriterionMapEntity legacyMacraMap "
                +   "JOIN legacyMacraMap.measureCriterionMap mcm "
                +   "WHERE legacyMacraMap.legacyMacraMeasureId = :macraMeasureId)",
                MeasureEntity.class);
        query.setParameter("macraMeasureId", macraMeasureId);
        List<MeasureEntity> entities = query.getResultList();

        MeasureEntity result = null;
        if (entities != null && entities.size() > 0) {
            result = entities.get(0);
        }
        return result;
    }

    public Set<Measure> findAll() {
        Query query = entityManager.createQuery(
                MEASURE_HQL_BEGIN
                + "WHERE measure.deleted = false",
                MeasureEntity.class);
        List<MeasureEntity> entities = query.getResultList();
        Set<Measure> results = new LinkedHashSet<Measure>(entities.size());
        if (entities != null && entities.size() > 0) {
            entities.stream().forEach(entity -> {
                results.add(entity.convert());
            });
        }
        return results;
    }

    public Measure update(Measure measure) {
        MeasureEntity result = getEntity(measure.getId());
        result.setAbbreviation(measure.getAbbreviation());
        result.setName(measure.getName());
        result.setRemoved(measure.getRemoved());

        update(result);

        return getById(result.getId());
    }

    public Measure create(Measure measure) {
        MeasureEntity entity = new MeasureEntity();
        entity.setDomain(MeasureDomainEntity.builder().id(measure.getDomain().getId()).build());
        entity.setAbbreviation(measure.getAbbreviation());
        entity.setRequiredTest(measure.getRequiredTest());
        entity.setName(measure.getName());
        entity.setCriteriaSelectionRequired(measure.getRequiresCriteriaSelection());
        entity.setRemoved(measure.getRemoved());
        entity.setLastModifiedUser(User.SYSTEM_USER_ID);
        entity.setDeleted(false);
        super.create(entity);
        return getById(entity.getId());
    }

    public MeasureEntity getEntity(Long id) {
        Query query = entityManager.createQuery(
                MEASURE_HQL_BEGIN
                + "WHERE measure.deleted = false "
                + "AND measure.id = :id ",
                MeasureEntity.class);
        query.setParameter("id", id);
        List<MeasureEntity> entities = query.getResultList();

        MeasureEntity result = null;
        if (entities != null && entities.size() > 0) {
            result = entities.get(0);
        }
        return result;
    }
}
