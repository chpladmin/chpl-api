package gov.healthit.chpl.dao;

import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.entity.MacraMeasureEntity;

@Repository("macraMeasuresDao")
public class MacraMeasureDAO extends BaseDAOImpl {

    public Long getMacraMeasureIdByCriterionAndValue(Long criterionId, String value) {
        Query query = entityManager.createQuery(
                "SELECT mme "
                + "FROM MacraMeasureEntity mme "
                + "LEFT OUTER JOIN FETCH mme.certificationCriterion cce "
                + "LEFT OUTER JOIN FETCH cce.certificationEdition "
                + "WHERE (NOT mme.deleted = true) "
                + "AND cce.id = :criterionId "
                + "AND (UPPER(mme.value) = :value)",
                MacraMeasureEntity.class);
        query.setParameter("criterionId", criterionId);
        query.setParameter("value", value.trim().toUpperCase());
        List<MacraMeasureEntity> result = query.getResultList();
        if (result == null || result.size() == 0) {
            return null;
        }
        return result.get(0).getId();
    }

    public MacraMeasureEntity create(MacraMeasureEntity entity) {
        super.create(entity);
        return getById(entity.getId());
    }

    private MacraMeasureEntity getById(Long id) {
        Query query = entityManager.createQuery(
                "SELECT mme "
                + "FROM MacraMeasureEntity mme "
                + "LEFT OUTER JOIN FETCH mme.certificationCriterion cce "
                + "LEFT OUTER JOIN FETCH cce.certificationEdition "
                + "WHERE (NOT mme.deleted = true) "
                + "AND mme.id = :id ",
                MacraMeasureEntity.class);
        query.setParameter("id", id);
        List<MacraMeasureEntity> result = query.getResultList();
        if (result == null || result.size() == 0) {
            return null;
        }
        return result.get(0);
    }

}
