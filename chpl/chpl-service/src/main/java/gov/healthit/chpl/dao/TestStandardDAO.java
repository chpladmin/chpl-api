package gov.healthit.chpl.dao;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.TestStandard;
import gov.healthit.chpl.entity.TestStandardEntity;

@Repository("testStandardDAO")
public class TestStandardDAO extends BaseDAOImpl {

    public TestStandard getById(Long id) {
        TestStandard testStandard = null;
        TestStandardEntity entity = getEntityById(id);

        if (entity != null) {
            testStandard = entity.toDomain();
        }
        return testStandard;
    }

    public List<TestStandard> findAll() {
        List<TestStandardEntity> entities = getAllEntities();
        return entities.stream()
                .map(entity -> entity.toDomain())
                .collect(Collectors.toList());
    }

    @Deprecated
    public TestStandard getByNumberAndEdition(String number, Long editionId) {
        TestStandard dto = null;
        List<TestStandardEntity> entities = getEntitiesByNumberAndYear(number, editionId);

        if (entities != null && entities.size() > 0) {
            dto = entities.get(0).toDomain();
        }
        return dto;
    }

    private List<TestStandardEntity> getAllEntities() {
        return entityManager
                .createQuery("from TestStandardEntity where (NOT deleted = true) ", TestStandardEntity.class)
                .getResultList();
    }

    private TestStandardEntity getEntityById(Long id) {
        TestStandardEntity entity = null;

        Query query = entityManager.createQuery("SELECT ts "
                + "FROM TestStandardEntity ts "
                + "WHERE (NOT deleted = true) "
                + "AND (ts.id = :entityid) ", TestStandardEntity.class);
        query.setParameter("entityid", id);
        List<TestStandardEntity> result = query.getResultList();

        if (result.size() > 0) {
            entity = result.get(0);
        }
        return entity;
    }

    @Deprecated
    private List<TestStandardEntity> getEntitiesByNumberAndYear(String number, Long editionId) {
        String tsQuery = "SELECT ts "
                + "FROM TestStandardEntity ts "
                + "JOIN FETCH ts.certificationEdition edition "
                + "WHERE ts.deleted <> true "
                + "AND UPPER(ts.name) LIKE :number "
                + "AND edition.id = :editionId ";
        Query query = entityManager.createQuery(tsQuery, TestStandardEntity.class);
        query.setParameter("number", number.toUpperCase());
        query.setParameter("editionId", editionId);

        List<TestStandardEntity> matches = query.getResultList();
        if (matches == null || matches.size() == 0) {
            // if this didn't find anything try again with spaces removed from
            // the number
            query = entityManager.createQuery(tsQuery, TestStandardEntity.class);
            String numberWithoutSpaces = number.replaceAll("\\s", "");
            query.setParameter("number", numberWithoutSpaces.toUpperCase());
            query.setParameter("editionId", editionId);
            matches = query.getResultList();
        }
        return matches;
    }
}
