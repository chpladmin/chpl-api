package gov.healthit.chpl.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.dto.MacraMeasureDTO;
import gov.healthit.chpl.entity.MacraMeasureEntity;

@Repository("macraMeasuresDao")
public class MacraMeasureDAO extends BaseDAOImpl {

    public MacraMeasureDTO getById(Long id) {

        MacraMeasureDTO dto = null;
        MacraMeasureEntity entity = getEntityById(id);

        if (entity != null) {
            dto = new MacraMeasureDTO(entity);
        }
        return dto;
    }

    public List<MacraMeasureDTO> findAll() {
        Query query = entityManager.createQuery(
                "from MacraMeasureEntity mme " + "LEFT OUTER JOIN FETCH mme.certificationCriterion cce "
                        + "LEFT OUTER JOIN FETCH cce.certificationEdition " + "WHERE (NOT mme.deleted = true)",
                MacraMeasureEntity.class);
        List<MacraMeasureEntity> results = query.getResultList();

        List<MacraMeasureDTO> dtos = new ArrayList<MacraMeasureDTO>(results.size());
        for (MacraMeasureEntity result : results) {
            dtos.add(new MacraMeasureDTO(result));
        }
        return dtos;
    }

    public List<MacraMeasureDTO> getByCriteriaNumber(String criteriaNumber) {
        List<MacraMeasureEntity> entities = getMeasuresByCertificationCriteria(criteriaNumber);
        List<MacraMeasureDTO> dtos = new ArrayList<MacraMeasureDTO>();

        for (MacraMeasureEntity entity : entities) {
            MacraMeasureDTO dto = new MacraMeasureDTO(entity);
            dtos.add(dto);
        }
        return dtos;

    }

    public MacraMeasureDTO getByCriteriaNumberAndValue(String criteriaNumber, String value) {
        MacraMeasureEntity entity = getMeasureByCertificationCriteriaAndValue(criteriaNumber, value);
        if (entity == null) {
            return null;
        }
        return new MacraMeasureDTO(entity);
    }

    public void removeAllByValue(String value) {
        getMeasuresByValue(value).stream()
                .forEach(measure -> {
                    measure.setRemoved(true);
                    entityManager.merge(measure);
                });
        entityManager.flush();
    }

    public void updateSubstringInAllValues(final String oldSubstring, final String newSubstring) {
        getAllMeasuresWhereValueIsNotNull().stream()
            .forEach(measure -> {
                measure.setValue(measure.getValue().replaceAll(oldSubstring, newSubstring));
                entityManager.merge(measure);
            });
    }

    private List<MacraMeasureEntity> getMeasuresByCertificationCriteria(String criteriaNumber) {
        Query query = entityManager
                .createQuery("FROM MacraMeasureEntity mme "
                        + "LEFT OUTER JOIN FETCH mme.certificationCriterion cce "
                        + "LEFT OUTER JOIN FETCH cce.certificationEdition "
                        + "WHERE (NOT mme.deleted = true) "
                        + "AND (UPPER(cce.number) = :criteriaNumber)", MacraMeasureEntity.class);
        query.setParameter("criteriaNumber", criteriaNumber.trim().toUpperCase());
        List<MacraMeasureEntity> result = query.getResultList();
        return result;
    }

    private List<MacraMeasureEntity> getMeasuresByValue(String value) {
        Query query = entityManager.createQuery(
                "FROM MacraMeasureEntity mme "
                        + "LEFT OUTER JOIN FETCH mme.certificationCriterion cce "
                        + "LEFT OUTER JOIN FETCH cce.certificationEdition "
                        + "WHERE (NOT mme.deleted = true) "
                        + "AND (UPPER(mme.value) = :value)",
                MacraMeasureEntity.class);
        query.setParameter("value", value.trim().toUpperCase());
        return query.getResultList();
    }

    private MacraMeasureEntity getMeasureByCertificationCriteriaAndValue(String criteriaNumber, String value) {
        Query query = entityManager.createQuery(
                "FROM MacraMeasureEntity mme "
                        + "LEFT OUTER JOIN FETCH mme.certificationCriterion cce "
                        + "LEFT OUTER JOIN FETCH cce.certificationEdition "
                        + "WHERE (NOT mme.deleted = true) "
                        + "AND (UPPER(cce.number) = :criteriaNumber) "
                        + "AND (UPPER(mme.value) = :value)",
                MacraMeasureEntity.class);
        query.setParameter("criteriaNumber", criteriaNumber.trim().toUpperCase());
        query.setParameter("value", value.trim().toUpperCase());
        List<MacraMeasureEntity> result = query.getResultList();
        if (result == null || result.size() == 0) {
            return null;
        }
        return result.get(0);
    }

    private MacraMeasureEntity getEntityById(Long id) {
        MacraMeasureEntity entity = null;
        Query query = entityManager.createQuery(
                "from MacraMeasureEntity mme "
                        + "LEFT OUTER JOIN FETCH mme.certificationCriterion cce "
                        + "LEFT OUTER JOIN FETCH cce.certificationEdition "
                        + "where (NOT mme.deleted = true) AND (mme.id = :entityid) ",
                MacraMeasureEntity.class);
        query.setParameter("entityid", id);
        List<MacraMeasureEntity> result = query.getResultList();

        if (result.size() > 0) {
            entity = result.get(0);
        }
        return entity;
    }

    private List<MacraMeasureEntity> getAllMeasuresWhereValueIsNotNull() {
        Query query = entityManager
                .createQuery("FROM MacraMeasureEntity mme "
                        + "WHERE mme.value IS NOT NULL", MacraMeasureEntity.class);
        return query.getResultList();
    }
}
