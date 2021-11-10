package gov.healthit.chpl.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.TestStandard;
import gov.healthit.chpl.dto.TestStandardDTO;
import gov.healthit.chpl.entity.TestStandardEntity;

@Repository("testStandardDAO")
public class TestStandardDAO extends BaseDAOImpl {

    public TestStandardDTO getById(Long id) {
        TestStandardDTO dto = null;
        TestStandardEntity entity = getEntityById(id);

        if (entity != null) {
            dto = new TestStandardDTO(entity);
        }
        return dto;
    }

    public List<TestStandardDTO> findAll() {
        List<TestStandardEntity> entities = getAllEntities();
        List<TestStandardDTO> dtos = new ArrayList<TestStandardDTO>();

        for (TestStandardEntity entity : entities) {
            TestStandardDTO dto = new TestStandardDTO(entity);
            dtos.add(dto);
        }
        return dtos;
    }

    public TestStandardDTO getByNumberAndEdition(String number, Long editionId) {
        TestStandardDTO dto = null;
        List<TestStandardEntity> entities = getEntitiesByNumberAndYear(number, editionId);

        if (entities != null && entities.size() > 0) {
            dto = new TestStandardDTO(entities.get(0));
        }
        return dto;
    }

    public List<TestStandard> getAllByNumberAndEdition(String number, Long editionId) {
        List<TestStandardEntity> entities = getEntitiesByNumberAndYear(number, editionId);

        return entities.stream()
                .map(entity -> new TestStandard(entity))
                .collect(Collectors.toList());
    }

    public TestStandardDTO getByIdAndEdition(Long testStandardId, Long editionId) {
        TestStandardDTO dto = null;
        List<TestStandardEntity> entities = getEntitiesByIdAndYear(testStandardId, editionId);
        if (entities != null && entities.size() > 0) {
            dto = new TestStandardDTO(entities.get(0));
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

        Query query = entityManager.createQuery("SELECT ts " + "FROM TestStandardEntity ts "
                + "WHERE (NOT deleted = true) " + "AND (ts.id = :entityid) ", TestStandardEntity.class);
        query.setParameter("entityid", id);
        List<TestStandardEntity> result = query.getResultList();

        if (result.size() > 0) {
            entity = result.get(0);
        }
        return entity;
    }

    private List<TestStandardEntity> getEntitiesByNumberAndYear(String number, Long editionId) {
        TestStandardEntity entity = null;
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

    private List<TestStandardEntity> getEntitiesByIdAndYear(Long id, Long editionId) {
        TestStandardEntity entity = null;
        String tsQuery = "SELECT ts "
                + "FROM TestStandardEntity ts "
                + "JOIN FETCH ts.certificationEdition edition "
                + "WHERE ts.deleted <> true "
                + "AND ts.id = :id "
                + "AND edition.id = :editionId ";
        Query query = entityManager.createQuery(tsQuery, TestStandardEntity.class);
        query.setParameter("id", id);
        query.setParameter("editionId", editionId);

        return query.getResultList();
    }
}
