package gov.healthit.chpl.listing.measure;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.Measure;
import lombok.extern.log4j.Log4j2;

@Repository("measuresDao")
@Log4j2
public class MeasureDAO extends BaseDAOImpl {
    public static final String MEASURE_HQL_BEGIN = "SELECT DISTINCT measure "
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

    public Measure getByDomainAndAbbreviation(String measureDomain, String rtAbbreviation) {
        Query query = entityManager.createQuery(
                MEASURE_HQL_BEGIN
                + "WHERE measure.deleted = false "
                + "AND UPPER(measure.domain.domain) = :measureDomain "
                + "AND UPPER(measure.abbreviation) = :rtAbbreviation",
                MeasureEntity.class);
        query.setParameter("measureDomain", measureDomain.toUpperCase());
        query.setParameter("rtAbbreviation", rtAbbreviation.toUpperCase());
        List<MeasureEntity> entities = query.getResultList();

        Measure result = null;
        if (entities != null && entities.size() > 0) {
            result = entities.get(0).convert();
            if (entities.size() > 1) {
                LOGGER.warn("Expected only one measure to match '" + measureDomain + "' and '" + rtAbbreviation + "' but found more than one match. Only the first match is being used for flexible upload.");
            }
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
}
